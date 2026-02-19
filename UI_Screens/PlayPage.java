package UI_Screens;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import core.Main; 
import model.StoryData;
import model.GameConstants;

public class PlayPage extends JPanel {
    private int storyIndex = 0;
    private String fullText = "";
    private int charIndex = 0;
    private float textAlpha = 1.0f;
    
    private JTextPane textPane; 
    private Timer typeTimer;
    private Timer fadeTimer;

    public PlayPage(Font tFont) {
        setLayout(new GridBagLayout());
        setBackground(Color.BLACK); 

        textPane = new JTextPane() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, textAlpha));
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                super.paintComponent(g2d);
                g2d.dispose();
            }
        };
        
        textPane.setEditable(false);
        textPane.setFocusable(false);
        textPane.setOpaque(false);
        textPane.setForeground(Color.WHITE);
        textPane.setHighlighter(null); 
        textPane.setFont(new Font("Tahoma", Font.PLAIN, 32));

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                // 🛠️ แก้ปัญหาจอดำสนิท: เมื่อหน้านี้ปรากฏ ต้องสั่งเปิดม่าน GlassPane ของ Main ออก
                Main.brightnessAlpha = 0.0f;
                Main.repaintBrightness();
                
                updateTextLayout();
                resetAndStart();
            }
        });

        centerText();
        
        MouseAdapter clickAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handlePageClick(); 
            }
        };
        textPane.addMouseListener(clickAdapter);
        addMouseListener(clickAdapter);

        add(textPane);
    }

    private void resetAndStart() {
        storyIndex = 0;
        textAlpha = 1.0f;
        textPane.setText("");
        
        if (StoryData.SCENE_1 != null && StoryData.SCENE_1.length > 0) {
            String initialText = (String) StoryData.SCENE_1[storyIndex][1]; 
            startTypewriter(initialText);
        }
    }

    private void startTypewriter(String text) {
        fullText = text.replace("[PLAYER]", GameConstants.PLAYER_NAME);
        charIndex = 0;
        textAlpha = 1.0f; 
        textPane.setText(""); 
        
        if (typeTimer != null) typeTimer.stop();
        typeTimer = new Timer(GameConstants.TYPEWRITER_SPEED, e -> {
            if (charIndex < fullText.length()) {
                charIndex++;
                textPane.setText(fullText.substring(0, charIndex)); 
                centerText(); 
            } else {
                typeTimer.stop();
            }
        });
        typeTimer.start();
    }

    private void handlePageClick() {
        if (typeTimer != null && typeTimer.isRunning()) {
            typeTimer.stop();
            textPane.setText(fullText); 
            charIndex = fullText.length();
            centerText(); 
        } else if (fadeTimer == null || !fadeTimer.isRunning()) {
            startFadeOutNext();
        }
    }

    private void centerText() {
        StyledDocument doc = textPane.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
    }

    private void updateTextLayout() {
        int w = getWidth(); 
        int h = getHeight();
        if (w <= 0 || h <= 0) return;
        textPane.setPreferredSize(new Dimension((int)(w * 0.8), h / 2));
        textPane.setFont(new Font("Tahoma", Font.PLAIN, Math.max(24, h / 30)));
        revalidate();
    }

    private void startFadeOutNext() {
        if (fadeTimer != null) fadeTimer.stop();
        fadeTimer = new Timer(30, e -> {
            textAlpha -= 0.15f; 
            if (textAlpha <= 0.0f) {
                textAlpha = 0.0f;
                fadeTimer.stop();
                storyIndex++; 
                goToNextSentence();
            }
            repaint();
        });
        fadeTimer.start();
    }

    private void goToNextSentence() {
        if (StoryData.SCENE_1 != null && storyIndex < StoryData.SCENE_1.length) {
            startTypewriter((String) StoryData.SCENE_1[storyIndex][1]);
        } else {
            // 🛠️ แก้ปัญหาไม่ไป SCENE_1: 
            // 1. เปิดม่านดำออกก่อนเปลี่ยนหน้า
            Main.brightnessAlpha = 0.0f;
            Main.repaintBrightness();
            
            // 2. สั่งเปลี่ยนหน้า
            if (Main.cardLayout != null) {
                Main.cardLayout.show(Main.mainContainer, "PLAY_SCENE"); 
            }
        }
    }
}