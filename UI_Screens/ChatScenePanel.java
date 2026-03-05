package UI_Screens;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.GameConstants;

public class ChatScenePanel extends JPanel {

    private static class ChatBubble {
        String speaker;
        String text;
        String avatarPath;    // รูปเล็ก (chibi) ใน bubble
        String portraitPath;  // รูปใหญ่ในกรอบวงกลมซ้าย
        boolean isPlayer;
        boolean isNarration;

        ChatBubble(String speaker, String text, String avatarPath, String portraitPath, boolean isPlayer) {
            this.speaker = speaker;
            this.text = text;
            this.avatarPath = avatarPath;
            this.portraitPath = portraitPath;
            this.isPlayer = isPlayer;
            this.isNarration = speaker.isEmpty();
        }
    }

    private final List<ChatBubble> allBubbles = new ArrayList<>();
    private final List<ChatBubble> shown      = new ArrayList<>();
    private int currentIndex = 0;
    private Runnable onFinished;
    private Image bgImage;
    private final Map<String, Image> imgCache = new HashMap<>();

    // Portrait ปัจจุบันทางซ้าย
    private String currentPortraitPath = null;

    private float animAlpha   = 1f;
    private float animOffsetY = 0f;
    private boolean isAnimating = false;
    
    private Timer animTimer;

    private int scrollY = 0;

    private static final int AVATAR_SIZE = 65;
    private static final int PAD         = 14;
    private static final int SPACING     = 14;
    private static final int SIDE_MARGIN = 20;
    private static final int MAX_BW      = 500;

    // ✨ เพิ่ม field
    private boolean showPortrait;

    public ChatScenePanel(Object[][] sceneData, String bgPath, Runnable onFinished, boolean showPortrait) {
        setOpaque(false);
        setLayout(null);
        this.onFinished = onFinished;
        this.showPortrait = showPortrait; // ✨
        loadImg(bgPath, "__BG__");
        parseScene(sceneData);

        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (!isAnimating) next();
            }
        });
    }

    public void resetAndStart() {
    shown.clear();
    currentIndex = 0;
    scrollY = 0;
    currentPortraitPath = null;
    isAnimating = false;
    if (animTimer != null) animTimer.stop();
    repaint();

    // ✅ เด้งอัตโนมัติทีละอัน ไม่ต้องแตะ
    autoShowNext();
}

    private void autoShowNext() {
        if (currentIndex >= allBubbles.size()) return;

        Timer t = new Timer(800, e -> {  // หน่วง 800ms ต่อข้อความ
            next();
            // ถ้ายังไม่ถึง choice ให้เด้งต่อ
            if (currentIndex < allBubbles.size()) {
                ChatBubble nextBubble = allBubbles.get(currentIndex);
                boolean isChoice = false;
                // เช็คว่า bubble ถัดไปเป็น choice ไหม (speaker = PLAYER + มี choice)
                if (nextBubble.isPlayer && currentIndex == allBubbles.size() - 1) {
                    isChoice = true;
                }
                if (!isChoice) {
                    autoShowNext(); // เรียกต่อไปเรื่อยๆ
                } else {
                    // ถึง choice แล้ว รอให้ผู้เล่นแตะเอง
                    next(); // แสดง choice
                }
            }
        });
        t.setRepeats(false);
        t.start();
    }

    private void parseScene(Object[][] data) {
        if (data == null) return;
        for (Object[] line : data) {
            if (line == null || line.length < 2) continue;

            boolean hasChoice = false;
            for (Object o : line) {
                if (o instanceof Object[][]) { hasChoice = true; break; }
            }
            if (hasChoice) continue;

            String speaker = line[0] != null ? line[0].toString().trim() : "";
            String text    = line[1] != null ? line[1].toString() : "";
            String raw     = (line.length >= 3 && line[2] != null) ? line[2].toString() : "none";

            boolean isPlayer = speaker.equalsIgnoreCase("PLAYER");

            // portraitPath = รูปเต็มตัวละคร (lineData[2])
            String portraitPath = null;
            if (!raw.equalsIgnoreCase("none") && !raw.isEmpty()) {
                portraitPath = raw.startsWith("model/") ? raw : GameConstants.CHAR_PATH + raw;
                loadImg(portraitPath, portraitPath);
            }

            // avatarPath = chibi ถ้ามี ไม่งั้นใช้รูปเดิม
            String avatarPath = getChibiPath(speaker);
            if (avatarPath != null) {
                loadImg(avatarPath, avatarPath);
            } else {
                avatarPath = portraitPath; // ใช้รูปเดิมแทน
            }

            allBubbles.add(new ChatBubble(speaker, text, avatarPath, portraitPath, isPlayer));
        }
    }

    private String getChibiPath(String speaker) {
        Map<String, String> map = new HashMap<>();
        String base = GameConstants.CHAR_PATH + "chibi_";
        map.put("ธีร์",  base + "teer.png");
        map.put("คีริน", base + "kirin.png");
        map.put("เทียน", base + "tian.png");
        map.put("PLAYER",base + "player.png");
        String path = map.get(speaker);
        if (path == null) return null;
        java.net.URL url = getClass().getClassLoader().getResource(path);
        return url != null ? path : null;
    }

    private void next() {
        if (currentIndex >= allBubbles.size()) {
            if (onFinished != null) onFinished.run();
            return;
        }
        ChatBubble b = allBubbles.get(currentIndex);
        shown.add(b);
        currentIndex++;

        // อัปเดต portrait ซ้ายเมื่อ NPC พูด
        if (!b.isNarration && !b.isPlayer && b.portraitPath != null) {
            currentPortraitPath = b.portraitPath;
        }

        startAnim();
    }

    private void startAnim() {
        if (animTimer != null) animTimer.stop();
        animAlpha = 0f; animOffsetY = 25f;
        isAnimating = true;
        animTimer = new Timer(16, e -> {
            animAlpha   = Math.min(1f, animAlpha + 0.1f);
            animOffsetY = Math.max(0f, animOffsetY - 3f);
            repaint();
            if (animAlpha >= 1f && animOffsetY <= 0f) {
                ((Timer)e.getSource()).stop();
                isAnimating = false;
                autoScroll();
            }
        });
        animTimer.start();
    }

    private void autoScroll() {
        Graphics2D g2 = (Graphics2D) getGraphics();
        if (g2 == null) return;
        int chatW = showPortrait ? (int)(getWidth() * 0.62) : (int)(getWidth() * 0.55);
        int total = 20;
        for (ChatBubble b : shown) total += bubbleH(b, chatW, g2) + SPACING;
        g2.dispose();
        int visible = getHeight() - 60;
        scrollY = Math.max(0, total - visible);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        drawBG(g2, w, h);

        int chatX, chatW;

        if (showPortrait && currentPortraitPath != null) {
            // มีรูปจริงๆ ค่อยวาดและเลื่อนแชทไปขวา
            int portraitSize = (int)(h * 0.35);
            int portraitX    = (int)(w * 0.05);
            int portraitY    = (h - portraitSize) / 2-50;
            drawPortrait(g2, currentPortraitPath, portraitX, portraitY, portraitSize);
            chatX = (int)(w * 0.36);
            chatW = w - chatX - 10;
        } else {
            // ไม่มีรูป → แชทกลางจอ
            chatW = (int)(w * 0.55);
            chatX = (w - chatW) / 2;
        }

        g2.translate(chatX, -scrollY + 20);
        drawBubbles(g2, chatW);
        g2.translate(-chatX, scrollY - 20);

        drawHint(g2, w, h);
        g2.dispose();
    }

    private void drawPortrait(Graphics2D g2, String path, int x, int y, int size) {
        if (path == null || !imgCache.containsKey(path)) return;

        // เงา
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillOval(x + 6, y + 6, size, size);

        // รูปในวงกลม
        Shape old = g2.getClip();
        g2.setClip(new Ellipse2D.Float(x, y, size, size));
        g2.drawImage(imgCache.get(path), x, y, size, size, null);
        g2.setClip(old);

        // กรอบทองชั้นใน
        g2.setColor(new Color(212, 175, 55, 230));
        g2.setStroke(new BasicStroke(4f));
        g2.drawOval(x, y, size, size);

        // กรอบทองชั้นนอก (เส้นบาง)
        g2.setColor(new Color(212, 175, 55, 100));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(x - 12, y - 12, size + 24, size + 24);
    }

    private void drawBG(Graphics2D g2, int w, int h) {
        Image bg = imgCache.get("__BG__");
        if (bg != null) {
            double s = Math.max((double)w/bg.getWidth(null), (double)h/bg.getHeight(null));
            int dw = (int)(bg.getWidth(null)*s), dh = (int)(bg.getHeight(null)*s);
            g2.drawImage(bg, (w-dw)/2, (h-dh)/2, dw, dh, null);
        } else {
            g2.setColor(new Color(12, 16, 28));
            g2.fillRect(0, 0, w, h);
        }
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRect(0, 0, w, h);
    }

    private void drawBubbles(Graphics2D g2, int panelW) {
        int y = 0;
        for (int i = 0; i < shown.size(); i++) {
            ChatBubble b = shown.get(i);
            boolean isLast = (i == shown.size() - 1);
            float alpha = isLast ? animAlpha   : 1f;
            int   offY  = isLast ? (int)animOffsetY : 0;
            int   bh    = bubbleH(b, panelW, g2);
            drawOneBubble(g2, b, panelW, y + offY, bh, alpha);
            y += bh + SPACING;
        }
    }

    private void drawOneBubble(Graphics2D g2, ChatBubble b, int panelW, int y, int bh, float alpha) {
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        String text = b.text.replace("[PLAYER]", GameConstants.PLAYER_NAME);

        if (b.isNarration) {
            g2.setFont(new Font("Tahoma", Font.ITALIC, 16));
            g2.setColor(new Color(200, 200, 200, 190));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, (panelW - fm.stringWidth(text))/2, y + bh/2 + fm.getAscent()/2);
            g2.setComposite(AlphaComposite.SrcOver);
            return;
        }

        FontMetrics fm = g2.getFontMetrics(new Font("Tahoma", Font.PLAIN, 18));
        int maxTW = Math.min(MAX_BW, panelW - AVATAR_SIZE - SIDE_MARGIN*2 - 16) - PAD*2;
        List<String> lines = wrap(text, fm, maxTW);
        int nameH = (!b.isPlayer && !b.speaker.isEmpty()) ? 22 : 0;
        int actualBW = Math.min(MAX_BW, Math.max(120, getMaxW(lines, fm) + PAD*2 + 16));

        if (b.isPlayer) {
            int avatarX = panelW - SIDE_MARGIN - AVATAR_SIZE;
            int bubbleX = avatarX - 10 - actualBW;
            int avatarY = y + (bh - AVATAR_SIZE)/2;

            drawBox(g2, bubbleX, y, actualBW, bh, true);
            g2.setFont(new Font("Tahoma", Font.PLAIN, 18));
            g2.setColor(Color.WHITE);
            int ty = y + PAD + fm.getAscent();
            for (String l : lines) { g2.drawString(l, bubbleX + PAD, ty); ty += fm.getHeight(); }
            drawAvatar(g2, b.avatarPath, avatarX, avatarY);

        } else {
            int avatarX = SIDE_MARGIN;
            int bubbleX = avatarX + AVATAR_SIZE + 10;
            int avatarY = y + (bh - AVATAR_SIZE)/2;

            drawBox(g2, bubbleX, y, actualBW, bh, false);
            g2.setFont(new Font("Tahoma", Font.BOLD, 14));
            g2.setColor(new Color(100, 210, 255));
            g2.drawString(b.speaker.toUpperCase(), bubbleX + PAD, y + PAD + 14);
            g2.setFont(new Font("Tahoma", Font.PLAIN, 18));
            g2.setColor(Color.WHITE);
            int ty = y + PAD + nameH + fm.getAscent();
            for (String l : lines) { g2.drawString(l, bubbleX + PAD, ty); ty += fm.getHeight(); }
            drawAvatar(g2, b.avatarPath, avatarX, avatarY);
        }

        g2.setComposite(AlphaComposite.SrcOver);
    }

    private void drawBox(Graphics2D g2, int x, int y, int w, int h, boolean isPlayer) {
        g2.setColor(isPlayer ? new Color(30,55,90,220) : new Color(15,22,40,220));
        g2.fillRoundRect(x, y, w, h, 18, 18);
        g2.setColor(new Color(212,175,55,150));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x, y, w, h, 18, 18);
    }

    private void drawAvatar(Graphics2D g2, String path, int x, int y) {
        g2.setColor(new Color(20,28,48,210));
        g2.fillOval(x, y, AVATAR_SIZE, AVATAR_SIZE);
        if (path != null && imgCache.containsKey(path)) {
            Shape old = g2.getClip();
            g2.setClip(new Ellipse2D.Float(x, y, AVATAR_SIZE, AVATAR_SIZE));
            g2.drawImage(imgCache.get(path), x, y, AVATAR_SIZE, AVATAR_SIZE, null);
            g2.setClip(old);
        }
        g2.setColor(new Color(212,175,55,230));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawOval(x, y, AVATAR_SIZE, AVATAR_SIZE);
    }

    private void drawHint(Graphics2D g2, int w, int h) {
        if (currentIndex >= allBubbles.size()) return;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        g2.setFont(new Font("Tahoma", Font.PLAIN, 15));
        g2.setColor(new Color(220,220,220));
        String hint = "แตะเพื่อดำเนินการ";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(hint, (w - fm.stringWidth(hint))/2, h - 18);
        g2.setComposite(AlphaComposite.SrcOver);
    }

    private int bubbleH(ChatBubble b, int panelW, Graphics2D g2) {
        if (b.isNarration) return 36;
        String text = b.text.replace("[PLAYER]", GameConstants.PLAYER_NAME);
        FontMetrics fm = g2.getFontMetrics(new Font("Tahoma", Font.PLAIN, 18));
        int maxTW = Math.min(MAX_BW, panelW - AVATAR_SIZE - SIDE_MARGIN*2 - 16) - PAD*2;
        List<String> lines = wrap(text, fm, maxTW);
        int nameH = (!b.isPlayer && !b.speaker.isEmpty()) ? 22 : 0;
        return Math.max(AVATAR_SIZE, nameH + lines.size()*fm.getHeight() + PAD*2);
    }

    private List<String> wrap(String text, FontMetrics fm, int maxW) {
        List<String> res = new ArrayList<>();
        if (text == null || text.isEmpty()) { res.add(""); return res; }
        StringBuilder line = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (fm.stringWidth(line + String.valueOf(c)) > maxW && line.length() > 0) {
                res.add(line.toString()); line = new StringBuilder();
            }
            line.append(c);
        }
        if (line.length() > 0) res.add(line.toString());
        return res;
    }

    private int getMaxW(List<String> lines, FontMetrics fm) {
        int max = 0;
        for (String l : lines) max = Math.max(max, fm.stringWidth(l));
        return max;
    }

    private void loadImg(String path, String key) {
        if (path == null || path.isEmpty() || path.equalsIgnoreCase("none")) return;
        try {
            java.net.URL url = getClass().getClassLoader().getResource(path);
            if (url != null) imgCache.put(key, new ImageIcon(url).getImage());
        } catch (Exception ignored) {}
    }
}