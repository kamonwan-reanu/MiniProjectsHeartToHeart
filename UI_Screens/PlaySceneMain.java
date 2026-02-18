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
        this.sceneName = sceneName; 

        if (charPath != null && !charPath.startsWith("model/")) {
            this.currentChar = GameConstants.CHAR_PATH + charPath;
        } else {
            this.currentChar = charPath;
        }

        setLayout(null);
        setOpaque(true);
        setBackground(Color.BLACK); 
        
        setupUIComponents(); 
        
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateUIStyles(); 
            }
            @Override
            public void componentShown(ComponentEvent e) {
                updateUIStyles(); 
            }
        });

        // ✅ ตัวรับคลิกหลัก ย้ายไปจัดการใน DialogueBox ที่กางเต็มจอแล้ว
        dialogueBox.setOnNextRequested(this::handleInteraction);

        if (currentSceneData != null && currentSceneData.length > 0) {
            updateScene(currentSceneData[storyIndex]);
        }
    }

    public void loadNewScene(Object[][] nextSceneData, String newSceneName) {
        if (nextSceneData == null || nextSceneData.length == 0) {
            return;
        }
        this.currentSceneData = nextSceneData;
        this.sceneName = newSceneName; 
        this.storyIndex = 0;
        updateScene(currentSceneData[storyIndex]);
        
        updateUIStyles();
        revalidate();
        repaint();
    }

    private void setupUIComponents() {
        bgLayer = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (currentBG != null && !currentBG.isEmpty() && !currentBG.equals("none")) {
                    ImageIcon icon = new ImageIcon(currentBG);
                    Image img = icon.getImage();
                    if (img != null && img.getWidth(null) > 0) {
                        g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                    }
                }
            }
        };
        
        characterLayer = new CharacterSprite(currentChar); 
        dialogueBox = new DialogueBox(); 

        // ✅ ลำดับชั้น (Z-Order): ตัวรับคลิก (DialogueBox) ต้องอยู่หน้าสุด (Index 0)
        add(dialogueBox);     
        add(characterLayer);  
        add(bgLayer);         
    }

    private void updateUIStyles() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        // พื้นหลังและตัวละครขยายเต็ม Panel
        bgLayer.setBounds(0, 0, w, h);
        characterLayer.setBounds(0, 0, w, h);

        // ✅ หัวใจสำคัญ: กาง DialogueBox ให้เต็มหน้าจอเพื่อดักจับ Mouse Event ทุกจุด
        dialogueBox.setBounds(0, 0, w, h); 

        // คำนวณขนาดกล่องคำพูด (ที่อยู่ข้างใน DialogueBox อีกที)
        int groupW = (int)(w * 0.85); 
        int groupH = (int)(h * 0.25); 
        
        // ส่งค่า h เพื่อให้ DialogueBox ไปวางกล่องไว้ข้างล่างสุดของจอ
        dialogueBox.updateLayout(groupW, groupH, h); 
        
        revalidate();
        repaint();
    }

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
                handleSceneTransition();
            }
        }
    }

    private void handleSceneTransition() {
        if (sceneName.equals("SCENE_1")) {
            loadNewScene(model.StoryData.SCENE_2, "SCENE_2");
        } else if (sceneName.equals("SCENE_2")) {
            loadNewScene(model.StoryData.SCENE_3, "SCENE_3");
        } else if (sceneName.equals("SCENE_3")) {
            loadNewScene(model.StoryData.SCENE_4, "SCENE_4");
        }
    }

    private void updateScene(Object[] lineData) {
        if (lineData == null || lineData.length < 2) return; 

        currentSpeaker = (String) lineData[0]; 
        fullText = (String) lineData[1];

        if (lineData.length >= 3) {
            String fileName = (String) lineData[2];
            if (fileName != null && !fileName.isEmpty()) {
                String fullCharPath = fileName.startsWith("model/") ? fileName : GameConstants.CHAR_PATH + fileName;
                characterLayer.updateCharacter(fullCharPath);
            }
        }

        if (lineData.length >= 4) {
            String fileName = (String) lineData[3];
            if (fileName != null && !fileName.isEmpty() && !fileName.equals("none")) {
                this.currentBG = fileName.startsWith("model/") ? fileName : GameConstants.SCENE_PATH + fileName; 
                bgLayer.repaint(); 
            }
        }
        startTypewriter();
    }

    private void startTypewriter() {
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