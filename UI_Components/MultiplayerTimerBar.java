package UI_Components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * MultiplayerTimerBar - แถบ Timer แบบ TFT ที่แสดงบน PlayScene
 * 
 * แสดง:
 * - เวลานับถอยหลัง (วินาที)
 * - ชื่อรอบ (เช่น ซีน 1/5)
 * - จำนวนคนที่ถึง Choice แล้ว
 * - ล็อคปุ่ม Choice จนกว่าทุกคนถึง หรือเวลาหมด
 */
public class MultiplayerTimerBar extends JPanel {

    private int totalSeconds;
    private int remainingSeconds;
    private int playersReady;
    private int totalPlayers;
    private String roundLabel = "ซีน 1";
    private boolean choiceLocked = true;  // ล็อคปุ่มเลือก
    private Runnable onUnlock;            // callback เมื่อปลดล็อค

    private javax.swing.Timer countdownTimer;

    private static final Color BAR_BG    = new Color(20, 20, 40, 220);
    private static final Color BAR_FULL  = new Color(100, 220, 100);
    private static final Color BAR_MID   = new Color(255, 200, 50);
    private static final Color BAR_LOW   = new Color(255, 80, 80);
    private static final Color TEXT_COL  = Color.WHITE;
    private static final Font  F_BOLD_16 = new Font("Tahoma", Font.BOLD, 16);
    private static final Font  F_BOLD_20 = new Font("Tahoma", Font.BOLD, 20);

    public MultiplayerTimerBar() {
        setOpaque(false);
        setPreferredSize(new Dimension(400, 55));
    }

    // เริ่มนับถอยหลัง
    public void startTimer(int seconds, int players) {
        this.totalSeconds     = seconds;
        this.remainingSeconds = seconds;
        this.totalPlayers     = players;
        this.playersReady     = 0;
        this.choiceLocked     = true;

        if (countdownTimer != null) countdownTimer.stop();

        countdownTimer = new javax.swing.Timer(1000, e -> {
            remainingSeconds--;
            if (remainingSeconds <= 0) {
                remainingSeconds = 0;
                ((javax.swing.Timer)e.getSource()).stop();
                unlock(); // เวลาหมด → ปลดล็อค
            }
            repaint();
        });
        countdownTimer.start();
        setVisible(true);
        repaint();
    }

    // เมื่อผู้เล่นถึง Choice
    public void playerReachedChoice() {
        playersReady++;
        if (playersReady >= totalPlayers) {
            unlock(); // ทุกคนถึงแล้ว → ปลดล็อคทันที
        }
        repaint();
    }

    // ปลดล็อคปุ่มเลือก
    private void unlock() {
        if (!choiceLocked) return;
        choiceLocked = false;
        if (countdownTimer != null) countdownTimer.stop();
        if (onUnlock != null) onUnlock.run();
        repaint();
    }

    public void stopTimer() {
        if (countdownTimer != null) countdownTimer.stop();
        setVisible(false);
    }

    public void setRoundLabel(String label)  { this.roundLabel = label; repaint(); }
    public void setOnUnlock(Runnable r)      { this.onUnlock = r; }
    public boolean isChoiceLocked()          { return choiceLocked; }
    public void forceUnlock()                { unlock(); }

    @Override
    protected void paintComponent(Graphics g) {
        if (!isVisible()) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // พื้นหลัง
        g2.setColor(BAR_BG);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 16, 16));

        // Progress bar เวลา
        double ratio = totalSeconds > 0 ? (double)remainingSeconds / totalSeconds : 0;
        Color barColor = ratio > 0.5 ? BAR_FULL : (ratio > 0.25 ? BAR_MID : BAR_LOW);
        int barW = (int)((w - 20) * ratio);
        g2.setColor(barColor);
        g2.fillRoundRect(10, h - 12, barW, 7, 6, 6);

        // ข้อความ
        g2.setFont(F_BOLD_16);
        g2.setColor(TEXT_COL);
        g2.drawString(roundLabel, 14, 20);

        // เวลา
        String timeStr = remainingSeconds + " วิ";
        g2.setFont(F_BOLD_20);
        g2.setColor(barColor);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(timeStr, w/2 - fm.stringWidth(timeStr)/2, 24);

        // จำนวนคนพร้อม
        String readyStr = "พร้อม: " + playersReady + "/" + totalPlayers;
        g2.setFont(F_BOLD_16);
        g2.setColor(choiceLocked ? new Color(255,200,100) : new Color(100,255,100));
        FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString(readyStr, w - fm2.stringWidth(readyStr) - 14, 20);

        // สถานะล็อค
        if (choiceLocked) {
            g2.setColor(new Color(255, 150, 50));
            g2.setFont(F_BOLD_16);
            g2.drawString("[ รอผู้เล่นอื่น... ]", 14, h - 17);
        } else {
            g2.setColor(new Color(100, 255, 120));
            g2.setFont(F_BOLD_16);
            g2.drawString("[ เลือกได้แล้ว! ]", 14, h - 17);
        }

        g2.dispose();
    }
}