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

        buttons = new JButton[]{ // แก้ไข syntax error ในการประกาศ array ของปุ่ม
            new JButton("เริ่มเกม"), new JButton("โหลดเกม"), 
            new JButton("ตั้งค่า"), new JButton("เกี่ยวกับคนสร้าง"), 
            new JButton("ออกจากเกม")
        };

        for (JButton btn : buttons) {
            btn.setBackground(Color.WHITE);
            btn.setFocusable(false);
            btn.setBorder(BorderFactory.createLineBorder(new Color(255, 182, 193), 2));
            buttonBox.add(btn);
        }

        // --- ปุ่มเริ่มเกม: เรียกใช้ UI ในเกมแทน JOptionPane ---
        buttons[0].addActionListener(e -> showRegisterUI());
        buttons[1].addActionListener(e -> loadGame());
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

    // 🌸 ฟังก์ชันแสดงหน้ากรอกชื่อแบบ UI ในเกม (เวอร์ชันล็อคปุ่มข้างหลัง)
    private void showRegisterUI() {
        model.GameConstants.PLAYER_NAME = ""; 

        if (registerOverlay != null) {
            layeredPane.remove(registerOverlay);
            registerOverlay = null;
        }

        registerOverlay = new JPanel(null);
        registerOverlay.setOpaque(false);
        registerOverlay.setBounds(0, 0, getWidth(), getHeight());

        JPanel dimmer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0, 0, 0, 120));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        dimmer.setBounds(0, 0, getWidth(), getHeight());

        MouseAdapter lockMouse = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { e.consume(); }
            @Override public void mousePressed(MouseEvent e) { e.consume(); }
            @Override public void mouseReleased(MouseEvent e) { e.consume(); }
        };
        dimmer.addMouseListener(lockMouse);
        
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
        box.addMouseListener(lockMouse);

        JLabel title = new JLabel("กรุณาระบุชื่อของคุณ");
        title.setFont(new Font("Tahoma", Font.BOLD, 24));
        title.setForeground(new Color(255, 105, 180));
        title.setBounds(0, 30, 500, 40); // ขยับขึ้นนิดนึง
        title.setHorizontalAlignment(SwingConstants.CENTER);

        // ✨ [เพิ่ม] ข้อความแจ้งเตือน (ซ่อนไว้ก่อน)
        JLabel warningLabel = new JLabel("! กรุณาใส่ชื่อตัวละครก่อนเริ่มต้นการเดินทาง !");
        warningLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        warningLabel.setForeground(Color.RED);
        warningLabel.setBounds(0, 75, 500, 25);
        warningLabel.setHorizontalAlignment(SwingConstants.CENTER);
        warningLabel.setVisible(false); // ปิดไว้ก่อน

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
            if (name.isEmpty()) {
                warningLabel.setVisible(true);
                inputField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.RED));
                box.repaint();
                return; 
            }
            
            model.GameConstants.PLAYER_NAME = name;
            
            // 1. รีเซ็ตคะแนนความสัมพันธ์ทั้งหมดกลับเป็น 0
            model.Relation.getInstance().resetAll();
            UI_Components.RelationUI.getInstance().updateAllScores();
            
            // 2. บังคับให้ PlaySceneMain กลับไปที่ SCENE_1 เสมอ
            for (Component comp : core.Main.mainContainer.getComponents()) {
                if (comp instanceof UI_Screens.PlaySceneMain) {
                    ((UI_Screens.PlaySceneMain) comp).loadNewScene(model.StoryData.SCENE_1, "SCENE_1");
                    break;
                }
            }
            if (registerOverlay != null) {
                layeredPane.remove(registerOverlay);
                registerOverlay = null; 
            }
            
            startFadeOutAction();
        });
        box.add(title);
        box.add(warningLabel); 
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
        if (registerOverlay != null) {
            layeredPane.remove(registerOverlay);
            registerOverlay = null;
            layeredPane.revalidate();
            layeredPane.repaint();
        }
        stopMenuMusic();
        
        Timer fadeOutTimer = new Timer(20, new ActionListener() {
            float alpha = 0.0f;
            @Override
            public void actionPerformed(ActionEvent e) {
                alpha += 0.05f;
                if (alpha >= 1.0f) {
                    ((Timer)e.getSource()).stop();
                    core.Main.brightnessAlpha = 0.0f; 
                    core.Main.repaintBrightness();
                    core.Main.cardLayout.show(core.Main.mainContainer, "PLAY_PAGE"); 
                } else {
                    core.Main.brightnessAlpha = alpha;
                    core.Main.repaintBrightness();
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

    private void loadGame() {
        try {
            String projectPath = System.getProperty("user.dir");
            String savePath = projectPath + File.separator + "savegame.dat";
            
            System.out.println("กำลังโหลดไฟล์จาก: " + savePath);
            
            if (!Files.exists(Paths.get(savePath))) {
                JOptionPane.showMessageDialog(this, 
                    " ไม่พบไฟล์บันทึกเกม", 
                    "ข้อผิดพลาด", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // อ่านข้อมูลจากไฟล์
            String content = new String(Files.readAllBytes(Paths.get(savePath)), "UTF-8");
            String[] lines = content.split("\n");
            
            int storyIndex = 0;
            String sceneName = "SCENE_1";
            int ahriAffection = 0;
            String playerName = "";
            
            for (String line : lines) {
                if (line.startsWith("storyIndex=")) {
                    storyIndex = Integer.parseInt(line.substring(11));
                } else if (line.startsWith("sceneName=")) {
                    sceneName = line.substring(10);
                } else if (line.startsWith("playerName=")) {
                    playerName = line.substring(11);
                } else if (line.startsWith("ahriAffection=")) {
                    ahriAffection = Integer.parseInt(line.substring(14));
                }
            }
            
            // โหลดข้อมูลความสัมพันธ์
            try {
                Relation.getInstance().setAffection("Ahri", ahriAffection);
                UI_Components.RelationUI.getInstance().updateAllScores();
                System.out.println("ตั้งค่าความสัมพันธ์ Ahri: " + ahriAffection);
            } catch (Exception ex) {
                System.err.println("ไม่สามารถตั้งค่าความสัมพันธ์: " + ex.getMessage());
            }
            
            // ตั้งชื่อผู้เล่น
            if (!playerName.isEmpty()) {
                GameConstants.PLAYER_NAME = playerName;
                System.out.println("ตั้งชื่อผู้เล่น: " + playerName);
            }
            
            System.out.println("โหลดข้อมูล: sceneName=" + sceneName + ", storyIndex=" + storyIndex);
            
            // ค้นหา PlaySceneMain ในหน้าจอหลัก
            UI_Screens.PlaySceneMain playScene = null;
            for (Component comp : core.Main.mainContainer.getComponents()) {
                if (comp instanceof UI_Screens.PlaySceneMain) {
                    playScene = (UI_Screens.PlaySceneMain) comp;
                    break;
                }
            }
            
            if (playScene != null) {
                // 1. ดึงข้อมูลฉากเป้าหมาย
                Object[][] loadedSceneData = getSceneData(sceneName);

                // 2. เรียกใช้เมธอด public เพื่อตั้งค่าฉากเบื้องต้น (อันนี้จะรีเซ็ต storyIndex เป็น 0)
                playScene.loadNewScene(loadedSceneData, sceneName);
                
                // 3. ใช้ Reflection เพื่อยัดค่า storyIndex ที่โหลดมากลับเข้าไป
                Field indexField = playScene.getClass().getDeclaredField("storyIndex");
                indexField.setAccessible(true);
                indexField.set(playScene, storyIndex);

                // 4. บังคับให้อัปเดตหน้าจอเพื่อดึงข้อความของประโยคที่ถูกต้องมาแสดง
                playScene.updateScene(loadedSceneData[storyIndex]);

                // 5. เปลี่ยนหน้าจอไปยังฉากเล่นเกม
                core.Main.cardLayout.show(core.Main.mainContainer, "PLAY_SCENE");
                
                JOptionPane.showMessageDialog(this, 
                    "✅ โหลดเกมสำเร็จ!\n" +
                    "ซีน: " + sceneName + "\n" +
                    "ดัชนี: " + storyIndex + "\n" +
                    "ความสัมพันธ์ Ahri: " + ahriAffection,
                    "โหลดเกม",
                    JOptionPane.INFORMATION_MESSAGE);
                    
                System.out.println("ข้ามไปหน้าจอ PLAY_SCENE แล้ว");
            } else {
                System.err.println("ไม่พบ PlaySceneMain สำหรับโหลดข้อมูล");
            }
                
        } catch (Exception ex) {
            System.err.println("ไม่สามารถโหลดเกมได้: " + ex.getMessage());
            ex.printStackTrace();
            
            JOptionPane.showMessageDialog(this, 
                "❌ ไม่สามารถโหลดเกมได้: " + ex.getMessage(), 
                "ข้อผิดพลาด", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private Object[][] getSceneData(String sceneName) {
        try {
            // ใช้ Reflection เพื่อดึงข้อมูลซีนจาก StoryData
            Class<?> storyDataClass = Class.forName("model.StoryData");
            Field sceneField = storyDataClass.getDeclaredField(sceneName);
            sceneField.setAccessible(true);
            return (Object[][]) sceneField.get(null);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
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
