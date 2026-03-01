package UI_Screens;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.lang.reflect.Field;
import core.Main; 
import model.GameConstants; 
import model.Relation;
import model.StoryData; 

public class MainMenu extends JPanel {
    private JLabel gameName;
    private JPanel buttonBox;
    private JButton[] buttons;
    private Timer bgmLoopTimer;

    private JLayeredPane layeredPane;
    private JPanel mainContentPanel; 
    private JPanel registerOverlay;
    private JTextField inputField;
    private JLabel warningLabel;

    public MainMenu(Font titleFont, Font menuFont) {
        setLayout(new BorderLayout());
        setBackground(new Color(255, 230, 240));

        layeredPane = new JLayeredPane();
        add(layeredPane, BorderLayout.CENTER);

        // --- 1. หน้า Main Menu ปกติ (Layer 0) ---
        mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setOpaque(false);
        
        gameName = new JLabel("HeartToHeart", SwingConstants.CENTER);
        gameName.setForeground(new Color(255, 105, 180));
        mainContentPanel.add(gameName, BorderLayout.NORTH);

        JPanel menuButtonPanel = new JPanel(new GridBagLayout());
        menuButtonPanel.setOpaque(false);
        
        buttonBox = new JPanel(new GridLayout(6, 1, 0, 15)); 
        buttonBox.setOpaque(false);

        buttons = new JButton[]{ 
            new JButton("เริ่มเกม"), 
            new JButton("เล่นหลายคน"),  // ✅ เพิ่มใหม่
            new JButton("โหลดเกม"), 
            new JButton("ตั้งค่า"), 
            new JButton("เกี่ยวกับคนสร้าง"), 
            new JButton("ออกจากเกม")
        };

        for (JButton btn : buttons) {
            btn.setBackground(Color.WHITE);
            btn.setFocusable(false);
            btn.setBorder(BorderFactory.createLineBorder(new Color(255, 182, 193), 2));
            buttonBox.add(btn);
        }

        buttons[0].addActionListener(e -> showRegisterUI());
        buttons[1].addActionListener(e -> Main.cardLayout.show(Main.mainContainer, "MULTIPLAYER")); // ✅ ใหม่
        buttons[2].addActionListener(e -> loadGame());
        buttons[3].addActionListener(e -> Main.cardLayout.show(Main.mainContainer, "SETTING"));
        buttons[4].addActionListener(e -> Main.cardLayout.show(Main.mainContainer, "CREDIT"));
        buttons[5].addActionListener(e -> showCustomExitDialog());

        menuButtonPanel.add(buttonBox);
        mainContentPanel.add(menuButtonPanel, BorderLayout.CENTER);
        
        // แอดปุ่มเมนูไว้เลเยอร์ล่างสุด
        layeredPane.add(mainContentPanel, Integer.valueOf(0));

        // --- 2. สร้างหน้าต่างกรอกชื่อ (Layer 300) เตรียมไว้ตั้งแต่ต้น ---
        setupRegisterOverlay();

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                applyResponsiveLayout();
            }
        });

        startMenuMusic();
    }

    private void setupRegisterOverlay() {
        registerOverlay = new JPanel(null);
        registerOverlay.setOpaque(false);
        registerOverlay.setVisible(false); // ซ่อนไว้ก่อน

        // ตัวแผ่นใสบังจอ
        JPanel dimmer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0, 0, 0, 150)); // สีดำโปร่งแสง
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        // 🛑 หัวใจสำคัญ: บล็อกเมาส์ทุกรูปแบบไม่ให้ทะลุไปโดนปุ่มด้านล่าง 🛑
        MouseAdapter blockMouse = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { e.consume(); }
            @Override public void mousePressed(MouseEvent e) { e.consume(); }
            @Override public void mouseReleased(MouseEvent e) { e.consume(); }
            @Override public void mouseEntered(MouseEvent e) { e.consume(); }
            @Override public void mouseExited(MouseEvent e) { e.consume(); }
            @Override public void mouseMoved(MouseEvent e) { e.consume(); }
            @Override public void mouseDragged(MouseEvent e) { e.consume(); }
        };
        dimmer.addMouseListener(blockMouse);
        dimmer.addMouseMotionListener(blockMouse);

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
        box.addMouseListener(blockMouse);
        box.addMouseMotionListener(blockMouse);

        JLabel title = new JLabel("กรุณาระบุชื่อของคุณ");
        title.setFont(new Font("Tahoma", Font.BOLD, 24));
        title.setForeground(new Color(255, 105, 180));
        title.setBounds(0, 30, 500, 40);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        warningLabel = new JLabel("! กรุณาใส่ชื่อตัวละครก่อนเริ่มต้นการเดินทาง !");
        warningLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        warningLabel.setForeground(Color.RED);
        warningLabel.setBounds(0, 75, 500, 25);
        warningLabel.setHorizontalAlignment(SwingConstants.CENTER);
        warningLabel.setVisible(false);

        inputField = new JTextField();
        inputField.setFont(new Font("Tahoma", Font.PLAIN, 22));
        inputField.setHorizontalAlignment(JTextField.CENTER);
        inputField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(255, 182, 193)));
        inputField.setBounds(100, 120, 300, 45);

        // กดปุ่ม ENTER เพื่อเริ่มเกม หรือ ESC เพื่อปิด
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    closeRegisterOverlay();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    startGameAction();
                }
            }
        });

        JButton confirmBtn = new JButton("เริ่มต้นการเดินทาง");
        confirmBtn.setFont(new Font("Tahoma", Font.BOLD, 18));
        confirmBtn.setBackground(new Color(255, 105, 180));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFocusPainted(false);
        confirmBtn.setBounds(150, 200, 200, 50);
        confirmBtn.addActionListener(e -> startGameAction());

        box.add(title);
        box.add(warningLabel); 
        box.add(inputField);
        box.add(confirmBtn);
        
        registerOverlay.add(box);
        registerOverlay.add(dimmer);

        // วางใน Layer บนสุด 
        layeredPane.add(registerOverlay, Integer.valueOf(300));
    }

    private void showRegisterUI() {
        // รีเซ็ตค่าและแสดงหน้าต่างที่ซ่อนอยู่
        inputField.setText("");
        inputField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(255, 182, 193)));
        warningLabel.setVisible(false);
        applyResponsiveLayout(); // จัดตำแหน่งให้เป๊ะก่อนโชว์
        registerOverlay.setVisible(true);
        inputField.requestFocusInWindow();
    }

    private void closeRegisterOverlay() {
        registerOverlay.setVisible(false);
        Main.mainFrame.requestFocusInWindow();
    }

    private void startGameAction() {
        String name = inputField.getText().trim();
        if (name.isEmpty()) {
            warningLabel.setVisible(true);
            inputField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.RED));
            registerOverlay.repaint();
            return; 
        }
        
        GameConstants.PLAYER_NAME = name;
        Relation.getInstance().resetAll();
        UI_Components.RelationUI.getInstance().updateAllScores();
        
        for (Component comp : core.Main.mainContainer.getComponents()) {
            if (comp instanceof UI_Screens.PlaySceneMain) {
                ((UI_Screens.PlaySceneMain) comp).loadNewScene(StoryData.SCENE_1, "SCENE_1");
                break;
            }
        }
        
        closeRegisterOverlay();
        startFadeOutAction();
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
                    Main.brightnessAlpha = 0.0f; 
                    Main.repaintBrightness();
                    Main.cardLayout.show(Main.mainContainer, "PLAY_PAGE"); 
                } else {
                    Main.brightnessAlpha = alpha;
                    Main.repaintBrightness();
                }
            }
        });
        fadeOutTimer.start();
    }

    private void applyResponsiveLayout() {
        int w = getWidth(); int h = getHeight();
        if (w <= 0 || h <= 0) return;

        mainContentPanel.setBounds(0, 0, w, h);
        
        if (registerOverlay != null) {
            registerOverlay.setBounds(0, 0, w, h);
            // อัปเดตขนาดให้ dimmer และ box
            if (registerOverlay.getComponentCount() > 1) {
                Component box = registerOverlay.getComponent(0);
                Component dimmer = registerOverlay.getComponent(1);
                dimmer.setBounds(0, 0, w, h);
                box.setBounds((w - 500) / 2, (h - 300) / 2, 500, 300);
            }
        }

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

    private void loadGame() {
        try {
            String projectPath = System.getProperty("user.dir");
            String savePath = projectPath + File.separator + "savegame.dat";
            
            if (!Files.exists(Paths.get(savePath))) {
                JOptionPane.showMessageDialog(this, 
                    "❌ ไม่พบไฟล์บันทึกเกม", "ข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String content = new String(Files.readAllBytes(Paths.get(savePath)), "UTF-8");
            String[] lines = content.split("\n");
            
            int storyIndex = 0;
            String sceneName = "SCENE_1";
            int ahriAffection = 0;
            String playerName = "";
            
            for (String line : lines) {
                if (line.startsWith("storyIndex=")) storyIndex = Integer.parseInt(line.substring(11).trim());
                else if (line.startsWith("sceneName=")) sceneName = line.substring(10).trim();
                else if (line.startsWith("playerName=")) playerName = line.substring(11).trim();
                else if (line.startsWith("ahriAffection=")) ahriAffection = Integer.parseInt(line.substring(14).trim());
            }
            
            Relation.getInstance().setAffection("Ahri", ahriAffection);
            UI_Components.RelationUI.getInstance().updateAllScores();
            
            if (!playerName.isEmpty()) GameConstants.PLAYER_NAME = playerName;
            
            UI_Screens.PlaySceneMain playScene = null;
            for (Component comp : core.Main.mainContainer.getComponents()) {
                if (comp instanceof UI_Screens.PlaySceneMain) {
                    playScene = (UI_Screens.PlaySceneMain) comp;
                    break;
                }
            }
            
            if (playScene != null) {
                Object[][] loadedSceneData = getSceneData(sceneName);
                if(loadedSceneData != null) {
                    playScene.loadNewScene(loadedSceneData, sceneName);
                    
                    Field indexField = playScene.getClass().getDeclaredField("storyIndex");
                    indexField.setAccessible(true);
                    indexField.set(playScene, storyIndex);
                    
                    playScene.updateScene(loadedSceneData[storyIndex]);
                    
                    JOptionPane.showMessageDialog(this, 
                        "✅ โหลดเกมสำเร็จ!\nซีน: " + sceneName + "\nความสัมพันธ์ Ahri: " + ahriAffection,
                        "โหลดเกม", JOptionPane.INFORMATION_MESSAGE);
                        
                    stopMenuMusic();
                    core.Main.cardLayout.show(core.Main.mainContainer, "PLAY_SCENE");
                }
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ โหลดเกมล้มเหลว: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private Object[][] getSceneData(String sceneName) {
        try {
            Class<?> storyDataClass = Class.forName("model.StoryData");
            Field sceneField = storyDataClass.getDeclaredField(sceneName);
            sceneField.setAccessible(true);
            return (Object[][]) sceneField.get(null);
        } catch (Exception ex) { return null; }
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

    private void startMenuMusic() {
        String bgmPath = GameConstants.SOUND_PATH + "music_mainmenu.wav"; 
        Main.soundManager.playBGM(bgmPath); 
    }

    public void stopMenuMusic() {
        if (bgmLoopTimer != null) bgmLoopTimer.stop();
        Main.soundManager.stopBGM(); 
    }
}