package UI_Screens;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import core.Main; 
import model.StoryData;

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

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                resetAndStart(); 
            }
            @Override
            public void componentResized(ComponentEvent e) {
                updateTextLayout();
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
        
        // แก้ไขการเข้าถึงตัวแปรใน Main (ใช้ชื่อคลาส Main ให้ถูกต้อง)
        Main.brightnessAlpha = 0.0f; 
        Main.repaintBrightness();
        
        // ✅ เปลี่ยนวิธีดึงข้อมูล: ดึงเฉพาะข้อความจาก Object[][] (index 1)
        if (StoryData.SCENE_1 != null && StoryData.SCENE_1.length > 0) {
            String initialText = (String) StoryData.SCENE_1[storyIndex][1]; 
            startTypewriter(initialText);
        }
    }

    private void handlePageClick() {
        if (typeTimer != null && typeTimer.isRunning()) {
            typeTimer.stop();
            textPane.setText(fullText); 
            charIndex = fullText.length();
            centerText(); 
        } 
        else if (fadeTimer == null || !fadeTimer.isRunning()) {
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

        int fontSize = Math.max(22, h / 28); 
        textPane.setFont(new Font("Tahoma", Font.PLAIN, fontSize));
        textPane.setPreferredSize(new Dimension((int)(w * 0.8), h / 2));
        
        revalidate();
    }

    private void startTypewriter(String text) {
        fullText = text;
        charIndex = 0;
        textAlpha = 1.0f;
        textPane.setText(""); 
        
        if (typeTimer != null) typeTimer.stop();
        if (fadeTimer != null) fadeTimer.stop();

        typeTimer = new Timer(50, e -> {
            if (charIndex < fullText.length()) {
                charIndex++;
                textPane.setText(fullText.substring(0, charIndex)); 
                centerText(); 
            } else {
                typeTimer.stop();
            }
        });
        typeTimer.start();
        repaint();
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
        // ✅ ตรวจสอบข้อมูลก่อนดึงมาแสดงผล
        if (StoryData.SCENE_1 != null && storyIndex < StoryData.SCENE_1.length) {
            String nextText = (String) StoryData.SCENE_1[storyIndex][1];
            startTypewriter(nextText);
        } else {
            textPane.setText(""); 
            // ✅ เปลี่ยนหน้าไปยังฉากเล่นเกมหลัก
            if (Main.cardLayout != null) {
                Main.cardLayout.show(Main.mainContainer, "PLAY_SCENE"); 
            }
        }
    }
}