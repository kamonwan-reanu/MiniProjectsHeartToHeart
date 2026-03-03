package UI_Screens;

import UI_Components.CharacterSprite;
import UI_Components.DialogueBox;
import UI_Components.EffectManager;
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
    private boolean sentSceneReady = false;
    private boolean hasChosen         = false;
    private boolean forceNextPending  = false;  // รอให้ random เสร็จก่อน

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

    private static final Random RNG = new Random();

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

    // ════════════════════════════════════════════════════════
    //  Public API
    // ════════════════════════════════════════════════════════
    public void startMPGame(GameServer server, GameClient client) {
        this.mpServer = server;
        this.mpClient = client;

        Relation.getInstance().resetAll();
        RelationUI.getInstance().updateAllScores();
        effectManager.stopAll();
        timerBar.resetAndHide();
        hasChosen = false;

        if (client != null) client.setListener(buildClientListener());

        loadScene("MP_INTRO");
        relayout();
    }

    public void setOnGameFinished(Runnable r) { this.onGameFinished = r; }

    // ── Host callbacks ────────────────────────────────────────────────────
    public void onHostPhaseRead(String scene, int sec, int total) {
        timerBar.startReadPhase(scene, sec, total);
        relayout();
    }

    public void onHostPhaseChoice(String scene, int sec, int total) {
        timerBar.startChoicePhase(scene, sec, total);
        // ✅ skipToChoice ก่อน แล้วค่อย unlock
        if (!isChoiceMode || countChoiceButtons() == 0) skipToChoice(false);
        unlockChoiceButtons();
        relayout();
    }

    public void onHostForceNext(String scene) {
        if (hasChosen) return;  // ✅ random กำลัง navigate อยู่แล้ว
        forceNextPending = true;
        if (!isChoiceMode || countChoiceButtons() == 0) {
            if (pendingChoices == null) skipToChoice(false);
        }
        if (pendingChoices != null && pendingChoices.length > 0) {
            doRandomChoice();
        } else {
            doForceNext();
        }
    }

    public void onHostTimerSync(int t) { timerBar.setTimeLeft(t); }

    public void onHostReadyCount(int ready, int total) { timerBar.setReadyCount(ready, total); }

    /** FIX 1: alert — แสดงบน parent ไม่ใช่บน timerBar เพื่อไม่บัง */
    public void onHostChoiceAlert(String scene) {
        showAlertOverlay();
    }

    /** FIX 2: countdown */
    public void onHostCountdown(int n) {
        timerBar.showChoiceAlertBanner(n);
    }

    /** FIX 4: random choice จาก server — server บอกว่าสุ่มให้แล้ว */
    public void onHostRandomChoice(String scene, int choiceIndex) {
        if (hasChosen) return;  // เลือกเองแล้ว ไม่ต้องสุ่ม
        if (!isChoiceMode || countChoiceButtons() == 0) {
            if (pendingChoices == null) skipToChoice(false);
        }
        if (pendingChoices != null && pendingChoices.length > 0) {
            int idx = Math.min(choiceIndex, pendingChoices.length - 1);
            doRandomChoiceAt(idx);
            // FORCE_NEXT จะมาทีหลัง แต่ hasChosen=true แล้ว จะไม่ทำซ้ำ
        }
    }

    // ✅ แสดง alert overlay บน panel ตัวเอง (ไม่ใช่ใน timerBar) เพื่อไม่บังเวลา
    private JLabel alertOverlayLabel = null;
    private void showAlertOverlay() {
        SwingUtilities.invokeLater(() -> {
            if (alertOverlayLabel == null) {
                alertOverlayLabel = new JLabel("⚡  เลือกตัวเลือกได้เลย!", SwingConstants.CENTER) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(new Color(255, 180, 0, 220));
                        g2.fill(new java.awt.geom.RoundRectangle2D.Float(0,0,getWidth(),getHeight(),18,18));
                        g2.setColor(new Color(100,60,0,200));
                        g2.setStroke(new BasicStroke(2f));
                        g2.draw(new java.awt.geom.RoundRectangle2D.Float(1,1,getWidth()-2,getHeight()-2,18,18));
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                alertOverlayLabel.setFont(new Font("Tahoma", Font.BOLD, 22));
                alertOverlayLabel.setForeground(new Color(20,10,0));
                alertOverlayLabel.setOpaque(false);
            }
            // วางใต้ timerBar (y=90) ไม่ทับ
            int w = getWidth();
            int bw = Math.min((int)(w * 0.55f), 480);
            int bh = 48;
            alertOverlayLabel.setBounds((w - bw)/2, 90, bw, bh);
            add(alertOverlayLabel);
            setComponentZOrder(alertOverlayLabel, 0);
            repaint();

            new Timer(1600, e -> {
                ((Timer)e.getSource()).stop();
                remove(alertOverlayLabel);
                repaint();
            }).start();
        });
    }

    // ════════════════════════════════════════════════════════
    //  Client Listener
    // ════════════════════════════════════════════════════════
    private GameClient.ClientListener buildClientListener() {
        return new GameClient.ClientListener() {
            @Override public void onConnected(String n) {}
            @Override public void onPlayerListReceived(java.util.List<String> n) {}
            @Override public void onGameStart(int s) {}
            @Override public void onPlayerJoined(String n, int t) {}
            @Override public void onPlayerLeft(String n, int t) {}
            @Override public void onScoreUpdate(String n, int s) {}
            @Override public void onLeaderboard(Map<String,Integer> sc) {}
            @Override public void onChatMessage(String s, String m) {}
            @Override public void onDisconnected(String r) {}
            @Override public void onError(String m) {}

            @Override public void onPhaseRead(String scene, int sec, int total) {
                SwingUtilities.invokeLater(() -> {
                    timerBar.startReadPhase(scene, sec, total);
                    relayout();
                });
            }

            @Override public void onPhaseChoice(String scene, int sec, int total) {
                SwingUtilities.invokeLater(() -> {
                    timerBar.startChoicePhase(scene, sec, total);
                    unlockChoiceButtons();
                    if (!isChoiceMode || countChoiceButtons() == 0) skipToChoice(true);
                    relayout();
                });
            }

            @Override public void onChoiceAlert(String scene) {
                SwingUtilities.invokeLater(() -> showAlertOverlay());
            }

            @Override public void onCountdown(int n) {
                SwingUtilities.invokeLater(() -> timerBar.showChoiceAlertBanner(n));
            }

            @Override public void onRandomChoice(String scene, int choiceIndex) {
                SwingUtilities.invokeLater(() -> {
                    if (hasChosen) return;
                    if (!isChoiceMode || countChoiceButtons() == 0) {
                        if (pendingChoices == null) skipToChoice(false);
                    }
                    if (pendingChoices != null && pendingChoices.length > 0) {
                        int idx = Math.min(choiceIndex, pendingChoices.length - 1);
                        doRandomChoiceAt(idx);
                    }
                });
            }

            // ✅ FORCE_NEXT: ถ้าไม่เลือก ให้สุ่มก่อน แล้วค่อยไป
            //    ถ้า random กำลังทำงานอยู่ (hasChosen=true) ไม่ต้องทำอะไร
            @Override public void onForceNext(String scene) {
                SwingUtilities.invokeLater(() -> {
                    if (hasChosen) return;  // random กำลัง navigate อยู่แล้ว
                    forceNextPending = true;
                    // ถ้ายังไม่ถึง choice line ให้ข้ามไปก่อน
                    if (!isChoiceMode || countChoiceButtons() == 0) {
                        if (pendingChoices == null) skipToChoice(false);
                    }
                    if (pendingChoices != null && pendingChoices.length > 0) {
                        doRandomChoice();  // สุ่มแล้ว doChoice จะ navigate เอง
                    } else {
                        doForceNext();     // ไม่มี choice → ไปซีนต่อเลย
                    }
                });
            }

            @Override public void onTimerSync(int t) {
                SwingUtilities.invokeLater(() -> timerBar.setTimeLeft(t));
            }

            @Override public void onReadyCount(int ready, int total) {
                SwingUtilities.invokeLater(() -> timerBar.setReadyCount(ready, total));
            }
        };
    }

    // ════════════════════════════════════════════════════════
    //  Random Choice
    // ════════════════════════════════════════════════════════
    private void doRandomChoice() {
        if (pendingChoices == null || pendingChoices.length == 0) return;
        doRandomChoiceAt(RNG.nextInt(pendingChoices.length));
    }

    private void doRandomChoiceAt(int idx) {
        if (pendingChoices == null || idx >= pendingChoices.length) return;
        hasChosen = true;  // ✅ mark ทันทีเพื่อกัน doForceNext ที่อาจมาทีหลัง
        Object[] chosen = pendingChoices[idx];
        String target = (String)  chosen[1];
        String cn     = chosen.length >= 4 ? (String)  chosen[2] : null;
        int    cs     = chosen.length >= 4 ? (Integer) chosen[3] : 0;

        flashRandomChoiceEffect(idx);
        new Timer(600, e -> {
            ((Timer) e.getSource()).stop();
            forceNextPending = false;
            doChoice(target, cn, cs);
        }).start();
    }

    private void flashRandomChoiceEffect(int idx) {
        int i = 0;
        for (Component c : choiceLayer.getComponents()) {
            if (c instanceof UI_Components.ChoiceButton) {
                if (i == idx) {
                    c.setBackground(new Color(255, 215, 0));
                    c.setEnabled(true);
                }
                i++;
            }
        }
        choiceLayer.repaint();
    }

    // ── Force Next ─────────────────────────────────────────
    private void unlockChoiceButtons() {
        for (Component c : choiceLayer.getComponents())
            if (c instanceof UI_Components.ChoiceButton) c.setEnabled(true);
        repaint();
    }

    private void doForceNext() {
        // ✅ ถ้า random choice กำลังทำงานอยู่ ไม่ต้อง force ซ้ำ
        if (hasChosen) return;
        isChoiceMode      = false;
        pendingChoices    = null;
        sentSceneReady    = false;
        hasChosen         = false;
        forceNextPending  = false;
        choiceLayer.removeAll();
        choiceLayer.setVisible(false);
        timerBar.resetAndHide();
        nextScene();
    }

    // ════════════════════════════════════════════════════════
    //  Story Map
    // ════════════════════════════════════════════════════════
    private void initStoryMap() {
        for (String name : StoryDataMP.SCENE_ORDER) {
            Object[][] data = StoryDataMP.getScene(name);
            if (data != null) storyMap.put(name, data);
        }
    }

    // ════════════════════════════════════════════════════════
    //  UI Setup
    // ════════════════════════════════════════════════════════
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

        add(timerBar);
        add(choiceLayer);
        add(dialogueBox);
        add(effectLayer);
        add(characterLayer);
        add(bgLayer);
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

    private void relayout() {
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

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
        int w = getWidth();
        int cx = (w - gw) / 2;
        int btnH = 55, gap = 10, y = 0;
        for (Component c : choiceLayer.getComponents()) {
            if (c instanceof UI_Components.ChoiceButton) {
                c.setBounds(0, y, gw, btnH);
                y += btnH + gap;
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

    // ════════════════════════════════════════════════════════
    //  Scene Logic
    // ════════════════════════════════════════════════════════
    private void loadScene(String name) {
        Object[][] data = storyMap.get(name);
        if (data == null) { finishGame(); return; }
        effectManager.stopAll();

        currentScene   = data;
        sceneName      = name;
        sceneIndex     = 0;
        sentSceneReady    = false;
        isChoiceMode      = false;
        pendingChoices    = null;
        hasChosen         = false;
        forceNextPending  = false;
        choiceLayer.removeAll();
        choiceLayer.setVisible(false);

        int total = (mpServer != null) ? mpServer.getPlayerCount() + 1 : 2;

        if (mpServer != null) {
            mpServer.startReadPhase(name);
        } else if (mpClient != null) {
            timerBar.startReadPhase(name, GameServer.READ_SECONDS, total);
        } else {
            timerBar.startLocalReadPhase(name, GameServer.READ_SECONDS, () -> {
                SwingUtilities.invokeLater(() -> {
                    showAlertOverlay();
                    timerBar.startLocalChoicePhase(name, GameServer.CHOICE_SECONDS, () -> {
                        SwingUtilities.invokeLater(() -> {
                            if (!hasChosen && isChoiceMode && pendingChoices != null) {
                                doRandomChoice();
                            } else {
                                doForceNext();
                            }
                        });
                    });
                    unlockChoiceButtons();
                    if (!isChoiceMode || countChoiceButtons() == 0) skipToChoice(true);
                    relayout();
                });
            });
        }

        renderLine(currentScene[sceneIndex]);
    }

    private void renderLine(Object[] line) {
        if (line == null || line.length < 2) return;

        choiceLayer.removeAll();
        choiceLayer.setVisible(false);
        isChoiceMode   = false;
        pendingChoices = null;

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
            String fn = (String) line[2];
            currentChar = (fn==null||fn.isEmpty()||fn.equals("none")) ? ""
                : fn.startsWith("model/") ? fn : GameConstants.CHAR_PATH + fn;
            characterLayer.updateCharacter(currentChar);
        }
        if (line.length >= 4) {
            String bg = (String) line[3];
            currentBG = (bg==null||bg.isEmpty()||bg.equals("none")) ? ""
                : bg.startsWith("model/") ? bg : GameConstants.SCENE_PATH + bg;
        }
        for (int i = 4; i < line.length; i++) {
            Object slot = line[i];
            if (slot instanceof Object[][]) {
                pendingChoices = (Object[][]) slot;
                isChoiceMode   = true;
            } else if (slot instanceof String) {
                String val = (String) slot;
                if (val==null||val.isEmpty()||val.equalsIgnoreCase("none")) continue;
                String up = val.toUpperCase();
                if (up.contains("FADE")||up.contains("WHITE")||up.contains("BLACK")
                        ||up.equals("SHAKE")||up.equals("FLASH")) {
                    effectManager.stopAll(); effectManager.play(val);
                } else {
                    String path = GameConstants.SOUND_PATH + val + ".wav";
                    if (up.startsWith("BGM")) soundManager.playBGM(path);
                    else soundManager.playSE(path);
                }
            }
        }
        dialogueBox.setVisible(true);
        relayout();
        startTypewriter();
    }

    private void showChoices(Object[][] choices, boolean alreadyUnlocked) {
        if (choices == null) return;
        isChoiceMode = true;
        choiceLayer.removeAll();

        for (int i = 0; i < choices.length; i++) {
            final String target = (String)  choices[i][1];
            final String cn     = choices[i].length >= 4 ? (String)  choices[i][2] : null;
            final int    cs     = choices[i].length >= 4 ? (Integer) choices[i][3] : 0;
            final String text   = (String)  choices[i][0];
            final String num    = String.valueOf(i + 1);

            UI_Components.ChoiceButton btn = new UI_Components.ChoiceButton(num, text, () -> {
                if (timerBar.isChoiceLocked()) return;
                hasChosen = true;
                // ✅ ใช้ method ที่ถูกต้องตาม GameClient API
                if (mpClient != null) mpClient.notifyChoiceMade(sceneName);
                if (mpServer != null) mpServer.hostMadeChoice(sceneName);
                doChoice(target, cn, cs);
            });
            btn.setEnabled(alreadyUnlocked);
            choiceLayer.add(btn);
        }

        relayout();
        choiceLayer.setVisible(true);
        fixZOrder();

        if (alreadyUnlocked) unlockChoiceButtons();

        if (!alreadyUnlocked && !sentSceneReady) {
            sentSceneReady = true;
            if (mpClient != null) mpClient.notifySceneReady(sceneName);
            if (mpServer != null) mpServer.hostSceneReady(sceneName);
        }
    }

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
                sceneIndex     = i;
                pendingChoices = (Object[][]) line[j];
                isChoiceMode   = true;
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
                dialogueBox.setVisible(true);
                showChoices(pendingChoices, unlock);
                return;
            }
        }
        nextScene();
    }

    private void doChoice(String target, String charName, int score) {
        isChoiceMode   = false;
        pendingChoices = null;
        sentSceneReady = false;
        choiceLayer.removeAll();
        choiceLayer.setVisible(false);
        timerBar.resetAndHide();

        if (charName != null && !charName.isEmpty() && score != 0) {
            Relation.getInstance().addAffection(charName, score);
            RelationUI.getInstance().updateScore(charName);
        }
        if (target != null && storyMap.containsKey(target)) loadScene(target);
        else finishGame();
    }

    private void onNext() {
        if (typeTimer != null && typeTimer.isRunning()) {
            typeTimer.stop(); charIndex = fullText.length();
            dialogueBox.setText(currentSpeaker, fullText); return;
        }
        if (isChoiceMode && countChoiceButtons() == 0 && pendingChoices != null) {
            showChoices(pendingChoices, !timerBar.isChoiceLocked());
            return;
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
            } else {
                ((Timer)e.getSource()).stop();
            }
        });
        typeTimer.start();
    }

    private void setupKeys() {
        InputMap  im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        im.put(KeyStroke.getKeyStroke(model.KeyConfig.getNextMsg(), 0), "mp_next");
        am.put("mp_next", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { onNext(); }
        });
    }
}