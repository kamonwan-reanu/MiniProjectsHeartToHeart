package UI_Screens;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import UI_Components.DialogueBox;
import UI_Components.CharacterSprite;
import UI_Components.EffectManager;
import model.GameConstants;
import model.SoundManager;
import model.StoryData;

public class PlaySceneMain extends JPanel {
    // ข้อมูลเนื้อเรื่อง
    private Map<String, Object[][]> storyMap = new HashMap<>();
    private Object[][] currentSceneData;
    private String sceneName = "";
    private int storyIndex = 0;

    // สถานะ UI
    private boolean isChoiceMode = false;
    private String currentBG = "";
    private String currentChar = "";
    private String currentSpeaker = "";
    private String fullText = "";
    private int charIndex = 0;

    // ระบบภาพและ Effect ของเพื่อน
    private double bgScaleOffset = 0.0;
    private int bgOffsetX = 0;
    private int bgOffsetY = 0;

    // Components
    private JLabel bgLayer;
    private CharacterSprite characterLayer;
    private JPanel effectLayer;
    private DialogueBox dialogueBox;
    private ChoiceOverlay choiceOverlay;
    private Timer typeTimer;
    
    // Managers
    private SoundManager soundManager = new SoundManager();
    private EffectManager effectManager;

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

        // สร้าง DialogueBox และ EffectManager ก่อน เพื่อไม่ให้ Error
        this.dialogueBox = new DialogueBox();
        this.effectManager = new EffectManager(this, dialogueBox);

        initStoryMap();
        setupUIComponents();

        // ระบบ Responsive ปรับขนาดตามจอ
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) { updateUIStyles(); }
            @Override
            public void componentShown(ComponentEvent e) {
                updateUIStyles();
                updateScene(currentSceneData[storyIndex]);
            }
        });

        dialogueBox.setOnNextRequested(this::handleInteraction);
    }

    private void initStoryMap() {
        storyMap.put("SCENE_1", StoryData.SCENE_1);
        storyMap.put("SCENE_2", StoryData.SCENE_2);
        storyMap.put("SCENE_3", StoryData.SCENE_3);
        storyMap.put("SCENE_4", StoryData.SCENE_4);
        storyMap.put("SCENE_5", StoryData.SCENE_5);
        storyMap.put("SCENE_6", StoryData.SCENE_6);
    }

    private void setupUIComponents() {
        bgLayer = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.translate(effectManager.getShakeX(), effectManager.getShakeY()); // Effect ของเพื่อน
                if (currentBG != null && !currentBG.isEmpty() && !currentBG.equals("none")) {
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
                }
            }
        };
        
        characterLayer = new CharacterSprite(currentChar) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.translate(effectManager.getShakeX(), effectManager.getShakeY()); // Effect ของเพื่อน
                if (currentChar != null && !currentChar.isEmpty() && !currentChar.equals("none")) {
                    Image img = new ImageIcon(currentChar).getImage();
                    if (img != null && img.getWidth(null) > 0) {
                        int panelH = getHeight();
                        double ratio = (double) (panelH + 30) / img.getHeight(null);
                        int drawW = (int) (img.getWidth(null) * ratio);
                        g2d.drawImage(img, (getWidth() - drawW) / 2, 0, drawW, panelH + 30, this);
                    }
                }
            }
        };

        effectLayer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                effectManager.drawEffects(g2d, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        effectLayer.setOpaque(false);

        // จัดการ Z-Order
        add(dialogueBox);      
        add(effectLayer);      
        add(characterLayer);   
        add(bgLayer);          
        
        setComponentZOrder(dialogueBox, 0); 
        setComponentZOrder(effectLayer, 1); 
        setComponentZOrder(characterLayer, 2); 
        setComponentZOrder(bgLayer, 3); 
    }

    private void updateUIStyles() {
        int w = getWidth(); int h = getHeight();
        if (w <= 0 || h <= 0) return;

        bgLayer.setBounds(0, 0, w, h);
        characterLayer.setBounds(0, 0, w, h); 
        effectLayer.setBounds(0, 0, w, h); 
        
        dialoguePaneManager(w, h);

        if (isChoiceMode && choiceOverlay != null) {
            int targetY = (int) (h * 0.35); 
            int groupH = (int) (h * 0.25);
            choiceOverlay.updateBounds(w, h, targetY, groupH);
        }
        
        revalidate(); repaint();
    }

    private void dialoguePaneManager(int w, int h) {
        dialogueBox.setBounds(0, 0, w, h);
        int groupW = (int) (w * 0.85);
        int groupH = (int) (h * 0.25);

        if (isChoiceMode) {
            int targetY = (int) (h * 0.35); 
            dialogueBox.moveTo(groupW, groupH, targetY);
        } else {
            dialogueBox.updateLayout(groupW, groupH, h);
        }
    }

    public void updateScene(Object[] lineData) {
        if (lineData == null || lineData.length < 2) return; 

        if (!effectManager.isPlaying()) effectManager.stopAll(); 
        checkAndPlayEffect();

        // 1. จัดการ Speaker
        currentSpeaker = ""; 
        Object speakerData = lineData[0];
        if (speakerData != null) {
            String speakerStr = speakerData.toString().trim();
            if (speakerStr.equalsIgnoreCase("PLAYER") || speakerStr.equals("........") || speakerData == GameConstants.PLAYER_NAME) {
                currentSpeaker = GameConstants.PLAYER_NAME;
            } else if (!speakerStr.isEmpty() && !speakerStr.equalsIgnoreCase("none")) {
                currentSpeaker = speakerStr; 
            }
        }
        dialogueBox.setText(currentSpeaker, ""); 

        // 2. จัดการ Text
        fullText = lineData[1].toString();
        if (fullText.contains("[PLAYER]")) {
            fullText = fullText.replace("[PLAYER]", GameConstants.PLAYER_NAME);
        }

        // 3. จัดการรูปตัวละคร
        if (lineData.length >= 3) {
            String fileName = (String) lineData[2];
            this.currentChar = (fileName == null || fileName.isEmpty() || fileName.equals("none")) ? "" : (fileName.startsWith("model/") ? fileName : GameConstants.CHAR_PATH + fileName);
            characterLayer.updateCharacter(this.currentChar);
        }

        // 4. จัดการรูปพื้นหลัง
        if (lineData.length >= 4) {
            String bgImg = (String) lineData[3];
            this.currentBG = (bgImg == null || bgImg.isEmpty() || bgImg.equals("none")) ? "" : (bgImg.startsWith("model/") ? bgImg : GameConstants.SCENE_PATH + bgImg);
        }

        // 🌟 แก้ปัญหาการตีกันของข้อมูล (Sound VS Choice) โดยเช็คประเภทข้อมูล (instanceof)
        this.isChoiceMode = false;
        for (int i = 4; i < lineData.length; i++) {
            Object extraData = lineData[i];
            if (extraData instanceof Object[][]) {
                // ถ้าเป็น Array 2 มิติแปลว่าเป็นตัวเลือกของเรา
                this.isChoiceMode = true;
                showSpiritChoices((Object[][]) extraData);
            } else if (extraData instanceof String && i == 4) {
                // ถ้าเป็น String ในตำแหน่งที่ 4 แปลว่าเป็นระบบ Sound ของเพื่อน
                String soundName = (String) extraData;
                if (!soundName.isEmpty() && !soundName.equalsIgnoreCase("none")) {
                    String fullPath = GameConstants.SOUND_PATH + soundName + ".wav";
                    if (soundName.startsWith("BGM")) soundManager.playBGM(fullPath);
                    else soundManager.playSE(fullPath);
                }
            }
        }

        updateUIStyles();
        repaint();
        startTypewriter();
    }

    private void checkAndPlayEffect() {
        if (storyIndex < currentSceneData.length) {
            Object[] lineData = currentSceneData[storyIndex];
            if (lineData.length >= 6) {
                String effectName = (String) lineData[5];
                if (effectName != null && !effectName.equalsIgnoreCase("none") && !effectName.isEmpty()) {
                    effectManager.play(effectName);
                }
            }
        }
    }

    private void handleInteraction() {
        boolean hasEffect = false;
        if (storyIndex < currentSceneData.length) {
            Object[] lineData = currentSceneData[storyIndex];
            if (lineData.length >= 6) {
                String effectName = (String) lineData[5];
                hasEffect = (effectName != null && !effectName.equalsIgnoreCase("none") && !effectName.isEmpty());
            }
        }

        if (typeTimer != null && typeTimer.isRunning()) {
            if (hasEffect) return; // ถ้ากำลังเล่น Effect อยู่ ให้คลิกข้ามไม่ได้
            
            typeTimer.stop();
            charIndex = fullText.length();
            dialogueBox.setText(currentSpeaker, fullText);
        } else if (!isChoiceMode) {
            storyIndex++;
            if (storyIndex < currentSceneData.length) {
                updateScene(currentSceneData[storyIndex]);
            } else {
                handleSceneTransition();
            }
        }
    }

    private void handleSceneTransition() {
        try {
            int currentNum = Integer.parseInt(sceneName.replace("SCENE_", ""));
            String nextSceneKey = "SCENE_" + (currentNum + 1);

            if (storyMap.containsKey(nextSceneKey)) {
                loadNewScene(storyMap.get(nextSceneKey), nextSceneKey);
            } else {
                System.out.println("End of Story");
            }
        } catch (Exception e) {
            System.err.println("Scene naming error: " + sceneName);
        }
    }

    public void loadNewScene(Object[][] nextSceneData, String newSceneName) {
        if (nextSceneData == null) return;
        this.currentSceneData = nextSceneData;
        this.sceneName = newSceneName;
        this.storyIndex = 0;
        updateScene(currentSceneData[storyIndex]);
    }

    // ✅ ใช้ระบบปุ่มจาก ChoiceOverlay ที่เราแยกไฟล์กันไว้
    private void showSpiritChoices(Object[][] choices) {
        int w = getWidth();
        int h = getHeight();
        int groupW = (int) (w * 0.85);
        int groupH = (int) (h * 0.25);
        int targetY = (int) (h * 0.35); 
        
        dialogueBox.moveTo(groupW, groupH, targetY);

        if (choiceOverlay != null) remove(choiceOverlay);

        choiceOverlay = new ChoiceOverlay(choices, targetScene -> {
            this.isChoiceMode = false;
            remove(choiceOverlay);
            dialogueBox.updateLayout(groupW, groupH, getHeight());
            
            Object[][] nextData = storyMap.get(targetScene);
            if (nextData != null) {
                loadNewScene(nextData, targetScene);
            } else {
                System.err.println("❌ ไม่พบฉาก: " + targetScene);
            }
            revalidate(); repaint();
        });

        choiceOverlay.updateBounds(w, h, targetY, groupH);
        
        add(choiceOverlay, 0); 
        setComponentZOrder(dialogueBox, 1); 
        
        revalidate(); repaint();
    }

    private void startTypewriter() {
        charIndex = 0;
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
}