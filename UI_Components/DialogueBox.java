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
    }

    public void setOnNextRequested(Runnable action) {
        this.onNextRequested = action;
    }

    private void initUI() {
        // 1. กล่องข้อความหลัก (สีน้ำเงินเข้มโปร่งแสง)
        mainBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(15, 20, 35, 220)); 
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // เส้นขอบสีทองครีม
                g2d.setColor(new Color(212, 175, 55, 150));
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawRect(8, 8, getWidth() - 16, getHeight() - 16);
                g2d.dispose();
            }
        };
        mainBox.setLayout(null);
        mainBox.setOpaque(false);

        // 2. ป้ายชื่อ (ตั้งค่าสีฟ้าตามรูป Reference)
        nameLabel = new JLabel("");
        nameLabel.setForeground(new Color(100, 210, 255)); // สีฟ้าสว่าง
        nameLabel.setFont(new Font("Tahoma", Font.BOLD, 22));
        nameLabel.setVisible(false); // เริ่มต้นให้ซ่อนไว้ก่อน

        // 3. พื้นที่ข้อความ
        speechText = new JTextArea("");
        speechText.setEditable(false);
        speechText.setOpaque(false);
        speechText.setFocusable(false);
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
                g2d.setColor(new Color(150, 230, 255, 200)); // สีฟ้าอ่อนเข้ากับชื่อ
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

        setupClickHandler();
    }

    private void setupClickHandler() {
        MouseAdapter listener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onNextRequested != null) onNextRequested.run();
            }
        };
        mainBox.addMouseListener(listener);
        nextButton.addMouseListener(listener);
    }

    public void updateLayout(int groupW, int groupH, int h) {
        mainBox.setBounds(0, 0, groupW, groupH);
        
        // ถ้ามีชื่อ ให้เว้นที่ด้านบนไว้ให้ชื่อ
        if (nameLabel.isVisible()) {
            nameLabel.setBounds(45, 25, 300, 30);
            speechText.setBounds(45, 65, groupW - 90, groupH - 100);
        } else {
            // ถ้าไม่มีชื่อ ให้ข้อความอยู่กลางกล่อง
            speechText.setBounds(45, 45, groupW - 90, groupH - 90);
        }
        
        nextButton.setBounds(groupW - 70, groupH - 65, 40, 40);
    }

    // ✅ ปรับ Method นี้ให้เช็คว่าควรโชว์ชื่อไหม
    public void setText(String name, String text) {
        speechText.setText(text);
        
        // ถ้า name ไม่ว่าง และไม่ใช่พวก "???" หรือ "Narrator" ให้โชว์ชื่อสีฟ้า
        if (name != null && !name.isEmpty() && !name.equals("narrator")) {
            nameLabel.setText(name.toUpperCase());
            nameLabel.setVisible(true);
        } else {
            nameLabel.setVisible(false);
        }
        
        // อัปเดตตำแหน่ง Layout ทันทีที่เปลี่ยนโหมด
        updateLayout(getWidth(), getHeight(), 0);
    }
}