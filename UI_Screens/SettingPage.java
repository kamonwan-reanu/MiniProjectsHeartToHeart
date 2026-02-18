package UI_Screens;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import core.Main; 

public class SettingPage extends JPanel {
    private long lastUpdateTime = 0;

    public SettingPage(Font tFont, Font bFont) {
        setLayout(new BorderLayout());
        setBackground(new Color(230, 230, 250)); 
        // ✅ ลด Padding ขอบนอกลงหน่อยเพื่อให้จอ 800x600 มีพื้นที่หายใจ
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel label = new JLabel("ตั้งค่าระบบ", JLabel.CENTER);
        label.setFont(tFont);
        add(label, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        
        JPanel contentCard = new JPanel(new GridBagLayout());
        contentCard.setOpaque(false);
        // ✅ เอา setPreferredSize(650, 450) ออก เพื่อให้มันปรับตัวตาม Layout แทน
        
        setupSettingLogic(contentCard);
        centerWrapper.add(contentCard);
        add(centerWrapper, BorderLayout.CENTER);

        JButton backBtn = new JButton("ย้อนกลับ");
        backBtn.setFont(new Font("Tahoma", Font.BOLD, 22)); 
        backBtn.setPreferredSize(new Dimension(200, 60));   
        backBtn.setBackground(Color.WHITE);
        backBtn.setFocusable(false);
        backBtn.addActionListener(e -> Main.cardLayout.show(Main.mainContainer, "MENU"));
        
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setOpaque(false);
        bottom.add(backBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    private void setupSettingLogic(JPanel content) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        Font labelFont = new Font("Tahoma", Font.BOLD, 18);
        Font valueFont = new Font("Tahoma", Font.BOLD, 16);

        // --- 1. ระดับแสง ---
        gbc.gridy = 0; gbc.gridx = 0; gbc.weightx = 0.0;
        content.add(new JLabel("ระดับแสง:") {{ setFont(labelFont); }}, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        JSlider brightSlider = new JSlider(0, 200, 100);
        // ✅ ล็อคขนาดขั้นต่ำ ไม่ให้กลายเป็นจุดเล็กๆ ตอนจอ 800x600
        brightSlider.setMinimumSize(new Dimension(200, 40)); 
        
        JLabel pLabel = new JLabel("50%", JLabel.RIGHT);
        pLabel.setPreferredSize(new Dimension(60, 30));
        pLabel.setFont(valueFont);

        brightSlider.addChangeListener(e -> {
            int val = brightSlider.getValue();
            pLabel.setText((val / 2) + "%");
            long now = System.currentTimeMillis();
            if (now - lastUpdateTime > 30) { 
                updateBrightness(val);
                lastUpdateTime = now;
            }
        });

        JPanel g1 = new JPanel(new BorderLayout(10, 0));
        g1.setOpaque(false); g1.add(brightSlider, BorderLayout.CENTER); g1.add(pLabel, BorderLayout.EAST);
        content.add(g1, gbc);

        // --- 2. ระดับเสียง ---
        gbc.gridy = 1; gbc.gridx = 0; gbc.weightx = 0.0;
        content.add(new JLabel("ระดับเสียง:") {{ setFont(labelFont); }}, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        JSlider volSlider = new JSlider(0, 100, 50); 
        volSlider.setMinimumSize(new Dimension(200, 40)); 
        JLabel vLabel = new JLabel("50%", JLabel.RIGHT);
        vLabel.setPreferredSize(new Dimension(60, 30));
        vLabel.setFont(valueFont);
        
        volSlider.addChangeListener(e -> {
            vLabel.setText(volSlider.getValue() + "%");
            long now = System.currentTimeMillis();
            if (now - lastUpdateTime > 40) { 
                Main.soundManager.setVolume(volSlider.getValue() / 100f);
                lastUpdateTime = now;
            }
        });

        volSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                Main.soundManager.setVolume(volSlider.getValue() / 100f);
            }
        });
        
        JPanel g2 = new JPanel(new BorderLayout(10, 0));
        g2.setOpaque(false); g2.add(volSlider, BorderLayout.CENTER); g2.add(vLabel, BorderLayout.EAST);
        content.add(g2, gbc);

        // --- 3. ขนาดหน้าจอ ---
        gbc.gridy = 2; gbc.gridx = 0; gbc.weightx = 0.0;
        content.add(new JLabel("ขนาดหน้าจอ:") {{ setFont(labelFont); }}, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        JComboBox<String> resBox = new JComboBox<>(new String[]{"1920x1080", "1280x720","800x600"});
        resBox.setFont(valueFont);
        resBox.addActionListener(e -> {
            String res = (String)resBox.getSelectedItem();
            if(res.equals("800x600")) Main.mainFrame.setSize(800, 600);
            else if(res.equals("1280x720")) Main.mainFrame.setSize(1280, 720);
            else Main.mainFrame.setSize(1920, 1080);
            Main.mainFrame.setLocationRelativeTo(null);
        });
        content.add(resBox, gbc);

        // --- 4. รูปแบบหน้าจอ ---
        gbc.gridy = 3; gbc.gridx = 0; gbc.weightx = 0.0;
        content.add(new JLabel("รูปแบบหน้าจอ:") {{ setFont(labelFont); }}, gbc);

        gbc.gridx = 1;
        JComboBox<String> modeBox = new JComboBox<>(new String[]{"Windowed", "Borderless", "Full Screen"});
        modeBox.setFont(valueFont);
        modeBox.addActionListener(e -> {
            String mode = (String)modeBox.getSelectedItem();
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            Main.mainFrame.dispose();
            if (mode.equals("Full Screen")) { 
                Main.mainFrame.setUndecorated(true); 
                gd.setFullScreenWindow(Main.mainFrame); 
            } else {
                gd.setFullScreenWindow(null); 
                Main.mainFrame.setUndecorated(mode.equals("Borderless"));
                if(mode.equals("Borderless")) Main.mainFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
                else Main.mainFrame.setSize(1920, 1080);
                Main.mainFrame.setLocationRelativeTo(null);
                Main.mainFrame.setVisible(true);
            }
        });
        content.add(modeBox, gbc);
    }

    private void updateBrightness(int val) {
        if (val < 100) { 
            Main.overlayColor = Color.BLACK; 
            Main.brightnessAlpha = (100 - val) / 125.0f; 
        } else { 
            Main.overlayColor = Color.WHITE; 
            Main.brightnessAlpha = (val - 100) / 250.0f; 
        }
        Main.repaintBrightness();
    }
}