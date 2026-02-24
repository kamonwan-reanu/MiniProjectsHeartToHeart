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
import model.GameConstants;
import model.SoundManager;
import model.StoryData;

public class PlaySceneMain extends JPanel {
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

    private JLabel bgLayer;
    private CharacterSprite characterLayer;
    private JPanel effectLayer;
    private JPanel choiceLayer; 
    private DialogueBox dialogueBox;
    private Timer typeTimer;
    
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
        setComponentZOrder(effectLayer, 0); 
        setComponentZOrder(choiceLayer, 1); 
        setComponentZOrder(dialogueBox, 2);
        setComponentZOrder(characterLayer, 3); 
        setComponentZOrder(bgLayer, 4); 
    }

    private void updateUIStyles() {
        int w = getWidth(); int h = getHeight();
        if (w <= 0 || h <= 0) return;

        bgLayer.setBounds(0, 0, w, h);
        characterLayer.setBounds(0, 0, w, h);
        effectLayer.setBounds(0, 0, w, h);
        
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
    }

    public void updateScene(Object[] lineData) {
        if (lineData == null || lineData.length < 2) return; 

        choiceLayer.removeAll();
        choiceLayer.setVisible(false);
        isChoiceMode = false; 
        pendingChoices = null;

        currentSpeaker = ""; 
        Object speakerData = lineData[0];
        if (speakerData != null) {
            String speakerStr = speakerData.toString().trim();
            if (speakerStr.equalsIgnoreCase("PLAYER") || speakerStr.equals(GameConstants.PLAYER_NAME)) {
                currentSpeaker = GameConstants.PLAYER_NAME;
            } else if (!speakerStr.isEmpty() && !speakerStr.equalsIgnoreCase("none")) {
                currentSpeaker = speakerStr; 
            }
        }
        dialogueBox.setText(currentSpeaker, ""); 
        fullText = lineData[1].toString().replace("[PLAYER]", GameConstants.PLAYER_NAME);

        if (lineData.length >= 3) {
            String fileName = (String) lineData[2];
            this.currentChar = (fileName == null || fileName.isEmpty() || fileName.equals("none")) ? "" : (fileName.startsWith("model/") ? fileName : GameConstants.CHAR_PATH + fileName);
            characterLayer.updateCharacter(this.currentChar);
        }

        if (lineData.length >= 4) {
            String bgImg = (String) lineData[3];
            this.currentBG = (bgImg == null || bgImg.isEmpty() || bgImg.equals("none")) ? "" : (bgImg.startsWith("model/") ? bgImg : GameConstants.SCENE_PATH + bgImg);
        }

        boolean hasNewEffect = false;
        for (int i = 4; i < lineData.length; i++) {
            if (lineData[i] instanceof Object[][]) {
                this.pendingChoices = (Object[][]) lineData[i];
                this.isChoiceMode = true; 
            } else if (lineData[i] instanceof String) {
                String val = (String) lineData[i];
                if (val == null || val.isEmpty() || val.equalsIgnoreCase("none")) continue;

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

        // ✨ ถ้ามีเอฟเฟกต์ ให้ซ่อนกล่องไว้ก่อนจนกว่าจะเริ่มพิมพ์
        if (hasNewEffect) {
            dialogueBox.setVisible(false);
        } else {
            dialogueBox.setVisible(true);
        }

        updateUIStyles();
        this.revalidate();
        this.repaint(); 
        startTypewriter();
    }

    private void showChoices(Object[][] choices) {
        if (choices == null) return;
        isChoiceMode = true;
        choiceLayer.removeAll();
        int targetWidth = (int)(getWidth() * 0.70); 
        int fixedHeight = 50; 

        for (int i = 0; i < choices.length; i++) {
            String num = String.valueOf(i + 1);
            String text = (String) choices[i][0];
            String targetScene = (String) choices[i][1];
            ChoiceButton btn = new ChoiceButton(num, text, () -> {
                isChoiceMode = false;
                pendingChoices = null; 
                choiceLayer.removeAll();
                choiceLayer.setVisible(false);
                loadNewScene(storyMap.get(targetScene), targetScene);
            });
            btn.setMaximumSize(new Dimension(targetWidth, fixedHeight));
            btn.setPreferredSize(new Dimension(targetWidth, fixedHeight));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT); 
            choiceLayer.add(btn);
            if (i < choices.length - 1) choiceLayer.add(Box.createRigidArea(new Dimension(0, 10))); 
        }
        updateUIStyles(); 
        choiceLayer.setVisible(true);
    }

    private void handleInteraction() {
        if (effectManager != null && effectManager.isPlaying()) return; 

        if (typeTimer != null && typeTimer.isRunning()) {
            typeTimer.stop();
            charIndex = fullText.length();
            dialogueBox.setText(currentSpeaker, fullText);
            return;
        }

        int buttonCount = (choiceLayer.getComponentCount() + 1) / 2;
        if (isChoiceMode && buttonCount == 0 && pendingChoices != null) {
            showChoices(pendingChoices);
            return; 
        }

        if (isChoiceMode && buttonCount > 0) return;

        storyIndex++;
        if (storyIndex < currentSceneData.length) {
            updateScene(currentSceneData[storyIndex]);
        } else {
            handleSceneTransition();
        }
    }

    private void handleSceneTransition() {
        try {
            int currentNum = Integer.parseInt(sceneName.replace("SCENE_", ""));
            String nextSceneKey = "SCENE_" + (currentNum + 1);
            if (storyMap.containsKey(nextSceneKey)) {
                loadNewScene(storyMap.get(nextSceneKey), nextSceneKey);
            }
        } catch (Exception e) {}
    }

    public void loadNewScene(Object[][] nextSceneData, String newSceneName) {
        if (nextSceneData == null) return;
        this.currentSceneData = nextSceneData;
        this.sceneName = newSceneName;
        this.storyIndex = 0;
        updateScene(currentSceneData[storyIndex]);
    }

    private void startTypewriter() {
        charIndex = 0;
        if (typeTimer != null) typeTimer.stop();

        Timer waitTimer = new Timer(50, null);
        waitTimer.addActionListener(e -> {
            // ✨ รอจนกว่าเอฟเฟกต์จะจางจนมองเห็นได้ (Alpha < 0.3)
            if (effectManager != null && (effectManager.isPlaying() || effectManager.getAlpha() > 0.3f)) {
                return; 
            }
            waitTimer.stop();
            runActualTypewriter(); 
        });
        waitTimer.start();
    }

    private void runActualTypewriter() {
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
}