package UI_Components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DialogueBox extends JPanel {
    private JLabel nameLabel;
    private JTextArea speechText; 
    private JPanel mainBox;
    private JPanel nextButton;
    private Runnable onNextRequested;

    public DialogueBox() {
        setLayout(null);
        setOpaque(false);
        initUI();
        setupClickHandler();
    }

    public void setOnNextRequested(Runnable action) {
        this.onNextRequested = action;
    }

    private void initUI() {
        mainBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // สีพื้นหลังกล่อง (น้ำเงินเข้มโปร่งแสง)
                g2d.setColor(new Color(15, 20, 35, 220)); 
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // เส้นขอบสีทอง
                g2d.setColor(new Color(212, 175, 55, 150));
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawRect(8, 8, getWidth() - 16, getHeight() - 16);
                g2d.dispose();
            }
        };
        mainBox.setLayout(null);
        mainBox.setOpaque(false);

        nameLabel = new JLabel("");
        nameLabel.setForeground(new Color(100, 210, 255)); 
        nameLabel.setFont(new Font("Tahoma", Font.BOLD, 22));
        nameLabel.setVisible(false);

        speechText = new JTextArea("");
        speechText.setEditable(false);
        speechText.setOpaque(false);
        speechText.setFocusable(false);
        speechText.setLineWrap(true);       
        speechText.setWrapStyleWord(true); 
        speechText.setForeground(new Color(245, 245, 245)); 
        speechText.setFont(new Font("Tahoma", Font.PLAIN, 24));

        nextButton = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(150, 230, 255, 200)); 
                g2d.fillOval(0, 0, getWidth(), getHeight());
                g2d.setColor(Color.BLACK);
                g2d.fillPolygon(new int[]{15, 15, 25}, new int[]{12, 28, 20}, 3);
                g2d.dispose();
            }
        };

        mainBox.add(nameLabel);
        mainBox.add(speechText);
        mainBox.add(nextButton);
        add(mainBox);
    }

    private void setupClickHandler() {
        MouseAdapter listener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { 
                if (onNextRequested != null) {
                    onNextRequested.run();
                }
            }
        };
        this.addMouseListener(listener);
        mainBox.addMouseListener(listener);
        speechText.addMouseListener(listener);
        nextButton.addMouseListener(listener);
    }

    // ✅ เมธอดสำหรับจัดตำแหน่งปกติ (อยู่ด้านล่าง)
    public void updateLayout(int groupW, int groupH, int h) {
        if (groupW <= 0 || groupH <= 0) return;
        
        // บังคับให้ DialogueBox (ตัวแม่) คลุมเต็มจอก่อนเพื่อให้ดักคลิกได้ทั่ว
        this.setBounds(0, 0, getParent().getWidth(), h);
        
        int x = (getWidth() - groupW) / 2;
        int y = h - groupH - 45; 
        
        mainBox.setBounds(x, y, groupW, groupH);
        updateInsideLayout(groupW, groupH);
        revalidate();
        repaint();
    }

    // ✅ เมธอดสำหรับ "วาร์ป" ไปตำแหน่งที่ต้องการ (เช่น ตอนมีตัวเลือก)
    public void moveTo(int x, int y, int width, int height) {
        // x, y ที่ส่งมาคือตำแหน่งของตัวกล่อง mainBox
        int parentW = getParent().getWidth();
        int parentH = getParent().getHeight();
        
        this.setBounds(0, 0, parentW, parentH); // ตัวแม่ยังคงเต็มจอ
        mainBox.setBounds(x, y, width, height); // ตัวกล่องขยับไปตามพิกัดที่สั่ง
        
        updateInsideLayout(width, height); 
        revalidate();
        repaint();
    }

    // ✅ เมธอดรวมศูนย์สำหรับจัดวางองค์ประกอบภายในกล่อง
    private void updateInsideLayout(int width, int height) {
        if (nameLabel != null && speechText != null && nextButton != null) {
            if (nameLabel.isVisible()) {
                nameLabel.setBounds(45, 20, 400, 35);
                speechText.setBounds(45, 60, width - 90, height - 85);
            } else {
                speechText.setBounds(45, 40, width - 90, height - 80);
            }
            nextButton.setBounds(width - 65, height - 60, 40, 40);
        }
    }

    public void setText(String name, String text) {
        speechText.setText(text);
        if (name != null && !name.isEmpty() && !name.equals("narrator")) {
            nameLabel.setText(name.toUpperCase());
            nameLabel.setVisible(true);
        } else {
            nameLabel.setVisible(false);
        }
        
        // ตรวจสอบว่าถ้าไม่ได้เรียก moveTo อยู่ ก็ให้รักษาระเบียบเดิมไว้
        updateInsideLayout(mainBox.getWidth(), mainBox.getHeight());
    }
}