package UI_Screens;

import UI_Components.CharacterSprite;
import UI_Components.DialogueBox;
import UI_Components.EffectManager;
import UI_Components.PauseMenuUI;
import UI_Components.RelationUI;
import UI_Components.MultiplayerTimerBar;
import model.GameConstants;
import model.GameServer;
import model.GameClient;
import model.SoundManager;
import model.StoryDataMP;
import model.Relation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * PlaySceneMP — 2-phase TFT timer + countdown + random choice on timeout
 *
 * Phase 1 READ (50วิ) → Countdown (3,2,1) → Phase 2 CHOICE (10วิ) → FORCE_NEXT
 *
 * ถ้า FORCE_NEXT และผู้เล่นยังไม่เลือก → สุ่มเลือกให้อัตโนมัติ
 * ทุกคนไปซีนถัดไปพร้อมกัน (Server ส่ง targetScene มาด้วย)
 */
public class PlaySceneMP extends JPanel {

    private final Map<String, Object[][]> storyMap = new HashMap<>();
    private Object[][] currentScene;
    private String     sceneName     = "MP_INTRO";
    private int        sceneIndex    = 0;
    private Runnable   onGameFinished;

    private String  currentBG      = "";
    private String  currentChar    = "";
    private String  currentSpeaker = "";
    private String  fullText       = "";
    private int     charIndex      = 0;
    private boolean isChoiceMode   = false;
    private Object[][] pendingChoices  = null;
    private boolean sentSceneReady    = false;
    private boolean waitingForResult   = false; // ✅ รอ CHOICE_RESULT จาก server — block onNext()

    // ── UI ─────────────────────────────────────────────────
    private JLabel              bgLayer;
    private CharacterSprite     characterLayer;
    private JPanel              effectLayer;
    private JPanel              choiceLayer;
    private DialogueBox         dialogueBox;
    private Timer               typeTimer;
    private MultiplayerTimerBar timerBar;

    private final SoundManager soundManager  = new SoundManager();
    private EffectManager      effectManager;

    private GameServer mpServer = null;
    private GameClient mpClient = null;
    private final Random random = new Random();
    private java.util.function.Consumer<java.util.Map<String,Integer>> onLeaderboardCb = null;

    // ════════════════════════════════════════════════════
    public PlaySceneMP() {
        setLayout(null);
        setOpaque(true);
        setBackground(Color.BLACK);

        dialogueBox   = new DialogueBox();
        effectManager = new EffectManager(this, dialogueBox);
        effectManager.setMultiplayerBypass(true);

        initStoryMap();
        setupUI();

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { relayout(); }
            @Override public void componentShown(ComponentEvent e) {
                relayout();
                if (currentScene != null && sceneIndex < currentScene.length)
                    renderLine(currentScene[sceneIndex]);
            }
        });
        dialogueBox.setOnNextRequested(this::onNext);
        setupKeys();
    }

    // ════════════════════════════════════════════════════
    //  Public API
    // ════════════════════════════════════════════════════

    public void startMPGame(GameServer server, GameClient client) {
        this.mpServer = server;
        this.mpClient = client;

        Relation.getInstance().resetAll();
        // ไม่เรียก updateAllScores ใน MP — ทำให้ RelationUI แสดงตัวเอง
        RelationUI.getInstance().setVisible(false);  // ซ่อนใน MP mode
        effectManager.stopAll();
        timerBar.resetAndHide();

        // ✅ ไม่ set listener ที่นี่ — ให้ MultiplayerLobby เป็นคน set และ forward มาแทน
        // register nextScene map ให้ Server รู้
        if (server != null) registerNextScenes(server);

        loadScene("MP_INTRO");
        relayout();
    }

    public void setOnGameFinished(Runnable r)  { this.onGameFinished = r; }
    public void setOnLeaderboard(java.util.function.Consumer<java.util.Map<String,Integer>> cb) { this.onLeaderboardCb = cb; }

    // ── Host callbacks ────────────────────────────────────
    public void onHostPhaseRead(String scene, int sec, int total) {
        SwingUtilities.invokeLater(() -> {
            RelationUI.getInstance().setVisible(false); // ซ่อนใน MP ทุกครั้ง
            if (mpClient != null) {
                if (!scene.equals(sceneName) || currentScene == null) {
                    loadScene(scene);
                }
                timerBar.startReadPhase(scene, sec, total);
            } else {
                timerBar.startReadPhase(scene, sec, total);
            }
            relayout();
        });
    }
    public void onHostCountdown(int n) {
        SwingUtilities.invokeLater(() -> timerBar.showCountdown(n));
    }
    public void onHostPhaseChoice(String scene, int sec, int total) {
        SwingUtilities.invokeLater(() -> {
            timerBar.startChoicePhase(scene, sec, total);
            unlockChoiceButtons();
            if (!isChoiceMode || countChoiceButtons() == 0) skipToChoice(true);
            relayout();
        });
    }
    public void onHostChoiceResult(String target, String charName, int score) {
        SwingUtilities.invokeLater(() -> applyChoiceResult(target, charName, score));
    }
    public void onHostForceNext(String scene, String targetScene) {
        SwingUtilities.invokeLater(() -> doForceNext(targetScene));
    }
    public void onHostTimerSync(int t) {
        SwingUtilities.invokeLater(() -> timerBar.setTimeLeft(t));
    }
    public void onHostReadyCount(int ready, int total) {
        SwingUtilities.invokeLater(() -> timerBar.setReadyCount(ready, total));
    }

    // ════════════════════════════════════════════════════
    //  Client Listener
    // ════════════════════════════════════════════════════
    private GameClient.ClientListener buildClientListener() {
        return new GameClient.ClientListener() {
            @Override public void onConnected(String n) {}
            @Override public void onPlayerListReceived(java.util.List<String> n) {}
            @Override public void onGameStart(int s) {}
            @Override public void onPlayerJoined(String n, int t) {}
            @Override public void onPlayerLeft(String n, int t) {}
            @Override public void onScoreUpdate(String n, int s) {}
            @Override public void onChatMessage(String s, String m) {}
            @Override public void onDisconnected(String r) {}
            @Override public void onError(String m) {}

            @Override public void onPhaseRead(String scene, int sec, int total) {
                SwingUtilities.invokeLater(() -> { timerBar.startReadPhase(scene, sec, total); relayout(); });
            }
            @Override public void onCountdown(int n) {
                SwingUtilities.invokeLater(() -> timerBar.showCountdown(n));
            }
            @Override public void onPhaseChoice(String scene, int sec, int total) {
                SwingUtilities.invokeLater(() -> {
                    timerBar.startChoicePhase(scene, sec, total);
                    unlockChoiceButtons();
                    if (!isChoiceMode || countChoiceButtons() == 0) skipToChoice(true);
                    relayout();
                });
            }
            @Override public void onChoiceResult(String target, String charName, int score) {
                SwingUtilities.invokeLater(() -> applyChoiceResult(target, charName, score));
            }
            @Override public void onForceNext(String scene, String targetScene) {
                SwingUtilities.invokeLater(() -> doForceNext(targetScene));
            }
            @Override public void onTimerSync(int t) {
                SwingUtilities.invokeLater(() -> timerBar.setTimeLeft(t));
            }
            @Override public void onReadyCount(int ready, int total) {
                SwingUtilities.invokeLater(() -> timerBar.setReadyCount(ready, total));
            }
            // ✅ FIX: forward LEADERBOARD พร้อม scores กลับไป Lobby
            @Override public void onLeaderboard(java.util.Map<String,Integer> scores) {
                SwingUtilities.invokeLater(() -> {
                    timerBar.resetAndHide();
                    if (onLeaderboardCb != null) onLeaderboardCb.accept(scores);
                });
            }
        };
    }

    // ════════════════════════════════════════════════════
    //  Force Next — สุ่มเลือกถ้ายังไม่เลือก
    // ════════════════════════════════════════════════════
    private void doForceNext(String targetScene) {
        timerBar.stopTimer();

        // ถ้ามีปุ่ม choice อยู่ + ยังไม่ได้เลือก → สุ่มให้
        if (isChoiceMode && pendingChoices != null && pendingChoices.length > 0) {
            int pick = random.nextInt(pendingChoices.length);
            Object[] chosen = pendingChoices[pick];
            String pickedTarget = (String) chosen[1];
            String cn = chosen.length >= 4 ? (String) chosen[2] : null;
            int    cs = chosen.length >= 4 ? (Integer) chosen[3] : 0;

            if (countChoiceButtons() > 0) highlightRandomPick(pick);

            // apply affection ของตัวเอง
            if (cn != null && !cn.isEmpty() && cs != 0) {
                Relation.getInstance().addAffection(cn, cs);
                RelationUI.getInstance().setVisible(false);
            }

            if (mpServer != null) {
                // HOST: timeout → ส่ง choice สุ่มไป Server แต่ forceTimeout (ไม่รอ clients)
                new Timer(500, e -> {
                    ((Timer)e.getSource()).stop();
                    isChoiceMode = false; pendingChoices = null;
                    choiceLayer.removeAll(); choiceLayer.setVisible(false);
                    timerBar.resetAndHide();
                    mpServer.forceChoiceTimeout(pickedTarget);
                }).start();
            } else if (mpClient != null) {
                // CLIENT: timeout → ส่ง choice สุ่มให้ Server รวบรวม
                new Timer(500, e -> {
                    ((Timer)e.getSource()).stop();
                    isChoiceMode = false; pendingChoices = null;
                    choiceLayer.removeAll(); choiceLayer.setVisible(false);
                    lockChoiceButtons();
                    timerBar.showWaitingForPlayers();
                    mpClient.sendPlayerChoice(pickedTarget, cn, cs);
                }).start();
            } else {
                // Singleplayer
                new Timer(500, e -> {
                    ((Timer)e.getSource()).stop();
                    applyChoiceResult(pickedTarget, cn, cs);
                }).start();
            }
            return;
        }

        // ไม่มี choice → ไปตาม targetScene
        isChoiceMode   = false;
        pendingChoices = null;
        sentSceneReady = false;
        choiceLayer.removeAll();
        choiceLayer.setVisible(false);
        timerBar.resetAndHide();

        if (targetScene != null && !targetScene.isEmpty() && storyMap.containsKey(targetScene)) {
            if (mpServer != null) {
                // Host broadcast ให้ทุกคนไปพร้อมกัน (ไม่มี affection)
                mpServer.broadcastChoiceResult(targetScene, null, 0);
            } else if (mpClient != null) {
                // Client รอรับ CHOICE_RESULT จาก Server
            } else {
                loadScene(targetScene); // Singleplayer
            }
        } else {
            if (mpServer != null) nextScene();
            else if (mpClient != null) { /* รอ Server */ }
            else nextScene();
        }
    }

    /** highlight ปุ่มที่ถูกสุ่มเลือก */
    private void highlightRandomPick(int index) {
        int i = 0;
        for (Component c : choiceLayer.getComponents()) {
            if (c instanceof UI_Components.ChoiceButton) {
                c.setBackground(i == index
                    ? new Color(255, 180, 0)
                    : new Color(60, 60, 80));
                i++;
            }
        }
        repaint();
    }

    // ════════════════════════════════════════════════════
    //  Register next scenes for FORCE_NEXT routing
    // ════════════════════════════════════════════════════
    private void registerNextScenes(GameServer server) {
        String[] order = StoryDataMP.SCENE_ORDER;
        for (int i = 0; i < order.length - 1; i++)
            server.registerNextScene(order[i], order[i+1]);
    }

    // ════════════════════════════════════════════════════
    //  UI Setup
    // ════════════════════════════════════════════════════
    private void initStoryMap() {
        for (String name : StoryDataMP.SCENE_ORDER) {
            Object[][] data = StoryDataMP.getScene(name);
            if (data != null) storyMap.put(name, data);
        }
    }

    private void setupUI() {
        bgLayer = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g); drawBG((Graphics2D) g);
            }
        };
        characterLayer = new CharacterSprite("") {
            @Override protected void paintComponent(Graphics g) { drawChar((Graphics2D) g); }
        };
        effectLayer = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g.create();
                if (effectManager != null) effectManager.drawEffects(g2, getWidth(), getHeight());
                g2.dispose();
            }
        };
        effectLayer.setOpaque(false);
        choiceLayer = new JPanel(null);
        choiceLayer.setOpaque(false);
        timerBar = new MultiplayerTimerBar();

        add(timerBar); add(choiceLayer); add(dialogueBox);
        add(effectLayer); add(characterLayer); add(bgLayer);
        fixZOrder();
    }

    private void fixZOrder() {
        try {
            setComponentZOrder(timerBar,       0);
            setComponentZOrder(choiceLayer,    1);
            setComponentZOrder(dialogueBox,    2);
            setComponentZOrder(effectLayer,    3);
            setComponentZOrder(characterLayer, 4);
            setComponentZOrder(bgLayer,        5);
        } catch (Exception ignored) {}
    }

    private void drawBG(Graphics2D g2) {
        if (effectManager != null) g2.translate(effectManager.getShakeX(), effectManager.getShakeY());
        if (currentBG == null || currentBG.isEmpty() || currentBG.equals("none")) return;
        try {
            Image img = new ImageIcon(currentBG).getImage();
            if (img == null || img.getWidth(null) <= 0) return;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int pw = getWidth(), ph = getHeight();
            double s = Math.max((double)pw/img.getWidth(null),(double)ph/img.getHeight(null)) + 0.15;
            int dw = (int)(img.getWidth(null)*s), dh = (int)(img.getHeight(null)*s);
            g2.drawImage(img, (pw-dw)/2, (ph-dh)/2, dw, dh, this);
        } catch (Exception ignored) {}
    }

    private void drawChar(Graphics2D g2) {
        if (effectManager != null) g2.translate(effectManager.getShakeX(), effectManager.getShakeY());
        if (currentChar == null || currentChar.isEmpty() || currentChar.equals("none")) return;
        try {
            Image img = new ImageIcon(currentChar).getImage();
            if (img == null || img.getWidth(null) <= 0) return;
            int ph = getHeight();
            double r = (double)(ph+30)/img.getHeight(null);
            int dw = (int)(img.getWidth(null)*r);
            g2.drawImage(img, (getWidth()-dw)/2, 0, dw, ph+30, this);
        } catch (Exception ignored) {}
    }

    // ── Layout ─────────────────────────────────────────────
    private void relayout() {
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

        // ซ่อน RelationUI ใน MP mode เสมอ
        RelationUI.getInstance().setVisible(false);

        bgLayer.setBounds(0, 0, w, h);
        characterLayer.setBounds(0, 0, w, h);
        effectLayer.setBounds(0, 0, w, h);

        int tw = Math.min((int)(w * 0.70), 560);
        int th = 72;
        timerBar.setBounds((w - tw) / 2, 8, tw, th);

        int gw = (int)(w * 0.70);
        int gh = (int)(h * 0.25);
        int btnCount = countChoiceButtons();
        int dialogueY = (isChoiceMode && btnCount > 0)
            ? h - gh - (btnCount * 65) - 70
            : h - gh - 70;
        dialogueY = Math.max(dialogueY, th + 16);

        dialogueBox.moveTo(gw, gh, dialogueY);
        layoutChoiceButtons(gw, dialogueY + gh + 10);

        revalidate(); repaint();
    }

    private void layoutChoiceButtons(int gw, int startY) {
        int w = getWidth(), cx = (w - gw) / 2;
        int btnH = 55, gap = 10, y = 0;
        for (Component c : choiceLayer.getComponents()) {
            if (c instanceof UI_Components.ChoiceButton) {
                c.setBounds(0, y, gw, btnH); y += btnH + gap;
            }
        }
        choiceLayer.setBounds(cx, startY, gw, Math.max(y, 50));
    }

    private int countChoiceButtons() {
        int n = 0;
        for (Component c : choiceLayer.getComponents())
            if (c instanceof UI_Components.ChoiceButton) n++;
        return n;
    }

    /** แสดง toast แจ้งเตือนผู้เล่นออกจากห้อง */
    public void showPlayerLeftToast(String playerName) {
        SwingUtilities.invokeLater(() -> {
            // ใช้ JWindow แบบ undecorated ลอยเหนือหน้าจอ
            Window parent = SwingUtilities.getWindowAncestor(this);
            JWindow popup = new JWindow(parent);

            JPanel panel = new JPanel(new BorderLayout(8, 0));
            panel.setBackground(new Color(30, 30, 30, 220));
            panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 60, 60), 2),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
            ));
            JLabel icon = new JLabel("✖ ");
            icon.setFont(new Font("Dialog", Font.BOLD, 14));
            icon.setForeground(new Color(255, 80, 80));
            JLabel msg = new JLabel(playerName + " ออกจากห้องแล้ว");
            msg.setFont(new Font("Tahoma", Font.PLAIN, 14));
            msg.setForeground(Color.WHITE);
            panel.add(icon, BorderLayout.WEST);
            panel.add(msg, BorderLayout.CENTER);
            popup.add(panel);
            popup.pack();

            // วางมุมขวาล่างของ parent window
            if (parent != null) {
                int px = parent.getX() + parent.getWidth()  - popup.getWidth()  - 14;
                int py = parent.getY() + parent.getHeight() - popup.getHeight() - 14;
                popup.setLocation(px, py);
            }
            popup.setOpacity(0.92f);
            popup.setVisible(true);

            new javax.swing.Timer(3000, e -> {
                ((javax.swing.Timer)e.getSource()).stop();
                popup.dispose();
            }).start();
        });
    }

    /** เรียกเมื่อ player ออกระหว่าง choice — reset UI ให้ host กดได้ */
    public void resetWaitingState() {
        SwingUtilities.invokeLater(() -> {
            timerBar.resetWaitingForPlayers(); // ซ่อน "รอผู้เล่นคนอื่น..."
            unlockChoiceButtons();             // unlock ปุ่มถ้า host ยังไม่กด
        });
    }

    private void lockChoiceButtons() {
        for (Component comp : choiceLayer.getComponents())
            if (comp instanceof UI_Components.ChoiceButton) comp.setEnabled(false);
        repaint();
    }
    private void unlockChoiceButtons() {
        for (Component c : choiceLayer.getComponents())
            if (c instanceof UI_Components.ChoiceButton) c.setEnabled(true);
        repaint();
    }

    // ════════════════════════════════════════════════════
    //  Scene Logic
    // ════════════════════════════════════════════════════
    private void loadScene(String name) {
        Object[][] data = storyMap.get(name);
        if (data == null) { finishGame(); return; }
        effectManager.stopAll();

        currentScene = data; sceneName = name;
        sceneIndex = 0; sentSceneReady = false; waitingForResult = false;
        isChoiceMode = false; pendingChoices = null;
        choiceLayer.removeAll(); choiceLayer.setVisible(false);

        int total = (mpServer != null) ? mpServer.getPlayerCount() + 1 : 2;

        if (mpServer != null) {
            // ถ้ามี pendingScene = name แสดงว่ามาจาก CHOICE_RESULT → แจ้ง server ว่าโหลดเสร็จ
            // ถ้าเป็นซีนแรก (MP_INTRO) → startReadPhase โดยตรง
            if (mpServer.hasPendingScene(name)) {
                mpServer.hostSceneLoaded(name); // server จะ startReadPhase เมื่อทุกคนโหลดเสร็จ
            } else {
                mpServer.startReadPhase(name);  // ซีนแรก หรือ forced navigation
            }
            // timerBar จะเริ่มผ่าน onHostPhaseRead callback
        } else if (mpClient != null) {
            // ✅ ไม่ start timer ที่นี่ — onHostPhaseRead จาก server จะ set timer
            // แค่ reset timer bar ให้พร้อม
            timerBar.resetAndHide();
        } else {
            // Singleplayer
            timerBar.startLocalReadPhase(name, GameServer.READ_SECONDS, () ->
                SwingUtilities.invokeLater(() ->
                    timerBar.startLocalCountdownOverlay(() ->
                        SwingUtilities.invokeLater(() -> {
                            timerBar.startLocalChoicePhase(name, GameServer.CHOICE_SECONDS,
                                () -> SwingUtilities.invokeLater(() -> doForceNext("")));
                            unlockChoiceButtons();
                            if (!isChoiceMode || countChoiceButtons() == 0) skipToChoice(true);
                            relayout();
                        })
                    )
                )
            );
        }

        renderLine(currentScene[sceneIndex]);
    }

    private void renderLine(Object[] line) {
        if (line == null || line.length < 2) return;
        choiceLayer.removeAll(); choiceLayer.setVisible(false);
        isChoiceMode = false; pendingChoices = null;

        currentSpeaker = "";
        Object sp = line[0];
        if (sp != null) {
            String s = sp.toString().trim();
            if (s.equalsIgnoreCase("PLAYER") || s.equals(GameConstants.PLAYER_NAME))
                currentSpeaker = GameConstants.PLAYER_NAME;
            else if (!s.isEmpty() && !s.equalsIgnoreCase("none"))
                currentSpeaker = s;
        }
        dialogueBox.setText(currentSpeaker, "");
        fullText = line[1].toString().replace("[PLAYER]", GameConstants.PLAYER_NAME);

        if (line.length >= 3) {
            String fn = (String)line[2];
            currentChar = (fn==null||fn.isEmpty()||fn.equals("none")) ? ""
                : fn.startsWith("model/") ? fn : GameConstants.CHAR_PATH+fn;
            characterLayer.updateCharacter(currentChar);
        }
        if (line.length >= 4) {
            String bg = (String)line[3];
            currentBG = (bg==null||bg.isEmpty()||bg.equals("none")) ? ""
                : bg.startsWith("model/") ? bg : GameConstants.SCENE_PATH+bg;
        }
        for (int i = 4; i < line.length; i++) {
            Object slot = line[i];
            if (slot instanceof Object[][]) {
                pendingChoices = (Object[][]) slot; isChoiceMode = true;
            } else if (slot instanceof String) {
                String val = (String)slot;
                if (val==null||val.isEmpty()||val.equalsIgnoreCase("none")) continue;
                String up = val.toUpperCase();
                if (up.contains("FADE")||up.contains("WHITE")||up.contains("BLACK")
                        ||up.equals("SHAKE")||up.equals("FLASH")) {
                    effectManager.stopAll(); effectManager.play(val);
                } else {
                    String path = GameConstants.SOUND_PATH+val+".wav";
                    if (up.startsWith("BGM")) soundManager.playBGM(path); else soundManager.playSE(path);
                }
            }
        }
        dialogueBox.setVisible(true);
        relayout(); startTypewriter();
    }

    // ════════════════════════════════════════════════════
    //  Show Choices
    // ════════════════════════════════════════════════════
    private void showChoices(Object[][] choices, boolean alreadyUnlocked) {
        if (choices == null) return;
        isChoiceMode = true; choiceLayer.removeAll();

        for (int i = 0; i < choices.length; i++) {
            final String target = (String)  choices[i][1];
            final String cn     = choices[i].length >= 4 ? (String)  choices[i][2] : null;
            final int    cs     = choices[i].length >= 4 ? (Integer) choices[i][3] : 0;
            final String text   = (String)  choices[i][0];
            final String num    = String.valueOf(i + 1);

            UI_Components.ChoiceButton btn = new UI_Components.ChoiceButton(num, text, () -> {
                if (timerBar.isChoiceLocked()) return;
                doChoice(target, cn, cs);
            });
            btn.setEnabled(alreadyUnlocked);
            choiceLayer.add(btn);
        }

        relayout(); choiceLayer.setVisible(true); fixZOrder();
        if (alreadyUnlocked) unlockChoiceButtons();

        if (!alreadyUnlocked && !sentSceneReady) {
            sentSceneReady = true;
            if (mpClient != null) mpClient.notifySceneReady(sceneName);
            if (mpServer != null) mpServer.hostSceneReady(sceneName);
        }
    }

    // ════════════════════════════════════════════════════
    //  Skip to Choice
    // ════════════════════════════════════════════════════
    private void skipToChoice(boolean unlock) {
        if (typeTimer != null) typeTimer.stop();
        if (isChoiceMode && countChoiceButtons() > 0) {
            if (unlock) unlockChoiceButtons();
            return;
        }
        if (currentScene == null) return;
        for (int i = sceneIndex; i < currentScene.length; i++) {
            Object[] line = currentScene[i];
            if (line == null || line.length < 2) continue;
            for (int j = 4; j < line.length; j++) {
                if (!(line[j] instanceof Object[][])) continue;
                sceneIndex = i; pendingChoices = (Object[][])line[j]; isChoiceMode = true;
                currentSpeaker = "";
                if (line[0] != null) {
                    String s = line[0].toString().trim();
                    if (s.equalsIgnoreCase("PLAYER")||s.equals(GameConstants.PLAYER_NAME))
                        currentSpeaker = GameConstants.PLAYER_NAME;
                    else if (!s.isEmpty() && !s.equalsIgnoreCase("none"))
                        currentSpeaker = s;
                }
                fullText = line[1].toString().replace("[PLAYER]", GameConstants.PLAYER_NAME);
                dialogueBox.setText(currentSpeaker, fullText);
                if (line.length>=3) { String fn=(String)line[2]; currentChar=(fn==null||fn.isEmpty()||fn.equals("none"))?"":fn.startsWith("model/")?fn:GameConstants.CHAR_PATH+fn; characterLayer.updateCharacter(currentChar); }
                if (line.length>=4) { String bg=(String)line[3]; currentBG=(bg==null||bg.isEmpty()||bg.equals("none"))?"":bg.startsWith("model/")?bg:GameConstants.SCENE_PATH+bg; }
                dialogueBox.setVisible(true);
                showChoices(pendingChoices, unlock);
                return;
            }
        }
        nextScene();
    }

    // ════════════════════════════════════════════════════
    //  doChoice
    // ════════════════════════════════════════════════════
    private void doChoice(String target, String charName, int score) {
        // apply affection ของตัวเองทันที
        if (charName != null && !charName.isEmpty() && score != 0) {
            Relation.getInstance().addAffection(charName, score);
            RelationUI.getInstance().setVisible(false); // ซ่อนใน MP
        }

        isChoiceMode = false; pendingChoices = null; sentSceneReady = false;
        choiceLayer.removeAll(); choiceLayer.setVisible(false);
        timerBar.stopTimer();
        lockChoiceButtons();

        if (mpServer != null) {
            waitingForResult = true; // ✅ รอ CHOICE_RESULT
            mpServer.broadcastChoiceResult(target, charName, score);
            timerBar.showWaitingForPlayers();
        } else if (mpClient != null) {
            waitingForResult = true; // ✅ รอ CHOICE_RESULT
            mpClient.sendPlayerChoice(target, charName, score);
            timerBar.showWaitingForPlayers();
        } else {
            // Singleplayer
            timerBar.resetAndHide();
            if (target != null && storyMap.containsKey(target)) loadScene(target);
            else finishGame();
        }
    }

    /** ทุกคนรับ choice result นี้พร้อมกัน (Host+Client) */
    private void applyChoiceResult(String target, String charName, int score) {
        // ถ้าผู้เล่นยังไม่ได้กดเลือก (isChoiceMode ยังเป็น true) → สุ่ม affection ให้
        if (isChoiceMode && pendingChoices != null && pendingChoices.length > 0) {
            int pick = random.nextInt(pendingChoices.length);
            Object[] chosen = pendingChoices[pick];
            String cn = chosen.length >= 4 ? (String) chosen[2] : null;
            int    cs = chosen.length >= 4 ? (Integer) chosen[3] : 0;
            if (cn != null && !cn.isEmpty() && cs != 0) {
                Relation.getInstance().addAffection(cn, cs);
                RelationUI.getInstance().setVisible(false);
            }
        }

        isChoiceMode = false; pendingChoices = null; sentSceneReady = false;
        waitingForResult = false; // ✅ ได้รับ CHOICE_RESULT แล้ว
        choiceLayer.removeAll(); choiceLayer.setVisible(false);
        timerBar.resetAndHide();

        if (target == null || target.isEmpty() || !storyMap.containsKey(target)) {
            finishGame(); return;
        }

        if (mpServer != null) {
            loadScene(target);
        } else if (mpClient != null) {
            loadScene(target);
            mpClient.notifySceneLoaded(target);
        } else {
            loadScene(target);
        }
    }

    // ════════════════════════════════════════════════════
    //  Navigation
    // ════════════════════════════════════════════════════
    private void onNext() {
        // ✅ FIX: รอ CHOICE_RESULT จาก server — ห้ามคลิกข้าม
        if (waitingForResult) return;

        if (typeTimer != null && typeTimer.isRunning()) {
            typeTimer.stop(); charIndex = fullText.length();
            dialogueBox.setText(currentSpeaker, fullText); return;
        }
        if (isChoiceMode && countChoiceButtons() == 0 && pendingChoices != null) {
            showChoices(pendingChoices, !timerBar.isChoiceLocked()); return;
        }
        if (isChoiceMode && countChoiceButtons() > 0) return;

        sceneIndex++;
        if (sceneIndex < currentScene.length) renderLine(currentScene[sceneIndex]);
        else nextScene();
    }

    private void nextScene() {
        timerBar.stopTimer();
        if ("MP_END".equals(sceneName)) { finishGame(); return; }
        String[] order = StoryDataMP.SCENE_ORDER;
        for (int i = 0; i < order.length - 1; i++) {
            if (order[i].equals(sceneName)) { loadScene(order[i+1]); return; }
        }
        finishGame();
    }

    private void finishGame() {
        timerBar.resetAndHide();
        if (onGameFinished != null) onGameFinished.run();
    }

    private void startTypewriter() {
        charIndex = 0;
        if (typeTimer != null) typeTimer.stop();
        dialogueBox.setVisible(true);
        typeTimer = new Timer(GameConstants.TYPEWRITER_SPEED, e -> {
            if (charIndex < fullText.length()) {
                charIndex++;
                dialogueBox.setText(currentSpeaker, fullText.substring(0, charIndex));
            } else { ((Timer)e.getSource()).stop(); }
        });
        typeTimer.start();
    }

    private void setupKeys() {
        // ✅ WHEN_IN_FOCUSED_WINDOW — ไม่ต้องการ focus จาก JPanel
        // ❌ ลบ KeyListener และ requestFocusInWindow ออกทั้งหมด
        setFocusable(false); // ป้องกัน focus cycle ดูด focus จาก window

        InputMap  im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(model.KeyConfig.getNextMsg(), 0), "mp_next");
        am.put("mp_next", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { onNext(); }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "mp_escape");
        am.put("mp_escape", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { handleEsc(); }
        });
    }

    private void handleEsc() {
        PauseMenuUI pause = PauseMenuUI.getInstance();
        if (pause.isMenuVisible()) { pause.hideMenu(); return; }
        pause.showMenu(PlaySceneMP.this, false);
    }
}