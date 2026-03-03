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

public class PlaySceneMP extends JPanel {

    private final Map<String, Object[][]> storyMap = new HashMap<>();
    private Object[][] currentScene;
    private String     sceneName  = "MP_INTRO";
    private int        sceneIndex = 0;
    private Runnable   onGameFinished;

    private String  currentBG      = "";
    private String  currentChar    = "";
    private String  currentSpeaker = "";
    private String  fullText       = "";
    private int     charIndex      = 0;
    private boolean isChoiceMode   = false;
    private Object[][] pendingChoices = null;

    private JLabel              bgLayer;
    private CharacterSprite     characterLayer;
    private JPanel              effectLayer;
    private JPanel              choiceLayer;
    private DialogueBox         dialogueBox;
    private Timer               typeTimer;
    private MultiplayerTimerBar timerBar;

    private final SoundManager  soundManager  = new SoundManager();
    private EffectManager       effectManager;

    private GameServer mpServer       = null;
    private GameClient mpClient       = null;
    private int        mpReadSeconds  = 30;
    private int        mpTotalPlayers = 2;

    public PlaySceneMP() {
        setLayout(null);
        setOpaque(true);
        setBackground(Color.BLACK);

        dialogueBox   = new DialogueBox();
        effectManager = new EffectManager(this, dialogueBox);
        effectManager.setMultiplayerBypass(true);

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

    // ── Public API ─────────────────────────────────────────
    public void startMPGame(GameServer server, GameClient client, int readSec) {
        this.mpServer       = server;
        this.mpClient       = client;
        this.mpReadSeconds  = (readSec > 0) ? readSec : 30;
        this.mpTotalPlayers = (server != null) ? Math.max(server.getPlayerCount() + 1, 2) : 2;

        Relation.getInstance().resetAll();
        RelationUI.getInstance().updateAllScores();
        effectManager.stopAll();
        effectManager.setMultiplayerBypass(true);
        timerBar.resetAndHide();

        loadScene("MP_INTRO");
        updateLayout();
    }

    public void setOnGameFinished(Runnable r) { this.onGameFinished = r; }

    public void onRemotePlayerReady() {
        if (timerBar != null) SwingUtilities.invokeLater(timerBar::otherPlayerReady);
    }

    // ── Story Map ──────────────────────────────────────────
    private void initStoryMap() {
        for (String name : StoryDataMP.SCENE_ORDER) {
            Object[][] data = StoryDataMP.getScene(name);
            if (data != null) storyMap.put(name, data);
        }
    }

    // ── UI Setup ───────────────────────────────────────────
    private void setupUIComponents() {
        bgLayer = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBG((Graphics2D) g);
            }
        };

        characterLayer = new CharacterSprite("") {
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

        add(timerBar);
        add(choiceLayer);
        add(dialogueBox);
        add(effectLayer);
        add(characterLayer);
        add(bgLayer);

        refreshZOrder();
    }

    private void drawBG(Graphics2D g2) {
        if (effectManager != null) g2.translate(effectManager.getShakeX(), effectManager.getShakeY());
        if (currentBG == null || currentBG.isEmpty() || currentBG.equals("none")) return;
        try {
            Image img = new ImageIcon(currentBG).getImage();
            if (img == null || img.getWidth(null) <= 0) return;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int pw = getWidth(), ph = getHeight();
            double s = Math.max((double) pw / img.getWidth(null), (double) ph / img.getHeight(null)) + 0.15;
            int dw = (int)(img.getWidth(null) * s), dh = (int)(img.getHeight(null) * s);
            g2.drawImage(img, (pw - dw) / 2, (ph - dh) / 2, dw, dh, this);
        } catch (Exception ignored) {}
    }

    private void drawChar(Graphics2D g2) {
        if (effectManager != null) g2.translate(effectManager.getShakeX(), effectManager.getShakeY());
        if (currentChar == null || currentChar.isEmpty() || currentChar.equals("none")) return;
        try {
            Image img = new ImageIcon(currentChar).getImage();
            if (img == null || img.getWidth(null) <= 0) return;
            int ph = getHeight();
            double r = (double)(ph + 30) / img.getHeight(null);
            int dw = (int)(img.getWidth(null) * r);
            g2.drawImage(img, (getWidth() - dw) / 2, 0, dw, ph + 30, this);
        } catch (Exception ignored) {}
    }

    private void refreshZOrder() {
        try {
            setComponentZOrder(timerBar,       0);
            setComponentZOrder(choiceLayer,    1);
            setComponentZOrder(dialogueBox,    2);
            setComponentZOrder(effectLayer,    3);
            setComponentZOrder(characterLayer, 4);
            setComponentZOrder(bgLayer,        5);
        } catch (Exception ignored) {}
    }

    private void updateLayout() {
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

        bgLayer.setBounds(0, 0, w, h);
        characterLayer.setBounds(0, 0, w, h);
        effectLayer.setBounds(0, 0, w, h);

        // Timer bar — กลางบน ไม่ขึ้นกับ isVisible()
        int tw = Math.min(460, w - 40), th = 62;
        timerBar.setBounds((w - tw) / 2, 10, tw, th);

        int gw = (int)(w * 0.70);
        int gh = (int)(h * 0.25);
        int btnCount = countChoiceButtons();
        int dialogueY = (isChoiceMode && btnCount > 0)
            ? h - gh - (btnCount * 65) - 80
            : h - gh - 80;
        dialogueY = Math.max(dialogueY, 80);

        dialogueBox.moveTo(gw, gh, dialogueY);
        choiceLayer.setBounds((w - gw) / 2, dialogueY + gh + 14, gw, Math.max(btnCount * 65, 50));

        revalidate();
        repaint();
    }

    private int countChoiceButtons() {
        int n = 0;
        for (Component c : choiceLayer.getComponents())
            if (c instanceof UI_Components.ChoiceButton) n++;
        return n;
    }

    // ── Scene Logic ────────────────────────────────────────
    private void loadScene(String name) {
        Object[][] data = storyMap.get(name);
        if (data == null) { finishGame(); return; }
        effectManager.stopAll();
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
            currentChar = (fn == null || fn.isEmpty() || fn.equals("none")) ? ""
                : fn.startsWith("model/") ? fn : GameConstants.CHAR_PATH + fn;
            characterLayer.updateCharacter(currentChar);
        }

        if (line.length >= 4) {
            String bg = (String) line[3];
            currentBG = (bg == null || bg.isEmpty() || bg.equals("none")) ? ""
                : bg.startsWith("model/") ? bg : GameConstants.SCENE_PATH + bg;
        }

        for (int i = 4; i < line.length; i++) {
            Object slot = line[i];
            if (slot instanceof Object[][]) {
                pendingChoices = (Object[][]) slot;
                isChoiceMode   = true;
            } else if (slot instanceof String) {
                String val = (String) slot;
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

        dialogueBox.setVisible(true);
        updateLayout();
        startTypewriter();
    }

    // ── Show Choices ───────────────────────────────────────
    private void showChoices(Object[][] choices) {
        if (choices == null) return;
        isChoiceMode = true;
        choiceLayer.removeAll();
        int tw = Math.max((int)(getWidth() * 0.70), 300);

        for (int i = 0; i < choices.length; i++) {
            final String targetScene = (String)  choices[i][1];
            final String cn          = choices[i].length >= 4 ? (String)  choices[i][2] : null;
            final int    cs          = choices[i].length >= 4 ? (Integer) choices[i][3] : 0;
            final String text        = (String)  choices[i][0];
            final String num         = String.valueOf(i + 1);

            UI_Components.ChoiceButton btn = new UI_Components.ChoiceButton(num, text, () -> {
                if (timerBar != null && timerBar.isChoiceLocked()) return;
                doChoice(targetScene, cn, cs);
            });

            btn.setMaximumSize(new Dimension(tw, 55));
            btn.setPreferredSize(new Dimension(tw, 55));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            choiceLayer.add(btn);
            if (i < choices.length - 1)
                choiceLayer.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        updateLayout();
        choiceLayer.setVisible(true);
        refreshZOrder();

        if (mpClient != null) mpClient.notifyChoiceReady();

        String roundName = sceneName.replace("MP_", "");
        timerBar.setRoundLabel("[ " + roundName + " ]");

        int totalP = (mpServer != null) ? Math.max(mpServer.getPlayerCount() + 1, 2) : mpTotalPlayers;

        // ล็อคปุ่มเฉพาะ multiplayer (totalP > 1)
        if (totalP > 1) {
            for (Component c : choiceLayer.getComponents())
                if (c instanceof UI_Components.ChoiceButton) c.setEnabled(false);
        }

        timerBar.setOnUnlock(() -> SwingUtilities.invokeLater(() -> {
            for (Component c : choiceLayer.getComponents())
                if (c instanceof UI_Components.ChoiceButton) c.setEnabled(true);
            repaint();
        }));

        timerBar.startTimer(mpReadSeconds, totalP);

        if (mpServer != null) timerBar.playerReachedChoice();
    }

    private void doChoice(String targetScene, String charName, int score) {
        isChoiceMode   = false;
        pendingChoices = null;
        choiceLayer.removeAll();
        choiceLayer.setVisible(false);
        timerBar.stopTimer();
        timerBar.resetAndHide();

        if (charName != null && !charName.isEmpty() && score != 0) {
            Relation.getInstance().addAffection(charName, score);
            RelationUI.getInstance().updateScore(charName);
        }

        if (targetScene != null && storyMap.containsKey(targetScene)) {
            loadScene(targetScene);
        } else {
            finishGame();
        }
    }

    // ── onNext ─────────────────────────────────────────────
    private void onNext() {
        if (typeTimer != null && typeTimer.isRunning()) {
            typeTimer.stop();
            charIndex = fullText.length();
            dialogueBox.setText(currentSpeaker, fullText);
            return;
        }

        int btnCount = countChoiceButtons();
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
        if ("MP_END".equals(sceneName)) { finishGame(); return; }
        String[] order = StoryDataMP.SCENE_ORDER;
        for (int i = 0; i < order.length - 1; i++) {
            if (order[i].equals(sceneName)) { loadScene(order[i + 1]); return; }
        }
        finishGame();
    }

    private void finishGame() {
        timerBar.resetAndHide();
        if (onGameFinished != null) onGameFinished.run();
    }

    // ── Typewriter ─────────────────────────────────────────
    private void startTypewriter() {
        charIndex = 0;
        if (typeTimer != null) typeTimer.stop();
        dialogueBox.setVisible(true);
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

    // ── Keys ───────────────────────────────────────────────
    private void setupKeys() {
        InputMap  im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        im.put(KeyStroke.getKeyStroke(model.KeyConfig.getNextMsg(), 0), "mp_next");
        am.put("mp_next", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { onNext(); }
        });
    }
}