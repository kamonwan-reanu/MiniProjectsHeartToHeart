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
        SwingUtilities.invokeLater(() -> {
            try {
                mainFrame = new JFrame("HeartToHeart");
                mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                mainFrame.setResizable(true); 

                setupScreens();
                
                mainFrame.add(mainContainer);
                
                // ✅ แก้จาก pack() เป็น setSize เพื่อให้เปิดมาเป็น 1920x1080 ทันที
                mainFrame.setSize(1920, 1080); 
                mainFrame.setLocationRelativeTo(null); 
                
                initBrightnessSystem();
                soundManager.setVolume(0.5f);
                
                mainFrame.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private static void setupScreens() {
        Font titleFont = new Font("Tahoma", Font.BOLD, 80); 
        Font menuFont = new Font("Tahoma", Font.BOLD, 24);  
        Font subTitleFont = new Font("Tahoma", Font.BOLD, 40);

        try {
            mainContainer.add(new MainMenu(titleFont, menuFont), "MENU");
            mainContainer.add(new SettingPage(subTitleFont, menuFont), "SETTING");
            mainContainer.add(new CreditPage(subTitleFont, menuFont), "CREDIT");
            mainContainer.add(new PlayPage(subTitleFont), "PLAY");
            
            PlaySceneMain gameplayScene = new PlaySceneMain(StoryData.SCENE_2, null, "SCENE_2");
            mainContainer.add(gameplayScene, "PLAY_SCENE");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initBrightnessSystem() {
        brightnessOverlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
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