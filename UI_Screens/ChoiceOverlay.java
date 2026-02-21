package UI_Screens;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ChoiceOverlay extends JPanel {

    public ChoiceOverlay(Object[][] choices, Consumer<String> onChoiceSelected) {
        setLayout(new GridBagLayout());
        setOpaque(false); // พื้นหลังโปร่งใส

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE; // ไม่ให้ยืดเต็มกรอบ ให้ใช้ขนาดจาก getPreferredSize

        for (int i = 0; i < choices.length; i++) {
            final int index = i;
            String btnText = (String) choices[i][0];
            String targetScene = (String) choices[i][1];

            gbc.gridy = i;
            gbc.insets = new Insets(10, 0, 10, 0); // ระยะห่างบนล่าง 10px

            JButton btn = new JButton() {
                // ✅ 1. Dynamic Size: กว้าง 60% ของจอ สูง 60px เสมอ
                @Override
                public Dimension getPreferredSize() {
                    int currentW = ChoiceOverlay.this.getParent() != null ? ChoiceOverlay.this.getParent().getWidth() : 800;
                    return new Dimension((int)(currentW * 0.6), 60);
                }

                // ✅ 2. Custom UI: วาดปุ่มเอง
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // แถบพื้นหลังข้อความ
                    g2.setColor(new Color(20, 20, 35, 230));
                    g2.fillRect(70, 0, getWidth() - 140, getHeight());
                    
                    // กล่องตัวเลขสีชมพู
                    g2.setColor(new Color(255, 105, 180));
                    g2.fillRoundRect(0, 5, 60, getHeight() - 10, 10, 10);
                    
                    // ตัวเลข
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Tahoma", Font.BOLD, 24));
                    g2.drawString(String.valueOf(index + 1), 20, getHeight() / 2 + 8);
                    
                    // ข้อความตัวเลือก
                    g2.setFont(new Font("Tahoma", Font.PLAIN, 22));
                    g2.drawString(btnText, 100, getHeight() / 2 + 8);
                    
                    g2.dispose();
                }
            };

            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusable(false);

            // ✅ 3. เมื่อปุ่มถูกกด ส่งชื่อฉากกลับไปให้ PlaySceneMain
            btn.addActionListener(e -> {
                if (onChoiceSelected != null) {
                    onChoiceSelected.accept(targetScene);
                }
            });

            add(btn, gbc);
        }
    }

    // ✅ 4. อัปเดตตำแหน่งเวลาย่อ/ขยายจอ (อยู่ใต้กล่องข้อความ 20px)
    public void updateBounds(int w, int h, int targetY, int groupH) {
        int overlayY = targetY + groupH + 20; 
        this.setBounds(0, overlayY, w, h - overlayY);
        this.revalidate();
        this.repaint();
    }
}