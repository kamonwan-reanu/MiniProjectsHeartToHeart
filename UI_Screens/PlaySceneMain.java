package UI_Screens;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import UI_Components.DialogueBox;
import UI_Components.ChoiceButton; 
import UI_Components.CharacterSprite;
import UI_Components.EffectManager;
import UI_Components.HeartHUDPanel;
import model.GameConstants;
import model.SoundManager;
import model.StoryData;

public class PlaySceneMain extends JPanel {

    private static final java.util.Set<String> CHAT_SCENES = new java.util.HashSet<>(
        java.util.Arrays.asList(
            "SCENE_4","SCENE_7","SCENE_8","SCENE_9",
            "SCENE_13_CHAT","SCENE_14","SCENE_15","SCENE_16"
        )
    );

    private Map<String, Object[][]> storyMap = new HashMap<>();
    private Object[][] currentSceneData;
    private String sceneName = "";
    private int storyIndex = 0;

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

    private JLabel captionLabel;
    private JLabel bgLayer;
    private CharacterSprite characterLayer;
    private JPanel effectLayer;
    private JPanel choiceLayer; 
    private DialogueBox dialogueBox;
    private Timer typeTimer;
    
    private SoundManager soundManager = new SoundManager();
    private EffectManager effectManager;

    private boolean debugHUD = true;
    private boolean isCaptionMode = false;

    private HeartHUDPanel heartHUD;

    private ChatScenePanel inlineChatPanel;

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

        this.dialogueBox = new DialogueBox();
        this.effectManager = new EffectManager(this, dialogueBox);

        initStoryMap();
        setupUIComponents();

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) { 
                updateUIStyles(); 
            }
            @Override
            public void componentShown(ComponentEvent e) {
                updateUIStyles();
                if (currentSceneData != null && storyIndex < currentSceneData.length) {
                    updateScene(currentSceneData[storyIndex]);
                }
            }
        });

        dialogueBox.setOnNextRequested(this::handleInteraction);

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleInteraction();
            }
        });
    }

    private void initStoryMap() {
        storyMap.put("SCENE_1", StoryData.SCENE_1);
        storyMap.put("SCENE_2", StoryData.SCENE_2);
        storyMap.put("SCENE_3", StoryData.SCENE_3);
        storyMap.put("SCENE_4", StoryData.SCENE_4);
        storyMap.put("SCENE_5", StoryData.SCENE_5);

        storyMap.put("SCENE_6", StoryData.SCENE_6);   // HUB
        storyMap.put("SCENE_7", StoryData.SCENE_7);   // ธีร์
        storyMap.put("SCENE_8", StoryData.SCENE_8);   // คีริน
        storyMap.put("SCENE_9", StoryData.SCENE_9);   // เทียน
        storyMap.put("SCENE_10", StoryData.SCENE_10); // ไปต่อ
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

    private void setupUIComponents() {
        bgLayer = new JLabel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            renderBackground((Graphics2D) g);
        }
    };

    characterLayer = new CharacterSprite(currentChar) { 
        @Override 
        protected void paintComponent(Graphics g) { 
            renderCharacter((Graphics2D) g); 
        } 
    };

    effectLayer = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
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

        // ✅ สร้าง HUD ก่อน
        heartHUD = new HeartHUDPanel();
        heartHUD.setOpaque(false);

        captionLabel = new JLabel("", SwingConstants.CENTER);
        captionLabel.setForeground(Color.WHITE);
        captionLabel.setFont(new Font("Tahoma", Font.PLAIN, 32));
        captionLabel.setOpaque(false);
        captionLabel.setVisible(false);
        add(captionLabel);
        
        // ✅ add ให้ครบก่อน
        add(bgLayer);
        add(characterLayer);
        add(effectLayer);
        add(dialogueBox);
        add(choiceLayer);
        add(heartHUD);

        // ✅ แล้วค่อยจัดลำดับซ้อน
        refreshZOrder();
    }

    private void renderBackground(Graphics2D g2d) {
        g2d.translate(effectManager.getShakeX(), effectManager.getShakeY()); 
        if (currentBG != null && !currentBG.isEmpty() && !currentBG.equals("none")) {
            try {
                Image img = new ImageIcon(currentBG).getImage();
                if (img != null && img.getWidth(null) > 0) {
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    int panelW = getWidth(); int panelH = getHeight();
                    double scaleX = (double) panelW / img.getWidth(null);
                    double scaleY = (double) panelH / img.getHeight(null);
                    double finalScale = Math.max(scaleX, scaleY) + bgScaleOffset + 0.15;
                    int drawW = (int) (img.getWidth(null) * finalScale);
                    int drawH = (int) (img.getHeight(null) * finalScale);
                    g2d.drawImage(img, (panelW - drawW) / 2 + bgOffsetX, (panelH - drawH) / 2 + bgOffsetY, drawW, drawH, this);
                }
            } catch (Exception e) { }
        }
    }

    private void renderCharacter(Graphics2D g2d) {
        g2d.translate(effectManager.getShakeX(), effectManager.getShakeY()); 
        if (currentChar != null && !currentChar.isEmpty() && !currentChar.equals("none")) {
            try {
                Image img = new ImageIcon(currentChar).getImage();
                if (img != null && img.getWidth(null) > 0) {
                    int panelH = getHeight();
                    double ratio = (double) (panelH + 30) / img.getHeight(null);
                    int drawW = (int) (img.getWidth(null) * ratio);
                    g2d.drawImage(img, (getWidth() - drawW) / 2, 0, drawW, panelH + 30, this);
                }
            } catch (Exception e) { }
        }
    }

    private void refreshZOrder() {
        if (heartHUD != null)    setComponentZOrder(heartHUD, 0);
        if (captionLabel != null) setComponentZOrder(captionLabel, 1); // ← เพิ่ม
        setComponentZOrder(choiceLayer, 2);
        setComponentZOrder(dialogueBox, 3);
        setComponentZOrder(effectLayer, 4);
        setComponentZOrder(characterLayer, 5);
        setComponentZOrder(bgLayer, 6);
    }

    private void updateUIStyles() {
        int w = getWidth(); int h = getHeight();
        if (w <= 0 || h <= 0) return;

        bgLayer.setBounds(0, 0, w, h);
        characterLayer.setBounds(0, 0, w, h);
        effectLayer.setBounds(0, 0, w, h);
        if (captionLabel != null) captionLabel.setBounds(0, 0, w, h);
        
        // ✨ ปรับ groupH ให้สูงขึ้นเล็กน้อยเพื่อรองรับ 2 บรรทัดแบบโปรๆ
        int groupW = (int) (w * 0.70); 
        int groupH = (int) (h * 0.25); 
        
        int buttonCount = (choiceLayer.getComponentCount() + 1) / 2;
        int dialogueY;
        
        if (isChoiceMode && buttonCount > 0) {
            int dynamicOffset = (buttonCount * 65) + 100; 
            dialogueY = h - groupH - dynamicOffset; 
        } else {
            // ✨ ขยับขึ้นจากขอบล่าง 100px เพื่อให้ตัวหนังสือบรรทัดล่างไม่เบียด
            dialogueY = h - groupH - 100; 
        }
        
        dialogueBox.moveTo(groupW, groupH, dialogueY);

        int choiceY = dialogueY + groupH + 20; 
        int choiceH = (buttonCount > 0) ? (buttonCount * 65) : 100; 
        choiceLayer.setBounds((w - groupW) / 2, choiceY, groupW, choiceH);

        revalidate(); 
        repaint();

        heartHUD.setBounds(15, 15, 300, 140);

        // HUD ขนาดเล็กมุมซ้ายบน
        heartHUD.setBounds(30, 30, 320, 150);
        heartHUD.repaint();
    }

    public void updateScene(Object[] lineData) {
        if (lineData == null || lineData.length < 2) return;

        choiceLayer.removeAll();
        choiceLayer.setVisible(false);
        isChoiceMode = false;
        pendingChoices = null;
        currentSpeaker = "";

        // ✅ ตรวจ CAPTION ก่อนทำอย่างอื่น
        boolean isCaption = false;
        for (Object o : lineData) {
            if ("CAPTION".equals(o)) { isCaption = true; break; }
        }

        isCaptionMode = isCaption;

        Object speakerData = lineData[0];
        if (speakerData != null) {
            String speakerStr = speakerData.toString().trim();
            if (speakerStr.equalsIgnoreCase("PLAYER") || speakerStr.equals(GameConstants.PLAYER_NAME)) {
                currentSpeaker = GameConstants.PLAYER_NAME;
            } else if (!speakerStr.isEmpty() && !speakerStr.equalsIgnoreCase("none")) {
                currentSpeaker = speakerStr;
            }
        }

        fullText = lineData[1].toString().replace("[PLAYER]", GameConstants.PLAYER_NAME);

        if (lineData.length >= 3) {
            String fileName = (String) lineData[2];
            this.currentChar = (fileName == null || fileName.isEmpty() || fileName.equals("none")) ? ""
                : (fileName.startsWith("model/") ? fileName : GameConstants.CHAR_PATH + fileName);
            characterLayer.updateCharacter(this.currentChar);
        }

        if (lineData.length >= 4) {
            String bgImg = (String) lineData[3];
            this.currentBG = (bgImg == null || bgImg.isEmpty() || bgImg.equals("none")) ? ""
                : (bgImg.startsWith("model/") ? bgImg : GameConstants.SCENE_PATH + bgImg);
        }

        boolean hasNewEffect = false;
        for (int i = 4; i < lineData.length; i++) {
            if (lineData[i] instanceof Object[][]) {
                this.pendingChoices = (Object[][]) lineData[i];
                this.isChoiceMode = true;
            } else if (lineData[i] instanceof String) {
                String val = (String) lineData[i];
                if (val == null || val.isEmpty() || val.equalsIgnoreCase("none")) continue;
                if ("CAPTION".equals(val)) continue; // ✅ ข้าม CAPTION ไม่ส่งให้ sound

                String upperVal = val.toUpperCase();
                if (upperVal.contains("FADE") || upperVal.contains("WHITE") ||
                    upperVal.contains("BLACK") || upperVal.equals("SHAKE") || upperVal.equals("FLASH")) {
                    if (effectManager != null) {
                        effectManager.stopAll();
                        effectManager.play(val);
                        hasNewEffect = true;
                    }
                } else {
                    String fullPath = GameConstants.SOUND_PATH + val + ".wav";
                    if (upperVal.startsWith("BGM")) soundManager.playBGM(fullPath);
                    else soundManager.playSE(fullPath);
                }
            }
        }

        if (isCaptionMode) {
            // Caption mode: ซ่อนกล่อง แสดงข้อความลอยกลางจอ
            dialogueBox.setVisible(false);
            if (captionLabel != null) {
                captionLabel.setText("");
                captionLabel.setVisible(true);
            }
            
        } else {
            // ปกติ: ซ่อน caption
            if (captionLabel != null) captionLabel.setVisible(false);
            dialogueBox.setText(currentSpeaker, "");
            if (hasNewEffect) {
                dialogueBox.setVisible(false);
            } else {
                dialogueBox.setVisible(true);
            }
        }

        updateUIStyles();
        revalidate();
        repaint();
        startTypewriter();
    }


    private void showChoices(Object[][] choices) {
        if (choices == null) return;

        isChoiceMode = true;
        choiceLayer.removeAll();

        int targetWidth = (int) (getWidth() * 0.70);
        int fixedHeight = 50;

        for (int i = 0; i < choices.length; i++) {
            String num = String.valueOf(i + 1);
            String text = (String) choices[i][0];
            final String rawTarget = (String) choices[i][1];

            ChoiceButton btn = new ChoiceButton(num, text, () -> {
                isChoiceMode = false;
                pendingChoices = null;
                choiceLayer.removeAll();
                choiceLayer.setVisible(false);

                // ✅ แปลง PICKxx_... -> SCENE_...
                String nextSceneKey = resolvePickToken(rawTarget);

                System.out.println("[CHOICE] raw=" + rawTarget + " -> next=" + nextSceneKey
                    + " | teer=" + model.GameState.teerHeart
                    + " tian=" + model.GameState.tianHeart
                    + " kirin=" + model.GameState.kirinHeart);

                Object[][] next = storyMap.get(nextSceneKey);
                if (next == null) {
                    System.out.println("[ERROR] Scene not found: " + nextSceneKey);
                    return;
                }
                loadNewScene(next, nextSceneKey);
            });

            btn.setMaximumSize(new Dimension(targetWidth, fixedHeight));
            btn.setPreferredSize(new Dimension(targetWidth, fixedHeight));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);

            choiceLayer.add(btn);
            if (i < choices.length - 1) choiceLayer.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        updateUIStyles();
        choiceLayer.setVisible(true);
        choiceLayer.revalidate();
        choiceLayer.repaint();
    }

    private void handleInteraction() {
        System.out.println("[DEBUG] handleInteraction called");
        System.out.println("  effectPlaying=" + effectManager.isPlaying());
        System.out.println("  typeRunning=" + (typeTimer != null && typeTimer.isRunning()));
        System.out.println("  isCaptionMode=" + isCaptionMode);
        System.out.println("  isChoiceMode=" + isChoiceMode);

        if (effectManager != null && effectManager.isPlaying()) return;

        if (typeTimer != null && typeTimer.isRunning()) {
            typeTimer.stop();
            charIndex = fullText.length();
            if (!isCaptionMode) {
                dialogueBox.setText(currentSpeaker, fullText);
            }
            return;
        }

        int buttonCount = (choiceLayer.getComponentCount() + 1) / 2;
        if (isChoiceMode && buttonCount == 0 && pendingChoices != null) {
            showChoices(pendingChoices);
            return;
        }
        
        if (isChoiceMode && buttonCount > 0) return;

        // ✅ ซ่อน captionLabel ก่อนไปฉากถัดไป
        if (isCaptionMode && captionLabel != null) {
            captionLabel.setVisible(false);
            isCaptionMode = false;
        }

        storyIndex++;
        if (storyIndex < currentSceneData.length) {
            updateScene(currentSceneData[storyIndex]);
        } else {
            handleSceneTransition();
        }
    }

    private void handleSceneTransition() {
        if (sceneName.equals("SCENE_13")) {
            showInlineChat(StoryData.SCENE_13_CHAT, 
                GameConstants.SCENE_PATH + "dating_chat.png");
            return;
        }

        try {
            int currentNum = Integer.parseInt(sceneName.replace("SCENE_", ""));
            String nextSceneKey = "SCENE_" + (currentNum + 1);

            // ✅ ใช้ core.Main.CHAT_SCENES แทน
            if (core.Main.CHAT_SCENES.contains(nextSceneKey)) {
                String chatKey = "CHAT_" + (currentNum + 1);
                ChatScenePanel panel = core.Main.chatPanels.get(chatKey);
                if (panel != null) panel.resetAndStart();
                core.Main.cardLayout.show(core.Main.mainContainer, chatKey);
                return;
            }
            if (storyMap.containsKey(nextSceneKey)) {
                loadNewScene(storyMap.get(nextSceneKey), nextSceneKey);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadNewScene(Object[][] nextSceneData, String newSceneName) {
        if (nextSceneData == null) return;

        // ✨ ถ้าเป็น SCENE_13 ที่ส่งมาจาก choiceOnly → ไม่ต้อง redirect ไป chat
        boolean isChoiceOnly = (nextSceneData.length == 1);

        if (!isChoiceOnly && CHAT_SCENES.contains(newSceneName)) {
            String chatKey = "CHAT_" + newSceneName.replace("SCENE_", "");
            ChatScenePanel panel = core.Main.chatPanels.get(chatKey);
            if (panel != null) panel.resetAndStart();
            core.Main.cardLayout.show(core.Main.mainContainer, chatKey);
            return;
        }

        this.currentSceneData = nextSceneData;
        this.sceneName = newSceneName;
        this.storyIndex = 0;
        updateScene(currentSceneData[storyIndex]);
    }

    private void startTypewriter() {
        charIndex = 0;
        if (typeTimer != null) typeTimer.stop();

        // ✅ ถ้าเป็น Caption mode ไม่ต้องรอ effect เลย รัน typewriter ทันที
        if (isCaptionMode) {
            runActualTypewriter();
            return;
        }

        Timer waitTimer = new Timer(50, null);
        waitTimer.addActionListener(e -> {
            if (effectManager != null && (effectManager.isPlaying() ||
                    effectManager.getAlpha() > 0.3f)) {
                return;
            }
            waitTimer.stop();
            runActualTypewriter();
        });
        waitTimer.start();
    }

    private void runActualTypewriter() {
        if (!isCaptionMode) {
            dialogueBox.setVisible(true);
        }

        if (typeTimer != null) typeTimer.stop();
        typeTimer = new Timer(GameConstants.TYPEWRITER_SPEED, e -> {
            if (charIndex < fullText.length()) {
                charIndex++;
                if (isCaptionMode) {
                    if (captionLabel != null) {
                        captionLabel.setText(fullText.substring(0, charIndex));
                    }
                } else {
                    dialogueBox.setText(currentSpeaker, fullText.substring(0, charIndex));
                }
            } else {
                typeTimer.stop();
            }
        });
        typeTimer.start();
    }

    private void drawDebugHUD(Graphics g) {
        if (!debugHUD) return;

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(15, 15, 360, 120, 15, 15);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Tahoma", Font.BOLD, 16));
        g2.drawString("DEBUG HUD", 25, 40);

        g2.setFont(new Font("Tahoma", Font.PLAIN, 14));
        g2.drawString("TEER: " + model.GameState.teerHeart, 25, 65);
        g2.drawString("KIRIN: " + model.GameState.kirinHeart, 25, 85);
        g2.drawString("TIAN: " + model.GameState.tianHeart, 25, 105);

        g2.dispose();
    }

        @Override
        protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // debugHUD ปิดไว้ก่อน กันซ้อน
        // drawDebugHUD(g);
    }

    // ===============================
    // PICK TOKEN RESOLVER
    // ===============================
    private String resolvePickToken(String token) {
        if (token == null) return null;
        token = token.trim();

        // ไม่ใช่ PICK ก็ให้ไปฉากนั้นตรง ๆ
        if (!token.startsWith("PICK")) return token;

        // PICK13
        if (token.startsWith("PICK13_")) {
            if (!model.GameState.picked13) {
                addHeartByToken(token);
                model.GameState.picked13 = true;
                heartHUD.repaint();
            }
            if (token.endsWith("_TEER")) return "SCENE_14";
            if (token.endsWith("_TIAN")) return "SCENE_15";
            if (token.endsWith("_KIRIN")) return "SCENE_16";
        }

        // PICK17
        if (token.startsWith("PICK17_")) {
            if (!model.GameState.picked17) {
                addHeartByToken(token);
                model.GameState.picked17 = true;
                heartHUD.repaint();
            }
            String end = decideEndingIfAny();
            if (end != null) return end;

            return "SCENE_21";
        }

        // PICK22
        if (token.startsWith("PICK22_")) {
            if (!model.GameState.picked22) {
                addHeartByToken(token);
                model.GameState.picked22 = true;
                heartHUD.repaint();
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

    public void showInlineChat(Object[][] chatData, String bgPath) {
        if (inlineChatPanel != null) {
            remove(inlineChatPanel);
        }
        
        inlineChatPanel = new ChatScenePanel(chatData, bgPath, () -> {
            // ✅ แชทจบแล้ว → แสดง choice
            Object[][] choiceOnly = new Object[][] {
                chatData[chatData.length - 1] // บรรทัดสุดท้ายคือ choice
            };
            loadNewScene(choiceOnly, "SCENE_13_CHAT");
            
            // ลบ chat panel ออก
            remove(inlineChatPanel);
            inlineChatPanel = null;
            revalidate();
            repaint();
        }, false);
        
        int w = getWidth();
        int h = getHeight();
        int chatH = (int)(h * 0.50);
        inlineChatPanel.setBounds(0, 0, w, chatH);
        
        add(inlineChatPanel);
        setComponentZOrder(inlineChatPanel, 0);
        revalidate();
        repaint();
        inlineChatPanel.resetAndStart();
    }

}