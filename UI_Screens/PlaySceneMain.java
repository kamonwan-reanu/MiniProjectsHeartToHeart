package UI_Screens;

import UI_Components.CharacterSprite;
import UI_Components.DialogueBox;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
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
    private DialogueBox dialogueBox;     
    private Timer typeTimer;             
    private SoundManager soundManager = new SoundManager();

    public PlaySceneMain(Object[][] sceneData, String charPath, String sceneName) {
        this.currentSceneData = sceneData; 
        this.sceneName = sceneName; 

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
            public void componentShown(ComponentEvent e) { 
                if (PlaySceneMain.this.sceneName.equals("SCENE_1")) {
                    loadNewScene(StoryData.SCENE_2, "SCENE_2");
                } else {
                    updateUIStyles();
                    updateScene(currentSceneData[storyIndex]);
                }
            }
        });

        dialogueBox.setOnNextRequested(this::handleInteraction);
    }

    private void setupUIComponents() {
        bgLayer = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (currentBG != null && !currentBG.isEmpty() && !currentBG.equals("none")) {
                    Image img = new ImageIcon(currentBG).getImage();
                    if (img != null && img.getWidth(null) > 0) {
                        Graphics2D g2d = (Graphics2D) g;
                        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        int panelW = getWidth();
                        int panelH = getHeight();
                        int imgW = img.getWidth(null);
                        int imgH = img.getHeight(null);
                        double scaleX = (double) panelW / imgW;
                        double scaleY = (double) panelH / imgH;
                        double finalScale = Math.max(scaleX, scaleY) + bgScaleOffset;
                        int drawW = (int) (imgW * finalScale);
                        int drawH = (int) (imgH * finalScale);
                        int x = (panelW - drawW) / 2 + bgOffsetX;
                        int y = (panelH - drawH) / 2 + bgOffsetY;
                        g2d.drawImage(img, x, y, drawW, drawH, this);
                    }
                }
            }
        };
        
        characterLayer = new CharacterSprite(currentChar) {
            @Override
            protected void paintComponent(Graphics g) {
                if (currentChar != null && !currentChar.isEmpty() && !currentChar.equals("none")) {
                    Image img = new ImageIcon(currentChar).getImage();
                    if (img != null && img.getWidth(null) > 0) {
                        int panelH = getHeight();
                        int imgW = img.getWidth(null);
                        int imgH = img.getHeight(null);
                        double ratio = (double) panelH / imgH;
                        int drawW = (int) (imgW * ratio);
                        int drawH = panelH;
                        g.drawImage(img, (getWidth() - drawW) / 2, 0, drawW, drawH, this);
                    }
                }
            }
        };

        dialogueBox = new DialogueBox(); 
        add(dialogueBox);      
        add(characterLayer);   
        add(bgLayer);          
        
        setComponentZOrder(dialogueBox, 0);
        setComponentZOrder(characterLayer, 1);
        setComponentZOrder(bgLayer, 2);
    }

    private void updateUIStyles() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;
        bgLayer.setBounds(0, 0, w, h);
        characterLayer.setBounds(0, 0, w, h); 
        dialogueBox.setBounds(0, 0, w, h); 
        dialogueBox.updateLayout((int)(w * 0.85), (int)(h * 0.25), h); 
        revalidate();
        repaint();
    }

    private void updateScene(Object[] lineData) {
        if (lineData == null || lineData.length < 2) return; 

        currentSpeaker = ""; 
        Object speakerData = lineData[0];
        
        if (speakerData != null) {
            String speakerStr = speakerData.toString().trim();
            
            // เช็ค Keyword "PLAYER" เพื่อเอาชื่อไปใส่บนหัวกล่อง
            if (speakerStr.equalsIgnoreCase("PLAYER") || 
                speakerStr.equals("........") || 
                speakerData == GameConstants.PLAYER_NAME) {
                
                currentSpeaker = GameConstants.PLAYER_NAME;
            } 
            else if (speakerStr.isEmpty() || speakerStr.equalsIgnoreCase("none")) {
                currentSpeaker = ""; 
            } 
            else {
                currentSpeaker = speakerStr; 
            }
        }
        
        dialogueBox.setText(currentSpeaker, ""); 

        // --- จัดการเนื้อหาบทพูด ---
        fullText = (String) lineData[1];
        
        // ✨ แก้ไข: แทนที่ชื่อผู้เล่นเฉพาะเมื่อพิมพ์ [PLAYER] เท่านั้น
        // จะไม่ไปยุ่งกับเครื่องหมายจุดไข่ปลา (...) อีกแล้วค่ะ
        if (fullText.contains("[PLAYER]")) {
            fullText = fullText.replace("[PLAYER]", GameConstants.PLAYER_NAME);
        }

        // --- ส่วนจัดการรูปภาพและเสียง (คงเดิม) ---
        if (lineData.length >= 3) {
            String fileName = (String) lineData[2];
            if (fileName != null && !fileName.isEmpty() && !fileName.equals("none")) {
                this.currentChar = fileName.startsWith("model/") ? fileName : GameConstants.CHAR_PATH + fileName;
            } else if ("none".equals(fileName)) {
                this.currentChar = "";
            }
        }

        if (lineData.length >= 4) {
            String fileName = (String) lineData[3];
            if (fileName != null && !fileName.isEmpty() && !fileName.equals("none")) {
                this.currentBG = fileName.startsWith("model/") ? fileName : GameConstants.SCENE_PATH + fileName; 
            }
        }

        if (lineData.length >= 5) {
            String soundName = (String) lineData[4];
            if (soundName != null && !soundName.isEmpty() && !soundName.equals("none")) {
                String fullPath = GameConstants.SOUND_PATH + soundName + ".wav";
                if (soundName.startsWith("BGM")) soundManager.playBGM(fullPath);
                else soundManager.playSE(fullPath);
            }
        }

        repaint(); 
        startTypewriter(); 
    }

    private void handleInteraction() {
        if (typeTimer != null && typeTimer.isRunning()) {
            typeTimer.stop();
            charIndex = fullText.length();
            dialogueBox.setText(currentSpeaker, fullText);
        } else {
            storyIndex++;
            if (storyIndex == 17) { 
                startHeartMiniGame(); 
            }
            if (storyIndex < currentSceneData.length) updateScene(currentSceneData[storyIndex]);
            else handleSceneTransition();
        }
    }

    private void handleSceneTransition() {
        if (sceneName.equals("SCENE_1")) loadNewScene(StoryData.SCENE_2, "SCENE_2");
        else if (sceneName.equals("SCENE_2")) loadNewScene(StoryData.SCENE_3, "SCENE_3");
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
            } else typeTimer.stop();
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