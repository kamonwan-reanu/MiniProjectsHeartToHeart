import javax.swing.*;
import java.awt.*;

public class Main {
    public static JFrame mainFrame;
    public static CardLayout cardLayout = new CardLayout();
    public static JPanel mainContainer = new JPanel(cardLayout);
    
    // สถานะระบบสำหรับปรับความมืด (Brightness)
    public static float brightnessAlpha = 0.0f;
    public static Color overlayColor = Color.BLACK;
    private static JPanel brightnessOverlay;

    public static void main(String[] args) {
        mainFrame = new JFrame("HeartToHeart");
        
        // --- [1. ตั้งค่าพื้นฐานป้องกันจอเพี้ยน] ---
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // กำหนดขนาดพื้นที่แสดงผลข้างใน (Content Pane) ให้เป็น 1920x1080
        // วิธีนี้จะทำให้พื้นที่สีขาวข้างในตรงเป๊ะ ไม่รวมขอบหน้าต่าง Windows
        mainContainer.setPreferredSize(new Dimension(1920, 1080)); 
        mainFrame.setResizable(true); 

        // เริ่มต้นระบบปรับความสว่าง (GlassPane)
        initBrightnessSystem();

        // สร้าง Font มาตรฐานที่ใช้ในแต่ละหน้า
        Font titleFont = new Font("Tahoma", Font.BOLD, 80);
        Font menuFont = new Font("Tahoma", Font.BOLD, 24);
        Font subTitleFont = new Font("Tahoma", Font.BOLD, 40);

        // --- [2. เพิ่มหน้าจอต่างๆ ลงในระบบ CardLayout] ---
        // ตรวจสอบว่า Class เหล่านี้ถูกสร้างไว้แล้วในโปรเจกต์ของคุณ
        mainContainer.add(new MainMenu(titleFont, menuFont), "MENU");
        mainContainer.add(new SettingPage(subTitleFont, menuFont), "SETTING");
        mainContainer.add(new CreditPage(subTitleFont, menuFont), "CREDIT");
        mainContainer.add(new PlayPage(subTitleFont), "PLAY");
        
        // หน้าเกมหลักที่มี UI กล่องข้อความและตัวละคร
        mainContainer.add(new PlaySceneMain(), "GAME_PLAY"); 

        // --- [3. การประกอบร่าง UI] ---
        mainFrame.add(mainContainer);
        
        // pack() จะทำให้ JFrame หดมาล้อมรอบ 1920x1080 พอดีเป๊ะ
        mainFrame.pack(); 
        mainFrame.setLocationRelativeTo(null); // วางกึ่งกลางหน้าจอคอมพิวเตอร์

        // แสดงหน้าแรก (ในที่นี้คือหน้าเมนูหลัก)
        cardLayout.show(mainContainer, "MENU"); 
        
        mainFrame.setVisible(true);
    }

    /**
     * ระบบ Brightness โดยใช้ GlassPane เพื่อทำ Layer มืดทับทั้งเกม
     */
    private static void initBrightnessSystem() {
        brightnessOverlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                // ตั้งค่าความเนียนของกราฟิก
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // ตั้งค่าความโปร่งใสตามตัวแปร brightnessAlpha
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, brightnessAlpha));
                g2d.setColor(overlayColor);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        brightnessOverlay.setOpaque(false);
        mainFrame.setGlassPane(brightnessOverlay);
        brightnessOverlay.setVisible(true);
    }

    /**
     * สั่งให้ Layer ความมืดวาดใหม่เมื่อมีการปรับตั้งค่า
     */
    public static void repaintBrightness() {
        if (brightnessOverlay != null) brightnessOverlay.repaint();
    }
}