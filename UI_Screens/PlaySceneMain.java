package UI_Screens;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import UI_Components.DialogueBox; 
import UI_Components.CharacterSprite;
import UI_Components.EffectManager; 
import model.GameConstants; 
import model.SoundManager; 
import model.StoryData; 

public class PlaySceneMain extends JPanel {
    private Object[][] currentSceneData; 
    private String currentBG = "";                  
    private String currentChar = "";                    
    private String sceneName = ""; 
    
    private double bgScaleOffset = 0.0; 
    private int bgOffsetX = 0;          
    private int bgOffsetY = 0;          

    private int storyIndex = 0;                    
    private String fullText = "";                
    private int charIndex = 0;                    
    private String currentSpeaker = "";   
    
    private JLabel bgLayer;                        
    private CharacterSprite characterLayer; 
    private JPanel effectLayer; 
    private DialogueBox dialogueBox;     
    private Timer typeTimer;             
    private SoundManager soundManager = new SoundManager();
    private EffectManager effectManager;

    public PlaySceneMain(Object[][] sceneData, String charPath, String sceneName) {
        this.currentSceneData = sceneData; 
        this.sceneName = sceneName;
        
        // ✨ แก้ไขจุดที่ 1: สร้าง dialogueBox ก่อน เพื่อส่งให้ EffectManager
        this.dialogueBox = new DialogueBox(); 
        this.effectManager = new EffectManager(this, dialogueBox); 

        if (charPath != null && !charPath.isEmpty() && !charPath.equalsIgnoreCase("none")) {
            this.currentChar = charPath.startsWith("model/") ? charPath : GameConstants.CHAR_PATH + charPath;
        }

        setLayout(null);
        setOpaque(true);
        setBackground(Color.BLACK);

        setupUIComponents();

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) { updateUIStyles(); }
            @Override
            public void componentShown(ComponentEvent e) { updateUIStyles(); }
        });

        dialogueBox.setOnNextRequested(this::handleInteraction);
    }

    private void setupUIComponents() {
        bgLayer = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.translate(effectManager.getShakeX(), effectManager.getShakeY());
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
                g2d.translate(effectManager.getShakeX(), effectManager.getShakeY());
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

        // dialogueBox สร้างไปแล้วใน Constructor
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
        dialogueBox.setBounds(0, 0, w, h); 
        dialogueBox.updateLayout((int)(w * 0.85), (int)(h * 0.25), h); 
        revalidate(); repaint();
    }

    public void updateScene(Object[] lineData) {
        if (lineData == null || lineData.length < 2) return; 

        // หยุด Effect เก่า (ถ้าไม่ได้กำลังเล่นอยู่)
        if (!effectManager.isPlaying()) effectManager.stopAll(); 

        // ✨ แก้ไขจุดที่ 2: สั่งเล่น Effect ทันทีที่เปลี่ยนบรรทัด
        checkAndPlayEffect();

        currentSpeaker = ""; 
        Object speakerData = lineData[0];
        if (speakerData != null) {
            String speakerStr = speakerData.toString().trim();
            if (speakerStr.equalsIgnoreCase("PLAYER") || speakerStr.equals("........") || speakerData == GameConstants.PLAYER_NAME) {
                currentSpeaker = GameConstants.PLAYER_NAME;
            } else if (speakerStr.isEmpty() || speakerStr.equalsIgnoreCase("none")) {
                currentSpeaker = ""; 
            } else {
                currentSpeaker = speakerStr; 
            }
        }
        dialogueBox.setText(currentSpeaker, ""); 

        fullText = (String) lineData[1];
        if (fullText.contains("[PLAYER]")) {
            fullText = fullText.replace("[PLAYER]", GameConstants.PLAYER_NAME);
        }

        // 1. จัดการรูปตัวละคร
        if (lineData.length >= 3) {
            String fileName = (String) lineData[2];
            this.currentChar = (fileName == null || fileName.isEmpty() || fileName.equals("none")) ? "" : (fileName.startsWith("model/") ? fileName : GameConstants.CHAR_PATH + fileName);
        }

        // 2. จัดการรูปพื้นหลัง
        if (lineData.length >= 4) {
            String fileName = (String) lineData[3];
            if (fileName != null && !fileName.isEmpty() && !fileName.equals("none")) {
                this.currentBG = fileName.startsWith("model/") ? fileName : GameConstants.SCENE_PATH + fileName; 
            }
        }

        if (lineData.length >= 5) {
            String soundName = (String) lineData[4];
            if (soundName != null && !soundName.isEmpty() && !soundName.equalsIgnoreCase("none")) {
                String fullPath = GameConstants.SOUND_PATH + soundName + ".wav";
                if (soundName.startsWith("BGM")) soundManager.playBGM(fullPath);
                else soundManager.playSE(fullPath);
            }
        }

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
            if (hasEffect) return; 
            
            typeTimer.stop();
            charIndex = fullText.length();
            dialogueBox.setText(currentSpeaker, fullText);
        } else {
            storyIndex++;
            if (storyIndex < currentSceneData.length) updateScene(currentSceneData[storyIndex]);
            else handleSceneTransition();
        }
    }

    private void handleSceneTransition() {
        if (sceneName.equals("SCENE_2")) loadNewScene(StoryData.SCENE_3, "SCENE_3");
        else if (sceneName.equals("SCENE_3")) loadNewScene(StoryData.SCENE_4, "SCENE_4");
    }

    public void loadNewScene(Object[][] nextSceneData, String newSceneName) {
        if (nextSceneData == null || nextSceneData.length == 0) return;
        this.currentSceneData = nextSceneData;
        this.sceneName = newSceneName; 
        this.storyIndex = 0;
        updateScene(currentSceneData[storyIndex]);
        updateUIStyles();
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

    private void showSpiritChoices(Object[][] choices) {
        int w = getWidth();
        int h = getHeight();
        int dialogW = (int)(w * 0.85);
        int dialogH = (int)(h * 0.22);
        
        // ขยับกล่องข้อความขึ้นบน (Y = 15% ของความสูงจอ)
        int targetY = (int)(h * 0.15); 
        dialogueBox.moveTo((w - dialogW) / 2, targetY, dialogW, dialogH);

        JPanel choiceOverlay = new JPanel(new GridBagLayout());
        choiceOverlay.setOpaque(false);
        
        // Overlay เริ่มต้นใต้กล่องข้อความ
        int overlayY = targetY + dialogH + 50;
        choiceOverlay.setBounds(0, overlayY, w, h - overlayY); 

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; 
        gbc.insets = new Insets(15, 0, 15, 0); 
        gbc.fill = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < choices.length; i++) {
            final int index = i;
            String text = (String) choices[i][0];
            String target = (String) choices[i][1];

            JButton btn = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(20, 20, 35, 230));
                    g2.fillRect(70, 0, getWidth() - 140, getHeight());
                    g2.setColor(new Color(255, 105, 180));
                    g2.fillRoundRect(0, 5, 60, getHeight() - 10, 10, 10);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Tahoma", Font.BOLD, 32));
                    g2.drawString(String.valueOf(index + 1), 20, getHeight() / 2 + 12);
                    g2.setFont(new Font("Tahoma", Font.PLAIN, 28));
                    g2.drawString(text, 100, getHeight() / 2 + 10);
                    g2.dispose();
                }
            };

            btn.setPreferredSize(new Dimension(1300, 85));
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusable(false);

            btn.addActionListener(e -> {
                remove(choiceOverlay);
                // คืนค่าตำแหน่งกล่องข้อความลงด้านล่าง
                dialogueBox.updateLayout(dialogW, dialogH, h);
                
                Object[][] nextData = getSceneDataByName(target); 
                if (nextData != null) loadNewScene(nextData, target);
                
                revalidate(); repaint();
            });
            choiceOverlay.add(btn, gbc);
        }

        add(choiceOverlay, 0); 
        setComponentZOrder(dialogueBox, 0); 
        setComponentZOrder(choiceOverlay, 1);
        revalidate(); repaint();
    }

    private Object[][] getSceneDataByName(String name) {
        if (name == null) return null;
        switch (name) {
            case "SCENE_1": return model.StoryData.SCENE_1;
            case "SCENE_2": return model.StoryData.SCENE_2;
            case "SCENE_3": return model.StoryData.SCENE_3;
            case "SCENE_4": return model.StoryData.SCENE_4;
            default:
                System.err.println("หาฉากไม่เจอ: " + name);
                return null;
        }
    }
}