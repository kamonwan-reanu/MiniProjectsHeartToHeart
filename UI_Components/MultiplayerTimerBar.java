package UI_Components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * MultiplayerTimerBar v4
 *
 * ✅ Banner "เลือกได้เลย!" ถูกย้ายไปวาดบน PlaySceneMP แล้ว (ไม่บังเวลา)
 * ✅ showChoiceAlertBanner(n) n=1-3 → วงกลม countdown ที่มุมขวา timerBar
 * ✅ showChoiceAlertBanner(-1) → no-op (PlaySceneMP จัดการแทน)
 */
public class MultiplayerTimerBar extends JPanel {

    public enum Phase { NONE, READ, CHOICE }

    private Phase   phase        = Phase.NONE;
    private int     totalSeconds = 50;
    private int     timeLeft     = 50;
    private int     readyCount   = 0;
    private int     totalPlayers = 2;
    private boolean choiceLocked = true;
    private String  sceneName    = "";
    private Runnable onUnlockCb    = null;
    private Runnable onForceNextCb = null;

    // Countdown state (1-3 เท่านั้น)
    private boolean showCountdown  = false;
    private int     countdownNum   = 3;
    private float   countdownAlpha = 0f;
    private Timer   countdownTimer = null;

    // Local timer (singleplayer fallback)
    private Timer localTimer = null;

    // ── Colours ───────────────────────────────────────────
    private static final Color BG_DARK   = new Color(10, 14, 28, 240);
    private static final Color GOLD      = new Color(212, 175, 55);
    private static final Color GOLD_BAR  = new Color(255, 195, 0);
    private static final Color CYAN      = new Color(0, 200, 215);
    private static final Color CYAN_BAR  = new Color(0, 220, 230);
    private static final Color WARN      = new Color(235, 60, 35);
    private static final Color READY_ON  = new Color(55, 215, 95);
    private static final Color WHITE     = Color.WHITE;
    private static final Color DIM       = new Color(140, 150, 185);
    private static final Color UNLOCK_C  = new Color(55, 225, 115);
    private static final Color CNTDOWN_C = new Color(255, 70, 50);

    public MultiplayerTimerBar() {
        setOpaque(false);
        setVisible(false);
    }

    // ════════════════════════════════════════════════════
    //  Public API
    // ════════════════════════════════════════════════════

    public void startReadPhase(String scene, int sec, int total) {
        stopLocalTimer(); stopCountdown();
        this.phase = Phase.READ; this.sceneName = scene;
        this.totalSeconds = sec; this.timeLeft = sec;
        this.totalPlayers = total; this.readyCount = 0;
        this.choiceLocked = true;
        setVisible(true);
        SwingUtilities.invokeLater(this::repaint);
    }

    public void startChoicePhase(String scene, int sec, int total) {
        stopLocalTimer(); stopCountdown();
        this.phase = Phase.CHOICE; this.sceneName = scene;
        this.totalSeconds = sec; this.timeLeft = sec;
        this.totalPlayers = total; this.choiceLocked = false;
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
        this.readyCount = ready; this.totalPlayers = total;
        SwingUtilities.invokeLater(this::repaint);
    }

    /**
     * countdown == -1  → no-op (banner ถูกจัดการโดย PlaySceneMP.showAlertOverlay())
     * countdown == 1-3 → วงกลมตัวเลข countdown ที่มุมขวา timerBar
     */
    public void showChoiceAlertBanner(int countdown) {
        if (countdown == -1) return; // ✅ PlaySceneMP รับผิดชอบแทน
        SwingUtilities.invokeLater(() -> {
            stopCountdown();
            countdownNum   = countdown;
            countdownAlpha = 1f;
            showCountdown  = true;
            repaint();

            countdownTimer = new Timer(40, null);
            final long t0  = System.currentTimeMillis();
            countdownTimer.addActionListener(e -> {
                long elapsed = System.currentTimeMillis() - t0;
                if (elapsed < 600) return;
                countdownAlpha -= 0.07f;
                if (countdownAlpha <= 0f) {
                    countdownAlpha = 0f;
                    showCountdown  = false;
                    stopCountdown();
                }
                repaint();
            });
            countdownTimer.start();
        });
    }

    private void stopCountdown() {
        if (countdownTimer != null) { countdownTimer.stop(); countdownTimer = null; }
        showCountdown = false; countdownAlpha = 0f;
    }

    public void resetAndHide() {
        stopLocalTimer(); stopCountdown();
        phase = Phase.NONE; choiceLocked = true; timeLeft = 0; readyCount = 0;
        setVisible(false);
    }

    public void stopTimer()               { stopLocalTimer(); }
    public void setOnUnlock(Runnable r)   { this.onUnlockCb    = r; }
    public void setOnForceNext(Runnable r){ this.onForceNextCb = r; }
    public boolean isChoiceLocked()       { return choiceLocked; }
    public void triggerForceNext()        { if (onForceNextCb != null) onForceNextCb.run(); }

    // ── Singleplayer local countdown ──────────────────────
    public void startLocalReadPhase(String scene, int sec, Runnable onDone) {
        startReadPhase(scene, sec, 1);
        startLocalCountdown(onDone, false);
    }

    public void startLocalChoicePhase(String scene, int sec, Runnable onTimeout) {
        startChoicePhase(scene, sec, 1);
        startLocalCountdown(onTimeout, true);
    }

    private void startLocalCountdown(Runnable onDone, boolean isChoice) {
        stopLocalTimer();
        localTimer = new Timer(1000, null);
        localTimer.addActionListener(e -> {
            timeLeft = Math.max(0, timeLeft - 1);
            if (isChoice && timeLeft > 0 && timeLeft <= model.GameServer.ALERT_BEFORE_SECS)
                showChoiceAlertBanner(timeLeft);
            repaint();
            if (timeLeft <= 0) { stopLocalTimer(); if (onDone != null) onDone.run(); }
        });
        localTimer.start();
    }

    private void stopLocalTimer() {
        if (localTimer != null) { localTimer.stop(); localTimer = null; }
    }

    // Legacy compat
    public void startRound(String s, int sec, int p) { startReadPhase(s, sec, p); }
    public void startRound(int sec, int p)           { startReadPhase("", sec, p); }
    public void startTimer(int sec, int p)           { startReadPhase("", sec, p); }
    public void unlock()                             { choiceLocked = false; repaint(); if(onUnlockCb!=null) onUnlockCb.run(); }
    public void playerReachedChoice()                {}
    public void otherPlayerReady()                   {}
    public void setRoundLabel(String label)          { this.sceneName = label; repaint(); }

    // ════════════════════════════════════════════════════
    //  Paint
    // ════════════════════════════════════════════════════
    @Override protected void paintComponent(Graphics g) {
        if (!isVisible() || phase == Phase.NONE) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();

        boolean isChoice = (phase == Phase.CHOICE);
        Color   accent   = isChoice ? CYAN  : GOLD;
        Color   barFill  = isChoice ? CYAN_BAR : GOLD_BAR;

        // ── Background ─────────────────────────────────────
        g2.setColor(BG_DARK);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 20, 20));
        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 160));
        g2.setStroke(new BasicStroke(1.8f));
        g2.draw(new RoundRectangle2D.Float(1, 1, w-2, h-2, 20, 20));

        int row1Y = (int)(h * 0.38f);
        int row3Y = h - 6;

        // ── Scene label ────────────────────────────────────
        String sLbl = sceneName.isEmpty() ? "[ MP ]"
            : "[ " + sceneName.replace("MP_","") + " ]";
        g2.setFont(new Font("Tahoma", Font.BOLD, 14));
        g2.setColor(accent);
        g2.drawString(sLbl, 14, row1Y);

        // ── Timer ──────────────────────────────────────────
        String ts = timeLeft + " วิ";
        g2.setFont(new Font("Tahoma", Font.BOLD, 17));
        FontMetrics fmT = g2.getFontMetrics();
        Color tCol = (isChoice && timeLeft <= model.GameServer.ALERT_BEFORE_SECS) ? WARN
                   : (timeLeft <= 8) ? WARN : WHITE;
        g2.setColor(tCol);
        g2.drawString(ts, w - fmT.stringWidth(ts) - 14, row1Y);

        // ── Status ─────────────────────────────────────────
        String st = isChoice ? "เลือกได้เลย!" : "กำลังอ่าน...";
        Color  sc = isChoice ? UNLOCK_C : GOLD_BAR;
        g2.setFont(new Font("Tahoma", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(sc);
        g2.drawString(st, (w - fm.stringWidth(st)) / 2, row1Y);

        // ── Progress bar ────────────────────────────────────
        int bx = 12, bh = 9;
        int by = (int)(h * 0.52f) - bh/2;
        int bw = w - 24;
        g2.setColor(new Color(30, 38, 60));
        g2.fill(new RoundRectangle2D.Float(bx, by, bw, bh, 7, 7));
        float ratio = totalSeconds > 0
            ? Math.max(0f, Math.min(1f, (float) timeLeft / totalSeconds)) : 0f;
        if (ratio > 0) {
            Color fc = (isChoice && timeLeft <= model.GameServer.ALERT_BEFORE_SECS) ? WARN : barFill;
            g2.setColor(fc);
            g2.fill(new RoundRectangle2D.Float(bx, by, (int)(bw*ratio), bh, 7, 7));
            g2.setColor(new Color(255,255,255,55));
            g2.fill(new RoundRectangle2D.Float(bx, by, (int)(bw*ratio), bh/2, 4, 4));
        }

        // ── Sub / Ready ─────────────────────────────────────
        String sub = isChoice ? "[ เลือกเลย! ]" : "[ รอผู้เล่นทุกคนอ่านจบ... ]";
        g2.setFont(new Font("Tahoma", Font.PLAIN, 12));
        g2.setColor(isChoice ? UNLOCK_C : DIM);
        g2.drawString(sub, 14, row3Y);

        String rs = "พร้อม " + readyCount + "/" + totalPlayers;
        g2.setFont(new Font("Tahoma", Font.BOLD, 13));
        FontMetrics fmR = g2.getFontMetrics();
        g2.setColor(readyCount >= totalPlayers ? READY_ON : DIM);
        g2.drawString(rs, w - fmR.stringWidth(rs) - 14, row3Y);

        // ════════════════════════════════════════════════════
        //  FIX 2: Countdown circle 3-2-1 (ที่มุมขวาบนของ bar)
        //  ✅ ไม่บังตัวเลขเวลา วางซ้อนบน scene label ทางซ้าย
        // ════════════════════════════════════════════════════
        if (showCountdown && countdownAlpha > 0.02f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, countdownAlpha));

            int cr = Math.min(h / 2 - 4, 24);
            // ✅ วางตรงกลาง bar แนวตั้ง แต่ shift ซ้ายของตัวเลขเวลา
            int cx = w - fmT.stringWidth(ts) - 14 - cr - 10;
            int cy = h / 2;

            // เงา
            g2.setColor(new Color(0,0,0,(int)(80*countdownAlpha)));
            g2.fillOval(cx-cr+2, cy-cr+2, cr*2, cr*2);
            // วงกลม
            g2.setColor(new Color(CNTDOWN_C.getRed(), CNTDOWN_C.getGreen(),
                CNTDOWN_C.getBlue(), (int)(210 * countdownAlpha)));
            g2.fillOval(cx-cr, cy-cr, cr*2, cr*2);
            // ขอบ
            g2.setColor(new Color(255,255,255,(int)(180*countdownAlpha)));
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(cx-cr, cy-cr, cr*2, cr*2);
            // ตัวเลข
            g2.setFont(new Font("Tahoma", Font.BOLD, (int)(cr * 1.1f)));
            g2.setColor(new Color(255, 255, 255, (int)(255 * countdownAlpha)));
            FontMetrics fmC = g2.getFontMetrics();
            String cd = String.valueOf(countdownNum);
            g2.drawString(cd, cx - fmC.stringWidth(cd)/2, cy + fmC.getAscent()/2 - 1);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        g2.dispose();
    }
}