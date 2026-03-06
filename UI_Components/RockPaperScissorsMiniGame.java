package UI_Components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class RockPaperScissorsMiniGame extends JPanel {

    private static final int WIN_CONDITION       = 2;
    private static final int DEFAULT_TIME        = 15;
    private static final int ANIMATION_DELAY     = 50;
    private static final int RESULT_DISPLAY_TIME = 1500;

    private static final Color BG_DARK    = new Color(15, 20, 35, 240);
    private static final Color BG_LIGHT   = new Color(25, 30, 45, 240);
    private static final Color GOLD       = new Color(212, 175, 55);
    private static final Color GOLD_BRIGHT= new Color(255, 215, 0);
    private static final Color PINK       = new Color(255, 105, 180);
    private static final Color CYAN       = new Color(100, 200, 255);
    private static final Color GREEN      = new Color(100, 255, 100);
    private static final Color RED        = new Color(255, 100, 100);
    private static final Color YELLOW     = new Color(255, 255, 100);
    private static final Color WHITE      = Color.WHITE;

    public enum Move {
        ROCK("✊", "ค้อน"),
        PAPER("✋", "กระดาษ"),
        SCISSORS("✌", "กรรไกร");
        public final String emoji;
        public final String thaiName;
        Move(String e, String t) { emoji = e; thaiName = t; }
    }

    public enum GamePhase { COUNTDOWN, PLAYING, RESULT, FINISHED }
    public enum GameResult { WIN, LOSE, DRAW }

    // ── State ──
    private Move       playerMove    = null;
    private Move       computerMove  = null;
    private GamePhase  phase         = GamePhase.COUNTDOWN;
    private GameResult roundResult   = null;
    private int        playerScore   = 0;
    private int        computerScore = 0;
    private int        countdown     = 3;
    private int        timeLeft;
    private final int  totalTime;

    private boolean isMultiplayer  = false;
    private boolean canInteract    = false;
    private boolean isTimerRunning = false;

    // ── Callbacks ──
    private final Runnable onWinCallback;
    private final Runnable onFailCallback;

    // ── Timers ──
    private Timer gameTimer;
    private Timer countdownTimer;
    private Timer resultTimer;
    private Timer animationTimer;

    // ── Animation ──
    private float   shakeIntensity = 0;
    private float   winAlpha       = 0;
    private Point[] particles      = new Point[20];
    private float[] particleAlpha  = new float[20];
    private final Random random    = new Random();

    // ── UI ──
    private final Rectangle[] choiceBounds = new Rectangle[3];

    // ── Constructors ──
    public RockPaperScissorsMiniGame(Runnable onWin, Runnable onFail) {
        this(onWin, onFail, false, DEFAULT_TIME);
    }

    public RockPaperScissorsMiniGame(Runnable onWin, Runnable onFail, boolean isMultiplayer, int timeSeconds) {
        this.onWinCallback  = onWin;
        this.onFailCallback = onFail;
        this.isMultiplayer  = isMultiplayer;
        this.timeLeft       = timeSeconds > 0 ? timeSeconds : DEFAULT_TIME;
        this.totalTime      = this.timeLeft;

        setOpaque(false);
        initParticles();
        setupListeners();
        startCountdown();
    }

    private void initParticles() {
        for (int i = 0; i < particles.length; i++) {
            particles[i]     = new Point(0, 0);
            particleAlpha[i] = 0;
        }
    }

    private void setupListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!canInteract || phase != GamePhase.PLAYING || playerMove != null) return;
                Point p = e.getPoint();
                for (int i = 0; i < choiceBounds.length; i++) {
                    if (choiceBounds[i] != null && choiceBounds[i].contains(p)) {
                        playerMove = Move.values()[i];
                        repaint();
                        // ✅ FIX: เรียก playRound ทันทีหลังเลือก
                        playRound();
                        break;
                    }
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean over = false;
                for (Rectangle r : choiceBounds)
                    if (r != null && r.contains(e.getPoint())) { over = true; break; }
                setCursor(over && canInteract && playerMove == null
                    ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    : Cursor.getDefaultCursor());
                repaint();
            }
        });
    }

    // ── Game Flow ──
    private void startCountdown() {
        phase      = GamePhase.COUNTDOWN;
        canInteract = false;
        countdown  = 3;
        repaint();

        countdownTimer = new Timer(1000, e -> {
            countdown--;
            repaint();
            if (countdown <= 0) {
                ((Timer)e.getSource()).stop();
                startGame();
            }
        });
        countdownTimer.start();
    }

    private void startGame() {
        phase       = GamePhase.PLAYING;
        canInteract = true;
        playerMove  = null;
        computerMove = null;
        repaint();

        if (isMultiplayer) startMultiplayerTimer();
    }

    private void startMultiplayerTimer() {
        isTimerRunning = true;
        gameTimer = new Timer(1000, e -> {
            if (phase != GamePhase.PLAYING) return;
            timeLeft--;
            repaint();
            if (timeLeft <= 0) {
                ((Timer)e.getSource()).stop();
                isTimerRunning = false;
                // ✅ หมดเวลา → สุ่ม move ให้ผู้เล่นถ้ายังไม่เลือก
                if (playerMove == null) playerMove = Move.values()[random.nextInt(3)];
                playRound();
            }
        });
        gameTimer.start();
    }

    private void playRound() {
        if (playerMove == null) return;
        canInteract  = false;
        computerMove = Move.values()[random.nextInt(3)];

        // หน่วงเล็กน้อยให้ผู้เล่นเห็นการเลือกก่อน
        Timer thinkTimer = new Timer(400, e -> {
            ((Timer)e.getSource()).stop();
            determineWinner();
        });
        thinkTimer.setRepeats(false);
        thinkTimer.start();
        repaint();
    }

    private void determineWinner() {
        if (playerMove == null || computerMove == null) return;

        if (playerMove == computerMove) {
            roundResult = GameResult.DRAW;
        } else if (
            (playerMove == Move.ROCK     && computerMove == Move.SCISSORS) ||
            (playerMove == Move.PAPER    && computerMove == Move.ROCK)     ||
            (playerMove == Move.SCISSORS && computerMove == Move.PAPER)
        ) {
            roundResult = GameResult.WIN;
            playerScore++;
        } else {
            roundResult = GameResult.LOSE;
            computerScore++;
        }

        phase = GamePhase.RESULT;
        createWinEffect();
        repaint();

        resultTimer = new Timer(RESULT_DISPLAY_TIME, e -> {
            ((Timer)e.getSource()).stop();

            if (playerScore >= WIN_CONDITION) {
                phase = GamePhase.FINISHED;
                showEndAnimation(true);
            } else if (computerScore >= WIN_CONDITION) {
                phase = GamePhase.FINISHED;
                showEndAnimation(false);
            } else {
                // เริ่มรอบใหม่
                phase        = GamePhase.PLAYING;
                canInteract  = true;
                playerMove   = null;
                computerMove = null;
                roundResult  = null;
                // ✅ reset เวลาสำหรับรอบใหม่
                if (isMultiplayer) {
                    timeLeft = totalTime;
                    if (gameTimer != null) gameTimer.stop();
                    startMultiplayerTimer();
                }
                repaint();
            }
        });
        resultTimer.setRepeats(false);
        resultTimer.start();
    }

    private void showEndAnimation(boolean won) {
        if (gameTimer != null)  { gameTimer.stop();  isTimerRunning = false; }
        if (resultTimer != null)  resultTimer.stop();

        animationTimer = new Timer(ANIMATION_DELAY, new ActionListener() {
            float alpha = 0;
            @Override public void actionPerformed(ActionEvent e) {
                alpha += 0.05f;
                winAlpha = Math.min(alpha, 1f);
                repaint();
                if (alpha >= 1f) {
                    ((Timer)e.getSource()).stop();
                    if (won) { if (onWinCallback  != null) onWinCallback.run();  }
                    else      { if (onFailCallback != null) onFailCallback.run(); }
                }
            }
        });
        animationTimer.start();
    }

    // ── Effects ──
    private void startShake(int intensity) {
        shakeIntensity = intensity;
        new Timer(30, new ActionListener() {
            int c = 0;
            @Override public void actionPerformed(ActionEvent e) {
                shakeIntensity *= 0.7f; c++;
                repaint();
                if (c > 8 || shakeIntensity < 0.5f) {
                    shakeIntensity = 0; ((Timer)e.getSource()).stop();
                }
            }
        }).start();
    }

    private void createWinEffect() {
        int cx = getWidth() / 2, cy = getHeight() / 2;
        for (int i = 0; i < particles.length; i++) {
            particles[i].x  = cx; particles[i].y = cy;
            particleAlpha[i] = 1f;
        }
        new Timer(30, new ActionListener() {
            int c = 0;
            @Override public void actionPerformed(ActionEvent e) {
                c++;
                for (int i = 0; i < particles.length; i++) {
                    particles[i].x += (random.nextInt(20) - 10);
                    particles[i].y += (random.nextInt(20) - 15);
                    particleAlpha[i] *= 0.95f;
                }
                repaint();
                if (c > 30) ((Timer)e.getSource()).stop();
            }
        }).start();
    }

    // ── Painting ──
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (shakeIntensity > 0) {
            g2.translate((int)(random.nextFloat()*shakeIntensity*2-shakeIntensity),
                         (int)(random.nextFloat()*shakeIntensity*2-shakeIntensity));
        }

        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) { g2.dispose(); return; }

        drawBackground(g2, w, h);
        drawHeader(g2, w, h);
        drawScores(g2, w, h);
        drawChoices(g2, w, h);

        if (phase == GamePhase.RESULT && playerMove != null && computerMove != null)
            drawRoundResult(g2, w, h);

        drawParticles(g2);

        if (phase == GamePhase.COUNTDOWN)
            drawCountdown(g2, w, h);

        if (phase == GamePhase.FINISHED && winAlpha > 0)
            drawGameOverlay(g2, w, h);

        g2.dispose();
    }

    private void drawBackground(Graphics2D g2, int w, int h) {
        g2.setPaint(new GradientPaint(0, 0, BG_DARK, 0, h, BG_LIGHT));
        g2.fillRoundRect(0, 0, w, h, 30, 30);
        g2.setColor(GOLD); g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(2, 2, w-4, h-4, 28, 28);
        g2.setColor(new Color(255,255,255,20)); g2.setStroke(new BasicStroke(1));
        g2.drawRoundRect(10, 10, w-20, h-20, 20, 20);
    }

    private void drawHeader(Graphics2D g2, int w, int h) {
        g2.setColor(WHITE); g2.setFont(new Font("Tahoma", Font.BOLD, 32));
        g2.drawString("เป่ายิ้งฉุบ", 30, 50);

        if (isMultiplayer) {
            g2.setColor(CYAN); g2.setFont(new Font("Tahoma", Font.BOLD, 16));
            g2.drawString("Multiplayer", 30, 78);
        }

        // ✅ แสดง timer เสมอในโหมด multiplayer (ไม่ต้องรอ isTimerRunning)
        if (isMultiplayer && phase == GamePhase.PLAYING) {
            int tx = w-160, ty = 28, tw = 130, th = 28;
            g2.setColor(new Color(30,30,50));
            g2.fillRoundRect(tx, ty, tw, th, 12, 12);

            float pct = totalTime > 0 ? (float)timeLeft / totalTime : 0;
            int fw = (int)(tw * pct);
            Color tc = timeLeft > (totalTime*0.5) ? GREEN : (timeLeft > (totalTime*0.25) ? YELLOW : RED);
            g2.setColor(tc);
            if (fw > 0) g2.fillRoundRect(tx, ty, fw, th, 12, 12);

            g2.setColor(WHITE); g2.setFont(new Font("Tahoma", Font.BOLD, 14));
            String ts = timeLeft + "s";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(ts, tx+(tw-fm.stringWidth(ts))/2, ty+20);
        }
    }

    private void drawScores(Graphics2D g2, int w, int h) {
        int cx = w / 2;
        g2.setFont(new Font("Tahoma", Font.BOLD, 24));
        g2.setColor(CYAN);  g2.drawString("คุณ " + playerScore,  cx-150, 105);
        g2.setColor(GOLD);  g2.drawString("VS",                  cx-20,  105);
        g2.setColor(PINK);  g2.drawString("คอม " + computerScore, cx+50,  105);
        g2.setColor(new Color(150,150,150)); g2.setFont(new Font("Tahoma", Font.PLAIN, 14));
        g2.drawString("(ชนะ "+WIN_CONDITION+" รอบ)", cx-55, 128);
    }

    private void drawChoices(Graphics2D g2, int w, int h) {
        int btnW = 150, btnH = 150, gap = 30;
        int startX = (w - (3*btnW + 2*gap)) / 2;
        int btnY   = h - 220;
        Move[] moves = Move.values();

        for (int i = 0; i < 3; i++) {
            int x = startX + i*(btnW+gap);
            choiceBounds[i] = new Rectangle(x, btnY, btnW, btnH);

            Point mp  = getMousePosition();
            boolean hover    = mp != null && choiceBounds[i].contains(mp);
            boolean selected = playerMove == moves[i];

            // เงา
            g2.setColor(new Color(0,0,0,100));
            g2.fillRoundRect(x+3, btnY+3, btnW, btnH, 20, 20);

            // พื้นหลัง
            g2.setColor(selected ? GOLD_BRIGHT : (hover && canInteract && playerMove==null) ? new Color(80,100,150) : new Color(40,50,80));
            g2.fillRoundRect(x, btnY, btnW, btnH, 20, 20);

            // ขอบ
            g2.setColor(selected ? GOLD : (hover ? CYAN : new Color(100,100,150)));
            g2.setStroke(new BasicStroke(selected ? 3 : 2));
            g2.drawRoundRect(x, btnY, btnW, btnH, 20, 20);

            // Emoji
            g2.setColor(WHITE);
            g2.setFont(new Font("Dialog", Font.PLAIN, 55));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(moves[i].emoji, x+(btnW-fm.stringWidth(moves[i].emoji))/2, btnY+82);

            // ชื่อ
            g2.setFont(new Font("Tahoma", Font.PLAIN, 16));
            fm = g2.getFontMetrics();
            g2.drawString(moves[i].thaiName, x+(btnW-fm.stringWidth(moves[i].thaiName))/2, btnY+118);
        }
    }

    private void drawRoundResult(Graphics2D g2, int w, int h) {
        int cy = h/2 - 60;
        g2.setFont(new Font("Dialog", Font.PLAIN, 75));
        g2.setColor(WHITE);
        g2.drawString(playerMove.emoji,  w/4  - 30, cy);
        g2.drawString(computerMove.emoji, w*3/4 - 30, cy);

        g2.setFont(new Font("Tahoma", Font.BOLD, 38));
        g2.setColor(GOLD);
        g2.drawString("VS", w/2-30, cy);

        String rt; Color rc;
        switch (roundResult) {
            case WIN:  rt = "คุณชนะ!"; rc = GREEN;  break;
            case LOSE: rt = "คุณแพ้";  rc = RED;    break;
            default:   rt = "เสมอ";    rc = YELLOW; break;
        }
        g2.setFont(new Font("Tahoma", Font.BOLD, 30));
        g2.setColor(rc);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(rt, (w-fm.stringWidth(rt))/2, cy+100);

        g2.setFont(new Font("Tahoma", Font.PLAIN, 17));
        g2.setColor(WHITE);
        fm = g2.getFontMetrics();
        g2.drawString(playerMove.thaiName,   w/4 - fm.stringWidth(playerMove.thaiName)/2,   cy+52);
        g2.drawString(computerMove.thaiName, w*3/4 - fm.stringWidth(computerMove.thaiName)/2, cy+52);
    }

    private void drawParticles(Graphics2D g2) {
        for (int i = 0; i < particles.length; i++) {
            if (particleAlpha[i] > 0.1f) {
                g2.setColor(new Color(255,215,0,(int)(particleAlpha[i]*255)));
                g2.fillOval(particles[i].x-3, particles[i].y-3, 6, 6);
            }
        }
    }

    private void drawCountdown(Graphics2D g2, int w, int h) {
        String text = String.valueOf(countdown);
        g2.setFont(new Font("Tahoma", Font.BOLD, 120));
        g2.setColor(new Color(255,255,255,200));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, (w-fm.stringWidth(text))/2, h/2);

        g2.setFont(new Font("Tahoma", Font.BOLD, 24));
        g2.setColor(YELLOW);
        String ready = "เตรียมตัวให้พร้อม!";
        fm = g2.getFontMetrics();
        g2.drawString(ready, (w-fm.stringWidth(ready))/2, h/2+80);
    }

    private void drawGameOverlay(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(0,0,0,(int)(150*winAlpha)));
        g2.fillRect(0, 0, w, h);

        boolean playerWon = playerScore >= WIN_CONDITION;
        String rt  = playerWon ? "VICTORY!" : "GAME OVER";
        Color  col = new Color(playerWon ? 255 : 255, playerWon ? 215 : 100,
                               playerWon ? 0   : 100, (int)(255*winAlpha));
        g2.setFont(new Font("Tahoma", Font.BOLD, 64));
        g2.setColor(col);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(rt, (w-fm.stringWidth(rt))/2, h/2-40);

        g2.setFont(new Font("Tahoma", Font.BOLD, 28));
        g2.setColor(new Color(255,255,255,(int)(255*winAlpha)));
        String pts = playerWon ? "ได้ +5 คะแนนความสัมพันธ์" : "ได้ +2 คะแนนความสัมพันธ์";
        fm = g2.getFontMetrics();
        g2.drawString(pts, (w-fm.stringWidth(pts))/2, h/2+20);
    }

    // ── Public API (multiplayer sync) ──
    public void syncTime(int t)    { timeLeft = t; repaint(); }
    public int  getCurrentTime()   { return timeLeft; }
    public boolean isGameFinished(){ return phase == GamePhase.FINISHED; }
    public void forceMove(Move m)  { this.playerMove = m; playRound(); }
}