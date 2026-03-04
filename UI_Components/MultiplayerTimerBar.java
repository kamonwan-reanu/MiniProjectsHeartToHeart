package UI_Components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * MultiplayerTimerBar — 2-phase + countdown overlay
 *
 * Phase READ  (ทอง): "กำลังอ่าน..." นับ 50วิ
 * Countdown   (ขาว): "3 / 2 / 1" กลางจอ
 * Phase CHOICE (ฟ้า): "เลือกได้เลย!" นับ 10วิ + warn "จะไปแล้วนะ!"
 */
public class MultiplayerTimerBar extends JPanel {

    public enum Phase { NONE, READ, COUNTDOWN, CHOICE }

    private Phase   phase        = Phase.NONE;
    private int     totalSeconds = 50;
    private int     timeLeft     = 50;
    private int     readyCount   = 0;
    private int     totalPlayers = 2;
    private boolean choiceLocked = true;
    private String  sceneName    = "";
    private int     countdownNum = 0;   // 3,2,1

    private Runnable onUnlockCb    = null;
    private Runnable onForceNextCb = null;
    private boolean waitingForPlayers = false; // ✅ แสดงสถานะรอหลัง submit choice

    // local timer (singleplayer)
    private Timer localTimer = null;

    // ── Colours ───────────────────────────────────────────
    private static final Color BG_DARK  = new Color(10, 14, 28, 240);
    private static final Color GOLD     = new Color(212, 175, 55);
    private static final Color GOLD_BAR = new Color(255, 195, 0);
    private static final Color CYAN     = new Color(0, 200, 215);
    private static final Color CYAN_BAR = new Color(0, 220, 230);
    private static final Color WARN_RED = new Color(235, 60, 35);
    private static final Color READY_ON = new Color(55, 215, 95);
    private static final Color WHITE    = Color.WHITE;
    private static final Color DIM      = new Color(140, 150, 185);
    private static final Color UNLOCK_C = new Color(55, 225, 115);
    private static final Color CD_BG    = new Color(0, 0, 0, 180);

    public MultiplayerTimerBar() {
        setOpaque(false);
        setVisible(false);
    }

    // ════════════════════════════════════════════════════
    //  API
    // ════════════════════════════════════════════════════

    public void startReadPhase(String scene, int sec, int total) {
        stopLocalTimer();
        phase = Phase.READ; sceneName = scene;
        totalSeconds = sec; timeLeft = sec;
        totalPlayers = total; readyCount = 0; choiceLocked = true;
        setVisible(true);
        SwingUtilities.invokeLater(this::repaint);
    }

    /** แสดง countdown overlay (3→2→1) */
    public void showCountdown(int n) {
        phase = Phase.COUNTDOWN;
        countdownNum = n;
        setVisible(true);
        SwingUtilities.invokeLater(this::repaint);
    }

    public void startChoicePhase(String scene, int sec, int total) {
        stopLocalTimer();
        phase = Phase.CHOICE; sceneName = scene;
        totalSeconds = sec; timeLeft = sec;
        totalPlayers = total; choiceLocked = false;
        setVisible(true);
        SwingUtilities.invokeLater(() -> {
            repaint();
            if (onUnlockCb != null) onUnlockCb.run();
        });
    }

    public void setTimeLeft(int t) {
        this.timeLeft = Math.max(0, t);
        SwingUtilities.invokeLater(this::repaint);
    }

    public void setReadyCount(int ready, int total) {
        readyCount = ready; totalPlayers = total;
        SwingUtilities.invokeLater(this::repaint);
    }

    public void triggerForceNext() {
        SwingUtilities.invokeLater(() -> { if (onForceNextCb != null) onForceNextCb.run(); });
    }

    public void resetAndHide() {
        stopLocalTimer(); phase = Phase.NONE; choiceLocked = true;
        timeLeft = 0; readyCount = 0; waitingForPlayers = false; setVisible(false);
    }

    public void stopTimer()                  { stopLocalTimer(); }
    public void setOnUnlock(Runnable r)      { onUnlockCb = r; }
    public void setOnForceNext(Runnable r)   { onForceNextCb = r; }
    public boolean isChoiceLocked()          { return choiceLocked; }

    /** แสดงสถานะ "รอผู้เล่นคนอื่น..." หลังจาก submit choice แล้ว */
    public void showWaitingForPlayers() {
        choiceLocked = true;
        waitingForPlayers = true;
        SwingUtilities.invokeLater(this::repaint);
    }

    // ── Singleplayer local ────────────────────────────────
    public void startLocalReadPhase(String scene, int sec, Runnable onDone) {
        startReadPhase(scene, sec, 1);
        startLocalCountdown(onDone);
    }
    public void startLocalCountdownOverlay(Runnable afterCountdown) {
        final int[] n = {3};
        showCountdown(n[0]);
        stopLocalTimer();
        localTimer = new Timer(1000, null);
        localTimer.addActionListener(e -> {
            n[0]--;
            if (n[0] > 0) showCountdown(n[0]);
            else { stopLocalTimer(); if (afterCountdown != null) afterCountdown.run(); }
        });
        localTimer.start();
    }
    public void startLocalChoicePhase(String scene, int sec, Runnable onTimeout) {
        startChoicePhase(scene, sec, 1);
        startLocalCountdown(onTimeout);
    }

    private void startLocalCountdown(Runnable onDone) {
        stopLocalTimer();
        localTimer = new Timer(1000, null);
        localTimer.addActionListener(e -> {
            timeLeft = Math.max(0, timeLeft - 1);
            repaint();
            if (timeLeft <= 0) { stopLocalTimer(); if (onDone != null) onDone.run(); }
        });
        localTimer.start();
    }

    private void stopLocalTimer() {
        if (localTimer != null) { localTimer.stop(); localTimer = null; }
    }

    // legacy
    public void startRound(String s, int sec, int p) { startReadPhase(s, sec, p); }
    public void startRound(int sec, int p)           { startReadPhase("", sec, p); }
    public void startTimer(int sec, int p)           { startReadPhase("", sec, p); }
    public void unlock()                             { choiceLocked = false; repaint(); if(onUnlockCb!=null) onUnlockCb.run(); }
    public void setRoundLabel(String label)          { sceneName = label; repaint(); }
    public void playerReachedChoice()                {}
    public void otherPlayerReady()                   {}

    // ════════════════════════════════════════════════════
    //  Paint
    // ════════════════════════════════════════════════════
    @Override protected void paintComponent(Graphics g) {
        if (!isVisible() || phase == Phase.NONE) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();

        if (phase == Phase.COUNTDOWN) {
            paintCountdown(g2, w, h);
        } else {
            paintTimerBar(g2, w, h);
        }
        g2.dispose();
    }

    // ── Countdown overlay (ตัวเลขกลางจอ) ─────────────────
    private void paintCountdown(Graphics2D g2, int w, int h) {
        // background semi-transparent
        g2.setColor(CD_BG);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 20, 20));

        // เลขกลาง
        g2.setFont(new Font("Tahoma", Font.BOLD, (int)(h * 0.75)));
        FontMetrics fm = g2.getFontMetrics();
        String n = String.valueOf(countdownNum);
        // glow
        g2.setColor(new Color(255, 200, 0, 80));
        g2.drawString(n, (w - fm.stringWidth(n))/2 + 2, h - (h - fm.getAscent())/2 + 2);
        // main
        g2.setColor(GOLD_BAR);
        g2.drawString(n, (w - fm.stringWidth(n))/2, h - (h - fm.getAscent())/2);

        // sub text
        g2.setFont(new Font("Tahoma", Font.BOLD, 13));
        g2.setColor(DIM);
        String sub = "กำลังจะเริ่มเลือก...";
        FontMetrics fms = g2.getFontMetrics();
        g2.drawString(sub, (w - fms.stringWidth(sub))/2, h - 6);
    }

    // ── Timer bar (READ / CHOICE) ──────────────────────────
    private void paintTimerBar(Graphics2D g2, int w, int h) {
        boolean isChoice = (phase == Phase.CHOICE);
        Color accent  = isChoice ? CYAN  : GOLD;
        Color barFill = isChoice ? CYAN_BAR : GOLD_BAR;

        // bg
        g2.setColor(BG_DARK);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 20, 20));
        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 160));
        g2.setStroke(new BasicStroke(1.8f));
        g2.draw(new RoundRectangle2D.Float(1, 1, w-2, h-2, 20, 20));

        int row1Y = (int)(h * 0.38f);
        int row3Y = h - 6;

        // Scene label (ซ้าย)
        String sLbl = sceneName.isEmpty() ? "[ MP ]"
            : "[ " + sceneName.replace("MP_","") + " ]";
        g2.setFont(new Font("Tahoma", Font.BOLD, 14));
        g2.setColor(accent);
        g2.drawString(sLbl, 14, row1Y);

        // Timer (ขวา)
        boolean warn = isChoice && timeLeft <= 3;
        String ts = warn ? "⚠ " + timeLeft + " วิ" : timeLeft + " วิ";
        g2.setFont(new Font("Tahoma", Font.BOLD, 17));
        FontMetrics fmT = g2.getFontMetrics();
        g2.setColor(warn ? WARN_RED : (timeLeft <= 10 && isChoice) ? WARN_RED : WHITE);
        g2.drawString(ts, w - fmT.stringWidth(ts) - 14, row1Y);

        // Status (กลาง)
        String st = isChoice ? "เลือกได้เลย!" : "กำลังอ่าน...";
        Color  sc = isChoice ? UNLOCK_C : GOLD_BAR;
        g2.setFont(new Font("Tahoma", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(sc);
        g2.drawString(st, (w - fm.stringWidth(st)) / 2, row1Y);

        // Progress bar
        int bx = 12, bh = 9;
        int by = (int)(h * 0.52f) - bh/2;
        int bw = w - 24;
        g2.setColor(new Color(30, 38, 60));
        g2.fill(new RoundRectangle2D.Float(bx, by, bw, bh, 7, 7));
        float ratio = totalSeconds > 0
            ? Math.max(0f, Math.min(1f, (float) timeLeft / totalSeconds)) : 0f;
        if (ratio > 0) {
            g2.setColor(ratio < 0.2f ? WARN_RED : barFill);
            g2.fill(new RoundRectangle2D.Float(bx, by, (int)(bw*ratio), bh, 7, 7));
            g2.setColor(new Color(255,255,255,55));
            g2.fill(new RoundRectangle2D.Float(bx, by, (int)(bw*ratio), bh/2, 4, 4));
        }

        // Sub label (ซ้ายล่าง)
        String sub;
        Color subCol;
        if (waitingForPlayers) {
            sub = "[ รอผู้เล่นคนอื่น... ]";
            subCol = DIM;
        } else if (isChoice && timeLeft <= 3) {
            sub = "[ จะไปซีนถัดไปแล้ว! ]";
            subCol = WARN_RED;
        } else if (isChoice) {
            sub = "[ เลือกเลย! ]";
            subCol = UNLOCK_C;
        } else {
            sub = "[ รอผู้เล่นทุกคนอ่านจบ... ]";
            subCol = DIM;
        }
        g2.setFont(new Font("Tahoma", Font.PLAIN, 12));
        g2.setColor(subCol);
        g2.drawString(sub, 14, row3Y);

        // Ready count (ขวาล่าง)
        String rs = "พร้อม " + readyCount + "/" + totalPlayers;
        g2.setFont(new Font("Tahoma", Font.BOLD, 13));
        FontMetrics fmR = g2.getFontMetrics();
        g2.setColor(readyCount >= totalPlayers ? READY_ON : DIM);
        g2.drawString(rs, w - fmR.stringWidth(rs) - 14, row3Y);
    }
}