package UI_Screens;

import UI_Components.CharacterSprite;
import UI_Components.DialogueBox;
import UI_Components.EffectManager;
import UI_Components.MultiplayerTimerBar;
import UI_Components.RelationUI;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import model.GameClient;
import model.GameConstants;
import model.GameServer;
import model.KeyConfig;
import model.SoundManager;
import model.StoryData;

public class PlaySceneMain extends JPanel {

    private Map<String, Object[][]> storyMap = new HashMap<>();
    private Object[][] currentSceneData;
    private String sceneName  = "";
    private int    storyIndex = 0;
    private Runnable onGameFinished;

    private String  currentBG      = "";
    private String  currentChar    = "";
    private String  currentSpeaker = "";
    private String  fullText       = "";
    private int     charIndex      = 0;
    private boolean isChoiceMode   = false;
    private Object[][] pendingChoices = null;

    private double bgScaleOffset = 0.0;
    private int    bgOffsetX = 0, bgOffsetY = 0;

    private JLabel          bgLayer;
    private CharacterSprite characterLayer;
    private JPanel          effectLayer;
    private JPanel          choiceLayer;
    private DialogueBox     dialogueBox;
    private Timer           typeTimer;

    private SoundManager  soundManager  = new SoundManager();
    private EffectManager effectManager;

    private InputMap  inputMap;
    private ActionMap actionMap;

    // ===== Multiplayer =====
    private boolean multiplayerMode         = false;
    private boolean multiplayerEffectBypass = false;
    private int     mpReadSeconds           = 30;
    private int     mpTotalPlayers          = 2;
    private GameServer mpServer             = null;
    private GameClient mpClient             = null;
    private MultiplayerTimerBar timerBar;

    // ===== MiniGame =====
    private JPanel activeMiniGamePanel = null;

    // ================================================================
    public PlaySceneMain(Object[][] sceneData, String charPath, String sceneName) {
        this.currentSceneData = sceneData;
        this.sceneName        = sceneName;

        if (charPath != null && !charPath.isEmpty() && !charPath.equalsIgnoreCase("none")) {
            this.currentChar = charPath.startsWith("model/") ? charPath : GameConstants.CHAR_PATH + charPath;
        }

        setLayout(null);
        setOpaque(true);
        setBackground(Color.BLACK);

        this.dialogueBox   = new DialogueBox();
        this.effectManager = new EffectManager(this, dialogueBox);

        initStoryMap();
        setupUIComponents();

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { updateUIStyles(); }
            @Override public void componentShown(ComponentEvent e) {
                updateUIStyles();
                if (currentSceneData != null && storyIndex < currentSceneData.length)
                    updateScene(currentSceneData[storyIndex]);
            }
        });

        dialogueBox.setOnNextRequested(this::handleInteraction);
        setupKeyBindings();
    }

    // ================================================================
    // Multiplayer API
    // ================================================================

    public void setMultiplayerMode(boolean enabled, int readSeconds, GameServer server, GameClient client) {
        this.multiplayerMode  = enabled;
        this.mpReadSeconds    = (readSeconds > 0) ? readSeconds : 30;
        this.mpServer         = server;
        this.mpClient         = client;
        this.mpTotalPlayers   = (server != null) ? server.getPlayerCount() + 1 : 2;

        if (enabled) {
            if (timerBar == null) {
                timerBar = new MultiplayerTimerBar();
                add(timerBar);
            }
            timerBar.resetAndHide();
            timerBar.setVisible(false);
            refreshZOrder();
        } else {
            if (timerBar != null) timerBar.resetAndHide();
        }

        // บอก EffectManager ด้วย
        if (effectManager != null) effectManager.setMultiplayerBypass(enabled);
    }

    public void setMultiplayerEffectBypass(boolean bypass) {
        this.multiplayerEffectBypass = bypass;
        if (effectManager != null) effectManager.setMultiplayerBypass(bypass);
    }

    public void setOnGameFinished(Runnable callback) { this.onGameFinished = callback; }

    /** รับแจ้งจาก GameClient ว่าผู้เล่นอื่น CHOICE_READY */
    public void onRemotePlayerReady() {
        if (timerBar != null) SwingUtilities.invokeLater(() -> timerBar.otherPlayerReady());
    }

    // ================================================================
    // KeyBindings
    // ================================================================

    private void setupKeyBindings() {
        inputMap  = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        actionMap = getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyConfig.getNextMsg(), 0), "next");
        actionMap.put("next", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { handleInteraction(); }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyConfig.getRelationUI(), 0), "relation");
        actionMap.put("relation", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                RelationUI.getInstance().updateAllScores();
                RelationUI.getInstance().setVisible(!RelationUI.getInstance().isVisible());
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyConfig.getEscape(), 0), "escape");
        actionMap.put("escape", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { /* pause */ }
        });
    }

    private void setupChoiceKeyBindings(Object[][] choices) {
        if (choices == null) return;
        clearChoiceKeyBindings();
        int[] keys = { KeyConfig.getChoice1(), KeyConfig.getChoice2(), KeyConfig.getChoice3() };
        for (int i = 0; i < Math.min(choices.length, 3); i++) {
            final String sc = (String) choices[i][1];
            final String cn = (choices[i].length >= 4) ? (String)  choices[i][2] : null;
            final int    cs = (choices[i].length >= 4) ? (Integer) choices[i][3] : 0;
            inputMap.put(KeyStroke.getKeyStroke(keys[i], 0), "ch" + i);
            actionMap.put("ch" + i, new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    if (!isChoiceMode || pendingChoices == null) return;
                    if (multiplayerMode && timerBar != null && timerBar.isChoiceLocked()) return;
                    doChoice(sc, cn, cs);
                }
            });
        }
    }

    private void clearChoiceKeyBindings() {
        int[] keys = { KeyConfig.getChoice1(), KeyConfig.getChoice2(), KeyConfig.getChoice3() };
        for (int i = 0; i < 3; i++) {
            inputMap.remove(KeyStroke.getKeyStroke(keys[i], 0));
            actionMap.remove("ch" + i);
        }
    }

    // ================================================================
    // Story Map
    // ================================================================

    private void initStoryMap() {
        storyMap.put("SCENE_1", StoryData.SCENE_1);
        storyMap.put("SCENE_2", StoryData.SCENE_2);
        storyMap.put("SCENE_3", StoryData.SCENE_3);
        storyMap.put("SCENE_4", StoryData.SCENE_4);
        storyMap.put("SCENE_5", StoryData.SCENE_5);
        if (StoryData.SCENE_MP != null) storyMap.put("SCENE_MP", StoryData.SCENE_MP);
    }

    // ================================================================
    // UI Setup
    // ================================================================

    private void setupUIComponents() {
        bgLayer = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                renderBackground((Graphics2D) g);
            }
        };
        characterLayer = new CharacterSprite(currentChar) {
            @Override protected void paintComponent(Graphics g) { renderCharacter((Graphics2D) g); }
        };
        effectLayer = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                if (effectManager != null) effectManager.drawEffects(g2, getWidth(), getHeight());
                g2.dispose();
            }
        };
        effectLayer.setOpaque(false);

        choiceLayer = new JPanel();
        choiceLayer.setLayout(new BoxLayout(choiceLayer, BoxLayout.Y_AXIS));
        choiceLayer.setOpaque(false);

        add(choiceLayer);
        add(dialogueBox);
        add(effectLayer);
        add(characterLayer);
        add(bgLayer);
        refreshZOrder();
    }

    private void renderBackground(Graphics2D g2d) {
        g2d.translate(effectManager.getShakeX(), effectManager.getShakeY());
        if (currentBG != null && !currentBG.isEmpty() && !currentBG.equals("none")) {
            try {
                Image img = new ImageIcon(currentBG).getImage();
                if (img != null && img.getWidth(null) > 0) {
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    int pw = getWidth(), ph = getHeight();
                    double scale = Math.max((double)pw/img.getWidth(null), (double)ph/img.getHeight(null)) + bgScaleOffset + 0.15;
                    int dw = (int)(img.getWidth(null)*scale), dh = (int)(img.getHeight(null)*scale);
                    g2d.drawImage(img, (pw-dw)/2+bgOffsetX, (ph-dh)/2+bgOffsetY, dw, dh, this);
                }
            } catch (Exception ignored) {}
        }
    }

    private void renderCharacter(Graphics2D g2d) {
        g2d.translate(effectManager.getShakeX(), effectManager.getShakeY());
        if (currentChar != null && !currentChar.isEmpty() && !currentChar.equals("none")) {
            try {
                Image img = new ImageIcon(currentChar).getImage();
                if (img != null && img.getWidth(null) > 0) {
                    int ph = getHeight();
                    double r = (double)(ph+30)/img.getHeight(null);
                    int dw = (int)(img.getWidth(null)*r);
                    g2d.drawImage(img, (getWidth()-dw)/2, 0, dw, ph+30, this);
                }
            } catch (Exception ignored) {}
        }
    }

    private void refreshZOrder() {
        try {
            int z = 0;
           if (activeMiniGamePanel != null) {
                setComponentZOrder(activeMiniGamePanel, z++);
            }
            
            if (timerBar != null && timerBar.isVisible()) {
                setComponentZOrder(timerBar, z++);
            }
            
            setComponentZOrder(effectLayer,    z++);
            setComponentZOrder(choiceLayer,    z++);
            setComponentZOrder(dialogueBox,    z++);
            setComponentZOrder(characterLayer, z++);
            setComponentZOrder(bgLayer,        z);
        } catch (Exception ignored) {}
    }

    private void updateUIStyles() {
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

        bgLayer.setBounds(0, 0, w, h);
        characterLayer.setBounds(0, 0, w, h);
        effectLayer.setBounds(0, 0, w, h);

        if (timerBar != null && timerBar.isVisible()) {
            int tw = 460, th = 62;
            timerBar.setBounds((w - tw) / 2, 8, tw, th);
        }

        int gw = (int)(w * 0.70);
        int gh = (int)(h * 0.25);
        int btnCount = (choiceLayer.getComponentCount() + 1) / 2;
        int dialogueY = (isChoiceMode && btnCount > 0)
            ? h - gh - (btnCount * 65) - 100
            : h - gh - 100;

        dialogueBox.moveTo(gw, gh, dialogueY);
        choiceLayer.setBounds((w - gw) / 2, dialogueY + gh + 20, gw, btnCount > 0 ? btnCount * 65 : 100);

      if (activeMiniGamePanel != null) {
            int gameW = 800; 
            int gameH = 600;
            activeMiniGamePanel.setBounds((w - gameW) / 2, (h - gameH) / 2, gameW, gameH);
        }

        revalidate(); repaint();
    }

    // ================================================================
    // Scene Logic
    // ================================================================

    public void updateScene(Object[] lineData) {
        if (lineData == null || lineData.length < 2) return;

        choiceLayer.removeAll();
        choiceLayer.setVisible(false);
        isChoiceMode   = false;
        pendingChoices = null;

        // Speaker
        currentSpeaker = "";
        Object sp = lineData[0];
        if (sp != null) {
            String s = sp.toString().trim();
            if (s.equalsIgnoreCase("PLAYER") || s.equals(GameConstants.PLAYER_NAME))
                currentSpeaker = GameConstants.PLAYER_NAME;
            else if (!s.isEmpty() && !s.equalsIgnoreCase("none"))
                currentSpeaker = s;
        }
        dialogueBox.setText(currentSpeaker, "");
        fullText = lineData[1].toString().replace("[PLAYER]", GameConstants.PLAYER_NAME);

        // Character
        if (lineData.length >= 3) {
            String fn = (String) lineData[2];
            currentChar = (fn == null || fn.isEmpty() || fn.equals("none")) ? ""
                : (fn.startsWith("model/") ? fn : GameConstants.CHAR_PATH + fn);
            characterLayer.updateCharacter(currentChar);
        }

        // Background
        if (lineData.length >= 4) {
            String bg = (String) lineData[3];
            currentBG = (bg == null || bg.isEmpty() || bg.equals("none")) ? ""
                : (bg.startsWith("model/") ? bg : GameConstants.SCENE_PATH + bg);
        }

        // Effects / Sound / Choices
        boolean hasEffect = false;
        for (int i = 4; i < lineData.length; i++) {
            if (lineData[i] instanceof Object[][]) {
                pendingChoices = (Object[][]) lineData[i];
                isChoiceMode   = true;
            } else if (lineData[i] instanceof String) {
                String val = (String) lineData[i];
                if (val == null || val.isEmpty() || val.equalsIgnoreCase("none")) continue;
                String upper = val.toUpperCase();
                if (upper.contains("FADE") || upper.contains("WHITE") || upper.contains("BLACK")
                        || upper.equals("SHAKE") || upper.equals("FLASH")) {
                    if (effectManager != null) {
                        effectManager.stopAll();
                        effectManager.play(val);
                        hasEffect = true;
                    }
                } else {
                    String path = GameConstants.SOUND_PATH + val + ".wav";
                    if (upper.startsWith("BGM")) soundManager.playBGM(path);
                    else soundManager.playSE(path);
                }
            }
        }

        // FIX #2: ใน MP mode แสดง dialogueBox เสมอ ไม่ว่าจะมี effect
        if (multiplayerEffectBypass) {
            dialogueBox.setVisible(true);
        } else {
            dialogueBox.setVisible(!hasEffect);
        }

        updateUIStyles();
        revalidate(); repaint();
        startTypewriter();
    }

    private void showChoices(Object[][] choices) {
        if (choices == null) return;
        isChoiceMode = true;
        choiceLayer.removeAll();
        int tw = (int)(getWidth() * 0.70);

        setupChoiceKeyBindings(choices);

        for (int i = 0; i < choices.length; i++) {
            String num         = String.valueOf(i + 1);
            String text        = (String) choices[i][0];
            String targetScene = (String) choices[i][1];
            final String cn    = (choices[i].length >= 4) ? (String)  choices[i][2] : null;
            final int    cs    = (choices[i].length >= 4) ? (Integer) choices[i][3] : 0;

            UI_Components.ChoiceButton btn = new UI_Components.ChoiceButton(num, text, () -> {
                if (multiplayerMode && timerBar != null && timerBar.isChoiceLocked()) return;
                doChoice(targetScene, cn, cs);
            });
            btn.setMaximumSize(new Dimension(tw, 50));
            btn.setPreferredSize(new Dimension(tw, 50));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            choiceLayer.add(btn);
            if (i < choices.length - 1) choiceLayer.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        updateUIStyles();
        choiceLayer.setVisible(true);

        // ===== TFT Timer =====
        if (multiplayerMode && timerBar != null) {
            // บอกผู้เล่นอื่นว่าเราถึง choice แล้ว
            if (mpClient != null) mpClient.notifyChoiceReady();

            // ล็อคปุ่มก่อน
            for (Component c : choiceLayer.getComponents()) c.setEnabled(false);

            // ตั้งชื่อรอบ
            String roundName = "ซีน " + sceneName.replace("SCENE_", "").replace("MP", "MP");
            timerBar.setRoundLabel(roundName);

            // callback ปลดล็อค
            timerBar.setOnUnlock(() -> SwingUtilities.invokeLater(() -> {
                for (Component c : choiceLayer.getComponents()) c.setEnabled(true);
                repaint();
            }));

            // คำนวณจำนวนผู้เล่นจริง
            int totalP = (mpServer != null) ? mpServer.getPlayerCount() + 1 : mpTotalPlayers;
            if (totalP < 1) totalP = 1;

            // FIX #1: เริ่มนับ timer ทันที
            timerBar.startTimer(mpReadSeconds, totalP);

            // ถ้าเป็น Host นับตัวเองเป็น ready 1 คน
            if (mpServer != null) timerBar.playerReachedChoice();
        }
    }

    private void doChoice(String targetScene, String charName, int score) {
        isChoiceMode   = false;
        pendingChoices = null;
        choiceLayer.removeAll();
        choiceLayer.setVisible(false);
        clearChoiceKeyBindings();
        if (timerBar != null) timerBar.stopTimer();

        if (charName != null && score != 0) {
            model.Relation.getInstance().addAffection(charName, score);
            UI_Components.RelationUI.getInstance().updateScore(charName);
        }

        Object[][] nextScene = storyMap.get(targetScene);
        if (nextScene != null) {
            loadNewScene(nextScene, targetScene);
        } else {
            handleSceneTransition();
        }
    }

    // ================================================================
    // FIX #3: handleInteraction — แก้ bug storyIndex++ ซ้ำ
    // ================================================================
    private void handleInteraction() {
        // FIX #3: ใน MP mode ข้าม isPlaying() check
        if (!multiplayerEffectBypass && effectManager != null && effectManager.isPlaying()) return;

        // ถ้า typewriter กำลังพิมพ์ → เร่งให้พิมพ์เสร็จก่อน
        if (typeTimer != null && typeTimer.isRunning()) {
            typeTimer.stop();
            charIndex = fullText.length();
            dialogueBox.setText(currentSpeaker, fullText);
            return;
        }

        // ถ้าอยู่ใน choice mode และยังไม่แสดงปุ่ม → แสดงปุ่ม
        int btnCount = (choiceLayer.getComponentCount() + 1) / 2;
        if (isChoiceMode && btnCount == 0 && pendingChoices != null) {
            showChoices(pendingChoices);
            return;
        }

        // ถ้าปุ่มแสดงอยู่แล้ว → รอผู้เล่นกด ไม่ทำอะไร
        if (isChoiceMode && btnCount > 0) return;

        // ไปบรรทัดถัดไป (FIX #3: storyIndex++ ที่เดียว ไม่ซ้ำ)
        storyIndex++;
       if (storyIndex < currentSceneData.length) {
            updateScene(currentSceneData[storyIndex]);
        } else {
            // ✨ จุดที่ต้องแก้: เช็คชื่อฉากปัจจุบันเพื่อเลือกเปิดมินิเกมที่ถูกต้อง
        String checkName = sceneName.trim().toUpperCase(); 
        
        switch (checkName) {
            case "SCENE_1":
                startMiniGame("MATCH"); 
                break;
            case "SCENE_2":
                startMiniGame("HEART"); 
                break;
            case "SCENE_3":
                startMiniGame("RPS");  
                break;
            default:
                handleSceneTransition();
                break;
        }
    }
    }

    private void handleSceneTransition() {
        if (timerBar != null) timerBar.stopTimer();

        if (sceneName.equals("SCENE_MP")) {
            if (onGameFinished != null) onGameFinished.run();
            return;
        }

        try {
            int num  = Integer.parseInt(sceneName.replace("SCENE_", ""));
            String next = "SCENE_" + (num + 1);
            if (storyMap.containsKey(next)) {
                loadNewScene(storyMap.get(next), next);
            } else {
                if (onGameFinished != null) onGameFinished.run();
            }
        } catch (Exception e) {
            if (onGameFinished != null) onGameFinished.run();
        }
    }

    public void loadNewScene(Object[][] nextData, String newName) {
        if (nextData == null) return;
        // FIX: เคลียร์ effect เก่าทุกครั้งที่โหลดซีนใหม่
        if (effectManager != null) effectManager.stopAll();
        currentSceneData = nextData;
        sceneName        = newName;
        storyIndex       = 0;
        updateScene(currentSceneData[storyIndex]);
    }

    // ================================================================
    // Typewriter
    // ================================================================

    private void startTypewriter() {
        charIndex = 0;
        if (typeTimer != null) typeTimer.stop();

        Timer wait = new Timer(16, null);
        wait.addActionListener(e -> {
            // FIX: ใน MP mode ไม่รอ effect เลย — เริ่มพิมพ์ทันที
            if (!multiplayerEffectBypass) {
                if (effectManager != null &&
                    (effectManager.isPlaying() || effectManager.getAlpha() > 0.3f)) return;
            }
            ((Timer) e.getSource()).stop();
            runTypewriter();
        });
        wait.start();
    }

    private void runTypewriter() {
        dialogueBox.setVisible(true);
        if (typeTimer != null) typeTimer.stop();
        typeTimer = new Timer(GameConstants.TYPEWRITER_SPEED, e -> {
            if (charIndex < fullText.length()) {
                charIndex++;
                dialogueBox.setText(currentSpeaker, fullText.substring(0, charIndex));
            } else {
                ((Timer) e.getSource()).stop();
            }
        });
        typeTimer.start();
    }

    // ================================================================
    // MiniGame
    // ================================================================

public void startMiniGame(String type) {
        if (activeMiniGamePanel != null) return; 

        dialogueBox.setVisible(false); // ซ่อนกล่องคำพูด

        // 🏆 เมื่อชนะ: ไปฉากต่อไป
        Runnable winAction = () -> {
            remove(activeMiniGamePanel);
            activeMiniGamePanel = null;
            dialogueBox.setVisible(true);
            handleSceneTransition(); 
            repaint(); revalidate();
        };

        // ❌ เมื่อแพ้: เริ่มฉากเดิมใหม่
        Runnable failAction = () -> {
            remove(activeMiniGamePanel);
            activeMiniGamePanel = null;
            loadNewScene(currentSceneData, sceneName); 
            repaint(); revalidate();
        };

        // เลือกว่าจะดึงไฟล์มินิเกมตัวไหนจาก UI_Components มาใช้
        switch (type) {
            case "MATCH": activeMiniGamePanel = new UI_Components.MemoryMatchMiniGame(winAction, failAction); break;
            case "HEART": activeMiniGamePanel = new UI_Components.HeartClickMiniGame(winAction, failAction); break;
            case "RPS":   activeMiniGamePanel = new UI_Components.RPSMiniGame(winAction, failAction); break;
        }

        if (activeMiniGamePanel != null) {
            int gW = 800, gH = 600;
            activeMiniGamePanel.setBounds((getWidth() - gW) / 2, (getHeight() - gH) / 2, gW, gH);
            add(activeMiniGamePanel);
            setComponentZOrder(activeMiniGamePanel, 0); // ดันมาอยู่หน้าสุด
            repaint(); revalidate();
        }
    }
}