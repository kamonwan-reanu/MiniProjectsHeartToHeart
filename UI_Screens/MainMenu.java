package UI_Screens;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import core.Main; 
import model.GameConstants; 

public class MainMenu extends JPanel {
    private JLabel gameName;
    private JPanel buttonBox;
    private JButton[] buttons;
    private Timer bgmLoopTimer;

    // ✨ ส่วนประกอบสำหรับ In-Game UI
    private JLayeredPane layeredPane;
    private JPanel mainContentPanel; 
    private JPanel registerOverlay;

    public MainMenu(Font titleFont, Font menuFont) {
        setLayout(new BorderLayout());
        setBackground(new Color(255, 230, 240));

        // 🛠️ ใช้ JLayeredPane เพื่อทำ Layer ซ้อนทับกัน
        layeredPane = new JLayeredPane();
        add(layeredPane, BorderLayout.CENTER);

        // --- 1. หน้า Main Menu ปกติ ---
        mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setOpaque(false);
        
        gameName = new JLabel("HeartToHeart", SwingConstants.CENTER);
        gameName.setForeground(new Color(255, 105, 180));
        mainContentPanel.add(gameName, BorderLayout.NORTH);

        JPanel menuButtonPanel = new JPanel(new GridBagLayout());
        menuButtonPanel.setOpaque(false);
        
        buttonBox = new JPanel(new GridLayout(5, 1, 0, 15)); 
        buttonBox.setOpaque(false);

        buttons = new JButton[]{
            new JButton("เริ่มเกม"), new JButton("บันทึกเกม"), 
            new JButton("ตั้งค่า"), new JButton("เกี่ยวกับคนสร้าง"), 
            new JButton("ออกจากเกม")
        };

        for (JButton btn : buttons) {
            btn.setBackground(Color.WHITE);
            btn.setFocusable(false);
            btn.setBorder(BorderFactory.createLineBorder(new Color(255, 182, 193), 2));
            buttonBox.add(btn);
        }

        // --- ✨ ปุ่มเริ่มเกม: เรียกใช้ UI ในเกมแทน JOptionPane ---
        buttons[0].addActionListener(e -> showRegisterUI());

        buttons[2].addActionListener(e -> Main.cardLayout.show(Main.mainContainer, "SETTING")); 
        buttons[3].addActionListener(e -> Main.cardLayout.show(Main.mainContainer, "CREDIT"));
        buttons[4].addActionListener(e -> showCustomExitDialog());

        menuButtonPanel.add(buttonBox);
        mainContentPanel.add(menuButtonPanel, BorderLayout.CENTER);
        
        // ใส่หน้าเมนูลงใน Layer ล่างสุด
        layeredPane.add(mainContentPanel, JLayeredPane.DEFAULT_LAYER);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                applyResponsiveLayout();
            }
        });

        startMenuMusic();
    }

    // 🌸 ฟังก์ชันแสดงหน้ากรอกชื่อแบบ UI ในเกม
    private void showRegisterUI() {
        if (registerOverlay != null) return; 

        registerOverlay = new JPanel(null);
        registerOverlay.setOpaque(false);
        registerOverlay.setBounds(0, 0, getWidth(), getHeight());

        // พื้นหลังมืดจางๆ ให้หน้าเมนูดูดรอปลง
        JPanel dimmer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0, 0, 0, 120));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        dimmer.setBounds(0, 0, getWidth(), getHeight());
        
        // 🎨 กล่องลงทะเบียนสีชมพูขาว
        JPanel box = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 250));
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 40, 40));
                g2d.setColor(new Color(255, 105, 180));
                g2d.setStroke(new BasicStroke(3));
                g2d.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 40, 40));
                g2d.dispose();
            }
        };
        box.setBounds((getWidth()-500)/2, (getHeight()-300)/2, 500, 300);

        JLabel title = new JLabel("กรุณาระบุชื่อของคุณ");
        title.setFont(new Font("Tahoma", Font.BOLD, 24));
        title.setForeground(new Color(255, 105, 180));
        title.setBounds(0, 40, 500, 40);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JTextField inputField = new JTextField();
        inputField.setFont(new Font("Tahoma", Font.PLAIN, 22));
        inputField.setHorizontalAlignment(JTextField.CENTER);
        inputField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(255, 182, 193)));
        inputField.setBounds(100, 120, 300, 45);

        JButton confirmBtn = new JButton("เริ่มต้นการเดินทาง");
        confirmBtn.setFont(new Font("Tahoma", Font.BOLD, 18));
        confirmBtn.setBackground(new Color(255, 105, 180));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFocusPainted(false);
        confirmBtn.setBounds(150, 200, 200, 50);

        confirmBtn.addActionListener(e -> {
            String name = inputField.getText().trim();
            GameConstants.PLAYER_NAME = name.isEmpty() ? "นักเดินทาง" : name;
            startFadeOutAction();
        });

        box.add(title);
        box.add(inputField);
        box.add(confirmBtn);
        
        registerOverlay.add(box);
        registerOverlay.add(dimmer);

        layeredPane.add(registerOverlay, JLayeredPane.PALETTE_LAYER);
        layeredPane.revalidate();
        layeredPane.repaint();
        inputField.requestFocus();
    }

    private void startFadeOutAction() {
        stopMenuMusic();
        Timer fadeOutTimer = new Timer(20, new ActionListener() {
            float alpha = 0.0f;
            @Override
            public void actionPerformed(ActionEvent e) {
                alpha += 0.05f;
                if (alpha >= 1.0f) {
                    ((Timer)e.getSource()).stop();
                    Main.cardLayout.show(Main.mainContainer, "PLAY_PAGE"); 
                    Main.brightnessAlpha = 0.0f;
                    Main.repaintBrightness();
                } else {
                    Main.brightnessAlpha = alpha;
                    Main.repaintBrightness();
                }
            }
        });
        fadeOutTimer.start();
    }

    private void startMenuMusic() {
        String bgmPath = GameConstants.SOUND_PATH + "music_mainmenu.wav"; 
        Main.soundManager.playBGM(bgmPath); 
    }

    public void stopMenuMusic() {
        if (bgmLoopTimer != null) bgmLoopTimer.stop();
        Main.soundManager.stopBGM(); 
    }

    private void applyResponsiveLayout() {
        int w = getWidth(); int h = getHeight();
        if (w <= 0 || h <= 0) return;

        // จัดขนาด Layer ให้เต็มจอเสมอ
        mainContentPanel.setBounds(0, 0, w, h);
        if (registerOverlay != null) registerOverlay.setBounds(0, 0, w, h);

        mainContentPanel.setBorder(new EmptyBorder(h / 10, w / 5, h / 10, w / 5)); 
        float titleSize = Math.min(w * 0.08f, 65f);
        gameName.setFont(new Font("Tahoma", Font.BOLD, (int)titleSize));
        int btnW = Math.min((int)(w * 0.45), 400);
        int btnH = Math.max((int)(h * 0.07), 45); 
        float fontSize = Math.min(w * 0.035f, 20f); 
        for (JButton btn : buttons) {
            btn.setPreferredSize(new Dimension(btnW, btnH));
            btn.setFont(new Font("Tahoma", Font.BOLD, (int)fontSize));
        }
        revalidate(); repaint();
    }

    private void showCustomExitDialog() {
        JDialog exitDialog = new JDialog(Main.mainFrame, "ยืนยัน", true);
        exitDialog.setUndecorated(true);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 240, 245));
        panel.setBorder(BorderFactory.createLineBorder(new Color(255, 105, 180), 3));
        JLabel label = new JLabel("คุณต้องการออกจากเกมใช่ไหม?", SwingConstants.CENTER);
        label.setFont(new Font("Tahoma", Font.BOLD, 16));
        label.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(label, BorderLayout.NORTH);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        btnPanel.setOpaque(false);
        JButton yes = new JButton("ใช่"); JButton no = new JButton("ไม่");
        Font thaiFont = new Font("Tahoma", Font.BOLD, 14);
        yes.setFont(thaiFont); no.setFont(thaiFont);
        yes.setPreferredSize(new Dimension(80, 35)); no.setPreferredSize(new Dimension(80, 35));
        yes.setBackground(Color.WHITE); no.setBackground(Color.WHITE);
        yes.addActionListener(e -> System.exit(0));
        no.addActionListener(e -> exitDialog.dispose());
        btnPanel.add(yes); btnPanel.add(no);
        panel.add(btnPanel, BorderLayout.CENTER);
        exitDialog.add(panel); exitDialog.pack();
        exitDialog.setLocationRelativeTo(Main.mainFrame);
        exitDialog.setVisible(true);
    }
}