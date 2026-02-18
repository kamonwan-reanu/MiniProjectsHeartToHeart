package UI_Screens;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import core.Main; 
import model.GameConstants; 

public class MainMenu extends JPanel {
    private JLabel gameName;
    private JPanel buttonBox;
    private JButton[] buttons;

    // ✅ ตัดบรรทัด SoundManager soundManager = new SoundManager(); ออก 
    // เพื่อไปใช้ Main.soundManager แทน (ป้องกันโปรแกรมค้าง)
    private Timer bgmLoopTimer;

    public MainMenu(Font titleFont, Font menuFont) {
        setLayout(new BorderLayout());
        setBackground(new Color(255, 230, 240));

        gameName = new JLabel("HeartToHeart", SwingConstants.CENTER);
        gameName.setForeground(new Color(255, 105, 180));
        add(gameName, BorderLayout.NORTH);

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

        // --- Action Listeners ---
        buttons[0].addActionListener(e -> {
            stopMenuMusic(); // หยุดเพลงเมื่อเริ่มเกม
            buttons[0].setEnabled(false); 
            Timer fadeOutTimer = new Timer(20, new ActionListener() {
                float alpha = 0.0f;
                @Override
                public void actionPerformed(ActionEvent e2) {
                    alpha += 0.05f;
                    if (alpha >= 1.0f) {
                        alpha = 1.0f;
                        ((Timer)e2.getSource()).stop();
                        Main.cardLayout.show(Main.mainContainer, "PLAY"); 
                        buttons[0].setEnabled(true); 
                    }
                    Main.brightnessAlpha = alpha;
                    Main.repaintBrightness();
                }
            });
            fadeOutTimer.start();
        });

        // ✅ ปุ่มตั้งค่า
        buttons[2].addActionListener(e -> Main.cardLayout.show(Main.mainContainer, "SETTING")); 
        buttons[3].addActionListener(e -> Main.cardLayout.show(Main.mainContainer, "CREDIT"));
        buttons[4].addActionListener(e -> showCustomExitDialog());

        menuButtonPanel.add(buttonBox);
        add(menuButtonPanel, BorderLayout.CENTER);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                applyResponsiveLayout();
            }
        });

        // ✅ เริ่มระบบเพลงเมนู
        startMenuMusic();
    }

    private void startMenuMusic() {
        String bgmPath = GameConstants.SOUND_PATH + "music_mainmenu.wav"; 
        // ✅ เรียกแค่นี้พอครับ SoundManager จะจัดการวนลูปให้เองถาวร
        Main.soundManager.playBGM(bgmPath); 
    }

    public void stopMenuMusic() {
        if (bgmLoopTimer != null) bgmLoopTimer.stop();
        Main.soundManager.stopBGM(); 
    }

    private void applyResponsiveLayout() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        setBorder(new EmptyBorder(h / 10, w / 5, h / 10, w / 5)); 
        float titleSize = Math.min(w * 0.08f, 65f);
        gameName.setFont(new Font("Tahoma", Font.BOLD, (int)titleSize));

        int btnW = Math.min((int)(w * 0.45), 400);
        int btnH = Math.max((int)(h * 0.07), 45); 
        float fontSize = Math.min(w * 0.035f, 20f); 

        for (JButton btn : buttons) {
            btn.setPreferredSize(new Dimension(btnW, btnH));
            btn.setFont(new Font("Tahoma", Font.BOLD, (int)fontSize));
        }
        revalidate();
        repaint();
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

        JButton yes = new JButton("ใช่");
        JButton no = new JButton("ไม่");
        
        Font thaiFont = new Font("Tahoma", Font.BOLD, 14);
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