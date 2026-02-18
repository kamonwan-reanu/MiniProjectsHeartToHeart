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
            
            // ✅ ยอมให้ขยายจอได้
            mainFrame.setResizable(true); 

            // ✅ ตั้งขนาดพื้นที่เล่นเกม (ปรับเป็น 1280x720 หรือ 1920x1080 ตามที่ Ahri ต้องการ)
            mainContainer.setPreferredSize(new Dimension(1920, 1080)); 
            
            setupScreens();
            mainFrame.add(mainContainer);

            mainFrame.pack(); 

            // ✅ ล็อคขนาดขั้นต่ำไม่ให้หดจอเล็กเกินไป
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

        // 1. โหลดหน้าเมนูและหน้าอื่นๆ
        mainContainer.add(new MainMenu(titleFont, menuFont), "MENU");
        mainContainer.add(new SettingPage(subTitleFont, menuFont), "SETTING");
        mainContainer.add(new CreditPage(subTitleFont, menuFont), "CREDIT");
        mainContainer.add(new PlayPage(subTitleFont), "PLAY");

        // 2. ✅ เพิ่ม PLAY_SCENE เข้าไปใน CardLayout ให้ถูกต้อง
        // เราส่ง StoryData.SCENE_1 เพื่อเริ่มที่จุดไข่ปลา หรือ SCENE_2 เพื่อเริ่มที่เนื้อเรื่องเลยก็ได้ค่ะ
        PlaySceneMain gameplayScene = new PlaySceneMain(
            StoryData.SCENE_2,      // ข้อมูลฉากเริ่มต้น
            GameConstants.CHAR_AHRI, // รูปตัวละครหลัก
            "SCENE_2"               // ชื่อฉากเริ่มต้น
        );
        
        mainContainer.add(gameplayScene, "PLAY_SCENE");
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
    
    // ✅ เพิ่ม Method สำหรับสลับหน้าจอให้เรียกใช้ง่ายๆ
    public static void showScreen(String screenName) {
        cardLayout.show(mainContainer, screenName);
    }
}