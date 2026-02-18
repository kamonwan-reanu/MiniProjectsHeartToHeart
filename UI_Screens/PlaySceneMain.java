package UI_Screens;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import UI_Components.DialogueBox; 
import UI_Components.CharacterSprite; 
import model.GameConstants; 

public class PlaySceneMain extends JPanel {
    // ✅ เปลี่ยนจาก String[] เป็น Object[][] เพื่อเก็บ {ชื่อ, บทพูด, ไฟล์ภาพ}
    private Object[][] currentSceneData; 
    private String currentBG;          
    private String currentChar;        
    
    private int storyIndex = 0;
    private String fullText = "";
    private int charIndex = 0;
    private String currentSpeaker = "อวี่เชิน"; // เก็บชื่อคนพูดปัจจุบัน
    
    private JLabel bgLayer;
    private CharacterSprite characterLayer; 
    private DialogueBox dialogueBox; 
    private Timer typeTimer;

    // ✅ ปรับ Constructor ให้รับ Object[][]
    public PlaySceneMain(Object[][] sceneData, String bgPath, String charPath) {
        this.currentSceneData = sceneData;
        this.currentBG = bgPath;
        this.currentChar = charPath;

        setLayout(null);
        setOpaque(true);
        setupUIComponents();
        
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateUIStyles(); 
            }
        });

        dialogueBox.setOnNextRequested(() -> {
            handleInteraction();
        });

        if (currentSceneData != null && currentSceneData.length > 0) {
            // ✅ เริ่มฉากแรกด้วยข้อมูลชุดแรก
            updateScene(currentSceneData[storyIndex]);
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleInteraction(); 
            }
        });
    }

    private void setupUIComponents() {
        bgLayer = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                try {
                    java.net.URL imgURL = getClass().getClassLoader().getResource(currentBG);
                    if (imgURL != null) {
                        Image img = new ImageIcon(imgURL).getImage();
                        float scale = Math.max((float)getWidth() / img.getWidth(this), (float)getHeight() / img.getHeight(this));
                        int drawW = (int)(img.getWidth(this) * scale);
                        int drawH = (int)(img.getHeight(this) * scale);
                        g.drawImage(img, (getWidth() - drawW) / 2, (getHeight() - drawH) / 2, drawW, drawH, this);
                    }
                } catch (Exception e) {}
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
            dialogueBox.setText(currentSpeaker, fullText.substring(0, charIndex));
        }

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
                // ✅ ส่งข้อมูลแถวถัดไปไปอัปเดตหน้าจอ
                updateScene(currentSceneData[storyIndex]);
            } else {
                System.out.println("จบฉากแล้วจ้า Ahri!");
            }
        }
    }

    // ✅ เมธอดใหม่: อัปเดตทั้ง ชื่อ, ข้อความ และสีหน้าตัวละครพร้อมกัน
    private void updateScene(Object[] lineData) {
        currentSpeaker = (String) lineData[0];
        fullText = (String) lineData[1];
        String charImage = (String) lineData[2];

        // 1. เปลี่ยนรูปตัวละคร (ต้องมั่นใจว่าใน CharacterSprite มีเมธอด updateCharacter นะคะ)
        if (characterLayer != null) {
            characterLayer.updateCharacter(charImage);
        }

        // 2. เริ่มตัวหนังสือวิ่ง
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