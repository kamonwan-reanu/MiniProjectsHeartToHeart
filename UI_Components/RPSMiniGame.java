package UI_Components; 

 

import javax.swing.*; 

import java.awt.*; 

import java.awt.event.*; 

import java.util.Random; 

 

public class RPSMiniGame extends JPanel { 

    private String[] emojis = {"✊", "✌️", "✋"}; 

    private String[] labels = {"Rock", "Scissors", "Paper"}; 

    private Runnable onWin, onFail; 

    private int timeLeft = 5, playerHP = 1, comHP = 1; 

    private Timer gameTimer; 

    private String resultMsg = "Choose your move!"; 

    private int selectedIdx = -1; 

 

    public RPSMiniGame(Runnable onWin, Runnable onFail) { 

        this.onWin = onWin; this.onFail = onFail; 

        setOpaque(false); 

        setFocusable(true); 

 

        gameTimer = new Timer(1000, e -> { 

            timeLeft--; 

            if (timeLeft <= 0) { gameTimer.stop(); onFail.run(); } 

            repaint(); 

        }); 

        gameTimer.start(); 

 

        addMouseListener(new MouseAdapter() { 

            @Override 

            public void mousePressed(MouseEvent e) { 

                int w = getWidth() / 3; 

                if (e.getY() > 300) { 

                    play(e.getX() / w); 

                } 

            } 

        }); 

    } 

 

    private void play(int player) { 

        if (selectedIdx != -1) return; 

        selectedIdx = player; 

        gameTimer.stop(); 

        int com = new Random().nextInt(3); 

 

        Timer delay = new Timer(1000, e -> { 

            if (player == com) {  

                resultMsg = "DRAW! TRY AGAIN"; timeLeft = 5; selectedIdx = -1; gameTimer.start();  

            } else if ((player == 0 && com == 1) || (player == 1 && com == 2) || (player == 2 && com == 0)) { 

                resultMsg = "VICTORY!"; repaint(); 

                new Timer(1500, ex -> onWin.run()).start(); 

            } else { 

                resultMsg = "DEFEAT..."; repaint(); 

                new Timer(1500, ex -> onFail.run()).start(); 

            } 

            repaint(); 

        }); 

        delay.setRepeats(false); 

        delay.start(); 

    } 

 

    @Override 

    protected void paintComponent(Graphics g) { 

        Graphics2D g2d = (Graphics2D) g.create(); 

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 

 

        // Background Glass Effect 

        g2d.setColor(new Color(0, 0, 0, 180)); 

        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30); 

        g2d.setColor(new Color(255, 255, 255, 100)); 

        g2d.setStroke(new BasicStroke(3)); 

        g2d.drawRoundRect(5, 5, getWidth()-10, getHeight()-10, 25, 25); 

 

        // Timer Bar 

        g2d.setColor(Color.DARK_GRAY); 

        g2d.fillRoundRect(50, 40, getWidth()-100, 10, 5, 5); 

        g2d.setColor(timeLeft > 2 ? Color.CYAN : Color.RED); 

        g2d.fillRoundRect(50, 40, (int)((getWidth()-100) * (timeLeft/5.0)), 10, 5, 5); 

 

        // Messages 

        g2d.setColor(Color.WHITE); 

        g2d.setFont(new Font("Tahoma", Font.BOLD, 24)); 

        FontMetrics fm = g2d.getFontMetrics(); 

        g2d.drawString(resultMsg, (getWidth()-fm.stringWidth(resultMsg))/2, 100); 

 

        // Choices UI 

        for (int i = 0; i < 3; i++) { 

            int x = i * (getWidth()/3); 

            if (selectedIdx == i) { 

                g2d.setColor(new Color(255, 215, 0, 100)); 

                g2d.fillRoundRect(x+10, 320, getWidth()/3-20, 150, 20, 20); 

            } 

            g2d.setColor(Color.WHITE); 

            g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60)); 

            g2d.drawString(emojis[i], x + (getWidth()/3)/2 - 30, 400); 

            g2d.setFont(new Font("Tahoma", Font.BOLD, 16)); 

            g2d.drawString(labels[i], x + (getWidth()/3)/2 - 20, 440); 

        } 

        g2d.dispose(); 

    } 

} 