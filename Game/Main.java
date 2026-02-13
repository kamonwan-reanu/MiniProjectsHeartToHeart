import javax.swing.*;
import java.awt.*;

public class Main {
    public static JFrame mainFrame;
    public static CardLayout cardLayout = new CardLayout();
    public static JPanel mainContainer = new JPanel(cardLayout);
    
    // สถานะระบบ (ย้ายมาเป็น public static เพื่อให้ไฟล์อื่นเรียกใช้ได้)
    public static float brightnessAlpha = 0.0f;
    public static Color overlayColor = Color.BLACK;
    private static JPanel brightnessOverlay;

    public static void main(String[] args) {
        mainFrame = new JFrame("HeartToHeart");
        mainFrame.setSize(1920, 1080); // เริ่มต้นที่ 1080p ตามสั่ง
        mainFrame.setUndecorated(false);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);

        initBrightnessSystem();

        // สร้าง Font มาตรฐาน
        Font titleFont = new Font("Tahoma", Font.BOLD, 80);
        Font menuFont = new Font("Tahoma", Font.BOLD, 24);
        Font subTitleFont = new Font("Tahoma", Font.BOLD, 40);

        // ดึงหน้าจอจาก Class ต่างๆ มาใส่ Container
        mainContainer.add(new MainMenu(titleFont, menuFont), "MENU");
        mainContainer.add(new SettingPage(subTitleFont, menuFont), "SETTING");
        mainContainer.add(new CreditPage(subTitleFont, menuFont), "CREDIT");

        mainFrame.add(mainContainer);
        mainFrame.setVisible(true);
    }

    private static void initBrightnessSystem() {
        brightnessOverlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
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

    // ฟังก์ชันให้ไฟล์อื่นสั่ง Repaint ม่านแสง
    public static void repaintBrightness() {
        if (brightnessOverlay != null) brightnessOverlay.repaint();
    }
}