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

/**
 * PlaySceneMP — Visual Novel Engine สำหรับ Multiplayer โดยเฉพาะ
 * 
 * ไม่ต้องแตะ PlaySceneMain เลย
 * รองรับผู้เล่น 1-3 คน
 * จีบ Ahri เป็นตัวละครเดียว
 * มี TFT-style timer ทุก choice
 */
public class PlaySceneMP extends JPanel {

    // ============================================================
    // State
    // ============================================================
    private Map<String, Object[][]> storyMap = new HashMap<>();
    private Object[][] currentScene;
    private String     sceneName   = "MP_INTRO";
    private int        sceneIndex  = 0;
    private Runnable   onGameFinished;

    private String  currentBG      = "";
    private String  currentChar    = "";
    private String  currentSpeaker = "";
    private String  fullText       = "";
    private int     charIndex      = 0;
    private boolean isChoiceMode   = false;
    private Object[][] pendingChoices = null;

    // ============================================================
    // UI Layers
    // ============================================================
    private JLabel           bgLayer;
    private CharacterSprite  characterLayer;
    private JPanel           effectLayer;
    private JPanel           choiceLayer;
    private DialogueBox      dialogueBox;
    private Timer            typeTimer;
    private MultiplayerTimerBar timerBar;

    // ============================================================
    // Systems
    // ============================================================
    private SoundManager  soundManager  = new SoundManager();
    private EffectManager effectManager;

    // ============================================================
    // Multiplayer
    // ============================================================
    private GameServer mpServer      = null;
    private GameClient mpClient      = null;
    private int        mpReadSeconds = 30;
    private int        mpTotalPlayers = 2;

    // ============================================================
    // Constructor
    // ============================================================
    public PlaySceneMP() {
        setLayout(null);
        setOpaque(true);
        setBackground(Color.BLACK);

        this.dialogueBox   = new DialogueBox();
        this.effectManager = new EffectManager(this, dialogueBox);
        // MP mode — ไม่ซ่อน dialogueBox เมื่อมี effect
        this.effectManager.setMultiplayerBypass(true);

        initStoryMap();
        setupUIComponents();

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { updateLayout(); }
            @Override public void componentShown(ComponentEvent e) {
                updateLayout();
                if (currentScene != null && sceneIndex < currentScene.length)
                    renderLine(currentScene[sceneIndex]);
            }
        });

        dialogueBox.setOnNextRequested(this::onNext);
        setupKeys();
    }

    // ============================================================
    // Public API (เรียกจาก MultiplayerLobby)
    // ============================================================

    /**
     * เริ่มเกม MP ใหม่
     * @param server  GameServer ถ้าเป็น Host, null ถ้าเป็น Client
     * @param client  GameClient ถ้าเป็น Client, null ถ้าเป็น Host
     * @param readSec เวลา timer ต่อ choice (วินาที)
     */
    public void startMPGame(GameServer server, GameClient client, int readSec) {
        this.mpServer       = server;
        this.mpClient       = client;
        this.mpReadSeconds  = (readSec > 0) ? readSec : 30;
        this.mpTotalPlayers = (server != null) ? server.getPlayerCount() + 1 : 2;

        // reset state
        Relation.getInstance().resetAll();
        RelationUI.getInstance().updateAllScores();
        effectManager.stopAll();
        effectManager.setMultiplayerBypass(true);

        // โหลดซีนแรก
        loadScene("MP_INTRO");
        updateLayout();
    }

    public void setOnGameFinished(Runnable r) { this.onGameFinished = r; }

    /** GameClient เรียกเมื่อผู้เล่นอื่น CHOICE_READY */
    public void onRemotePlayerReady() {
        if (timerBar != null) SwingUtilities.invokeLater(() -> timerBar.otherPlayerReady());
    }

    // ============================================================
    // Story Map — ใช้ StoryDataMP
    // ============================================================
    private void initStoryMap() {
        for (String name : StoryDataMP.SCENE_ORDER) {
            Object[][] data = StoryDataMP.getScene(name);
            if (data != null) storyMap.put(name, data);
        }
    }

    // ============================================================
    // UI Setup
    // ============================================================
    private void setupUIComponents() {
        bgLayer = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBG((Graphics2D) g);
            }
        };

        characterLayer = new CharacterSprite(currentChar) {
            @Override protected void paintComponent(Graphics g) { drawChar((Graphics2D) g); }
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

        timerBar = new MultiplayerTimerBar();

        add(choiceLayer);
        add(dialogueBox);
        add(effectLayer);
        add(characterLayer);
        add(bgLayer);
        add(timerBar);

        refreshZOrder();
    }

    private void drawBG(Graphics2D g2) {
        g2.translate(effectManager.getShakeX(), effectManager.getShakeY());
        if (currentBG == null || currentBG.isEmpty() || currentBG.equals("none")) return;
        try {
            Image img = new ImageIcon(currentBG).getImage();
            if (img == null || img.getWidth(null) <= 0) return;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int pw = getWidth(), ph = getHeight();
            double s = Math.max((double)pw/img.getWidth(null), (double)ph/img.getHeight(null)) + 0.15;
            int dw = (int)(img.getWidth(null)*s), dh = (int)(img.getHeight(null)*s);
            g2.drawImage(img, (pw-dw)/2, (ph-dh)/2, dw, dh, this);
        } catch (Exception ignored) {}
    }

    private void drawChar(Graphics2D g2) {
        g2.translate(effectManager.getShakeX(), effectManager.getShakeY());
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

    private void refreshZOrder() {
        try {
            int z = 0;
            if (timerBar.isVisible()) setComponentZOrder(timerBar, z++);
            setComponentZOrder(effectLayer,    z++);
            setComponentZOrder(choiceLayer,    z++);
            setComponentZOrder(dialogueBox,    z++);
            setComponentZOrder(characterLayer, z++);
            setComponentZOrder(bgLayer,        z);
        } catch (Exception ignored) {}
    }

    private void updateLayout() {
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

        bgLayer.setBounds(0, 0, w, h);
        characterLayer.setBounds(0, 0, w, h);
        effectLayer.setBounds(0, 0, w, h);

        // timer bar — กลางบน
        if (timerBar.isVisible()) {
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
        choiceLayer.setBounds((w - gw) / 2, dialogueY + gh + 20, gw, Math.max(btnCount * 65, 50));

        revalidate(); repaint();
    }

    // ============================================================
    // Scene Logic
    // ============================================================
    private void loadScene(String name) {
        Object[][] data = storyMap.get(name);
        if (data == null) {
            // ไม่พบ scene → จบเกม
            finishGame();
            return;
        }
        if (effectManager != null) effectManager.stopAll();
        currentScene = data;
        sceneName    = name;
        sceneIndex   = 0;
        renderLine(currentScene[sceneIndex]);
    }

    private void renderLine(Object[] line) {
        if (line == null || line.length < 2) return;

        choiceLayer.removeAll();
        choiceLayer.setVisible(false);
        isChoiceMode   = false;
        pendingChoices = null;

        // ชื่อผู้พูด
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

        // รูปตัวละคร
        if (line.length >= 3) {
            String fn = (String) line[2];
            currentChar = (fn == null || fn.isEmpty() || fn.equals("none")) ? ""
                : (fn.startsWith("model/") ? fn : GameConstants.CHAR_PATH + fn);
            characterLayer.updateCharacter(currentChar);
        }

        // ฉากหลัง
        if (line.length >= 4) {
            String bg = (String) line[3];
            currentBG = (bg == null || bg.isEmpty() || bg.equals("none")) ? ""
                : (bg.startsWith("model/") ? bg : GameConstants.SCENE_PATH + bg);
        }

        // slot 4+ : effect / sound / choices
        for (int i = 4; i < line.length; i++) {
            if (line[i] instanceof Object[][]) {
                pendingChoices = (Object[][]) line[i];
                isChoiceMode   = true;
            } else if (line[i] instanceof String) {
                String val = (String) line[i];
                if (val == null || val.isEmpty() || val.equalsIgnoreCase("none")) continue;
                String up = val.toUpperCase();
                if (up.contains("FADE") || up.contains("WHITE") || up.contains("BLACK")
                        || up.equals("SHAKE") || up.equals("FLASH")) {
                    effectManager.stopAll();
                    effectManager.play(val);
                } else {
                    String path = GameConstants.SOUND_PATH + val + ".wav";
                    if (up.startsWith("BGM")) soundManager.playBGM(path);
                    else soundManager.playSE(path);
                }
            }
        }

        // MP bypass — แสดง dialogue เสมอ
        dialogueBox.setVisible(true);
        updateLayout();
        revalidate(); repaint();
        startTypewriter();
    }

    private void showChoices(Object[][] choices) {
        if (choices == null) return;
        isChoiceMode = true;
        choiceLayer.removeAll();
        int tw = (int)(getWidth() * 0.70);

        for (int i = 0; i < choices.length; i++) {
            String text        = (String) choices[i][0];
            String targetScene = (String) choices[i][1];
            final String cn    = (choices[i].length >= 4) ? (String)  choices[i][2] : null;
            final int    cs    = (choices[i].length >= 4) ? (Integer) choices[i][3] : 0;
            final String num   = String.valueOf(i + 1);

            UI_Components.ChoiceButton btn = new UI_Components.ChoiceButton(num, text, () -> {
                if (timerBar.isChoiceLocked()) return;
                doChoice(targetScene, cn, cs);
            });
            btn.setMaximumSize(new Dimension(tw, 50));
            btn.setPreferredSize(new Dimension(tw, 50));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            choiceLayer.add(btn);
            if (i < choices.length - 1) choiceLayer.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        updateLayout();
        choiceLayer.setVisible(true);

        // ===== TFT Timer =====
        // บอกผู้เล่นอื่นว่าเราถึง choice
        if (mpClient != null) mpClient.notifyChoiceReady();

        // ล็อคปุ่มก่อน
        for (Component c : choiceLayer.getComponents()) c.setEnabled(false);

        String roundName = "[ " + sceneName.replace("MP_", "") + " ]";
        timerBar.setRoundLabel(roundName);

        timerBar.setOnUnlock(() -> SwingUtilities.invokeLater(() -> {
            for (Component c : choiceLayer.getComponents()) c.setEnabled(true);
            repaint();
        }));

        int totalP = (mpServer != null) ? mpServer.getPlayerCount() + 1 : mpTotalPlayers;
        if (totalP < 1) totalP = 1;

        timerBar.startTimer(mpReadSeconds, totalP);

        // Host นับตัวเองเป็น ready ทันที
        if (mpServer != null) timerBar.playerReachedChoice();
    }

    private void doChoice(String targetScene, String charName, int score) {
        isChoiceMode   = false;
        pendingChoices = null;
        choiceLayer.removeAll();
        choiceLayer.setVisible(false);
        timerBar.stopTimer();

        if (charName != null && score != 0) {
            Relation.getInstance().addAffection(charName, score);
            RelationUI.getInstance().updateScore(charName);
        }

        // โหลด scene ถัดไป
        if (targetScene != null && storyMap.containsKey(targetScene)) {
            loadScene(targetScene);
        } else if ("MP_END".equals(targetScene)) {
            loadScene("MP_END");
        } else {
            finishGame();
        }
    }

    private void onNext() {
        // MP: ไม่ block ด้วย effectManager.isPlaying()
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

        sceneIndex++;
        if (sceneIndex < currentScene.length) {
            renderLine(currentScene[sceneIndex]);
        } else {
            nextScene();
        }
    }

    private void nextScene() {
        timerBar.stopTimer();

        // ถ้าเป็น MP_END → จบเกม
        if ("MP_END".equals(sceneName)) {
            finishGame();
            return;
        }

        // หา scene ถัดไปจาก SCENE_ORDER
        String[] order = StoryDataMP.SCENE_ORDER;
        for (int i = 0; i < order.length - 1; i++) {
            if (order[i].equals(sceneName)) {
                loadScene(order[i + 1]);
                return;
            }
        }
        finishGame();
    }

    private void finishGame() {
        timerBar.resetAndHide();
        if (onGameFinished != null) onGameFinished.run();
    }

    // ============================================================
    // Typewriter
    // ============================================================
    private void startTypewriter() {
        charIndex = 0;
        if (typeTimer != null) typeTimer.stop();
        // MP: เริ่มพิมพ์ทันที ไม่รอ effect
        runTypewriter();
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

    // ============================================================
    // Keys
    // ============================================================
    private void setupKeys() {
        InputMap  im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        im.put(KeyStroke.getKeyStroke(model.KeyConfig.getNextMsg(), 0), "next");
        am.put("next", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { onNext(); }
        });
    }
}