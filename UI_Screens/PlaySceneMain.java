package UI_Screens;

import UI_Components.CharacterSprite;
import UI_Components.DialogueBox;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import UI_Components.DialogueBox;
import UI_Components.CharacterSprite;
import UI_Components.EffectManager;
import UI_Components.RelationUI;
import UI_Components.MultiplayerTimerBar;
import model.GameConstants;
import model.GameServer;
import model.GameClient;
import model.SoundManager;
import model.StoryData;
import model.KeyConfig;
import javax.swing.*;
import model.GameConstants;
import model.SoundManager; 
import model.StoryData; 

public class PlaySceneMain extends JPanel {
    private Map<String, Object[][]> storyMap = new HashMap<>();
    private Object[][] currentSceneData;
    private String sceneName = "";
    private int storyIndex = 0;
    private Runnable onGameFinished;

    private String currentBG = "";
    private String currentChar = "";
    private String currentSpeaker = "";
    private String fullText = "";
    private int charIndex = 0;
    private boolean isChoiceMode = false;
    private Object[][] pendingChoices = null;

    private double bgScaleOffset = 0.0;
    private int bgOffsetX = 0;
    private int bgOffsetY = 0;

    private JLabel bgLayer;
    private CharacterSprite characterLayer;
    private JPanel effectLayer;
    private JPanel choiceLayer;
    private DialogueBox dialogueBox;
    private Timer typeTimer;

    private SoundManager soundManager = new SoundManager();
    private EffectManager effectManager;

    private InputMap inputMap;
    private ActionMap actionMap;

    // ======= Multiplayer =======
    private boolean multiplayerMode       = false;
    private boolean multiplayerEffectBypass = false; // FIX #4: ข้าม effect block ใน MP
    private int     mpReadSeconds         = 30;
    private GameServer mpServer           = null;
    private GameClient mpClient           = null;
    private MultiplayerTimerBar timerBar;

    public PlaySceneMain(Object[][] sceneData, String charPath, String sceneName) {
        this.currentSceneData = sceneData;
        this.sceneName = sceneName;

        if (charPath != null && !charPath.isEmpty() && !charPath.equalsIgnoreCase("none")) {
            this.currentChar = charPath.startsWith("model/") ? charPath : GameConstants.CHAR_PATH + charPath;
        } else {
            this.currentChar = "";
        }

        setLayout(null);
        setOpaque(true);
        setBackground(Color.BLACK);

        this.dialogueBox   = new DialogueBox();
        this.effectManager = new EffectManager(this, dialogueBox);

        initStoryMap();
        setupUIComponents();

        this.addComponentListener(new ComponentAdapter() {
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

    // =========================================
    // Multiplayer
    // =========================================

    public void setMultiplayerMode(boolean enabled, int readSeconds, GameServer server, GameClient client) {
        this.multiplayerMode  = enabled;
        this.mpReadSeconds    = readSeconds;
        this.mpServer         = server;
        this.mpClient         = client;

        if (enabled) {
            if (timerBar == null) {
                timerBar = new MultiplayerTimerBar();
                add(timerBar);
            }
            timerBar.setVisible(true);
            refreshZOrder();
        } else {
            if (timerBar != null) timerBar.setVisible(false);
        }
    }

    /** FIX #4: ข้าม effectManager.isPlaying() block ใน Multiplayer mode */
    public void setMultiplayerEffectBypass(boolean bypass) {
        this.multiplayerEffectBypass = bypass;
    }

    public void setOnGameFinished(Runnable callback) {
        this.onGameFinished = callback;
    }

    // =========================================
    // KeyBindings
    // =========================================

    private void setupKeyBindings() {
        inputMap  = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        actionMap = getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyConfig.getNextMsg(), 0), "nextAction");
        actionMap.put("nextAction", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { handleInteraction(); }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyConfig.getRelationUI(), 0), "toggleRelationUI");
        actionMap.put("toggleRelationUI", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                RelationUI.getInstance().updateAllScores();
                RelationUI.getInstance().setVisible(!RelationUI.getInstance().isVisible());
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyConfig.getEscape(), 0), "escapeAction");
        actionMap.put("escapeAction", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {}
        });
    }

    private void setupChoiceKeyBindings(Object[][] choices) {
        if (choices == null) return;
        clearChoiceKeyBindings();
        int[] keys = {KeyConfig.getChoice1(), KeyConfig.getChoice2(), KeyConfig.getChoice3()};
        for (int i = 0; i < Math.min(choices.length, 3); i++) {
            final int idx = i;
            final String scene   = (String) choices[i][1];
            final String cname   = (choices[i].length >= 4) ? (String)  choices[i][2] : null;
            final int    cscore  = (choices[i].length >= 4) ? (Integer) choices[i][3] : 0;
            inputMap.put(KeyStroke.getKeyStroke(keys[i], 0), "choice" + (i+1));
            actionMap.put("choice" + (i+1), new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    handleChoiceSelection(idx, scene, cname, cscore);
                }
            });
        }
    }

    private void clearChoiceKeyBindings() {
        int[] keys = {KeyConfig.getChoice1(), KeyConfig.getChoice2(), KeyConfig.getChoice3()};
        for (int i = 0; i < 3; i++) {
            inputMap.remove(KeyStroke.getKeyStroke(keys[i], 0));
            actionMap.remove("choice" + (i+1));
        }
    }

    private void handleChoiceSelection(int idx, String targetScene, String charName, int score) {
        if (!isChoiceMode || pendingChoices == null) return;
        if (multiplayerMode && timerBar != null && timerBar.isChoiceLocked()) return;
        doChoice(targetScene, charName, score);
    }

    // =========================================
    // Story Map
    // =========================================

    private void initStoryMap() {
        storyMap.put("SCENE_1", StoryData.SCENE_1);
        storyMap.put("SCENE_2", StoryData.SCENE_2);
        storyMap.put("SCENE_3", StoryData.SCENE_3);
        storyMap.put("SCENE_4", StoryData.SCENE_4);
        storyMap.put("SCENE_5", StoryData.SCENE_5);
        if (StoryData.SCENE_MP != null)
            storyMap.put("SCENE_MP", StoryData.SCENE_MP);
    }

    // =========================================
    // UI Setup
    // =========================================

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
        int z = 0;
        if (timerBar != null && timerBar.isVisible()) setComponentZOrder(timerBar, z++);
        setComponentZOrder(effectLayer,    z++);
        setComponentZOrder(choiceLayer,    z++);
        setComponentZOrder(dialogueBox,    z++);
        setComponentZOrder(characterLayer, z++);
        setComponentZOrder(bgLayer,        z);
    }

    private void updateUIStyles() {
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

        bgLayer.setBounds(0, 0, w, h);
        characterLayer.setBounds(0, 0, w, h);
        effectLayer.setBounds(0, 0, w, h);

        // Timer bar — กลางบน
        if (timerBar != null && timerBar.isVisible()) {
            int tw = 440, th = 58;
            timerBar.setBounds((w-tw)/2, 10, tw, th);
        }

        int gw = (int)(w * 0.70);
        int gh = (int)(h * 0.25);
        int btnCount = (choiceLayer.getComponentCount() + 1) / 2;
        int dialogueY = isChoiceMode && btnCount > 0
            ? h - gh - (btnCount*65) - 100
            : h - gh - 100;

        dialogueBox.moveTo(gw, gh, dialogueY);
        int cy = dialogueY + gh + 20;
        int ch = btnCount > 0 ? btnCount*65 : 100;
        choiceLayer.setBounds((w-gw)/2, cy, gw, ch);

        revalidate(); repaint();
    }

    // =========================================
    // Scene Logic
    // =========================================

    public void updateScene(Object[] lineData) {
        if (lineData == null || lineData.length < 2) return;

        choiceLayer.removeAll();
        choiceLayer.setVisible(false);
        isChoiceMode   = false;
        pendingChoices = null;

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

        // FIX #4: ใน MP mode ให้แสดง dialogueBox ได้เลยแม้มี effect
        dialogueBox.setVisible(multiplayerEffectBypass || !hasEffect);
        updateUIStyles();
        revalidate(); repaint();
        startTypewriter();
    }

    private void showChoices(Object[][] choices) {
        if (choices == null) return;
        isChoiceMode = true;
        choiceLayer.removeAll();
        int tw = (int)(getWidth() * 0.70);
        int fh = 50;

        setupChoiceKeyBindings(choices);

        for (int i = 0; i < choices.length; i++) {
            String num  = String.valueOf(i+1);
            String text = (String) choices[i][0];
            String targetScene = (String) choices[i][1];
            final String cn = (choices[i].length >= 4) ? (String)  choices[i][2] : null;
            final int    cs = (choices[i].length >= 4) ? (Integer) choices[i][3] : 0;

            UI_Components.ChoiceButton btn = new UI_Components.ChoiceButton(num, text, () -> {
                if (multiplayerMode && timerBar != null && timerBar.isChoiceLocked()) return;
                doChoice(targetScene, cn, cs);
            });
            btn.setMaximumSize(new Dimension(tw, fh));
            btn.setPreferredSize(new Dimension(tw, fh));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            choiceLayer.add(btn);
            if (i < choices.length-1) choiceLayer.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        updateUIStyles();
        choiceLayer.setVisible(true);

        // ===== TFT Timer =====
        if (multiplayerMode && timerBar != null) {
            if (mpClient != null) mpClient.notifyChoiceReady();

            // ล็อคปุ่มทั้งหมดก่อน
            for (Component c : choiceLayer.getComponents()) c.setEnabled(false);

            timerBar.setRoundLabel("ซีน " + sceneName.replace("SCENE_","").replace("MP",""));
            timerBar.setOnUnlock(() -> SwingUtilities.invokeLater(() -> {
                for (Component c : choiceLayer.getComponents()) c.setEnabled(true);
                repaint();
            }));
            timerBar.startTimer(mpReadSeconds, 2);
        }
    }

    /** ทำ choice ทั้งจากปุ่มและ keyboard */
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
        loadNewScene(storyMap.get(targetScene), targetScene);
    }

    private void handleInteraction() {
        // FIX #4: ใน Multiplayer ให้ข้าม effectManager.isPlaying() check
        if (!multiplayerEffectBypass && effectManager != null && effectManager.isPlaying()) return;

        if (typeTimer != null && typeTimer.isRunning()) {
            typeTimer.stop();
            charIndex = fullText.length();
            dialogueBox.setText(currentSpeaker, fullText);
            return;
        }

        int btnCount = (choiceLayer.getComponentCount() + 1) / 2;
        if (isChoiceMode && btnCount == 0 && pendingChoices != null) {
            showChoices(pendingChoices);
            return;
        }
        if (isChoiceMode && btnCount > 0) return;

        storyIndex++;
        if (storyIndex < currentSceneData.length) {
            updateScene(currentSceneData[storyIndex]);
        } else {
            handleSceneTransition();
            storyIndex++;
            if (storyIndex == 17) { 
                startHeartMiniGame(); 
            }
            if (storyIndex < currentSceneData.length) updateScene(currentSceneData[storyIndex]);
            else handleSceneTransition();
        }
    }

    private void handleSceneTransition() {
        if (timerBar != null) timerBar.stopTimer();

        // SCENE_MP จบแล้ว → end game
        if (sceneName.equals("SCENE_MP")) {
            if (onGameFinished != null) onGameFinished.run();
            return;
        }

        try {
            int num = Integer.parseInt(sceneName.replace("SCENE_", ""));
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
        currentSceneData = nextData;
        sceneName        = newName;
        storyIndex       = 0;
        updateScene(currentSceneData[storyIndex]);
    }

    private void startTypewriter() {
        charIndex = 0;
        if (typeTimer != null) typeTimer.stop();
        Timer wait = new Timer(50, null);
        wait.addActionListener(e -> {
            // FIX #4: ใน MP ไม่รอ effect
            if (!multiplayerEffectBypass && effectManager != null
                && (effectManager.isPlaying() || effectManager.getAlpha() > 0.3f)) return;
            wait.stop();
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
                typeTimer.stop();
            }
        });
        typeTimer.start();
    }

    private UI_Components.MemoryMiniGame currentMiniGame = null;

    public void startHeartMiniGame() {
        currentMiniGame = new UI_Components.MemoryMiniGame(() -> {
            // เมื่อชนะ: บวกคะแนนให้ธีร์ และลบเกมออก
            System.out.println("ชนะมินิเกมธีร์แล้ว!"); 
            this.remove(currentMiniGame);
            currentMiniGame = null;
            repaint();
            revalidate();
        });

        // ตั้งตำแหน่งให้อยู่กลางจอ
        currentMiniGame.setBounds((getWidth() - 500) / 2, (getHeight() - 400) / 2, 500, 400);
        
        this.add(currentMiniGame);
        this.setComponentZOrder(currentMiniGame, 0); // ให้ลอยอยู่หน้าสุดทับบทพูด
        repaint();
    }
}