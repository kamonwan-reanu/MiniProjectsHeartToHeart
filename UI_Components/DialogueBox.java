package UI_Components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DialogueBox extends JPanel {
    private JLabel nameLabel;
    private JTextArea speechText; 
    private JPanel whiteBox;
    private JPanel nameTagBackground;
    
    // ✅ 1. เพิ่มตัวแปรสำหรับรับคำสั่งไปต่อเนื้อเรื่อง
    private Runnable onNextRequested;

    public DialogueBox() {
        setLayout(null);
        setOpaque(false);
        initUI();
    }

    // ✅ 2. เพิ่ม Method สำหรับให้ PlaySceneMain ส่ง Action มาฝากไว้
    public void setOnNextRequested(Runnable action) {
        this.onNextRequested = action;
    }

    private void initUI() {
        // 1. ป้ายชื่อ
        nameLabel = new JLabel("", SwingConstants.CENTER);
        nameLabel.setForeground(new Color(255, 215, 0)); 
        nameLabel.setFont(new Font("Tahoma", Font.BOLD, 18)); 
        
        nameTagBackground = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(40, 40, 40, 180)); 
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
            }
        };
        nameTagBackground.setLayout(new BorderLayout());
        nameTagBackground.setOpaque(false);
        nameTagBackground.add(nameLabel, BorderLayout.CENTER);

        // 2. กล่องขาว
        whiteBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 240));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2d.setColor(new Color(212, 175, 55));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 25, 25);
                g2d.dispose();
            }
        };
        whiteBox.setLayout(null);
        whiteBox.setOpaque(false);

        // 3. พื้นที่ข้อความ
        speechText = new JTextArea("");
        speechText.setEditable(false);
        speechText.setOpaque(false);
        speechText.setFocusable(false);
        speechText.setLineWrap(true);       
        speechText.setWrapStyleWord(true); 
        speechText.setMargin(new Insets(25, 45, 25, 45));
        speechText.setFont(new Font("Tahoma", Font.PLAIN, 28));

        speechText.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                whiteBox.dispatchEvent(e);
            }
        });

        whiteBox.add(speechText);
        add(nameTagBackground); 
        add(whiteBox);

        setupClickHandler();
    }

    private void setupClickHandler() {
        MouseAdapter listener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("Ahri กดที่กล่องข้อความแล้ว!");
                
                // ✅ 3. ถ้ามีคำสั่งที่ฝากไว้ (action) ให้เรียกทำงานทันทีเพื่อไปต่อ
                if (onNextRequested != null) {
                    onNextRequested.run();
                }
            }
        };

        this.addMouseListener(listener);
        whiteBox.addMouseListener(listener);
    }

    public void updateLayout(int groupW, int groupH, int h) {
        if (nameLabel == null || whiteBox == null || speechText == null) return;

        int nameW = 160; 
        int nameH = 38;  
        int nameX = 40;  
        nameTagBackground.setBounds(nameX, 0, nameW, nameH);

        int boxY = (int)(nameH * 0.85); 
        whiteBox.setBounds(0, boxY, groupW, groupH - boxY);
        
        speechText.setBounds(0, 0, whiteBox.getWidth(), whiteBox.getHeight());
    }

    public void setText(String name, String text) {
        if (nameLabel != null) nameLabel.setText(name);
        if (speechText != null) speechText.setText(text);
    }
}