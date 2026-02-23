package UI_Components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ChoiceButton extends JPanel {
    private String number;
    private String text;
    private boolean isHovered = false;
    private Runnable onClick;

    // 🎨 ปรับ Alpha เป็น 180-200 ตามมาตรฐานกล่องข้อความหลักของ Ahri
    // ใช้สีกรมท่า (BG_DARK) ตัวเดียวกับ DialogueBox เพื่อความเนียน
    // 🎨 ปรับ Alpha ให้เท่ากับ DialogueBox (230 สำหรับพื้นหลัง, 180 สำหรับเส้นขอบ)
    private final Color BLUE_BOX = new Color(70, 170, 235, 230);        // พื้นหลังเลข (Alpha 230)
    private final Color BLUE_BOX_HOVER = new Color(100, 200, 255, 255);  // ตอนชี้ให้ทึบแสงไปเลยจะได้เด่น
    private final Color BG_DARK = new Color(15, 20, 35, 230);            // พื้นหลังกรมท่า (Alpha 230 เท่ากันเป๊ะ)
    private final Color GOLD_BORDER = new Color(212, 175, 55, 180);      // เส้นขอบสีทอง (Alpha 180 ตามขอบกล่องหลัก)

    public ChoiceButton(String number, String text, Runnable onClick) {
        this.number = number;
        this.text = text;
        this.onClick = onClick;
        
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
            @Override
            public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
            @Override
            public void mousePressed(MouseEvent e) { if (onClick != null) onClick.run(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int h = getHeight();
        int w = getWidth();
        int numBoxW = 50; 
        int padding = 4;

        // 🟦 1. วาดกล่องตัวเลข (ฝั่งซ้าย) - ใช้ค่าความโปร่งใสที่เท่ากัน
        g2d.setColor(isHovered ? BLUE_BOX_HOVER : BLUE_BOX);
        g2d.fillRect(0, padding, numBoxW, h - (padding * 2));
        
        // ✨ ขอบสีทองของกล่องตัวเลข
        g2d.setColor(GOLD_BORDER);
        g2d.setStroke(new BasicStroke(1.5f)); 
        g2d.drawRect(0, padding, numBoxW, h - (padding * 2) - 1);

        // ⬛ 2. วาดกล่องข้อความ (ฝั่งขวา) - สีและ Alpha เท่ากับ DialogueBox เป๊ะ
        g2d.setColor(isHovered ? new Color(30, 40, 65, 200) : BG_DARK);
        g2d.fillRect(numBoxW + 5, padding, w - numBoxW - 5, h - (padding * 2));

        // ⚜️ 3. วาดเส้นขอบสีทองรอบกล่องข้อความ
        g2d.setColor(GOLD_BORDER);
        g2d.drawRect(numBoxW + 5, padding, w - numBoxW - 6, h - (padding * 2) - 1);

        // ✍️ 4. วาดตัวเลข
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Tahoma", Font.BOLD, 20));
        FontMetrics fmNum = g2d.getFontMetrics();
        int numX = (numBoxW - fmNum.stringWidth(number)) / 2;
        int numY = ((h - fmNum.getHeight()) / 2) + fmNum.getAscent();
        g2d.drawString(number, numX, numY);

        // ✍️ 5. วาดข้อความตัวเลือก
        g2d.setColor(isHovered ? Color.WHITE : new Color(240, 240, 240));
        g2d.setFont(new Font("Tahoma", Font.PLAIN, 18));
        FontMetrics fmText = g2d.getFontMetrics();
        int textY = ((h - fmText.getHeight()) / 2) + fmText.getAscent();
        g2d.drawString(text, numBoxW + 25, textY);
    }
}