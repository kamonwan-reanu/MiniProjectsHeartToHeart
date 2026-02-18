package UI_Screens;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import UI_Components.DialogueBox;
import UI_Components.CharacterSprite;
import model.GameConstants;
import model.StoryData;

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

        dialogueBox.setOnNextRequested(this::handleInteraction);

        if (currentSceneData != null && currentSceneData.length > 0) {
            updateScene(currentSceneData[storyIndex]);
        }
    }

    public void loadNewScene(Object[][] nextSceneData, String newSceneName) {
        if (nextSceneData == null || nextSceneData.length == 0) return;
        
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

        // ลำดับชั้น: DialogueBox (Index 0) อยู่หน้าสุด
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
        dialogueBox.setBounds(0, 0, w, h);

        int groupW = (int) (w * 0.85);
        int groupH = (int) (h * 0.25);
        
        // ถ้าอยู่ในหน้าปกติ (ไม่ใช่หน้าตัวเลือก) ให้วางไว้ข้างล่าง
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
            loadNewScene(StoryData.SCENE_2, "SCENE_2");
        } else {
            System.out.println("จบเนื้อเรื่องบทนี้แล้ว");
        }
    }

    private void updateScene(Object[] lineData) {
        if (lineData == null || lineData.length < 2) return;

        currentSpeaker = (String) lineData[0];
        fullText = (String) lineData[1];

        // 1. จัดการรูปตัวละคร
        if (lineData.length >= 3) {
            String fileName = (String) lineData[2];
            if (fileName != null && !fileName.isEmpty() && !fileName.equals("none")) {
                String fullCharPath = fileName.startsWith("model/") ? fileName : GameConstants.CHAR_PATH + fileName;
                characterLayer.updateCharacter(fullCharPath);
            } else if (fileName != null && fileName.equals("none")) {
                characterLayer.updateCharacter("none");
            }
        }

        // 2. จัดการรูปพื้นหลัง
        if (lineData.length >= 4) {
            String fileName = (String) lineData[3];
            if (fileName != null && !fileName.isEmpty() && !fileName.equals("none")) {
                this.currentBG = fileName.startsWith("model/") ? fileName : GameConstants.SCENE_PATH + fileName;
                bgLayer.repaint();
            }
        }

        // 3. เช็คว่ามีตัวเลือกไหม (Index 4)
        if (lineData.length >= 5 && lineData[4] != null) {
            showSpiritChoices((Object[][]) lineData[4]);
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