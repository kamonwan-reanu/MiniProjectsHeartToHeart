package UI_Screens;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import UI_Components.DialogueBox; 
import UI_Components.CharacterSprite; 
import model.GameConstants; 

public class PlaySceneMain extends JPanel {
    private Object[][] currentSceneData; 
    private String currentBG = "";        
    private String currentChar;          
    private String sceneName = ""; 
    
    private int storyIndex = 0;          
    private String fullText = "";        
    private int charIndex = 0;           
    private String currentSpeaker = "";   
    
    private JLabel bgLayer;              
    private CharacterSprite characterLayer; 
    private DialogueBox dialogueBox;     
    private Timer typeTimer;             

    public PlaySceneMain(Object[][] sceneData, String charPath, String sceneName) {
        this.currentSceneData = sceneData; 
        this.currentChar = charPath;
        this.sceneName = sceneName; 

        setLayout(null);
        setOpaque(true);
        setBackground(Color.BLACK); 
        
        setupUIComponents(); 
        
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateUIStyles(); 
            }
        });

        dialogueBox.setOnNextRequested(this::handleInteraction);

        if (currentSceneData != null && currentSceneData.length > 0) {
            updateScene(currentSceneData[storyIndex]);
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleInteraction(); 
            }
        });
    }

    // ✅ ระบบโหลดฉากใหม่ที่คลีนขึ้น
    public void loadNewScene(Object[][] nextSceneData, String newSceneName) {
        if (nextSceneData == null || nextSceneData.length == 0) {
            core.Main.showScreen("MENU");
            return;
        }
        this.currentSceneData = nextSceneData;
        this.sceneName = newSceneName; 
        this.storyIndex = 0;
        updateScene(currentSceneData[storyIndex]);
        revalidate();
        repaint();
    }

    private void setupUIComponents() {
        bgLayer = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (currentBG == null || currentBG.isEmpty() || currentBG.equals("none")) {
                    g.setColor(Color.BLACK);
                    g.fillRect(0, 0, getWidth(), getHeight());
                    return;
                }
                try {
                    ImageIcon icon = new ImageIcon(currentBG);
                    Image img = icon.getImage();
                    if (img != null && img.getWidth(null) != -1) {
                        float scale = Math.max((float)getWidth() / img.getWidth(this), (float)getHeight() / img.getHeight(this));
                        int drawW = (int)(img.getWidth(this) * scale);
                        int drawH = (int)(img.getHeight(this) * scale);
                        g.drawImage(img, (getWidth() - drawW) / 2, (getHeight() - drawH) / 2, drawW, drawH, this);
                    }
                } catch (Exception e) {
                    System.err.println("Error rendering background: " + currentBG);
                }
            }
        };
        
        characterLayer = new CharacterSprite(currentChar); 
        dialogueBox = new DialogueBox(); 

        add(dialogueBox);     
        add(characterLayer);  
        add(bgLayer);         
    }

    private void updateUIStyles() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        bgLayer.setBounds(0, 0, w, h);
        characterLayer.setBounds(0, 0, w, h);

        int groupW = (int)(w * 0.85); 
        int groupH = (int)(h * 0.25); 
        int groupX = (w - groupW) / 2;
        int marginBottom = (h <= 600) ? 25 : 45;
        int groupY = h - groupH - marginBottom; 

        dialogueBox.setBounds(groupX, groupY, groupW, groupH);
        dialogueBox.updateLayout(groupW, groupH, h); 

        if (fullText != null && !fullText.isEmpty()) {
            dialogueBox.setText(currentSpeaker, fullText.substring(0, Math.min(charIndex, fullText.length())));
        }
    }

    // ✅ ปรับปรุง handleInteraction ให้เหลือแค่ Logic พื้นฐาน
    private void handleInteraction() {
        if (typeTimer != null && typeTimer.isRunning()) {
            typeTimer.stop();
            charIndex = fullText.length();
            dialogueBox.setText(currentSpeaker, fullText);
        } else {
            storyIndex++;
            if (storyIndex < currentSceneData.length) {
                updateScene(currentSceneData[storyIndex]);
            } else {
                // ✅ แก้ไขตรงนี้: สั่งให้ไปฉากถัดไปแทนการกลับเมนู
                System.out.println("Finished " + sceneName + ", moving to next scene...");
                
                if (sceneName.equals("SCENE_1")) {
                    loadNewScene(model.StoryData.SCENE_2, "SCENE_2");
                } else if (sceneName.equals("SCENE_2")) {
                    loadNewScene(model.StoryData.SCENE_3, "SCENE_3");
                } else if (sceneName.equals("SCENE_3")) {
                    loadNewScene(model.StoryData.SCENE_4, "SCENE_4");
                } else {
                    // ถ้าจบ SCENE_4 (ฉากสุดท้าย) แล้วจริงๆ ค่อยกลับเมนูค่ะ
                    //core.Main.showScreen("MENU"); 
                }
            }
        }
    }

    private void updateScene(Object[] lineData) {
        if (lineData == null || lineData.length < 2) return; 

        currentSpeaker = (String) lineData[0]; 
        fullText = (String) lineData[1];

        if (lineData.length >= 3) {
            String charImage = (String) lineData[2];
            if (characterLayer != null) characterLayer.updateCharacter(charImage);
        }

        if (lineData.length >= 4) {
            String bgImage = (String) lineData[3];
            if (bgImage != null && !bgImage.equals(currentBG)) {
                currentBG = bgImage;
                repaint(); 
            }
        }

        charIndex = 0;
        if (typeTimer != null) typeTimer.stop();
        dialogueBox.setText(currentSpeaker, ""); 

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