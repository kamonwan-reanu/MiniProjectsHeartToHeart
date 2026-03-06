package UI_Components; 

 

import javax.swing.*; 

import java.awt.*; 

import java.awt.event.*; 

import java.util.Random; 

 

public class HeartClickMiniGame extends JPanel { 

    private int score = 0, timeLeft = 10; 

    private final int GOAL = 8; 

    private Runnable onWin, onFail; 

    private Point heartPos = new Point(250, 200); 

    private int heartSize = 80; 

    private Timer gameTimer; 

 

    public HeartClickMiniGame(Runnable onWin, Runnable onFail) { 

        this.onWin = onWin; this.onFail = onFail; 

        setOpaque(false); 

 

        gameTimer = new Timer(1000, e -> { 

            timeLeft--; 

            if (timeLeft <= 0) { gameTimer.stop(); onFail.run(); } 

            repaint(); 

        }); 

        gameTimer.start(); 

 

        addMouseListener(new MouseAdapter() { 

            @Override 

            public void mousePressed(MouseEvent e) { 

                Rectangle heartRect = new Rectangle(heartPos.x, heartPos.y, heartSize, heartSize); 

                if (heartRect.contains(e.getPoint())) { 

                    score++; 

                    if (score >= GOAL) { gameTimer.stop(); onWin.run(); } 

                    else { moveHeart(); } 

                    repaint(); 

                } 

            } 

        }); 

    } 

 

    private void moveHeart() { 

        Random r = new Random(); 

        heartPos.x = r.nextInt(getWidth() - heartSize - 40) + 20; 

        heartPos.y = r.nextInt(getHeight() - heartSize - 100) + 80; 

    } 

 

    @Override 

    protected void paintComponent(Graphics g) { 

        Graphics2D g2d = (Graphics2D) g.create(); 

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 

 

        // UI Panel 

        g2d.setColor(new Color(255, 182, 193, 200)); // Pink Glass 

        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30); 

         

        g2d.setColor(Color.WHITE); 

        g2d.setFont(new Font("Tahoma", Font.BOLD, 22)); 

        g2d.drawString("TIME: " + timeLeft, 30, 45); 

        g2d.drawString("SCORE: " + score + "/" + GOAL, getWidth()-180, 45); 

 

        // Draw Heart Emoji 

        g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, heartSize)); 

        g2d.drawString("❤️", heartPos.x, heartPos.y + heartSize - 10); 

         

        g2d.dispose(); 

    } 

} 