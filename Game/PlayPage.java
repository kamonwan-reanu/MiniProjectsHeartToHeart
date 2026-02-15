import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PlayPage extends JPanel {
    private int storyIndex = 0;
    private String fullText = "";
    private int charIndex = 0;
    private float textAlpha = 1.0f;
    
    private JTextPane textPane; 
    private Timer typeTimer;
    private Timer fadeTimer;

    public PlayPage(Font tFont) {
        // ใช้ GridBagLayout เพื่อดึงทุกอย่างมากองไว้กึ่งกลางจอ
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
        textPane.setHighlighter(null); // ปิดการคลุมดำเพื่อให้เมาส์ไม่โดนดัก

        // จัดข้อความภายในให้กึ่งกลาง (Center Alignment)
        centerText();
        
        // --- [แก้จุดที่ 1: ทำให้ข้อความรับการคลิกเหมือนพื้นหลัง] ---
        textPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handlePageClick(); // คลิกที่ตัวหนังสือก็ทำงาน
            }
        });

        add(textPane);

        // --- [แก้จุดที่ 2: ทำให้พื้นหลังจอดำรับการคลิกได้] ---
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handlePageClick(); // คลิกที่ช่องว่างก็ทำงาน
            }
        });

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateTextLayout();
            }
        });

        if (StoryData.SCENE_1.length > 0) {
            startTypewriter(StoryData.SCENE_1[storyIndex]);
        }
    }

    // ฟังก์ชันจัดการการคลิก (รวมศูนย์ไว้ที่เดียว)
    private void handlePageClick() {
        if (typeTimer != null && typeTimer.isRunning()) {
            typeTimer.stop();
            textPane.setText(fullText); 
            charIndex = fullText.length();
            centerText(); // จัดกึ่งกลางหลังพิมพ์เสร็จทันที
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

        // ฟอนต์ Responsive ตามความสูงจอ
        int fontSize = Math.max(22, h / 28); 
        textPane.setFont(new Font("Tahoma", Font.PLAIN, fontSize));
        
        // ล็อคขนาดพื้นที่เพื่อความนิ่ง
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
                centerText(); // บังคับกึ่งกลางทุกตัวอักษรที่พิมพ์
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
        if (storyIndex < StoryData.SCENE_1.length) {
            startTypewriter(StoryData.SCENE_1[storyIndex]);
        } else {
            textPane.setText(""); 
            Main.cardLayout.show(Main.mainContainer, "GAME_PLAY"); 
        }
    }
}