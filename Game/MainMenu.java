import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenu extends JPanel {
    public MainMenu(Font titleFont, Font menuFont) {
        setLayout(new BorderLayout());
        setBackground(new Color(255, 230, 240));
        setBorder(new EmptyBorder(50, 50, 50, 50));

        JLabel gameName = new JLabel("HeartToHeart", SwingConstants.CENTER);
        gameName.setFont(titleFont);
        gameName.setForeground(new Color(255, 105, 180));
        add(gameName, BorderLayout.NORTH);

        JPanel menuButtonPanel = new JPanel(new GridBagLayout());
        menuButtonPanel.setOpaque(false);
        JPanel buttonBox = new JPanel(new GridLayout(5, 1, 0, 15));
        buttonBox.setOpaque(false);

        JButton[] buttons = {
            new JButton("เริ่มเกม"), new JButton("บันทึกเกม"), 
            new JButton("ตั้งค่า"), new JButton("เกี่ยวกับคนสร้าง"), 
            new JButton("ออกจากเกม")
        };

        for (JButton btn : buttons) {
            btn.setFont(menuFont);
            btn.setPreferredSize(new Dimension(350, 60));
            btn.setBackground(Color.WHITE);
            btn.setFocusable(false);
            buttonBox.add(btn);
        }

        // ==========================================
        // [จุดที่เพิ่มใหม่]: ระบบเริ่มเกมแบบ Fade Out (จอมืดลง)
        // ==========================================
        buttons[0].addActionListener(e -> {
            // --- [เพิ่มบรรทัดนี้]: ปิดปุ่มทันทีเพื่อป้องกันการกดซ้ำ ---
            buttons[0].setEnabled(false); 
            
            // สร้าง Timer เพื่อค่อยๆ เพิ่มความมืด
            Timer fadeOutTimer = new Timer(20, new ActionListener() {
                float alpha = 0.0f;

                @Override
                public void actionPerformed(ActionEvent e2) {
                    alpha += 0.02f; 
                    
                    if (alpha >= 1.0f) {
                        alpha = 1.0f;
                        ((Timer)e2.getSource()).stop();
                        
                        // เปลี่ยนหน้าไปยังหน้าเล่นเกม
                        Main.cardLayout.show(Main.mainContainer, "PLAY");
                        
                        // เรียกใช้เอฟเฟกต์ Fade In ให้หน้าใหม่สว่างขึ้น
                        fadeIn();
                        
                        // --- [เพิ่มบรรทัดนี้]: เปิดปุ่มกลับมาเผื่อกรณีผู้เล่นกลับมาหน้าเมนูอีกครั้ง ---
                        buttons[0].setEnabled(true); 
                    }
                    
                    Main.overlayColor = Color.BLACK;
                    Main.brightnessAlpha = alpha;
                    Main.repaintBrightness();
                }
            });
            fadeOutTimer.start();
        });
        
        // ปุ่มอื่นๆ ทำงานตามปกติ
        buttons[2].addActionListener(e -> Main.cardLayout.show(Main.mainContainer, "SETTING"));
        buttons[3].addActionListener(e -> Main.cardLayout.show(Main.mainContainer, "CREDIT"));
        buttons[4].addActionListener(e -> showCustomExitDialog());

        menuButtonPanel.add(buttonBox);
        add(menuButtonPanel, BorderLayout.CENTER);
    }

    // ==========================================
    // [ฟังก์ชันเพิ่มเติม]: ทำให้หน้าจอค่อยๆ สว่างคืนมา (Fade In)
    // ==========================================
    private void fadeIn() {
        Timer fadeInTimer = new Timer(20, new ActionListener() {
            float alpha = 1.0f; // เริ่มที่มืดสนิท

            @Override
            public void actionPerformed(ActionEvent e) {
                alpha -= 0.02f; // ค่อยๆ ลดความมืดลง
                
                if (alpha <= 0.0f) { // เมื่อสว่างสนิท
                    alpha = 0.0f;
                    ((Timer)e.getSource()).stop();
                }
                
                Main.brightnessAlpha = alpha;
                Main.repaintBrightness();
            }
        });
        fadeInTimer.start();
    }

    private void showCustomExitDialog() {
        // ... (โค้ด Dialog เดิมของคุณ) ...
        JDialog exitDialog = new JDialog(Main.mainFrame, "ยืนยัน", true);
        exitDialog.setUndecorated(true);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 240, 245));
        panel.setBorder(BorderFactory.createLineBorder(new Color(255, 105, 180), 3));

        JLabel label = new JLabel("คุณต้องการออกจากเกมใช่ไหม?", SwingConstants.CENTER);
        label.setFont(new Font("Tahoma", Font.BOLD, 18));
        label.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(label, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        btnPanel.setOpaque(false);

        JButton yes = new JButton("ใช่");
        JButton no = new JButton("ไม่");
        
        Font thaiFont = new Font("Tahoma", Font.BOLD, 16);
        yes.setFont(thaiFont);
        no.setFont(thaiFont);
        
        yes.setPreferredSize(new Dimension(80, 35));
        no.setPreferredSize(new Dimension(80, 35));
        yes.setBackground(Color.WHITE);
        no.setBackground(Color.WHITE);

        yes.addActionListener(e -> System.exit(0));
        no.addActionListener(e -> exitDialog.dispose());
        
        btnPanel.add(yes); btnPanel.add(no);
        panel.add(btnPanel, BorderLayout.CENTER);
        exitDialog.add(panel);
        exitDialog.pack();
        exitDialog.setLocationRelativeTo(Main.mainFrame);
        exitDialog.setVisible(true);
    }
}