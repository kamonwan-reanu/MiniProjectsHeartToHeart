package UI_Components;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import model.Relation;

/**
 * UI หลอดความสัมพันธ์ Ahri แบบพรีเมียม (Singleton)
 */
public class RelationUI extends JPanel {
    
    private static RelationUI instance;
    private int currentScore = 0;
    private final String TARGET_CHAR = "Ahri"; // ล็อคไว้แค่ Ahri ตามคำขอ
    
    // ค่าคงที่สำหรับดีไซน์ใหม่
    private static final int MAX_SCORE = 100;
    private static final int BOX_WIDTH = 220;
    private static final int BOX_HEIGHT = 65;
    private static final int BAR_HEIGHT = 10;
    private static final int PADDING = 12;

    private RelationUI() {
        setOpaque(false);
        setLayout(null);
        setVisible(true);
        updateAllScores();
    }

    public static RelationUI getInstance() {
        if (instance == null) instance = new RelationUI();
        return instance;
    }

    public void updateScore(String name) {
        if (TARGET_CHAR.equals(name)) updateAllScores();
    }

    public void updateAllScores() {
        // ดึงเฉพาะคะแนนของ Ahri มาแสดง
        currentScore = Relation.getInstance().getAffection(TARGET_CHAR);
        repaint();
    }

    public void updateBounds(int sw, int sh) {
        // จัดตำแหน่งไว้มุมขวาบน เว้นระยะจากขอบนิดหน่อยให้ดูแพง
        setBounds(sw - BOX_WIDTH - 25, 25, BOX_WIDTH, BOX_HEIGHT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        // เปิดโหมดเนียนขั้นสุด
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. วาดกล่องพื้นหลัง (สีกรมท่าเข้มโปร่งแสงตาม DialogueBox)
        g2d.setColor(new Color(15, 20, 35, 210));
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

        // 2. วาดขอบสีทองบางๆ (Gold Stroke)
        g2d.setColor(new Color(212, 175, 55, 150));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 15, 15);

        // ---------------------------------------------------------
    // 3. วาดไอคอนหัวใจด้วยรูปทรง (วางแทนการ drawString แบบเดิม)
    // ---------------------------------------------------------
    int heartX = PADDING;
    int heartY = PADDING + 2; // ปรับตำแหน่งขึ้นลงตามความเหมาะสม
    int size = 12; // ขนาดของหัวใจ

    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setColor(new Color(255, 105, 180)); // สีชมพู
    
    // วาดวงกลม 2 วงด้านบนหัวใจ
    g2d.fillOval(heartX, heartY, size/2 + 2, size/2 + 2);
    g2d.fillOval(heartX + size/2 - 1, heartY, size/2 + 2, size/2 + 2);
    
    // วาดสามเหลี่ยมด้านล่างหัวใจ
    int[] xPoints = {heartX, heartX + size + 1, heartX + size/2 + 1};
    int[] yPoints = {heartY + size/2, heartY + size/2, heartY + size};
    g2d.fillPolygon(xPoints, yPoints, 3);

    // วาดชื่อ Ahri และคะแนนต่อท้ายไอคอน
    g2d.setFont(new Font("Tahoma", Font.BOLD, 14));
    g2d.setColor(new Color(255, 255, 255, 230));
    g2d.drawString(TARGET_CHAR + ": " + currentScore + "/" + MAX_SCORE, heartX + size + 8, heartY + 11);

        // 4. วาดหลอด Progress Bar (พื้นหลังหลอด)
        int barY = PADDING + 22;
        int barWidth = getWidth() - (PADDING * 2);
        g2d.setColor(new Color(50, 50, 60, 180));
        g2d.fillRoundRect(PADDING, barY, barWidth, BAR_HEIGHT, 5, 5);

        // 5. วาดตัวหลอดสีชมพู (คะแนน)
        float percentage = Math.max(0, Math.min(1, (float) currentScore / MAX_SCORE));
        int fillWidth = (int) (barWidth * percentage);
        
        if (fillWidth > 0) {
            // ใส่ Gradient ให้หลอดดูมีมิติ
            GradientPaint pinkGrad = new GradientPaint(
                PADDING, barY, new Color(255, 105, 180), 
                PADDING + fillWidth, barY, new Color(255, 182, 193)
            );
            g2d.setPaint(pinkGrad);
            g2d.fillRoundRect(PADDING, barY, fillWidth, BAR_HEIGHT, 5, 5);

            // ✨ เพิ่ม Glossy Effect (แถบเงาสีขาวด้านบนหลอด)
            g2d.setColor(new Color(255, 255, 255, 80));
            g2d.fillRoundRect(PADDING, barY, fillWidth, BAR_HEIGHT / 3, 3, 3);
        }

        g2d.dispose();
    }
}