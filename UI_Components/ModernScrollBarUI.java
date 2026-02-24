package UI_Components; // ตรวจสอบให้ตรงกับชื่อโฟลเดอร์ของ Ahri นะคะ

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public class ModernScrollBarUI extends BasicScrollBarUI {

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        if (c instanceof JScrollBar) {
            ((JScrollBar) c).setOpaque(false);
        }
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        // ปล่อยว่างไว้เพื่อความโปร่งใส
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // สีทองจางๆ ตามสไตล์กล่องข้อความของ Ahri
        g2.setColor(new Color(212, 175, 55, 120)); 
        g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y, 4, thumbBounds.height, 5, 5);
        g2.dispose();
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createZeroButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createZeroButton();
    }

    private JButton createZeroButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(0, 0));
        return button;
    }
}