package UI_Components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MemoryMatchMiniGame extends JPanel {
    
    // ========== Constants ==========
    private static final int TOTAL_PAIRS = 4;
    private static final int CARD_WIDTH = 100;
    private static final int CARD_HEIGHT = 130;
    private static final int CARD_GAP = 15;
    private static final int GAME_TIME_SECONDS = 45; // เวลาที่เท่ากันสำหรับทุกคน
    
    // ========== Colors ==========
    private static final Color BG_DARK = new Color(15, 20, 35, 240);
    private static final Color GOLD = new Color(212, 175, 55);
    private static final Color GOLD_BRIGHT = new Color(255, 215, 0);
    private static final Color CARD_BACK_START = new Color(70, 130, 180);
    private static final Color CARD_BACK_END = new Color(30, 60, 90);
    private static final Color CARD_MATCHED = new Color(100, 255, 100, 100);
    private static final Color TEXT_WHITE = Color.WHITE;
    private static final Color TEXT_YELLOW = new Color(255, 255, 150);
    
    // ========== Inner Classes ==========
    private class Card {
        String emoji;
        boolean isRevealed = false;
        boolean isMatched = false;
        Rectangle bounds;
        float glowAlpha = 0f;
        Timer glowTimer;
        
        Card(String emoji) { 
            this.emoji = emoji; 
        }
        
        void startGlow() {
            if (glowTimer != null) glowTimer.stop();
            glowAlpha = 0.8f;
            glowTimer = new Timer(50, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    glowAlpha -= 0.1f;
                    repaint();
                    if (glowAlpha <= 0) {
                        glowAlpha = 0;
                        glowTimer.stop();
                    }
                }
            });
            glowTimer.start();
        }
    }
    
    private enum GamePhase {
        COUNTDOWN,  // นับถอยหลัง 3-2-1
        PLAYING,    // กำลังเล่น
        RESULT,     // แสดงผลแพ้ชนะ
        FINISHED    // จบเกม
    }
    
    // ========== Game State ==========
    private ArrayList<Card> cards = new ArrayList<>();
    private Card firstSelected = null;
    private Card secondSelected = null;
    
    private int timeLeft = GAME_TIME_SECONDS;
    private int pairsFound = 0;
    private int countdown = 3;
    
    private GamePhase phase = GamePhase.COUNTDOWN;
    private boolean canInteract = false;
    private boolean isMultiplayer = false;
    
    // ========== Callbacks ==========
    private Runnable onWinCallback;
    private Runnable onFailCallback;
    private Runnable onTimeSyncCallback; // สำหรับ sync เวลาใน multiplayer
    
    // ========== Timers ==========
    private Timer gameTimer;
    private Timer countdownTimer;
    private Timer flipBackTimer;
    private Timer resultTimer;
    
    // ========== Animation ==========
    private float winAlpha = 0f;
    private float[] particleX = new float[30];
    private float[] particleY = new float[30];
    private float[] particleVX = new float[30];
    private float[] particleVY = new float[30];
    private boolean showParticles = false;
    private Random random = new Random();
    
    // ========== Constructor ==========
    public MemoryMatchMiniGame(Runnable onWin, Runnable onFail) {
        this(onWin, onFail, false);
    }
    
    public MemoryMatchMiniGame(Runnable onWin, Runnable onFail, boolean isMultiplayer) {
        this.onWinCallback = onWin;
        this.onFailCallback = onFail;
        this.isMultiplayer = isMultiplayer;
        
        setOpaque(false);
        initCards();
        setupUI();
        startCountdown();
        initParticles();
    }
    
    // ========== Initialization ==========
    private void initCards() {
        String[] emojis = {"🌸", "🍬", "🎁", "⭐"};
        ArrayList<String> list = new ArrayList<>();
        for (String s : emojis) {
            list.add(s);
            list.add(s); // เพิ่มคู่
        }
        Collections.shuffle(list);
        
        for (String s : list) {
            cards.add(new Card(s));
        }
    }
    
    private void initParticles() {
        for (int i = 0; i < particleX.length; i++) {
            resetParticle(i);
        }
    }
    
    private void resetParticle(int i) {
        particleX[i] = getWidth() / 2f;
        particleY[i] = getHeight() / 2f;
        particleVX[i] = (random.nextFloat() - 0.5f) * 8;
        particleVY[i] = (random.nextFloat() - 0.5f) * 8 - 2;
    }
    
    private void setupUI() {
        setLayout(null);
        
        // Mouse listener สำหรับการเล่นเกม
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!canInteract || phase != GamePhase.PLAYING) return;
                
                Point p = e.getPoint();
                for (int i = 0; i < cards.size(); i++) {
                    Card card = cards.get(i);
                    if (card.bounds != null && card.bounds.contains(p) 
                            && !card.isRevealed && !card.isMatched) {
                        
                        handleCardClick(card);
                        break;
                    }
                }
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
        timeLeft = GAME_TIME_SECONDS;
        
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
                
                // หมดเวลา
                if (timeLeft <= 0) {
                    gameTimer.stop();
                    phase = GamePhase.RESULT;
                    showFailAnimation();
                }
            }
        });
        gameTimer.start();
    }
    
    private void handleCardClick(Card card) {
        // เล่นเสียง (ถ้ามี)
        playSound("card_flip");
        
        card.isRevealed = true;
        card.startGlow();
        repaint();
        
        if (firstSelected == null) {
            firstSelected = card;
        } else if (secondSelected == null && card != firstSelected) {
            secondSelected = card;
            canInteract = false;
            checkMatch();
        }
    }
    
    private void checkMatch() {
        flipBackTimer = new Timer(600, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (firstSelected.emoji.equals(secondSelected.emoji)) {
                    // เจอคู่
                    firstSelected.isMatched = true;
                    secondSelected.isMatched = true;
                    pairsFound++;
                    
                    // เล่นเสียงสำเร็จ
                    playSound("match");
                    
                    // สร้าง particle effect
                    showParticles = true;
                    for (int i = 0; i < particleX.length; i++) {
                        resetParticle(i);
                    }
                    
                    // ตรวจสอบว่าชนะหรือยัง
                    if (pairsFound == TOTAL_PAIRS) {
                        gameTimer.stop();
                        phase = GamePhase.RESULT;
                        showWinAnimation();
                    }
                } else {
                    // ไม่เจอคู่
                    firstSelected.isRevealed = false;
                    secondSelected.isRevealed = false;
                    
                    // เล่นเสียงผิดพลาด
                    playSound("mismatch");
                }
                
                firstSelected = null;
                secondSelected = null;
                canInteract = true;
                repaint();
                
                // หยุด particle effect หลังจาก 1 วินาที
                if (showParticles) {
                    Timer particleTimer = new Timer(1000, ev -> {
                        showParticles = false;
                        repaint();
                    });
                    particleTimer.setRepeats(false);
                    particleTimer.start();
                }
                
                ((Timer)e.getSource()).stop();
            }
        });
        flipBackTimer.setRepeats(false);
        flipBackTimer.start();
    }
    
    private void showWinAnimation() {
        // ชนะได้ 5 คะแนน
        if (onWinCallback != null) {
            onWinCallback.run();
        }
        
        // แสดง animation ชนะ
        resultTimer = new Timer(50, new ActionListener() {
            float alpha = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                alpha += 0.05f;
                winAlpha = alpha;
                repaint();
                
                if (alpha >= 1f) {
                    resultTimer.stop();
                    phase = GamePhase.FINISHED;
                    
                    // ปิดเกมหลังจาก 2 วินาที
                    Timer closeTimer = new Timer(2000, ev -> {
                        Container parent = getParent();
                        if (parent != null) {
                            parent.remove(MemoryMatchMiniGame.this);
                            parent.revalidate();
                            parent.repaint();
                        }
                    });
                    closeTimer.setRepeats(false);
                    closeTimer.start();
                }
            }
        });
        resultTimer.start();
    }
    
    private void showFailAnimation() {
        // แพ้ได้ 2 คะแนน
        if (onFailCallback != null) {
            onFailCallback.run();
        }
        
        // แสดง animation แพ้
        resultTimer = new Timer(50, new ActionListener() {
            float alpha = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                alpha += 0.05f;
                winAlpha = alpha;
                repaint();
                
                if (alpha >= 1f) {
                    resultTimer.stop();
                    phase = GamePhase.FINISHED;
                    
                    // ปิดเกมหลังจาก 2 วินาที
                    Timer closeTimer = new Timer(2000, ev -> {
                        Container parent = getParent();
                        if (parent != null) {
                            parent.remove(MemoryMatchMiniGame.this);
                            parent.revalidate();
                            parent.repaint();
                        }
                    });
                    closeTimer.setRepeats(false);
                    closeTimer.start();
                }
            }
        });
        resultTimer.start();
    }
    
    // ========== Multiplayer Methods ==========
    public void syncTime(int serverTime) {
        // ใช้สำหรับ sync เวลากับ server ในโหมด multiplayer
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
    
    // ========== Sound ==========
    private void playSound(String soundName) {
        // TODO: เชื่อมต่อกับ SoundManager ของโปรเจค
        // core.Main.soundManager.playSE(soundName + ".wav");
    }
    
    // ========== Painting ==========
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
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
        
        // วาดการ์ด
        drawCards(g2d, w, h);
        
        // วาด particles
        if (showParticles) {
            drawParticles(g2d);
        }
        
        // วาด countdown
        if (phase == GamePhase.COUNTDOWN) {
            drawCountdown(g2d, w, h);
        }
        
        // วาดผลลัพธ์
        if (phase == GamePhase.RESULT || phase == GamePhase.FINISHED) {
            drawResult(g2d, w, h);
        }
        
        g2d.dispose();
    }
    
    private void drawBackground(Graphics2D g2d, int w, int h) {
        // พื้นหลังหลัก
        g2d.setColor(BG_DARK);
        g2d.fillRoundRect(0, 0, w, h, 30, 30);
        
        // ขอบทอง
        g2d.setColor(GOLD);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(2, 2, w-4, h-4, 28, 28);
        
        // Effect ภายใน
        g2d.setColor(new Color(255, 255, 255, 20));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(10, 10, w-20, h-20, 20, 20);
    }
    
    private void drawHeader(Graphics2D g2d, int w, int h) {
        // ชื่อเกม
        g2d.setColor(TEXT_WHITE);
        g2d.setFont(new Font("Tahoma", Font.BOLD, 28));
        g2d.drawString("Memory Match", 30, 50);
        
        // สถานะ multiplayer
        if (isMultiplayer) {
            g2d.setColor(GOLD_BRIGHT);
            g2d.setFont(new Font("Tahoma", Font.BOLD, 16));
            g2d.drawString("👥 Multiplayer Mode", 30, 80);
        }
        
        // กรอบเวลา
        int timerX = w - 220;
        int timerY = 35;
        int timerW = 190;
        int timerH = 30;
        
        // พื้นหลังเวลา
        g2d.setColor(new Color(30, 30, 50));
        g2d.fillRoundRect(timerX, timerY, timerW, timerH, 15, 15);
        
        // แถบเวลา
        float timePercent = (float)timeLeft / GAME_TIME_SECONDS;
        int fillW = (int)(timerW * timePercent);
        
        // เปลี่ยนสีตามเวลาที่เหลือ
        Color timeColor;
        if (timeLeft > 20) {
            timeColor = new Color(100, 255, 100);
        } else if (timeLeft > 10) {
            timeColor = new Color(255, 255, 100);
        } else {
            timeColor = new Color(255, 100, 100);
        }
        
        g2d.setColor(timeColor);
        g2d.fillRoundRect(timerX, timerY, fillW, timerH, 15, 15);
        
        // ตัวเลขเวลา
        g2d.setColor(TEXT_WHITE);
        g2d.setFont(new Font("Tahoma", Font.BOLD, 18));
        FontMetrics fm = g2d.getFontMetrics();
        String timeText = timeLeft + "s";
        int textX = timerX + (timerW - fm.stringWidth(timeText)) / 2;
        int textY = timerY + 22;
        g2d.drawString(timeText, textX, textY);
        
        // คะแนน
        g2d.setFont(new Font("Tahoma", Font.BOLD, 20));
        g2d.drawString("คู่ที่เจอ: " + pairsFound + "/" + TOTAL_PAIRS, 30, 120);
    }
    
    private void drawCards(Graphics2D g2d, int w, int h) {
        // คำนวณตำแหน่ง grid
        int totalWidth = 4 * CARD_WIDTH + 3 * CARD_GAP;
        int startX = (w - totalWidth) / 2;
        int startY = 150;
        
        for (int i = 0; i < cards.size(); i++) {
            int row = i / 4;
            int col = i % 4;
            int x = startX + (col * (CARD_WIDTH + CARD_GAP));
            int y = startY + (row * (CARD_HEIGHT + CARD_GAP));
            
            Card card = cards.get(i);
            card.bounds = new Rectangle(x, y, CARD_WIDTH, CARD_HEIGHT);
            
            // วาดการ์ด
            drawCard(g2d, card, x, y);
        }
    }
    
    private void drawCard(Graphics2D g2d, Card card, int x, int y) {
        // เงา
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillRoundRect(x + 3, y + 3, CARD_WIDTH, CARD_HEIGHT, 15, 15);
        
        if (card.isMatched) {
            // การ์ดที่จับคู่แล้ว - โปร่งแสง
            g2d.setColor(CARD_MATCHED);
            g2d.fillRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 15, 15);
            
            g2d.setColor(TEXT_WHITE);
            g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
            FontMetrics fm = g2d.getFontMetrics();
            int emX = x + (CARD_WIDTH - fm.stringWidth(card.emoji)) / 2;
            int emY = y + CARD_HEIGHT/2 + 15;
            g2d.drawString(card.emoji, emX, emY);
            
        } else if (card.isRevealed) {
            // การ์ดที่เปิดอยู่
            // พื้นหลังขาว
            g2d.setColor(Color.WHITE);
            g2d.fillRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 15, 15);
            
            // ขอบทอง
            g2d.setColor(GOLD);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 15, 15);
            
            // อิโมจิ
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
            FontMetrics fm = g2d.getFontMetrics();
            int emX = x + (CARD_WIDTH - fm.stringWidth(card.emoji)) / 2;
            int emY = y + CARD_HEIGHT/2 + 15;
            g2d.drawString(card.emoji, emX, emY);
            
        } else {
            // การ์ดที่ปิดอยู่
            // พื้นหลัง gradient
            GradientPaint gp = new GradientPaint(
                x, y, CARD_BACK_START,
                x + CARD_WIDTH, y + CARD_HEIGHT, CARD_BACK_END
            );
            g2d.setPaint(gp);
            g2d.fillRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 15, 15);
            
            // ลวดลาย
            g2d.setColor(new Color(255, 255, 255, 30));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(x + 20, y + 20, x + CARD_WIDTH - 20, y + CARD_HEIGHT - 20);
            g2d.drawLine(x + CARD_WIDTH - 20, y + 20, x + 20, y + CARD_HEIGHT - 20);
            
            // เครื่องหมายคำถาม
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.setFont(new Font("Tahoma", Font.BOLD, 40));
            g2d.drawString("?", x + 35, y + 85);
        }
        
        // Glow effect สำหรับการ์ดที่เพิ่งถูกเปิด
        if (card.glowAlpha > 0) {
            g2d.setColor(new Color(255, 215, 0, (int)(card.glowAlpha * 100)));
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRoundRect(x-2, y-2, CARD_WIDTH+4, CARD_HEIGHT+4, 17, 17);
        }
        
        // ไฮไลท์สำหรับการ์ดที่ถูกเลือก
        if (card == firstSelected || card == secondSelected) {
            g2d.setColor(new Color(255, 215, 0, 150));
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRoundRect(x-2, y-2, CARD_WIDTH+4, CARD_HEIGHT+4, 17, 17);
        }
    }
    
    private void drawParticles(Graphics2D g2d) {
        g2d.setColor(GOLD_BRIGHT);
        for (int i = 0; i < particleX.length; i++) {
            particleX[i] += particleVX[i];
            particleY[i] += particleVY[i];
            particleVY[i] += 0.2f; // gravity
            
            int alpha = (int)(255 * (1 - i/particleX.length));
            g2d.setColor(new Color(255, 215, 0, alpha));
            g2d.fillOval((int)particleX[i] - 3, (int)particleY[i] - 3, 6, 6);
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
        g2d.setColor(TEXT_YELLOW);
        String ready = "เตรียมตัวให้พร้อม!";
        fm = g2d.getFontMetrics();
        x = (w - fm.stringWidth(ready)) / 2;
        y = h / 2 + 80;
        g2d.drawString(ready, x, y);
    }
    
    private void drawResult(Graphics2D g2d, int w, int h) {
        if (winAlpha <= 0) return;
        
        // พื้นหลังโปร่งแสง
        g2d.setColor(new Color(0, 0, 0, (int)(150 * winAlpha)));
        g2d.fillRect(0, 0, w, h);
        
        // ข้อความผลลัพธ์
        g2d.setFont(new Font("Tahoma", Font.BOLD, 64));
        
        String resultText;
        Color textColor;
        
        if (pairsFound == TOTAL_PAIRS) {
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
        g2d.setColor(TEXT_WHITE);
        
        String pointsText;
        if (pairsFound == TOTAL_PAIRS) {
            pointsText = "ได้ +5 คะแนนความสัมพันธ์";
        } else {
            pointsText = "ได้ +2 คะแนนความสัมพันธ์";
        }
        
        fm = g2d.getFontMetrics();
        x = (w - fm.stringWidth(pointsText)) / 2;
        y = h / 2 + 20;
        g2d.drawString(pointsText, x, y);
    }
}