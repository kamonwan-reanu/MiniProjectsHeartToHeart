package UI_Components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ChoiceButton — ธีมเดียวกับ DialogueBox (ดำ + ขอบทอง + ข้อความขาว)
 * รองรับ text wrap อัตโนมัติ + scroll ถ้าข้อความยาวเกิน
 */
public class ChoiceButton extends JPanel {

    // ── ธีม (ตรงกับ DialogueBox) ──
    private static final Color BG_NORMAL   = new Color(15,  20,  35, 220);
    private static final Color BG_HOVER    = new Color(25,  35,  60, 235);
    private static final Color BG_PRESSED  = new Color(10,  15,  28, 245);
    private static final Color BG_DISABLED = new Color(15,  20,  35, 140);
    private static final Color BORDER_GOLD = new Color(212, 175,  55, 180);  // ทอง — เหมือน DialogueBox
    private static final Color BORDER_HOV  = new Color(212, 175,  55, 255);  // ทองสว่างตอน hover
    private static final Color NUM_COLOR   = new Color(100, 210, 255);        // cyan — เหมือน nameLabel
    private static final Color TEXT_COLOR  = new Color(245, 245, 245);
    private static final Color TEXT_DIS    = new Color(110, 110, 130);

    private static final int PAD_X  = 14;
    private static final int PAD_Y  = 8;
    private static final int NUM_W  = 32;
    private static final int SCROLL_W = 6;

    private final String   num;
    private final String   text;
    private final Runnable onClick;

    private boolean hovered  = false;
    private boolean pressed  = false;

    // scroll state
    private int scrollY   = 0;
    private int contentH  = 0;
    private boolean dragging    = false;
    private int  dragStartY     = 0;
    private int  dragStartScroll= 0;

    public ChoiceButton(String num, String text, Runnable onClick) {
        this.num    = num  != null ? num  : "";
        this.text   = text != null ? text : "";
        this.onClick = onClick;
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFocusable(true);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e)  { hovered = true;  repaint(); }
            @Override public void mouseExited(MouseEvent e)   { hovered = false; pressed = false; repaint(); }
            @Override public void mousePressed(MouseEvent e)  {
                if (!isEnabled()) return;
                pressed = true;
                if (needsScroll() && e.getX() >= getWidth() - SCROLL_W - 6) {
                    dragging = true; dragStartY = e.getY(); dragStartScroll = scrollY;
                }
                repaint();
            }
            @Override public void mouseReleased(MouseEvent e) {
                pressed = false;
                if (dragging) { dragging = false; repaint(); return; }
                repaint();
                if (!isEnabled()) return;
                if (contains(e.getPoint()) && onClick != null) onClick.run();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (!dragging) return;
                int maxScroll = Math.max(0, contentH - innerH());
                int trackH = getHeight() - PAD_Y * 2;
                if (trackH > 0 && contentH > 0) {
                    int delta = (int)((double)(e.getY() - dragStartY) / trackH * contentH);
                    scrollY = Math.max(0, Math.min(maxScroll, dragStartScroll + delta));
                }
                repaint();
            }
        });

        addMouseWheelListener(e -> {
            if (!needsScroll()) return;
            int maxScroll = Math.max(0, contentH - innerH());
            scrollY = Math.max(0, Math.min(maxScroll, scrollY + (int)(e.getPreciseWheelRotation() * 16)));
            repaint();
        });

        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (!isEnabled()) return;
                int k = e.getKeyCode();
                if (k == KeyEvent.VK_ENTER || k == KeyEvent.VK_SPACE) { if (onClick != null) onClick.run(); }
                if (k == KeyEvent.VK_UP)   doScroll(-18);
                if (k == KeyEvent.VK_DOWN) doScroll(+18);
            }
        });
    }

    private void doScroll(int d) {
        scrollY = Math.max(0, Math.min(Math.max(0, contentH - innerH()), scrollY + d));
        repaint();
    }
    private boolean needsScroll() { return contentH > innerH() + 2; }
    private int innerH() { return Math.max(1, getHeight() - PAD_Y * 2); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth(), h = getHeight();
        if (w <= 4 || h <= 4) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        // ── พื้นหลัง ──
        Color bg = !isEnabled() ? BG_DISABLED : pressed ? BG_PRESSED : hovered ? BG_HOVER : BG_NORMAL;
        g2.setColor(bg);
        g2.fillRect(0, 0, w, h);

        // ── ขอบทองเหมือน DialogueBox ──
        Color bc = (hovered && isEnabled()) ? BORDER_HOV : BORDER_GOLD;
        g2.setColor(bc);
        g2.setStroke(new BasicStroke(hovered ? 2f : 1.5f));
        g2.drawRect(2, 2, w - 4, h - 4);

        // เส้นสว่างบน (เหมือน top highlight ของ DialogueBox)
        if (isEnabled() && hovered) {
            g2.setColor(new Color(212, 175, 55, 60));
            g2.fillRect(3, 3, w - 6, h - 6);
        }

        // ── เลขตัวเลือก (cyan เหมือน nameLabel) ──
        int fontSize = Math.max(11, Math.min(17, h / 3));
        Font numFont  = new Font("Tahoma", Font.BOLD,  fontSize);
        Font textFont = new Font("Tahoma", Font.PLAIN, fontSize);

        g2.setFont(numFont);
        FontMetrics numFm = g2.getFontMetrics();
        g2.setColor(isEnabled() ? NUM_COLOR : TEXT_DIS);
        String numStr = num + ".";
        int numY = h / 2 + numFm.getAscent() / 2 - 2;
        g2.drawString(numStr, PAD_X, numY);

        // ── Text area ──
        int textX = PAD_X + NUM_W;
        int scrollBarW = needsScroll() ? SCROLL_W + 4 : 0;
        int textW = w - textX - PAD_X - scrollBarW;
        if (textW < 10) { g2.dispose(); return; }

        // clip
        Shape clip = g2.getClip();
        g2.setClip(new Rectangle(textX, PAD_Y, textW, h - PAD_Y * 2));

        g2.setFont(textFont);
        FontMetrics fm = g2.getFontMetrics();
        List<String> lines = wrapText(text, fm, textW);

        int lineH  = fm.getHeight();
        contentH   = lines.size() * lineH;
        int iH     = innerH();

        // clamp scroll
        scrollY = Math.max(0, Math.min(scrollY, Math.max(0, contentH - iH)));

        int startY = contentH <= iH
            ? PAD_Y + (iH - contentH) / 2 + fm.getAscent()   // center ถ้าพอดี
            : PAD_Y + fm.getAscent() - scrollY;               // scroll mode

        g2.setColor(isEnabled() ? TEXT_COLOR : TEXT_DIS);
        for (String line : lines) {
            int ly = startY; startY += lineH;
            if (ly + fm.getDescent() < PAD_Y) continue;
            if (ly - fm.getAscent() > h - PAD_Y) break;
            g2.drawString(line, textX, ly);
        }

        g2.setClip(clip);

        // ── Scrollbar ──
        if (needsScroll()) {
            int tx = w - SCROLL_W - 2, trackH = h - PAD_Y * 2;
            g2.setColor(new Color(212, 175, 55, 40));
            g2.fillRect(tx, PAD_Y, SCROLL_W, trackH);

            int thumbH = Math.max(12, (int)((double)iH / contentH * trackH));
            int thumbY = PAD_Y + (int)((double)scrollY / Math.max(1, contentH - iH) * (trackH - thumbH));
            g2.setColor(new Color(212, 175, 55, hovered ? 200 : 130));
            g2.fillRect(tx, thumbY, SCROLL_W, thumbH);
        }

        g2.dispose();
    }

    // ── Word wrap ──
    private List<String> wrapText(String t, FontMetrics fm, int maxW) {
        List<String> out = new ArrayList<>();
        if (t == null || t.isEmpty()) { out.add(""); return out; }
        for (String para : t.split("\n", -1)) {
            if (fm.stringWidth(para) <= maxW) { out.add(para); continue; }
            StringBuilder cur = new StringBuilder();
            for (String word : para.split(" ")) {
                String test = cur.length() == 0 ? word : cur + " " + word;
                if (fm.stringWidth(test) <= maxW) {
                    cur = new StringBuilder(test);
                } else {
                    if (cur.length() > 0) { out.add(cur.toString()); cur = new StringBuilder(word); }
                    else { out.addAll(breakWord(word, fm, maxW)); }
                }
            }
            if (cur.length() > 0) out.add(cur.toString());
        }
        return out;
    }

    private List<String> breakWord(String word, FontMetrics fm, int maxW) {
        List<String> r = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (char c : word.toCharArray()) {
            if (fm.stringWidth(sb + "" + c) > maxW && sb.length() > 0) {
                r.add(sb.toString()); sb = new StringBuilder();
            }
            sb.append(c);
        }
        if (sb.length() > 0) r.add(sb.toString());
        return r;
    }

    @Override public void setEnabled(boolean e) {
        super.setEnabled(e);
        setCursor(e ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        repaint();
    }

    public void highlight() {
        hovered = true; repaint();
        new Timer(800, e -> { ((Timer)e.getSource()).stop(); hovered = false; repaint(); }).start();
    }
}