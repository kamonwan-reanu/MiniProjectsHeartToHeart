package core; 

import javax.swing.*;
import java.awt.*;
import UI_Screens.*; 
import model.StoryData;    
import model.SoundManager; 

public class Main {
    public static JFrame mainFrame;
    public static CardLayout cardLayout = new CardLayout();
    public static JPanel mainContainer = new JPanel(cardLayout);
    public static SoundManager soundManager = new SoundManager(); 
    
    public static float brightnessAlpha = 0.0f;
    public static Color overlayColor = Color.BLACK;
    private static JPanel brightnessOverlay;

    public static void main(String[] args) {
        // ✨ ตั้งค่าฟอนต์ภาษาไทยให้ทั้งระบบ (แก้ปัญหาสี่เหลี่ยม)
        setUIFont(new Font("Tahoma", Font.PLAIN, 18));

        SwingUtilities.invokeLater(() -> {
            try {
                mainFrame = new JFrame("HeartToHeart");
                mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                mainFrame.setResizable(true); 

                setupScreens(); 
                
                mainFrame.add(mainContainer);
                mainFrame.setSize(1920, 1080); 
                mainFrame.setLocationRelativeTo(null); 
                
                initBrightnessSystem();
                soundManager.setVolume(0.5f);
                
                mainFrame.setVisible(true);
                cardLayout.show(mainContainer, "MENU");

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // ฟังก์ชันช่วยบังคับฟอนต์ภาษาไทยให้ทุกจุด (Pop-up, ช่องพิมพ์)
    private static void setUIFont(Font font) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, font);
            }
        }
    }

    private static void setupScreens() {
        Font titleFont = new Font("Tahoma", Font.BOLD, 80); 
        Font menuFont = new Font("Tahoma", Font.BOLD, 24);  
        Font subTitleFont = new Font("Tahoma", Font.BOLD, 40);

        try {
            mainContainer.add(new MainMenu(titleFont, menuFont), "MENU");
            mainContainer.add(new SettingPage(subTitleFont, menuFont), "SETTING");
            mainContainer.add(new CreditPage(subTitleFont, menuFont), "CREDIT");
            
            // ใช้ชื่อ Key ว่า PLAY_PAGE ให้ตรงกับในเมนูนะคะ
            mainContainer.add(new PlayPage(subTitleFont), "PLAY_PAGE");
            
            PlaySceneMain gameplayScene = new PlaySceneMain(StoryData.SCENE_1, null, "SCENE_1");
            mainContainer.add(gameplayScene, "PLAY_SCENE");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initBrightnessSystem() {
        brightnessOverlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                // 🛑 ส่วนที่ต้องระวัง: ถ้า brightnessAlpha = 1.0f มันจะดำสนิทจนมองไม่เห็นข้อความด้านล่าง
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, brightnessAlpha));
                g2d.setColor(overlayColor);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
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