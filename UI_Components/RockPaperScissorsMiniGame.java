package UI_Components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Random;

/**
 * RockPaperScissorsMiniGame - มินิเกมเป่ายิ้งฉุบสุดพรีเมียม
 * รองรับทั้งโหมด单人และ multiplayer
 * 
 * คุณสมบัติ:
 * - ระบบ Best of 3 (ชนะ 2 ใน 3 ครั้ง)
 * - Animation สวยงามขณะเล่น
 * - ระบบเวลาในโหมด multiplayer
 * - เอฟเฟคเมื่อชนะ/แพ้
 * - รองรับการปรับขนาดหน้าจออัตโนมัติ
 */
public class RockPaperScissorsMiniGame extends JPanel {
    
    // ========== Constants ==========
    private static final int WIN_CONDITION = 2; // ชนะ 2 ใน 3
    private static final int DEFAULT_TIME = 15; // เวลาเริ่มต้น (วินาที)
    private static final int ANIMATION_DELAY = 50;
    private static final int RESULT_DISPLAY_TIME = 1500;
    
    // ========== Colors ==========
    private static final Color BG_DARK = new Color(15, 20, 35, 240);
    private static final Color BG_LIGHT = new Color(25, 30, 45, 240);
    private static final Color GOLD = new Color(212, 175, 55);
    private static final Color GOLD_BRIGHT = new Color(255, 215, 0);
    private static final Color PINK = new Color(255, 105, 180);
    private static final Color CYAN = new Color(100, 200, 255);
    private static final Color GREEN = new Color(100, 255, 100);
    private static final Color RED = new Color(255, 100, 100);
    private static final Color YELLOW = new Color(255, 255, 100);
    private static final Color WHITE = Color.WHITE;
    private static final Color BLACK = new Color(20, 20, 30);
    
    // ========== Enums ==========
    public enum Move { 
        ROCK("✊", "ค้อน"), 
        PAPER("✋", "กระดาษ"), 
        SCISSORS("✌️", "กรรไกร");
        
        public final String emoji;
        public final String thaiName;
        
        Move(String emoji, String thaiName) {
            this.emoji = emoji;
            this.thaiName = thaiName;
        }
    }
    
    public enum GamePhase {
        COUNTDOWN,  // นับถอยหลัง 3-2-1
        PLAYING,    // กำลังเล่น
        RESULT,     // แสดงผลรอบ
        FINISHED    // จบเกม
    }
    
    public enum GameResult {
        WIN, LOSE, DRAW
    }
    
    // ========== Game State ==========
    private Move playerMove = null;
    private Move computerMove = null;
    private GamePhase phase = GamePhase.COUNTDOWN;
    private GameResult roundResult = null;
    
    private int playerScore = 0;
    private int computerScore = 0;
    private int countdown = 3;
    private int timeLeft = DEFAULT_TIME;
    
    private boolean isMultiplayer = false;
    private boolean canInteract = false;
    private boolean isTimerRunning = false;
    
    // ========== Callbacks ==========
    private Runnable onWinCallback;
    private Runnable onFailCallback;
    private Runnable onTimeSyncCallback;
    
    // ========== Timers ==========
    private Timer gameTimer;
    private Timer countdownTimer;
    private Timer resultTimer;
    private Timer animationTimer;
    
    // ========== Animation ==========
    private float shakeIntensity = 0;
    private float glowAlpha = 0;
    private float winAlpha = 0;
    private Point[] particles = new Point[20];
    private float[] particleAlpha = new float[20];
    private Random random = new Random();
    
    // ========== UI Components ==========
    private Rectangle[] choiceBounds = new Rectangle[3];
    private Rectangle playerChoiceRect;
    private Rectangle computerChoiceRect;
    
    // ========== Constructor ==========
    public RockPaperScissorsMiniGame(Runnable onWin, Runnable onFail) {
        this(onWin, onFail, false, DEFAULT_TIME);
    }
    
    public RockPaperScissorsMiniGame(Runnable onWin, Runnable onFail, boolean isMultiplayer, int timeSeconds) {
        this.onWinCallback = onWin;
        this.onFailCallback = onFail;
        this.isMultiplayer = isMultiplayer;
        this.timeLeft = timeSeconds;
        
        setOpaque(false);
        initParticles();
        setupListeners();
        startCountdown();
    }
    
    // ========== Initialization ==========
    private void initParticles() {
        for (int i = 0; i < particles.length; i++) {
            particles[i] = new Point(0, 0);
            particleAlpha[i] = 0;
        }
    }
    
    private void setupListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!canInteract || phase != GamePhase.PLAYING || playerMove != null) {
                    return;
                }
                
                Point p = e.getPoint();
                for (int i = 0; i < choiceBounds.length; i++) {
                    if (choiceBounds[i] != null && choiceBounds[i].contains(p)) {
                        playerMove = Move.values()[i];
                        playSound("click");
                        startShake(5);
                        repaint();
                        break;
                    }
                }
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                // อัปเดต cursor เมื่ออยู่เหนือปุ่ม
                boolean overButton = false;
                for (Rectangle rect : choiceBounds) {
                    if (rect != null && rect.contains(e.getPoint())) {
                        overButton = true;
                        break;
                    }
                }
                setCursor(overButton ? 
                    Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : 
                    Cursor.getDefaultCursor());
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // สำหรับ hover effect
                repaint();
            }
        });
    }
    
    // ========== Game Flow ==========
    private void startCountdown() {
        phase = GamePhase.COUNTDOWN;
        canInteract = false;
        countdown = 3;
        
        countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                countdown--;
                playSound("countdown");
                repaint();
                
                if (countdown <= 0) {
                    countdownTimer.stop();
                    startGame();
                }
            }
        });
        countdownTimer.start();
    }
    
    private void startGame() {
        phase = GamePhase.PLAYING;
        canInteract = true;
        playerMove = null;
        computerMove = null;
        
        if (isMultiplayer) {
            startMultiplayerTimer();
        }
        
        repaint();
    }
    
    private void startMultiplayerTimer() {
        isTimerRunning = true;
        gameTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (phase != GamePhase.PLAYING) return;
                
                timeLeft--;
                repaint();
                
                // ส่ง sync เวลาในโหมด multiplayer
                if (isMultiplayer && onTimeSyncCallback != null) {
                    onTimeSyncCallback.run();
                }
                
                // หมดเวลา - ให้คอมพิวเตอร์ชนะ
                if (timeLeft <= 0) {
                    gameTimer.stop();
                    isTimerRunning = false;
                    playerMove = Move.values()[random.nextInt(3)];
                    computerMove = Move.values()[random.nextInt(3)];
                    determineWinner();
                }
            }
        });
        gameTimer.start();
    }
    
    private void playRound() {
        if (playerMove == null) return;
        
        canInteract = false;
        computerMove = Move.values()[random.nextInt(3)];
        
        // เล่นเสียง
        playSound("rps_throw");
        
        // สั่นเล็กน้อย
        startShake(10);
        
        // หน่วงเวลาเล็กน้อยแล้วค่อยหาผลลัพธ์
        Timer thinkingTimer = new Timer(600, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                determineWinner();
                ((Timer)e.getSource()).stop();
            }
        });
        thinkingTimer.setRepeats(false);
        thinkingTimer.start();
        
        repaint();
    }
    
    private void determineWinner() {
        if (playerMove == null || computerMove == null) return;
        
        // หาผู้ชนะ
        if (playerMove == computerMove) {
            roundResult = GameResult.DRAW;
            playSound("draw");
        } else if (
            (playerMove == Move.ROCK && computerMove == Move.SCISSORS) ||
            (playerMove == Move.PAPER && computerMove == Move.ROCK) ||
            (playerMove == Move.SCISSORS && computerMove == Move.PAPER)
        ) {
            roundResult = GameResult.WIN;
            playerScore++;
            playSound("win_round");
        } else {
            roundResult = GameResult.LOSE;
            computerScore++;
            playSound("lose_round");
        }
        
        phase = GamePhase.RESULT;
        
        // สร้าง particle effect
        createWinEffect();
        
        repaint();
        
        // ตรวจสอบผลรวม
        resultTimer = new Timer(RESULT_DISPLAY_TIME, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (playerScore >= WIN_CONDITION) {
                    // ผู้เล่นชนะ
                    phase = GamePhase.FINISHED;
                    showWinAnimation();
                } else if (computerScore >= WIN_CONDITION) {
                    // คอมพิวเตอร์ชนะ
                    phase = GamePhase.FINISHED;
                    showLoseAnimation();
                } else {
                    // เริ่มรอบใหม่
                    phase = GamePhase.PLAYING;
                    canInteract = true;
                    playerMove = null;
                    computerMove = null;
                    roundResult = null;
                    
                    if (isMultiplayer) {
                        timeLeft = DEFAULT_TIME;
                    }
                    
                    repaint();
                }
                ((Timer)e.getSource()).stop();
            }
        });
        resultTimer.setRepeats(false);
        resultTimer.start();
    }
    
    private void showWinAnimation() {
        // หยุด timer ทั้งหมด
        if (gameTimer != null) gameTimer.stop();
        isTimerRunning = false;
        
        // สร้าง animation ชนะ
        animationTimer = new Timer(ANIMATION_DELAY, new ActionListener() {
            float alpha = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                alpha += 0.05f;
                winAlpha = alpha;
                repaint();
                
                if (alpha >= 1.0f) {
                    animationTimer.stop();
                    
                    // เรียก callback
                    if (onWinCallback != null) {
                        onWinCallback.run();
                    }
                    
                    // ปิดเกมหลังจากแสดงผล
                    Timer closeTimer = new Timer(2000, ev -> {
                        Container parent = getParent();
                        if (parent != null) {
                            parent.remove(RockPaperScissorsMiniGame.this);
                            parent.revalidate();
                            parent.repaint();
                        }
                    });
                    closeTimer.setRepeats(false);
                    closeTimer.start();
                }
            }
        });
        animationTimer.start();
    }
    
    private void showLoseAnimation() {
        if (gameTimer != null) gameTimer.stop();
        isTimerRunning = false;
        
        animationTimer = new Timer(ANIMATION_DELAY, new ActionListener() {
            float alpha = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                alpha += 0.05f;
                winAlpha = alpha;
                repaint();
                
                if (alpha >= 1.0f) {
                    animationTimer.stop();
                    
                    if (onFailCallback != null) {
                        onFailCallback.run();
                    }
                    
                    Timer closeTimer = new Timer(2000, ev -> {
                        Container parent = getParent();
                        if (parent != null) {
                            parent.remove(RockPaperScissorsMiniGame.this);
                            parent.revalidate();
                            parent.repaint();
                        }
                    });
                    closeTimer.setRepeats(false);
                    closeTimer.start();
                }
            }
        });
        animationTimer.start();
    }
    
    // ========== Animation Effects ==========
    private void startShake(int intensity) {
        shakeIntensity = intensity;
        Timer shakeTimer = new Timer(30, new ActionListener() {
            int count = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                shakeIntensity *= 0.7f;
                count++;
                repaint();
                
                if (count > 8 || shakeIntensity < 0.5f) {
                    shakeIntensity = 0;
                    ((Timer)e.getSource()).stop();
                }
            }
        });
        shakeTimer.start();
    }
    
    private void createWinEffect() {
        for (int i = 0; i < particles.length; i++) {
            particles[i].x = getWidth() / 2;
            particles[i].y = getHeight() / 2;
            particleAlpha[i] = 1.0f;
        }
        
        Timer particleTimer = new Timer(30, new ActionListener() {
            int count = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                count++;
                for (int i = 0; i < particles.length; i++) {
                    particles[i].x += (random.nextInt(20) - 10);
                    particles[i].y += (random.nextInt(20) - 15);
                    particleAlpha[i] *= 0.95f;
                }
                repaint();
                
                if (count > 30) {
                    ((Timer)e.getSource()).stop();
                }
            }
        });
        particleTimer.start();
    }
    
    // ========== Multiplayer Methods ==========
    public void syncTime(int serverTime) {
        this.timeLeft = serverTime;
        repaint();
    }
    
    public void setOnTimeSyncCallback(Runnable callback) {
        this.onTimeSyncCallback = callback;
    }
    
    public int getCurrentTime() {
        return timeLeft;
    }
    
    public boolean isGameFinished() {
        return phase == GamePhase.FINISHED;
    }
    
    public void forceMove(Move move) {
        this.playerMove = move;
        playRound();
    }
    
    // ========== Sound ==========
    private void playSound(String soundName) {
        // TODO: เชื่อมต่อกับ SoundManager
        // core.Main.soundManager.playSE("rps_" + soundName + ".wav");
    }
    
    // ========== Painting ==========
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // ใช้ shake effect
        if (shakeIntensity > 0) {
            int shakeX = (int)(random.nextFloat() * shakeIntensity * 2 - shakeIntensity);
            int shakeY = (int)(random.nextFloat() * shakeIntensity * 2 - shakeIntensity);
            g2d.translate(shakeX, shakeY);
        }
        
        int w = getWidth();
        int h = getHeight();
        
        if (w <= 0 || h <= 0) {
            g2d.dispose();
            return;
        }
        
        // วาดพื้นหลัง
        drawBackground(g2d, w, h);
        
        // วาด header
        drawHeader(g2d, w, h);
        
        // วาดคะแนน
        drawScores(g2d, w, h);
        
        // วาดตัวเลือก (ด้านล่าง)
        drawChoices(g2d, w, h);
        
        // วาดผลการเล่นรอบปัจจุบัน
        if (phase == GamePhase.RESULT && playerMove != null && computerMove != null) {
            drawRoundResult(g2d, w, h);
        }
        
        // วาด particles
        drawParticles(g2d);
        
        // วาด countdown
        if (phase == GamePhase.COUNTDOWN) {
            drawCountdown(g2d, w, h);
        }
        
        // วาด overlay ตอนจบเกม
        if (phase == GamePhase.FINISHED && winAlpha > 0) {
            drawGameOverlay(g2d, w, h);
        }
        
        g2d.dispose();
    }
    
    private void drawBackground(Graphics2D g2d, int w, int h) {
        // พื้นหลังหลัก
        GradientPaint gp = new GradientPaint(0, 0, BG_DARK, 0, h, BG_LIGHT);
        g2d.setPaint(gp);
        g2d.fillRoundRect(0, 0, w, h, 30, 30);
        
        // ขอบทอง
        g2d.setColor(GOLD);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(2, 2, w-4, h-4, 28, 28);
        
        // ลวดลายด้านใน
        g2d.setColor(new Color(255, 255, 255, 20));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(10, 10, w-20, h-20, 20, 20);
    }
    
    private void drawHeader(Graphics2D g2d, int w, int h) {
        // ชื่อเกม
        g2d.setColor(WHITE);
        g2d.setFont(new Font("Tahoma", Font.BOLD, 32));
        g2d.drawString("เป่ายิ้งฉุบ", 30, 50);
        
        // สถานะ multiplayer
        if (isMultiplayer) {
            g2d.setColor(CYAN);
            g2d.setFont(new Font("Tahoma", Font.BOLD, 16));
            g2d.drawString("👥 Multiplayer", 30, 80);
        }
        
        // เวลา (เฉพาะโหมด multiplayer)
        if (isMultiplayer && phase == GamePhase.PLAYING && isTimerRunning) {
            int timerX = w - 150;
            int timerY = 40;
            int timerW = 120;
            int timerH = 25;
            
            // พื้นหลังเวลา
            g2d.setColor(new Color(30, 30, 50));
            g2d.fillRoundRect(timerX, timerY, timerW, timerH, 12, 12);
            
            // แถบเวลา
            float timePercent = (float)timeLeft / DEFAULT_TIME;
            int fillW = (int)(timerW * timePercent);
            
            Color timeColor = timeLeft > 8 ? GREEN : (timeLeft > 4 ? YELLOW : RED);
            g2d.setColor(timeColor);
            g2d.fillRoundRect(timerX, timerY, fillW, timerH, 12, 12);
            
            // ตัวเลขเวลา
            g2d.setColor(WHITE);
            g2d.setFont(new Font("Tahoma", Font.BOLD, 14));
            String timeText = timeLeft + "s";
            FontMetrics fm = g2d.getFontMetrics();
            int textX = timerX + (timerW - fm.stringWidth(timeText)) / 2;
            int textY = timerY + 18;
            g2d.drawString(timeText, textX, textY);
        }
    }
    
    private void drawScores(Graphics2D g2d, int w, int h) {
        int centerX = w / 2;
        
        // Player score
        g2d.setColor(CYAN);
        g2d.setFont(new Font("Tahoma", Font.BOLD, 24));
        String playerScoreText = "คุณ " + playerScore;
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(playerScoreText, centerX - 150, 100);
        
        // Computer score
        g2d.setColor(PINK);
        String compScoreText = "คอม " + computerScore;
        g2d.drawString(compScoreText, centerX + 50, 100);
        
        // VS divider
        g2d.setColor(GOLD);
        g2d.setFont(new Font("Tahoma", Font.BOLD, 20));
        g2d.drawString("VS", centerX - 20, 100);
        
        // เป้าหมาย
        g2d.setColor(new Color(150, 150, 150));
        g2d.setFont(new Font("Tahoma", Font.PLAIN, 14));
        g2d.drawString("(ชนะ " + WIN_CONDITION + " ใน " + WIN_CONDITION + ")", centerX - 60, 130);
    }
    
    private void drawChoices(Graphics2D g2d, int w, int h) {
        int choiceY = h - 200;
        int choiceW = 150;
        int choiceH = 150;
        int gap = 30;
        int startX = (w - (3 * choiceW + 2 * gap)) / 2;
        
        Move[] moves = Move.values();
        String[] moveNames = {"ค้อน", "กรรไกร", "กระดาษ"};
        
        for (int i = 0; i < 3; i++) {
            int x = startX + i * (choiceW + gap);
            choiceBounds[i] = new Rectangle(x, choiceY, choiceW, choiceH);
            
            // ตรวจสอบ hover
            Point mousePos = getMousePosition();
            boolean hover = mousePos != null && choiceBounds[i].contains(mousePos);
            
            // วาดปุ่มเลือก
            drawChoiceButton(g2d, x, choiceY, choiceW, choiceH, 
                           moves[i].emoji, moveNames[i], 
                           hover && canInteract && playerMove == null,
                           playerMove == moves[i]);
        }
    }
    
    private void drawChoiceButton(Graphics2D g2d, int x, int y, int w, int h, 
                                  String emoji, String name, boolean hover, boolean selected) {
        // เงา
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRoundRect(x + 3, y + 3, w, h, 20, 20);
        
        // พื้นหลัง
        Color bgColor;
        if (selected) {
            bgColor = GOLD_BRIGHT;
        } else if (hover) {
            bgColor = new Color(80, 100, 150);
        } else {
            bgColor = new Color(40, 50, 80);
        }
        
        g2d.setColor(bgColor);
        g2d.fillRoundRect(x, y, w, h, 20, 20);
        
        // ขอบ
        g2d.setColor(selected ? GOLD : (hover ? CYAN : new Color(100, 100, 150)));
        g2d.setStroke(new BasicStroke(selected ? 3 : 2));
        g2d.drawRoundRect(x, y, w, h, 20, 20);
        
        // Emoji
        g2d.setColor(WHITE);
        g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        FontMetrics fm = g2d.getFontMetrics();
        int emX = x + (w - fm.stringWidth(emoji)) / 2;
        g2d.drawString(emoji, emX, y + 80);
        
        // ชื่อ
        g2d.setFont(new Font("Tahoma", Font.PLAIN, 16));
        fm = g2d.getFontMetrics();
        int nameX = x + (w - fm.stringWidth(name)) / 2;
        g2d.drawString(name, nameX, y + 120);
    }
    
    private void drawRoundResult(Graphics2D g2d, int w, int h) {
        int centerY = h / 2 - 50;
        
        // ผู้เล่น
        g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        g2d.drawString(playerMove.emoji, w/4 - 40, centerY);
        
        // Computer
        g2d.drawString(computerMove.emoji, w*3/4 - 40, centerY);
        
        // VS
        g2d.setFont(new Font("Tahoma", Font.BOLD, 40));
        g2d.setColor(GOLD);
        g2d.drawString("VS", w/2 - 40, centerY);
        
        // ผลลัพธ์
        String resultText;
        Color resultColor;
        
        switch (roundResult) {
            case WIN:
                resultText = "คุณชนะ!";
                resultColor = GREEN;
                break;
            case LOSE:
                resultText = "คุณแพ้";
                resultColor = RED;
                break;
            default:
                resultText = "เสมอ";
                resultColor = YELLOW;
                break;
        }
        
        g2d.setFont(new Font("Tahoma", Font.BOLD, 32));
        g2d.setColor(resultColor);
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (w - fm.stringWidth(resultText)) / 2;
        g2d.drawString(resultText, textX, centerY + 100);
        
        // ชื่อท่าทาง
        g2d.setFont(new Font("Tahoma", Font.PLAIN, 18));
        g2d.setColor(WHITE);
        
        String playerMoveName = playerMove.thaiName;
        fm = g2d.getFontMetrics();
        g2d.drawString(playerMoveName, w/4 - fm.stringWidth(playerMoveName)/2, centerY + 50);
        
        String compMoveName = computerMove.thaiName;
        g2d.drawString(compMoveName, w*3/4 - fm.stringWidth(compMoveName)/2, centerY + 50);
    }
    
    private void drawParticles(Graphics2D g2d) {
        for (int i = 0; i < particles.length; i++) {
            if (particleAlpha[i] > 0.1f) {
                g2d.setColor(new Color(255, 215, 0, (int)(particleAlpha[i] * 255)));
                g2d.fillOval(particles[i].x - 3, particles[i].y - 3, 6, 6);
            }
        }
    }
    
    private void drawCountdown(Graphics2D g2d, int w, int h) {
        g2d.setFont(new Font("Tahoma", Font.BOLD, 120));
        g2d.setColor(new Color(255, 255, 255, 200));
        String text = String.valueOf(countdown);
        FontMetrics fm = g2d.getFontMetrics();
        int x = (w - fm.stringWidth(text)) / 2;
        int y = h / 2;
        g2d.drawString(text, x, y);
        
        g2d.setFont(new Font("Tahoma", Font.BOLD, 24));
        g2d.setColor(YELLOW);
        String ready = "เตรียมตัวให้พร้อม!";
        fm = g2d.getFontMetrics();
        x = (w - fm.stringWidth(ready)) / 2;
        y = h / 2 + 80;
        g2d.drawString(ready, x, y);
    }
    
    private void drawGameOverlay(Graphics2D g2d, int w, int h) {
        // พื้นหลังโปร่งแสง
        g2d.setColor(new Color(0, 0, 0, (int)(150 * winAlpha)));
        g2d.fillRect(0, 0, w, h);
        
        // ข้อความผลลัพธ์
        g2d.setFont(new Font("Tahoma", Font.BOLD, 64));
        
        String resultText;
        Color textColor;
        
        if (playerScore >= WIN_CONDITION) {
            resultText = "VICTORY!";
            textColor = new Color(255, 215, 0, (int)(255 * winAlpha));
        } else {
            resultText = "GAME OVER";
            textColor = new Color(255, 100, 100, (int)(255 * winAlpha));
        }
        
        g2d.setColor(textColor);
        FontMetrics fm = g2d.getFontMetrics();
        int x = (w - fm.stringWidth(resultText)) / 2;
        int y = h / 2 - 50;
        g2d.drawString(resultText, x, y);
        
        // คะแนนที่ได้รับ
        g2d.setFont(new Font("Tahoma", Font.BOLD, 32));
        g2d.setColor(WHITE);
        
        String pointsText;
        if (playerScore >= WIN_CONDITION) {
            pointsText = "🏆 ได้ +5 คะแนนความสัมพันธ์";
        } else {
            pointsText = "💔 ได้ +2 คะแนนความสัมพันธ์";
        }
        
        fm = g2d.getFontMetrics();
        x = (w - fm.stringWidth(pointsText)) / 2;
        y = h / 2 + 20;
        g2d.drawString(pointsText, x, y);
    }
}