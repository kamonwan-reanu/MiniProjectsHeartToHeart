package UI_Components; 

 

import javax.swing.*; 

import java.awt.*; 

import java.awt.event.*; 

import java.util.ArrayList; 

import java.util.Collections; 

 

public class MemoryMatchMiniGame extends JPanel { 

    private class Card { 

        String emoji; 

        boolean isRevealed = false; 

        boolean isMatched = false; 

        Rectangle bounds; 

 

        Card(String emoji) { this.emoji = emoji; } 

    } 

 

    private ArrayList<Card> cards = new ArrayList<>(); 

    private Card firstSelected = null; 

    private Card secondSelected = null; 

    private int timeLeft = 25; // ให้เวลา 25 วินาที 

    private int pairsFound = 0; 

    private Timer gameTimer; 

    private Runnable onWin, onFail; 

    private boolean canClick = true; 

 

    public MemoryMatchMiniGame(Runnable onWin, Runnable onFail) { 

        this.onWin = onWin; 

        this.onFail = onFail; 

        setOpaque(false); 

 

        // เตรียมข้อมูลการ์ด (4 คู่ = 8 ใบ) 

        String[] emojis = {"🌸", "🍬", "🎁", "⭐", "🌸", "🍬", "🎁", "⭐"}; 

        ArrayList<String> list = new ArrayList<>(); 

        for (String s : emojis) list.add(s); 

        Collections.shuffle(list); 

 

        for (String s : list) cards.add(new Card(s)); 

 

        // ระบบนับถอยหลัง 

        gameTimer = new Timer(1000, e -> { 

            timeLeft--; 

            if (timeLeft <= 0) { 

                gameTimer.stop(); 

                onFail.run(); 

            } 

            repaint(); 

        }); 

        gameTimer.start(); 

 

        // ระบบคลิกตรวจจับตำแหน่งการ์ด 

        addMouseListener(new MouseAdapter() { 

            @Override 

            public void mousePressed(MouseEvent e) { 

                if (!canClick) return; 

 

                for (Card card : cards) { 

                    if (card.bounds != null && card.bounds.contains(e.getPoint()) && !card.isRevealed && !card.isMatched) { 

                        card.isRevealed = true; 

                        repaint(); 

 

                        if (firstSelected == null) { 

                            firstSelected = card; 

                        } else { 

                            secondSelected = card; 

                            canClick = false; 

                            checkMatch(); 

                        } 

                        break; 

                    } 

                } 

            } 

        }); 

    } 

 

    private void checkMatch() { 

        Timer checkTimer = new Timer(600, e -> { 

            if (firstSelected.emoji.equals(secondSelected.emoji)) { 

                firstSelected.isMatched = true; 

                secondSelected.isMatched = true; 

                pairsFound++; 

                if (pairsFound == 4) { 

                    gameTimer.stop(); 

                    onWin.run(); 

                } 

            } else { 

                firstSelected.isRevealed = false; 

                secondSelected.isRevealed = false; 

            } 

            firstSelected = null; 

            secondSelected = null; 

            canClick = true; 

            repaint(); 

        }); 

        checkTimer.setRepeats(false); 

        checkTimer.start(); 

    } 

 

    @Override 

    protected void paintComponent(Graphics g) { 

        Graphics2D g2d = (Graphics2D) g.create(); 

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 

 

        // 1. วาดกรอบ UI (Glass Morphism) 

        g2d.setColor(new Color(20, 20, 40, 220)); // สีกรมท่าโปร่งแสง 

        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30); 

        g2d.setColor(new Color(212, 175, 55, 150)); // ขอบทอง 

        g2d.setStroke(new BasicStroke(4)); 

        g2d.drawRoundRect(5, 5, getWidth()-10, getHeight()-10, 25, 25); 

 

        // 2. วาดข้อมูล Header 

        g2d.setColor(Color.WHITE); 

        g2d.setFont(new Font("Tahoma", Font.BOLD, 24)); 

        g2d.drawString("จับคู่ความทรงจำกับ Theer", 40, 50); 

         

        // แถบเวลา 

        g2d.setFont(new Font("Tahoma", Font.PLAIN, 18)); 

        g2d.drawString("เวลาที่เหลือ: " + timeLeft + "s", getWidth() - 150, 50); 

 

        // 3. วาดการ์ด (จัดเรียง 4x2) 

        int cardW = 120; 

        int cardH = 160; 

        int gap = 20; 

        int startX = (getWidth() - (4 * cardW + 3 * gap)) / 2; 

        int startY = 100; 

 

        for (int i = 0; i < cards.size(); i++) { 

            int row = i / 4; 

            int col = i % 4; 

            int x = startX + (col * (cardW + gap)); 

            int y = startY + (row * (cardH + gap)); 

             

            Card card = cards.get(i); 

            card.bounds = new Rectangle(x, y, cardW, cardH); 

 

            if (card.isMatched) continue; // ถ้าถูกจับคู่แล้วให้หายไป (หรือจะวาดแบบจางๆ ก็ได้) 

 

            // วาดตัวการ์ด 

            if (card.isRevealed) { 

                // หน้าการ์ด (เปิดอยู่) 

                g2d.setColor(Color.WHITE); 

                g2d.fillRoundRect(x, y, cardW, cardH, 15, 15); 

                g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50)); 

                FontMetrics fm = g2d.getFontMetrics(); 

                g2d.drawString(card.emoji, x + (cardW - fm.stringWidth(card.emoji))/2, y + (cardH/2) + 20); 

            } else { 

                // หลังการ์ด (ปิดอยู่) 

                GradientPaint gp = new GradientPaint(x, y, new Color(70, 130, 180), x + cardW, y + cardH, new Color(30, 60, 90)); 

                g2d.setPaint(gp); 

                g2d.fillRoundRect(x, y, cardW, cardH, 15, 15); 

                g2d.setColor(new Color(255, 255, 255, 50)); 

                g2d.setStroke(new BasicStroke(2)); 

                g2d.drawRoundRect(x+5, y+5, cardW-10, cardH-10, 10, 10); 

                g2d.setColor(Color.WHITE); 

                g2d.setFont(new Font("Tahoma", Font.BOLD, 40)); 

                g2d.drawString("?", x + 45, y + 95); 

            } 

        } 

        g2d.dispose(); 

    } 

} 