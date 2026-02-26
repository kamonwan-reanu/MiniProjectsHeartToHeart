package UI_Screens;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import core.Main;
import model.KeyConfig; 

public class SettingPage extends JPanel {
    private long lastUpdateTime = 0;
    
    // ปุ่มสำหรับการตั้งค่าปุ่มกด
    private JButton[] keyButtons;
    private String[] keyNames;
    private String[] keyDescriptions;
    
    // สถานะการรอรับปุ่มใหม่
    private boolean isListening = false;
    private JButton listeningButton = null;

    public SettingPage(Font tFont, Font bFont) {
        setLayout(new BorderLayout());
        setBackground(new Color(230, 230, 250)); 
        // ลด Padding ขอบนอกลงหน่อยเพื่อให้จอ 800x600 มีพื้นที่หายใจ
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel label = new JLabel("ตั้งค่าระบบ", JLabel.CENTER);
        label.setFont(tFont);
        add(label, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        
        JPanel contentCard = new JPanel(new GridBagLayout());
        contentCard.setOpaque(false);
        
        initializeKeyData();
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

    private void initializeKeyData() {
        keyNames = new String[]{"NEXT_MSG", "CHOICE_1", "CHOICE_2", "CHOICE_3", "RELATION_UI", "ESCAPE"};
        keyDescriptions = new String[]{
            "ข้ามบทสนทนา / ต่อไป",
            "เลือกตัวเลือกที่ 1",
            "เลือกตัวเลือกที่ 2", 
            "เลือกตัวเลือกที่ 3",
            "ดูสถานะความสัมพันธ์",
            "กลับเมนู / ปิดหน้าต่าง"
        };
        keyButtons = new JButton[keyNames.length];
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

        // --- 5. ปุ่มควบคุม (Interactive Keybinding) ---
        gbc.gridy = 4; gbc.gridx = 0; gbc.weightx = 0.0;
        content.add(new JLabel("ปุ่มควบคุม:") {{ setFont(labelFont); }}, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        JPanel keyConfigPanel = new JPanel(new GridBagLayout());
        keyConfigPanel.setOpaque(false);
        
        GridBagConstraints keyGbc = new GridBagConstraints();
        keyGbc.insets = new Insets(8, 0, 8, 0);
        keyGbc.anchor = GridBagConstraints.WEST;
        keyGbc.fill = GridBagConstraints.HORIZONTAL;
        keyGbc.gridy = 0;
        
        // สร้างปุ่มสำหรับแต่ละปุ่มกด
        for (int i = 0; i < keyNames.length; i++) {
            JPanel keyRow = new JPanel(new BorderLayout(10, 0));
            keyRow.setOpaque(false);
            
            JLabel descLabel = new JLabel(keyDescriptions[i]);
            descLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
            descLabel.setForeground(new Color(50, 50, 80));
            descLabel.setPreferredSize(new Dimension(180, 25));
            
            JButton keyButton = createKeyButton(keyNames[i]);
            keyButtons[i] = keyButton;
            
            keyRow.add(descLabel, BorderLayout.WEST);
            keyRow.add(keyButton, BorderLayout.EAST);
            
            keyConfigPanel.add(keyRow, keyGbc);
            keyGbc.gridy++;
        }
        
        // เพิ่มปุ่ม Reset Defaults
        JButton resetButton = new JButton("คืนค่าเริ่มต้น");
        resetButton.setFont(new Font("Tahoma", Font.BOLD, 14));
        resetButton.setBackground(new Color(220, 53, 69));
        resetButton.setForeground(Color.WHITE);
        resetButton.setFocusPainted(false);
        resetButton.setBorder(BorderFactory.createLineBorder(new Color(180, 30, 40), 2));
        resetButton.setPreferredSize(new Dimension(150, 35));
        resetButton.addActionListener(e -> resetKeyBindings());
        
        JPanel resetPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        resetPanel.setOpaque(false);
        resetPanel.add(resetButton);
        
        keyConfigPanel.add(resetPanel, keyGbc);
        content.add(keyConfigPanel, gbc);
    }
    
    /**
     * สร้างปุ่มสำหรับการตั้งค่าปุ่มกด
     */
    private JButton createKeyButton(String keyName) {
        int currentKey = KeyConfig.getKey(keyName);
        JButton button = new JButton(KeyConfig.getKeyText(currentKey));
        
        // กำหนดสไตล์ปุ่มตามธีมเกม
        button.setBackground(new Color(15, 20, 35));
        button.setForeground(new Color(255, 255, 255));
        button.setBorder(BorderFactory.createLineBorder(new Color(212, 175, 55), 2));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(80, 30));
        button.setFont(new Font("Tahoma", Font.BOLD, 12));
        
        // Hover Effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isListening) {
                    button.setBackground(new Color(255, 105, 180));
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (!isListening) {
                    button.setBackground(new Color(15, 20, 35));
                }
            }
        });
        
        // Action Listener สำหรับเริ่มการรับปุ่มใหม่
        button.addActionListener(e -> {
            if (!isListening) {
                startKeyListening(keyName, button);
            }
        });
        
        return button;
    }
    
    /**
     * เริ่มการรับปุ่มใหม่
     */
    private void startKeyListening(String keyName, JButton button) {
        isListening = true;
        listeningButton = button;
        
        // เปลี่ยนสถานะปุ่มเป็น "[ กดปุ่มใหม่... ]"
        button.setText("[ กดปุ่มใหม่... ]");
        button.setBackground(new Color(255, 200, 100));
        
        // สร้าง KeyListener ชั่วคราว
        KeyListener tempListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int newKey = e.getKeyCode();
                
                // ตรวจสอบว่าปุ่มซ้ำกับปุ่มอื่นหรือไม่
                if (KeyConfig.isDuplicate(newKey, keyName)) {
                    button.setText("DUPLICATE!");
                    button.setBackground(new Color(255, 100, 100));
                    Timer timer = new Timer(1000, ev -> {
                        button.setText(KeyConfig.getKeyText(KeyConfig.getKey(keyName)));
                        button.setBackground(new Color(15, 20, 35));
                        stopKeyListening();
                    });
                    timer.setRepeats(false);
                    timer.start();
                    return;
                }
                
                // อัปเดตปุ่มใหม่
                KeyConfig.setKey(keyName, newKey);
                button.setText(KeyConfig.getKeyText(newKey));
                button.setBackground(new Color(100, 255, 100));
                
                // หยุดการรับฟังหลังจากอัปเดตเสร็จ
                Timer timer = new Timer(500, ev -> {
                    button.setBackground(new Color(15, 20, 35));
                    stopKeyListening();
                });
                timer.setRepeats(false);
                timer.start();
            }
        };
        
        // เพิ่ม KeyListener ชั่วคราวไปที่หน้าต่างหลัก
        Main.mainFrame.addKeyListener(tempListener);
        Main.mainFrame.setFocusable(true);
        Main.mainFrame.requestFocus();
        
        // เก็บ KeyListener ไว้เพื่อลบภายหลัง
        button.putClientProperty("tempListener", tempListener);
    }
    
    /**
     * หยุดการรับปุ่มใหม่
     */
    private void stopKeyListening() {
        if (isListening && listeningButton != null) {
            KeyListener tempListener = (KeyListener) listeningButton.getClientProperty("tempListener");
            if (tempListener != null) {
                Main.mainFrame.removeKeyListener(tempListener);
            }
            
            isListening = false;
            listeningButton = null;
        }
    }
    
    /**
     * คืนค่าปุ่มทั้งหมดเป็นค่าเริ่มต้น
     */
    private void resetKeyBindings() {
        KeyConfig.resetToDefaults();
        
        // อัปเดตข้อความบนปุ่มทั้งหมด
        for (int i = 0; i < keyNames.length; i++) {
            if (keyButtons[i] != null) {
                int defaultKey = KeyConfig.getKey(keyNames[i]);
                keyButtons[i].setText(KeyConfig.getKeyText(defaultKey));
                keyButtons[i].setBackground(new Color(15, 20, 35));
            }
        }
        
        // แสดงข้อความยืนยัน
        JOptionPane.showMessageDialog(this, 
            "คืนค่าปุ่มควบคุมเป็นค่าเริ่มต้นเรียบร้อยแล้ว", 
            "รีเซ็ตสำเร็จ", 
            JOptionPane.INFORMATION_MESSAGE);
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
