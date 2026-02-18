package core; 

import javax.swing.*;
import java.awt.*;
import UI_Screens.*; 
import model.StoryData;    
import model.GameConstants; 

public class Main {
    public static JFrame mainFrame;
    public static CardLayout cardLayout = new CardLayout();
    public static JPanel mainContainer = new JPanel(cardLayout);
    
    public static float brightnessAlpha = 0.0f;
    public static Color overlayColor = Color.BLACK;
    private static JPanel brightnessOverlay;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            mainFrame = new JFrame("HeartToHeart");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            // ✅ 1. ยอมให้ขยายจอใหญ่ได้ (Resizable = true)
            mainFrame.setResizable(true); 

            // ✅ 2. ตั้งขนาด "พื้นที่เล่นเกม" ที่ต้องการคือ 1920x1080
            mainContainer.setPreferredSize(new Dimension(1920, 1080)); 
            
            setupScreens();
            mainFrame.add(mainContainer);

            // ✅ 3. สั่ง pack() เพื่อคำนวณขนาด (800x600 + ขอบหน้าต่าง/แถบชื่อด้านบน)
            mainFrame.pack(); 

            // ✅ 4. 🔥 จุดสำคัญที่สุด: ล็อคกำแพงขั้นต่ำทันทีหลัง pack
            // เราใช้ค่าที่ pack ออกมา (ซึ่งคือขนาด 800x600 รวมขอบพอดี) 
            // มาตั้งเป็น MinimumSize เพื่อไม่ให้มือผู้เล่นลากหดจอไปได้มากกว่านี้ค่ะ
            mainFrame.setMinimumSize(mainFrame.getSize()); 

            mainFrame.setLocationRelativeTo(null); 
            initBrightnessSystem();
            mainFrame.setVisible(true);
        });
    }

    private static void setupScreens() {
        Font titleFont = new Font("Tahoma", Font.BOLD, 80); 
        Font menuFont = new Font("Tahoma", Font.BOLD, 24);  
        Font subTitleFont = new Font("Tahoma", Font.BOLD, 40);

        // โหลดหน้าจอตามปกติ
        mainContainer.add(new MainMenu(titleFont, menuFont), "MENU");
        mainContainer.add(new SettingPage(subTitleFont, menuFont), "SETTING");
        mainContainer.add(new CreditPage(subTitleFont, menuFont), "CREDIT");
        mainContainer.add(new PlayPage(subTitleFont), "PLAY");
        mainContainer.add(new PlaySceneMain(
            StoryData.SCENE_1, 
            GameConstants.BG_SCHOOL, 
            GameConstants.CHAR_AHRI
        ), "PLAY_SCENE");
    }

    private static void initBrightnessSystem() {
        brightnessOverlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (mainFrame == null) return;
                Graphics2D g2d = (Graphics2D) g;
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, brightnessAlpha));
                g2d.setColor(overlayColor);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        brightnessOverlay.setOpaque(false);
        mainFrame.setGlassPane(brightnessOverlay);
        brightnessOverlay.setVisible(true);
    }

    public static void repaintBrightness() {
        if (brightnessOverlay != null) brightnessOverlay.repaint();
    }
}