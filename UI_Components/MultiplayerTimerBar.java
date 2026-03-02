package UI_Components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class MultiplayerTimerBar extends JPanel {

    private int     totalSeconds     = 30;
    private int     remainingSeconds = 30;
    private int     playersReady     = 0;
    private int     totalPlayers     = 2;
    private String  roundLabel       = "ซีน 1";
    private boolean choiceLocked     = true;
    private boolean timerStarted     = false;
    private Runnable onUnlock;

    private javax.swing.Timer countdownTimer;

    private static final Color BG       = new Color(20, 20, 40, 215);
    private static final Color BAR_FULL = new Color(80, 210, 80);
    private static final Color BAR_MID  = new Color(240, 190, 40);
    private static final Color BAR_LOW  = new Color(240, 70, 70);
    private static final Color WHITE    = Color.WHITE;
    private static final Font  F16B = new Font("Tahoma", Font.BOLD, 16);
    private static final Font  F22B = new Font("Tahoma", Font.BOLD, 22);

    public MultiplayerTimerBar() {
        setOpaque(false);
        setPreferredSize(new Dimension(460, 62));
        setVisible(false);
    }

    /** เริ่มนับถอยหลัง */
    public void startTimer(int seconds, int players) {
        if (seconds <= 0) seconds = 30;
        if (players <= 0) players = 1;

        this.totalSeconds      = seconds;
        this.remainingSeconds  = seconds;
        this.totalPlayers      = players;
        this.playersReady      = 0;
        this.choiceLocked      = (players > 1);
        this.timerStarted      = true;

        if (countdownTimer != null) countdownTimer.stop();

        if (players <= 1) {
            // Solo — ปลดล็อคทันที
            choiceLocked = false;
            setVisible(true);
            if (onUnlock != null) SwingUtilities.invokeLater(onUnlock);
            repaint();
            return;
        }

        // Multiplayer — นับถอยหลัง
        final javax.swing.Timer t = new javax.swing.Timer(1000, null);
        t.addActionListener(e -> {
            if (remainingSeconds > 0) remainingSeconds--;
            if (remainingSeconds <= 0) {
                t.stop();
                unlock();
            }
            repaint();
        });
        countdownTimer = t;

        setVisible(true);
        repaint();

        // delay 100ms ให้ paint ครั้งแรกก่อน แล้วค่อยเริ่มนับ
        javax.swing.Timer delay = new javax.swing.Timer(100, e2 -> t.start());
        delay.setRepeats(false);
        delay.start();
    }

    /** ตัวเองถึง choice แล้ว */
    public void playerReachedChoice() {
        playersReady = Math.min(playersReady + 1, totalPlayers);
        if (playersReady >= totalPlayers) unlock();
        repaint();
    }

    /** ผู้เล่นอื่นถึง choice แล้ว (รับจาก network) */
    public void otherPlayerReady() {
        playerReachedChoice();
    }

    private void unlock() {
        if (!choiceLocked) return;
        choiceLocked = false;
        if (countdownTimer != null) countdownTimer.stop();
        if (onUnlock != null) SwingUtilities.invokeLater(onUnlock);
        repaint();
    }

    /** หยุด timer และซ่อน */
    public void stopTimer() {
        if (countdownTimer != null) countdownTimer.stop();
        timerStarted  = false;
        choiceLocked  = false;
        setVisible(false);
    }

    /** reset ทุกอย่างและซ่อน — เรียกตอน reset game */
    public void resetAndHide() {
        if (countdownTimer != null) countdownTimer.stop();
        remainingSeconds = 0;
        playersReady     = 0;
        choiceLocked     = true;
        timerStarted     = false;
        setVisible(false);
    }

    public void setRoundLabel(String label) { this.roundLabel = label; repaint(); }
    public void setOnUnlock(Runnable r)     { this.onUnlock = r; }
    public boolean isChoiceLocked()        { return choiceLocked; }
    public void forceUnlock()              { unlock(); }
    public int getRemainingSeconds()       { return remainingSeconds; }

    @Override
    protected void paintComponent(Graphics g) {
        if (!isVisible() || !timerStarted) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) { g2.dispose(); return; }

        // พื้นหลัง
        g2.setColor(BG);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 18, 18));

        // Progress bar track
        double ratio = (totalSeconds > 0) ? Math.max(0, (double) remainingSeconds / totalSeconds) : 0;
        Color barColor = (ratio > 0.5) ? BAR_FULL : (ratio > 0.25 ? BAR_MID : BAR_LOW);
        g2.setColor(new Color(40, 40, 60));
        g2.fillRoundRect(10, h - 13, w - 20, 8, 6, 6);
        int barW = (int)((w - 20) * ratio);
        if (barW > 0) {
            g2.setColor(barColor);
            g2.fillRoundRect(10, h - 13, barW, 8, 6, 6);
        }

        // ชื่อซีน (ซ้าย)
        g2.setFont(F16B);
        g2.setColor(WHITE);
        g2.drawString(roundLabel, 14, 22);

        // เวลา (กลาง)
        String timeStr = choiceLocked ? (remainingSeconds + " วิ") : "เลือกได้!";
        g2.setFont(F22B);
        g2.setColor(choiceLocked ? barColor : BAR_FULL);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(timeStr, (w - fm.stringWidth(timeStr)) / 2, 26);

        // พร้อม (ขวา)
        String readyStr = "พร้อม " + playersReady + "/" + totalPlayers;
        g2.setFont(F16B);
        g2.setColor(choiceLocked ? new Color(255, 200, 80) : BAR_FULL);
        FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString(readyStr, w - fm2.stringWidth(readyStr) - 14, 22);

        // สถานะล่าง
        g2.setFont(F16B);
        if (choiceLocked) {
            g2.setColor(new Color(255, 160, 60));
            g2.drawString("[ รอผู้เล่นอื่น... ]", 14, h - 18);
        } else {
            g2.setColor(BAR_FULL);
            g2.drawString("[ เลือกได้แล้ว! ]", 14, h - 18);
        }
        g2.dispose();
    }
}