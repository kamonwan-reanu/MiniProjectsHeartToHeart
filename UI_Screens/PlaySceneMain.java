package UI_Screens;

import UI_Components.CharacterSprite;
import UI_Components.ChoiceButton; 
import UI_Components.DialogueBox;
import UI_Components.EffectManager;
import UI_Components.PauseMenuUI;
import UI_Components.RelationUI;
import UI_Components.HeartHUDPanel;
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

    // UI Components
    private JLabel captionLabel;
    private JLabel bgLayer;
    private CharacterSprite characterLayer;
    private JPanel effectLayer;
    private JPanel choiceLayer;
    private DialogueBox dialogueBox;
    private Timer typeTimer;
    private HeartHUDPanel heartHUD;
    private ChatScenePanel inlineChatPanel;

    private SoundManager  soundManager  = new SoundManager();
    private EffectManager effectManager;

    // KeyBindings
    private InputMap  inputMap;
    private ActionMap actionMap;

    // State Flags
    private boolean isCaptionMode = false;

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
                
                SwingUtilities.invokeLater(() -> {
                    requestFocusInWindow();
                });
            }
        });

        dialogueBox.setOnNextRequested(this::handleInteraction);
        setupKeyBindings();
        setupEscapeKeyListener();
        
        setFocusable(true);

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleInteraction();
            }
        });
    }

    private void setupEscapeKeyListener() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    handleEscape();
                    e.consume();
                }
            }
        });
    }

    private void handleEscape() {
        PauseMenuUI pause = PauseMenuUI.getInstance();
        if (pause.isMenuVisible()) {
            pause.hideMenu();
            return;
        }
        if (effectManager != null && effectManager.isPlaying()) return;
        pause.showMenu(PlaySceneMain.this, !multiplayerMode);
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
            if (heartHUD != null) heartHUD.setVisible(false); // ซ่อนหลอดหัวใจเพื่อนตอนเล่นหลายคน
            refreshZOrder();
        } else {
            if (timerBar != null) timerBar.resetAndHide();
            if (heartHUD != null) heartHUD.setVisible(true); // โชว์ตอนเล่นคนเดียว
        }

        if (effectManager != null) effectManager.setMultiplayerBypass(enabled);
    }

    public void setMultiplayerEffectBypass(boolean bypass) {
        this.multiplayerEffectBypass = bypass;
        if (effectManager != null) effectManager.setMultiplayerBypass(bypass);
    }

    public void setOnGameFinished(Runnable callback) { this.onGameFinished = callback; }

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
        inputMap.put(KeyStroke.getKeyStroke(KeyConfig.getNextMsgAlt(), 0), "next"); // ✅ F key
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
            @Override public void actionPerformed(ActionEvent e) {
                handleEscape();
            }
        });
    }

    private void setupChoiceKeyBindings(Object[][] choices) {
        if (choices == null) return;
        clearChoiceKeyBindings();
        int[] keys = { KeyConfig.getChoice1(), KeyConfig.getChoice2(), KeyConfig.getChoice3() };
        for (int i = 0; i < Math.min(choices.length, 3); i++) {
            final String rawTarget = (String) choices[i][1];
            final String cn = (choices[i].length >= 4) ? (String)  choices[i][2] : null;
            final int    cs = (choices[i].length >= 4) ? (Integer) choices[i][3] : 0;
            
            inputMap.put(KeyStroke.getKeyStroke(keys[i], 0), "ch" + i);
            actionMap.put("ch" + i, new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    handleChoiceClick(rawTarget, cn, cs);
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

    private void handleChoiceClick(String rawTarget, String cn, int cs) {
        if (!isChoiceMode || pendingChoices == null) return;
        if (multiplayerMode && timerBar != null && timerBar.isChoiceLocked()) return;

        String targetToLoad = rawTarget;
        if (!multiplayerMode) {
            targetToLoad = resolvePickToken(rawTarget);
            if (cn != null && cs != 0) {
                model.Relation.getInstance().addAffection(cn, cs);
                UI_Components.RelationUI.getInstance().updateScore(cn);
            }
        } else {
            if (cn != null && cs != 0) {
                model.Relation.getInstance().addAffection(cn, cs);
                UI_Components.RelationUI.getInstance().updateScore(cn);
            }
        }
        doChoice(targetToLoad, cn, cs);
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
        storyMap.put("SCENE_6", StoryData.SCENE_6);   
        storyMap.put("SCENE_7", StoryData.SCENE_7);   
        storyMap.put("SCENE_8", StoryData.SCENE_8);   
        storyMap.put("SCENE_9", StoryData.SCENE_9);   
        storyMap.put("SCENE_10", StoryData.SCENE_10); 
        storyMap.put("SCENE_11", StoryData.SCENE_11);
        storyMap.put("SCENE_12", StoryData.SCENE_12);
        storyMap.put("SCENE_13", StoryData.SCENE_13);
        storyMap.put("SCENE_13_CHAT", StoryData.SCENE_13_CHAT);
        storyMap.put("SCENE_14", StoryData.SCENE_14);
        storyMap.put("SCENE_15", StoryData.SCENE_15);
        storyMap.put("SCENE_16", StoryData.SCENE_16);
        storyMap.put("SCENE_17", StoryData.SCENE_17);
        storyMap.put("SCENE_18", StoryData.SCENE_18);
        storyMap.put("SCENE_19", StoryData.SCENE_19);
        storyMap.put("SCENE_20", StoryData.SCENE_20);
        storyMap.put("SCENE_21", StoryData.SCENE_21);
        storyMap.put("SCENE_22", StoryData.SCENE_22);
        storyMap.put("SCENE_TEER", StoryData.SCENE_TEER_END);
        storyMap.put("SCENE_KIRIN", StoryData.SCENE_KIRIN_END);
        storyMap.put("SCENE_TIAN", StoryData.SCENE_TIAN_END);
        storyMap.put("SCENE_TRUE_END", StoryData.SCENE_TRUE_END);
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
            @Override protected void paintComponent(Graphics g) { 
                renderCharacter((Graphics2D) g); 
            } 
        };

        effectLayer = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                if (effectManager != null) effectManager.drawEffects(g2d, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        effectLayer.setOpaque(false);

        choiceLayer = new JPanel();
        choiceLayer.setLayout(new BoxLayout(choiceLayer, BoxLayout.Y_AXIS));
        choiceLayer.setOpaque(false);

        heartHUD = new HeartHUDPanel();
        heartHUD.setOpaque(false);

        captionLabel = new JLabel("", SwingConstants.CENTER);
        captionLabel.setForeground(Color.WHITE);
        captionLabel.setFont(new Font("Tahoma", Font.PLAIN, 32));
        captionLabel.setOpaque(false);
        captionLabel.setVisible(false);
        
        add(captionLabel);
        add(bgLayer);
        add(characterLayer);
        add(effectLayer);
        add(dialogueBox);
        add(choiceLayer);
        add(heartHUD);

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
<<<<<<< HEAD
           if (activeMiniGamePanel != null) {
                setComponentZOrder(activeMiniGamePanel, z++);
            }
            
            if (timerBar != null && timerBar.isVisible()) {
                setComponentZOrder(timerBar, z++);
            }
            
            setComponentZOrder(effectLayer,    z++);
            setComponentZOrder(choiceLayer,    z++);
            setComponentZOrder(dialogueBox,    z++);
=======
            if (currentMiniGame != null) setComponentZOrder(currentMiniGame, z++);
            if (inlineChatPanel != null) setComponentZOrder(inlineChatPanel, z++);
            if (heartHUD != null && !multiplayerMode) setComponentZOrder(heartHUD, z++);
            if (captionLabel != null) setComponentZOrder(captionLabel, z++);
            if (timerBar != null && timerBar.isVisible()) setComponentZOrder(timerBar, z++);
            setComponentZOrder(choiceLayer, z++);
            setComponentZOrder(dialogueBox, z++);
            setComponentZOrder(effectLayer, z++);
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
            setComponentZOrder(characterLayer, z++);
            setComponentZOrder(bgLayer, z);
        } catch (Exception ignored) {}
    }

    private void updateUIStyles() {
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

        bgLayer.setBounds(0, 0, w, h);
        characterLayer.setBounds(0, 0, w, h);
        effectLayer.setBounds(0, 0, w, h);
        if (captionLabel != null) captionLabel.setBounds(0, 0, w, h);

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

        if (heartHUD != null) heartHUD.setBounds(30, 30, 320, 150);

        revalidate(); repaint();
    }

    // ================================================================
    // Scene Logic
    // ================================================================

    public void updateScene(Object[] lineData) {
        if (lineData == null || lineData.length < 2) return;

        choiceLayer.removeAll();
        choiceLayer.setVisible(false);
        isChoiceMode = false;
        pendingChoices = null;
        currentSpeaker = "";

        boolean isCaption = false;
        for (Object o : lineData) {
            if ("CAPTION".equals(o)) { isCaption = true; break; }
        }
        isCaptionMode = isCaption;

        Object sp = lineData[0];
        if (sp != null) {
            String s = sp.toString().trim();
            if (s.equalsIgnoreCase("PLAYER") || s.equals(GameConstants.PLAYER_NAME))
                currentSpeaker = GameConstants.PLAYER_NAME;
            else if (!s.isEmpty() && !s.equalsIgnoreCase("none"))
                currentSpeaker = s;
        }
        fullText = lineData[1].toString().replace("[PLAYER]", GameConstants.PLAYER_NAME);

        if (lineData.length >= 3) {
            String fn = (String) lineData[2];
            currentChar = (fn == null || fn.isEmpty() || fn.equals("none")) ? ""
                : (fn.startsWith("model/") ? fn : GameConstants.CHAR_PATH + fn);
            characterLayer.updateCharacter(currentChar);
        }

        if (lineData.length >= 4) {
            String bg = (String) lineData[3];
            currentBG = (bg == null || bg.isEmpty() || bg.equals("none")) ? ""
                : (bg.startsWith("model/") ? bg : GameConstants.SCENE_PATH + bg);
        }

        boolean hasNewEffect = false;
        for (int i = 4; i < lineData.length; i++) {
            if (lineData[i] instanceof Object[][]) {
                pendingChoices = (Object[][]) lineData[i];
                isChoiceMode   = true;
            } else if (lineData[i] instanceof String) {
                String val = (String) lineData[i];
                if (val == null || val.isEmpty() || val.equalsIgnoreCase("none") || "CAPTION".equals(val)) continue;
                String upper = val.toUpperCase();
                if (upper.contains("FADE") || upper.contains("WHITE") || upper.contains("BLACK")
                        || upper.equals("SHAKE") || upper.equals("FLASH")) {
                    if (effectManager != null) {
                        effectManager.stopAll();
                        effectManager.play(val);
                        hasNewEffect = true;
                    }
                } else {
                    String path = GameConstants.SOUND_PATH + val + ".wav";
                    if (upper.startsWith("BGM")) soundManager.playBGM(path);
                    else soundManager.playSE(path);
                }
            }
        }

        if (isCaptionMode) {
            dialogueBox.setVisible(false);
            if (captionLabel != null) {
                captionLabel.setText("");
                captionLabel.setVisible(true);
            }
        } else {
            if (captionLabel != null) captionLabel.setVisible(false);
            dialogueBox.setText(currentSpeaker, "");
            
            if (multiplayerEffectBypass) {
                dialogueBox.setVisible(true);
            } else {
                dialogueBox.setVisible(!hasNewEffect);
            }
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
            final String rawTarget = (String) choices[i][1];
            final String cn    = (choices[i].length >= 4) ? (String)  choices[i][2] : null;
            final int    cs    = (choices[i].length >= 4) ? (Integer) choices[i][3] : 0;

            UI_Components.ChoiceButton btn = new UI_Components.ChoiceButton(num, text, () -> {
                handleChoiceClick(rawTarget, cn, cs);
            });
            btn.setMaximumSize(new Dimension(tw, 50));
            btn.setPreferredSize(new Dimension(tw, 50));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            choiceLayer.add(btn);
            if (i < choices.length - 1) choiceLayer.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        updateUIStyles();
        choiceLayer.setVisible(true);

        if (multiplayerMode && timerBar != null) {
            if (mpClient != null) mpClient.notifyChoiceReady();
            for (Component c : choiceLayer.getComponents()) c.setEnabled(false);
            String roundName = "ซีน " + sceneName.replace("SCENE_", "").replace("MP", "MP");
            timerBar.setRoundLabel(roundName);
            timerBar.setOnUnlock(() -> SwingUtilities.invokeLater(() -> {
                for (Component c : choiceLayer.getComponents()) c.setEnabled(true);
                repaint();
            }));
            int totalP = (mpServer != null) ? mpServer.getPlayerCount() + 1 : mpTotalPlayers;
            if (totalP < 1) totalP = 1;
            timerBar.startTimer(mpReadSeconds, totalP);
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

        if (!multiplayerMode) {
            Object[][] nextScene = storyMap.get(targetScene);
            if (nextScene != null) {
                loadNewScene(nextScene, targetScene);
            } else {
                this.sceneName = targetScene; 
                handleSceneTransition();
            }
        } else {
            Object[][] nextScene = storyMap.get(targetScene);
            if (nextScene != null) loadNewScene(nextScene, targetScene);
            else {
                this.sceneName = targetScene;
                handleSceneTransition();
            }
        }
    }

    private void handleInteraction() {
        if (PauseMenuUI.getInstance().isMenuVisible()) return;
        if (!multiplayerEffectBypass && effectManager != null && effectManager.isPlaying()) return;

        if (typeTimer != null && typeTimer.isRunning()) {
            typeTimer.stop();
            charIndex = fullText.length();
            if (isCaptionMode) {
                if (captionLabel != null) captionLabel.setText(fullText);
            } else {
                dialogueBox.setText(currentSpeaker, fullText);
            }
            return;
        }

        int btnCount = (choiceLayer.getComponentCount() + 1) / 2;
        if (isChoiceMode && btnCount == 0 && pendingChoices != null) {
            showChoices(pendingChoices);
            return;
        }

        if (isChoiceMode && btnCount > 0) return;

        if (isCaptionMode && captionLabel != null) {
            captionLabel.setVisible(false);
            isCaptionMode = false;
        }

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

        // SCENE_13 จบ → เปิด inline chat (choice อยู่ใน SCENE_13_CHAT)
        if (sceneName.equals("SCENE_13") && !multiplayerMode) {
            showInlineChat(StoryData.SCENE_13_CHAT, model.GameConstants.SCENE_PATH + "dating_chat.png");
            return;
        }

        try {
            int num  = Integer.parseInt(sceneName.replace("SCENE_", ""));
            String nextSceneKey = "SCENE_" + (num + 1);

            if (!multiplayerMode && core.Main.CHAT_SCENES.contains(nextSceneKey)) {
                String chatKey = "CHAT_" + (num + 1);
                ChatScenePanel panel = core.Main.chatPanels.get(chatKey);
                if (panel != null) panel.resetAndStart();
                core.Main.cardLayout.show(core.Main.mainContainer, chatKey);
                return;
            }

            if (storyMap.containsKey(nextSceneKey)) {
                loadNewScene(storyMap.get(nextSceneKey), nextSceneKey);
            } else {
                if (onGameFinished != null) onGameFinished.run();
            }
        } catch (Exception e) {
            if (onGameFinished != null) onGameFinished.run();
        }
    }

    public void loadNewScene(Object[][] nextData, String newName) {
        if (nextData == null) return;
        
        boolean isChoiceOnly = (nextData.length == 1);
        if (!isChoiceOnly && !multiplayerMode && core.Main.CHAT_SCENES.contains(newName)) {
            String chatKey = "CHAT_" + newName.replace("SCENE_", "");
            ChatScenePanel panel = core.Main.chatPanels.get(chatKey);
            if (panel != null) panel.resetAndStart();
            core.Main.cardLayout.show(core.Main.mainContainer, chatKey);
            return;
        }

        if (effectManager != null) effectManager.stopAll();
        this.currentSceneData = nextData;
        this.sceneName        = newName;
        this.storyIndex       = 0;
        updateScene(currentSceneData[storyIndex]);
    }

    private void startTypewriter() {
        charIndex = 0;
        if (typeTimer != null) typeTimer.stop();

        if (isCaptionMode) {
            runTypewriter();
            return;
        }

        Timer wait = new Timer(16, null);
        wait.addActionListener(e -> {
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
        if (!isCaptionMode) dialogueBox.setVisible(true);

        if (typeTimer != null) typeTimer.stop();
        typeTimer = new Timer(GameConstants.TYPEWRITER_SPEED, e -> {
            if (charIndex < fullText.length()) {
                charIndex++;
                if (isCaptionMode && captionLabel != null) {
                    captionLabel.setText(fullText.substring(0, charIndex));
                } else {
                    dialogueBox.setText(currentSpeaker, fullText.substring(0, charIndex));
                }
            } else {
                ((Timer) e.getSource()).stop();
            }
        });
        typeTimer.start();
    }

    // ===============================
    // PICK TOKEN RESOLVER (Single Player)
    // ===============================
    private String resolvePickToken(String token) {
        if (token == null) return null;
        token = token.trim();

        if (!token.startsWith("PICK")) return token;

        if (token.startsWith("PICK13_")) {
            if (!model.GameState.picked13) {
                addHeartByToken(token);
                model.GameState.picked13 = true;
                if (heartHUD != null) heartHUD.repaint();
            }
            if (token.endsWith("_TEER")) return "SCENE_14";
            if (token.endsWith("_TIAN")) return "SCENE_15";
            if (token.endsWith("_KIRIN")) return "SCENE_16";
        }

        if (token.startsWith("PICK17_")) {
            if (!model.GameState.picked17) {
                addHeartByToken(token);
                model.GameState.picked17 = true;
                if (heartHUD != null) heartHUD.repaint();
            }
            String end = decideEndingIfAny();
            if (end != null) return end;
            return "SCENE_21";
        }

        if (token.startsWith("PICK22_")) {
            if (!model.GameState.picked22) {
                addHeartByToken(token);
                model.GameState.picked22 = true;
                if (heartHUD != null) heartHUD.repaint();
            }
            return decideEndingFinal();
        }

        return token;
    }

    private void addHeartByToken(String token) {
        if (token.endsWith("_TEER")) model.GameState.teerHeart++;
        else if (token.endsWith("_TIAN")) model.GameState.tianHeart++;
        else if (token.endsWith("_KIRIN")) model.GameState.kirinHeart++;
    }

    private String decideEndingIfAny() {
        if (model.GameState.teerHeart >= 2) return "SCENE_TEER";
        if (model.GameState.tianHeart >= 2) return "SCENE_TIAN";
        if (model.GameState.kirinHeart >= 2) return "SCENE_KIRIN";
        return null;
    }

    private String decideEndingFinal() {
        String lockEnd = decideEndingIfAny();
        if (lockEnd != null) return lockEnd;
        if (model.GameState.teerHeart == 1 && model.GameState.tianHeart == 1 && model.GameState.kirinHeart == 1) {
            return "SCENE_TRUE_END";
        }
        return "SCENE_TRUE_END";
    }

    // ================================================================
    // Chat System
    // ================================================================

    public void showInlineChat(Object[][] chatData, String bgPath) {
        if (inlineChatPanel != null) { remove(inlineChatPanel); inlineChatPanel = null; }

        inlineChatPanel = new ChatScenePanel(chatData, bgPath, () -> {
            Object[][] choiceOnly = new Object[][] {
                chatData[chatData.length - 1]
            };
            if (inlineChatPanel != null) { remove(inlineChatPanel); inlineChatPanel = null; }
            revalidate(); repaint();
            loadNewScene(choiceOnly, "SCENE_13_CHAT");
        }, false);

        int w = getWidth(), h = getHeight();
        inlineChatPanel.setBounds(0, 0, w, h);
        add(inlineChatPanel);
        refreshZOrder();
        revalidate(); repaint();
        inlineChatPanel.resetAndStart();
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
    public void startHeartMiniGame() {
        if (currentMiniGame != null) { remove(currentMiniGame); currentMiniGame = null; }
        currentMiniGame = new UI_Components.MemoryMiniGame(() -> {
            remove(currentMiniGame);
            currentMiniGame = null;
            refreshZOrder();
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