package UI_Screens; // 1. ระบุ Package ให้ถูกต้อง

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import core.Main; // 2. Import Main มาเพื่อใช้ CardLayout และระบบแสง

public class SettingPage extends JPanel {
    public SettingPage(Font tFont, Font bFont) {
        setLayout(new BorderLayout());
        setBackground(new Color(230, 230, 250)); // สีพื้นหลังม่วงอ่อน
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // --- หัวข้อหน้าจอ ---
        JLabel label = new JLabel("ตั้งค่าระบบ");
        label.setFont(tFont);
        add(label, BorderLayout.NORTH);

        // --- ส่วนเนื้อหา ---
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        setupSettingLogic(content);
        add(content, BorderLayout.CENTER);

        // --- ปุ่มย้อนกลับ ---
        JButton backBtn = new JButton("ย้อนกลับ");
        backBtn.setFont(new Font("Tahoma", Font.BOLD, 22)); 
        backBtn.setPreferredSize(new Dimension(200, 60));   
        backBtn.setBackground(Color.WHITE);
        backBtn.setFocusable(false);
        
        backBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 200, 220), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

        // สั่งกลับหน้าเมนู
        backBtn.addActionListener(e -> Main.cardLayout.show(Main.mainContainer, "MENU"));
        
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setOpaque(false);
        bottom.add(backBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    private void setupSettingLogic(JPanel content) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 20, 15, 20);
        gbc.anchor = GridBagConstraints.WEST;
        Font labelFont = new Font("Tahoma", Font.BOLD, 18);
        Font valueFont = new Font("Tahoma", Font.BOLD, 16);

        // --- ระดับแสง ---
        gbc.gridy = 0; gbc.gridx = 0;
        JLabel brightLabel = new JLabel("ระดับแสง:");
        brightLabel.setFont(labelFont);
        content.add(brightLabel, gbc);
        
        JSlider brightSlider = new JSlider(0, 200, 100);
        brightSlider.setPreferredSize(new Dimension(200, 40));
        JLabel pLabel = new JLabel("50%");
        pLabel.setPreferredSize(new Dimension(70, 30));
        pLabel.setFont(valueFont);

        brightSlider.addChangeListener(e -> {
            int val = brightSlider.getValue();
            pLabel.setText((val / 2) + "%");
            if (val < 100) { 
                Main.overlayColor = Color.BLACK; 
                Main.brightnessAlpha = (100 - val) / 125.0f; 
            } else { 
                Main.overlayColor = Color.WHITE; 
                Main.brightnessAlpha = (val - 100) / 250.0f; 
            }
            Main.repaintBrightness();
        });

        gbc.gridx = 1;
        JPanel g1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        g1.setOpaque(false); g1.add(brightSlider); g1.add(pLabel);
        content.add(g1, gbc);

        // --- ระดับเสียง ---
        gbc.gridy = 1; gbc.gridx = 0;
        JLabel volLabel = new JLabel("ระดับเสียง:");
        volLabel.setFont(labelFont);
        content.add(volLabel, gbc);

        JSlider volSlider = new JSlider(0, 100, 80);
        volSlider.setPreferredSize(new Dimension(200, 40));
        JLabel vLabel = new JLabel("80%");
        vLabel.setPreferredSize(new Dimension(70, 30));
        vLabel.setFont(valueFont);
        volSlider.addChangeListener(e -> vLabel.setText(volSlider.getValue() + "%"));
        
        gbc.gridx = 1;
        JPanel g2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        g2.setOpaque(false); g2.add(volSlider); g2.add(vLabel);
        content.add(g2, gbc);

        // --- ขนาดหน้าจอ ---
        gbc.gridy = 2; gbc.gridx = 0;
        JLabel resLabel = new JLabel("ขนาดหน้าจอ:");
        resLabel.setFont(labelFont);
        content.add(resLabel, gbc);

        JComboBox<String> resBox = new JComboBox<>(new String[]{"1920x1080", "1280x720","800x600"});
        resBox.setPreferredSize(new Dimension(150, 35));
        resBox.addActionListener(e -> {
            String res = (String)resBox.getSelectedItem();
            if(res.equals("800x600")) Main.mainFrame.setSize(800, 600);
            else if(res.equals("1280x720")) Main.mainFrame.setSize(1280, 720);
            else Main.mainFrame.setSize(1920, 1080);
            Main.mainFrame.setLocationRelativeTo(null);
        });
        gbc.gridx = 1; content.add(resBox, gbc);

        // --- รูปแบบหน้าจอ ---
        gbc.gridy = 3; gbc.gridx = 0;
        JLabel modeLabel = new JLabel("รูปแบบหน้าจอ:");
        modeLabel.setFont(labelFont);
        content.add(modeLabel, gbc);

        JComboBox<String> modeBox = new JComboBox<>(new String[]{"Windowed", "Borderless", "Full Screen"});
        modeBox.setPreferredSize(new Dimension(150, 35));
        modeBox.addActionListener(e -> {
            String mode = (String)modeBox.getSelectedItem();
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            Main.mainFrame.dispose();
            if (mode.equals("Full Screen")) { 
                Main.mainFrame.setUndecorated(true); 
                gd.setFullScreenWindow(Main.mainFrame); 
            } else if (mode.equals("Borderless")) {
                gd.setFullScreenWindow(null); 
                Main.mainFrame.setUndecorated(true);
                Main.mainFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
                Main.mainFrame.setLocation(0, 0); 
                Main.mainFrame.setVisible(true);
            } else {
                gd.setFullScreenWindow(null); 
                Main.mainFrame.setUndecorated(false);
                Main.mainFrame.setSize(1920, 1080); 
                Main.mainFrame.setLocationRelativeTo(null); 
                Main.mainFrame.setVisible(true);
            }
        });
        gbc.gridx = 1; content.add(modeBox, gbc);
    }
}