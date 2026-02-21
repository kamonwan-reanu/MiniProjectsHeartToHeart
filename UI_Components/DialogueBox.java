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

    private int lastW = 0, lastH = 0;

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
                g2d.setColor(new Color(15, 20, 35, 235)); 
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                g2d.setColor(new Color(212, 175, 55, 180));
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawRect(5, 5, getWidth() - 10, getHeight() - 10);
                g2d.dispose();
            }
        };
        mainBox.setLayout(null);
        mainBox.setOpaque(false);

        nameLabel = new JLabel("");
        nameLabel.setForeground(new Color(100, 210, 255)); 
        nameLabel.setFont(new Font("Tahoma", Font.BOLD, 24));
        nameLabel.setHorizontalAlignment(SwingConstants.LEFT);
        nameLabel.setVisible(false);

        speechText = new JTextArea("");
        speechText.setEditable(false);
        speechText.setOpaque(false);
        speechText.setFocusable(false); 
        speechText.setLineWrap(true);       
        speechText.setWrapStyleWord(true); 
        speechText.setForeground(new Color(245, 245, 245)); 
        speechText.setFont(new Font("Tahoma", Font.PLAIN, 22));

        nextButton = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(150, 230, 255, 200)); 
                g2d.fillOval(0, 0, getWidth(), getHeight());
                g2d.setColor(Color.BLACK);
                g2d.fillPolygon(new int[]{12, 12, 25}, new int[]{10, 30, 20}, 3);
                g2d.dispose();
            }
        };

        mainBox.add(nameLabel);
        mainBox.add(speechText);
        mainBox.add(nextButton);
        add(mainBox);

        mainBox.setComponentZOrder(nameLabel, 0);
    }

    private void setupClickHandler() {
        MouseAdapter listener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { 
                if (onNextRequested != null) onNextRequested.run();
            }
        };
        this.addMouseListener(listener);
        mainBox.addMouseListener(listener);
        speechText.addMouseListener(listener);
    }

    // ✅ เมธอดจัดระเบียบฟอนต์และปุ่มด้านใน
    private void updateInsideLayout(int width, int height) {
        if (nameLabel != null && speechText != null && nextButton != null) {
            if (nameLabel.isVisible()) {
                nameLabel.setBounds(45, 20, width - 90, 35);
                speechText.setBounds(45, 60, width - 90, height - 85);
            } else {
                speechText.setBounds(45, 40, width - 90, height - 80);
            }
            nextButton.setBounds(width - 65, height - 60, 40, 40);
        }
    }

    // ✅ เมธอดสำหรับอยู่ด้านล่างจอ (โหมดปกติ)
    public void updateLayout(int groupW, int groupH, int h) {
        if (groupW <= 0 || groupH <= 0 || getWidth() <= 0) return;
        
        this.lastW = groupW;
        this.lastH = groupH;

        int x = (getWidth() - groupW) / 2;
        int y = getHeight() - groupH - 40; 
        
        this.setBounds(0, 0, getParent().getWidth(), h);
        mainBox.setBounds(x, y, groupW, groupH);
        
        updateInsideLayout(groupW, groupH);
        
        mainBox.revalidate();
        mainBox.repaint();
    }

    // ✅ เมธอดสำหรับลอยขึ้นบนจอ (โหมดมีตัวเลือก) แก้ไขจุดที่ Conflict ให้สมบูรณ์
    public void moveTo(int groupW, int groupH, int targetY) {
        if (groupW <= 0 || groupH <= 0 || getWidth() <= 0) return;
        
        this.lastW = groupW;
        this.lastH = groupH;

        int x = (getParent().getWidth() - groupW) / 2;
        
        this.setBounds(0, 0, getParent().getWidth(), getParent().getHeight());
        
        if (mainBox != null) {
            mainBox.setBounds(x, targetY, groupW, groupH);
        }
        
        updateInsideLayout(groupW, groupH);
        
        revalidate();
        repaint();
    }

    // ✅ เมธอดเซ็ตข้อความ
    public void setText(String name, String text) {
        speechText.setText(text);
        
        if (name != null && !name.trim().isEmpty() && 
            !name.equalsIgnoreCase("narrator") && 
            !name.equalsIgnoreCase("none")) {
            
            nameLabel.setText(name.trim().toUpperCase());
            nameLabel.setVisible(true);
            mainBox.setComponentZOrder(nameLabel, 0); 
        } else {
            nameLabel.setVisible(false);
            nameLabel.setText("");
        }
        
        if (lastW > 0 && lastH > 0) {
            updateInsideLayout(lastW, lastH);
        }
        
        repaint();
    }
}