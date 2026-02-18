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
        
        // บังคับให้เริ่มติดตั้งระบบดักจับการคลิก
        setupClickHandler();
    }

    public void setOnNextRequested(Runnable action) {
        this.onNextRequested = action;
    }

    private void initUI() {
        // 1. กล่องข้อความหลัก
        mainBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(15, 20, 35, 220)); 
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                g2d.setColor(new Color(212, 175, 55, 150));
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawRect(8, 8, getWidth() - 16, getHeight() - 16);
                g2d.dispose();
            }
        };
        mainBox.setLayout(null);
        mainBox.setOpaque(false);

        // 2. ป้ายชื่อ
        nameLabel = new JLabel("");
        nameLabel.setForeground(new Color(100, 210, 255)); 
        nameLabel.setFont(new Font("Tahoma", Font.BOLD, 22));
        nameLabel.setVisible(false);

        // 3. พื้นที่ข้อความ
        speechText = new JTextArea("");
        speechText.setEditable(false);
        speechText.setOpaque(false);
        speechText.setFocusable(false); // ✅ ป้องกันไม่ให้ตัวหนังสือแย่ง Cursor
        speechText.setLineWrap(true);       
        speechText.setWrapStyleWord(true); 
        speechText.setForeground(new Color(245, 245, 245)); 
        speechText.setFont(new Font("Tahoma", Font.PLAIN, 24));

        // 4. ปุ่มวงกลม Next
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
        
        // ✅ ใส่ Listener ให้ "ทุกส่วน" ที่อาจโดนจิ้ม
        this.addMouseListener(listener);        // พื้นที่ว่าง (แผ่นใสเต็มจอ)
        mainBox.addMouseListener(listener);     // กล่องสีน้ำเงิน
        speechText.addMouseListener(listener);  // ตัวหนังสือ (สำคัญมาก!)
        nextButton.addMouseListener(listener);  // ปุ่ม Next
    }

    public void updateLayout(int groupW, int groupH, int h) {
        if (groupW <= 0 || groupH <= 0) return;

        int x = (getWidth() - groupW) / 2;
        int y = getHeight() - groupH - 45; 
        
        mainBox.setBounds(x, y, groupW, groupH);
        
        if (nameLabel.isVisible()) {
            nameLabel.setBounds(45, 20, 400, 35);
            speechText.setBounds(45, 60, groupW - 90, groupH - 85);
        } else {
            speechText.setBounds(45, 40, groupW - 90, groupH - 80);
        }
        
        nextButton.setBounds(groupW - 65, groupH - 60, 40, 40);
        
        revalidate();
        repaint();
    }

    public void setText(String name, String text) {
        speechText.setText(text);
        
        if (name != null && !name.isEmpty() && !name.equals("narrator")) {
            nameLabel.setText(name.toUpperCase());
            nameLabel.setVisible(true);
        } else {
            nameLabel.setVisible(false);
        }
        
        updateLayout(mainBox.getWidth(), mainBox.getHeight(), getHeight());
    }
}