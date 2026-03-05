package UI_Components;

import javax.swing.*;
import java.awt.*;
import model.GameState;

public class HeartHUDPanel extends JPanel {

    private Image heartFull;
    private Image heartEmpty;

    public HeartHUDPanel() {
        setOpaque(false);

        heartFull  = new ImageIcon("model/img_ui/fullheart.png").getImage();
        heartEmpty = new ImageIcon("model/img_ui/emptyheart.png").getImage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // =============================
        // 🌫 Glassmorphism Background
        // =============================

        // ชั้นโปร่ง
        g2.setColor(new Color(255, 255, 255, 40));
        g2.fillRoundRect(0, 0, w, h, 25, 25);

        // ขอบกระจก
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(new Color(255, 255, 255, 120));
        g2.drawRoundRect(0, 0, w - 1, h - 1, 25, 25);

        // เงาด้านล่าง
        g2.setColor(new Color(0, 0, 0, 80));
        g2.drawRoundRect(1, 1, w - 3, h - 3, 25, 25);

        // =============================
        // 📝 หัวข้อ
        // =============================

        g2.setFont(new Font("Tahoma", Font.BOLD, 16));
        g2.setColor(new Color(255, 255, 255, 230));
        g2.drawString("ระดับความสัมพันธ์", 20, 30);

        // =============================
        // ❤️ แถวหัวใจ (ชื่อไทย)
        // =============================

        drawRow(g2, 20, 45, "ธีร์", GameState.teerHeart);
        drawRow(g2, 20, 75, "คีริน", GameState.kirinHeart);
        drawRow(g2, 20, 105, "เทียน", GameState.tianHeart);

        g2.dispose();
    }

    private void drawRow(Graphics2D g2, int x, int y, String name, int score) {

        g2.setFont(new Font("Tahoma", Font.PLAIN, 14));
        g2.setColor(new Color(255, 255, 255, 220));
        g2.drawString(name, x, y + 15);

        for (int i = 0; i < 2; i++) {
            Image img = (score > i) ? heartFull : heartEmpty;
            g2.drawImage(img, x + 80 + (i * 26), y, 20, 20, null);
        }
    }
}