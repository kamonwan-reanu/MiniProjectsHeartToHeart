package UI_Screens;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * EndCredits — cinematic scrolling credits (JPanel สำหรับ CardLayout)
 * ใช้งาน:
 *   mainContainer.add(new EndCredits(() -> cardLayout.show(mainContainer,"MENU")), "ENDING");
 */
public class EndCredits extends JPanel {

    // ── Colors (ธีม dark gold เดียวกับ DialogueBox) ──────────────────
    private static final Color BG_DARK     = new Color(10,  7,   5);
    private static final Color GOLD        = new Color(201, 168, 76);
    private static final Color GOLD_LIGHT  = new Color(232, 201, 122);
    private static final Color CREAM       = new Color(245, 238, 216);
    private static final Color CRIMSON_GLOW= new Color(139, 26,  26, 28);

    // ── รายชื่อทีม ────────────────────────────────────────────────────
    private static final String[][] CREDITS = {
        { "Project Manager",          "นางสาวปริยากร ธารพรศรี",         "Pariyakon Thanponsri",        "68543210078-0" },
        { "Scenario Writer",          "นางสาวนภัสประภา กุลสุทธิเสถียร", "Napatprapa Kulsuttisatien",   "68543210029-3" },
        { "UI Designer",              "นายนรินทร์ ไสวจำเนียรกูล",       "Narin Sawaijamniankul",       "68543210030-1" },
        { "Asset & Resource Manager", "นางสาวกมลวรรณ เรณู",             "Kamonwan Reanu",              "68543210060-8" },
        { "Tester",                   "นายกฤตภาส มงคลคลี",              "Krittaphat Mongkolklee",      "68543210061-6" },
        { "Lead Developer",           "นายนิรันดร์รักษ์ อนุสนธิ์",      "Niranrak Anuson",             "68543210075-6" },
    };

    private static final String[] BEHIND_LINES = {
        "จากความประทับใจในบทบาทชายหนุ่มผู้ทรงอิทธิพลจากจอแก้ว",
        "สู่การรังสรรค์ตัวละครในฝันที่เราอยากให้มีชีวิตจริง",
        "ทีมงานตั้งใจถอดรหัสเสน่ห์เหล่านั้นมาไว้ในเกมนี้",
        "เพื่อให้ผู้เล่นได้สัมผัสรสชาติความรักที่เข้มข้น",
        "และน่าตื่นเต้นที่สุด"
    };

    // ── State ─────────────────────────────────────────────────────────
    private Timer  scrollTimer;
    private float  scrollY            = 0f;
    private float  totalContentHeight = 0f;

    private final ArrayList<Spark> sparks = new ArrayList<>();
    private final Random rng = new Random();

    private final Runnable onBack; // callback กลับเมนู

    // ── Constructor ───────────────────────────────────────────────────
    public EndCredits(Runnable onBack) {
        this.onBack = onBack;
        setBackground(BG_DARK);
        setFocusable(true);
        for (int i = 0; i < 42; i++) sparks.add(new Spark(rng));

        addComponentListener(new ComponentAdapter() {
            @Override public void componentShown(ComponentEvent e)  { startAnimation(); }
            @Override public void componentHidden(ComponentEvent e) { stopAnimation();  }
        });

        // ESC → กลับเมนู
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) goBack();
            }
        });

        // Click → กลับเมนู (optional)
        // addMouseListener(new MouseAdapter() { ... });
    }

    // convenience ctor ไม่มี callback
    public EndCredits() { this(null); }

    // ── Animation control ─────────────────────────────────────────────
    public void startAnimation() {
        scrollY = 0f;
        if (scrollTimer != null) scrollTimer.stop();
        scrollTimer = new Timer(16, e -> {
            scrollY += 1.8f;
            if (totalContentHeight > 0 && scrollY > totalContentHeight + getHeight() * 0.3f) {
                ((javax.swing.Timer) e.getSource()).stop();
                goBack();
            }
            for (Spark s : sparks) s.update();
            repaint();
        });
        scrollTimer.start();
        requestFocusInWindow();
    }

    public void stopAnimation() {
        if (scrollTimer != null) scrollTimer.stop();
    }

    private void goBack() {
        stopAnimation();
        if (onBack != null) onBack.run();
    }

    // ── Paint ─────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth(), h = getHeight();

        // bg gradient
        g2.setPaint(new GradientPaint(0, 0, new Color(15,10,7), 0, h, BG_DARK));
        g2.fillRect(0, 0, w, h);

        // crimson top glow
        g2.setPaint(new RadialGradientPaint(new Point2D.Float(w/2f, 0), w*0.6f,
            new float[]{0f,1f}, new Color[]{CRIMSON_GLOW, new Color(0,0,0,0)}));
        g2.fillRect(0, 0, w, h);

        // gold bottom glow
        g2.setPaint(new RadialGradientPaint(new Point2D.Float(w/2f, h), w*0.5f,
            new float[]{0f,1f}, new Color[]{new Color(201,168,76,14), new Color(0,0,0,0)}));
        g2.fillRect(0, 0, w, h);

        // sparks
        for (Spark s : sparks) s.draw(g2, w, h);

        // vertical border lines
        int lx = Math.max(60, w/14);
        float[] fracs = {0f,0.25f,0.75f,1f};
        Color[] lc = {new Color(201,168,76,0),new Color(201,168,76,32),new Color(201,168,76,32),new Color(201,168,76,0)};
        g2.setPaint(new LinearGradientPaint(0,0,0,h,fracs,lc));
        g2.setStroke(new BasicStroke(0.8f));
        g2.drawLine(lx,0,lx,h);
        g2.drawLine(w-lx,0,w-lx,h);

        // scrolling content
        drawContent(g2, w, h);

        // vignette
        g2.setPaint(new GradientPaint(0,0,new Color(0,0,0,215),0,90,new Color(0,0,0,0)));
        g2.fillRect(0,0,w,90);
        g2.setPaint(new GradientPaint(0,h-90,new Color(0,0,0,0),0,h,new Color(0,0,0,215)));
        g2.fillRect(0,h-90,w,90);
        g2.setPaint(new RadialGradientPaint(new Point2D.Float(w/2f,h/2f), Math.max(w,h)*0.75f,
            new float[]{0.38f,1f}, new Color[]{new Color(0,0,0,0),new Color(0,0,0,185)}));
        g2.fillRect(0,0,w,h);

        // ESC hint
        g2.setFont(new Font("Tahoma", Font.PLAIN, 11));
        g2.setColor(new Color(201,168,76,50));
        String hint = "ESC — กลับเมนูหลัก";
        g2.drawString(hint, 14, h-12);

        g2.dispose();
    }

    // ── Content ───────────────────────────────────────────────────────
    private void drawContent(Graphics2D g2, int w, int h) {
        int cx = w/2;
        float y = h - scrollY;
        float startY = y;

        boolean hasThai = isFontAvail("TH Sarabun New");
        Font thaiLarge = hasThai ? new Font("TH Sarabun New", Font.BOLD,   22) : new Font("Tahoma", Font.BOLD,   17);
        Font thaiSmall = hasThai ? new Font("TH Sarabun New", Font.PLAIN,  17) : new Font("Tahoma", Font.PLAIN,  14);
        Font roleFnt   = new Font("Tahoma",  Font.PLAIN,         11);
        Font engFnt    = new Font("Tahoma",  Font.BOLD,          22);
        Font idFnt     = new Font("Tahoma",  Font.PLAIN,         11);
        Font endFnt    = new Font("Serif",   Font.BOLD|Font.ITALIC, 50);

        // ── opening ──
        y += 70;
        dc(g2,"✦  ✦  ✦", cx,(int)y, new Font("Serif",Font.PLAIN,22), new Color(201,168,76,135));
        y += 48;
        dc(g2,"H e a r t T o H e a r t", cx,(int)y, new Font("Tahoma",Font.BOLD,30), new Color(232,201,122,210));
        y += 32;
        dc(g2,"T H E   T E A M   B E H I N D   T H E   S T O R Y", cx,(int)y,
            new Font("Tahoma",Font.PLAIN,11), new Color(201,168,76,90));
        y += 72;

        // ── credits ──
        for (int i = 0; i < CREDITS.length; i++) {
            String[] c = CREDITS[i];

            dc(g2, c[0].toUpperCase(), cx,(int)y, roleFnt, new Color(201,168,76,155));
            y += 26;
            dc(g2, c[1], cx,(int)y, thaiLarge, CREAM);
            y += 28;
            dc(g2, c[2], cx,(int)y, engFnt, new Color(210,200,175,200));
            y += 24;
            dc(g2, c[3], cx,(int)y, idFnt, new Color(201,168,76,100));
            y += 44;

            if (i < CREDITS.length - 1) {
                drawDivider(g2, cx,(int)y, Math.min(200,w/5));
                y += 40;
            }
        }

        // ── behind the story ──
        y += 55;
        drawRule(g2, cx,(int)y, Math.min(260,w/4));
        y += 42;
        dc(g2,"B E H I N D   T H E   S T O R Y", cx,(int)y,
            new Font("Tahoma",Font.PLAIN,11), new Color(201,168,76,115));
        y += 38;

        dc(g2,"\u201C", cx-18,(int)y, new Font("Serif",Font.PLAIN,55), new Color(201,168,76,38));
        y += 10;
        for (String line : BEHIND_LINES) {
            dc(g2, line, cx,(int)y, thaiSmall, new Color(210,200,175,185));
            y += 31;
        }
        dc(g2,"\u201D", cx+18,(int)y, new Font("Serif",Font.PLAIN,55), new Color(201,168,76,38));
        y += 38;
        drawRule(g2, cx,(int)y, Math.min(260,w/4));

        // ── The End ──
        y += 75;
        dc(g2,"— \u2726 —", cx,(int)y, new Font("Serif",Font.PLAIN,18), new Color(201,168,76,88));
        y += 52;

        FontMetrics efm = g2.getFontMetrics(endFnt);
        int etx = cx - efm.stringWidth("The End")/2;
        for (int gl=5; gl>=1; gl--) {
            g2.setColor(new Color(201,168,76, 7*gl));
            g2.setFont(endFnt);
            g2.drawString("The End", etx-gl, (int)y+gl);
            g2.drawString("The End", etx+gl, (int)y+gl);
        }
        dc(g2,"The End", cx,(int)y, endFnt, GOLD_LIGHT);

        y += 38;
        dc(g2,"T H A N K   Y O U   F O R   P L A Y I N G", cx,(int)y,
            new Font("Tahoma",Font.PLAIN,11), new Color(201,168,76,62));
        y += 90;

        totalContentHeight = y - startY;
    }

    // ── Helpers ───────────────────────────────────────────────────────
    private void dc(Graphics2D g2, String text, int cx, int y, Font font, Color color) {
        g2.setFont(font); g2.setColor(color);
        FontMetrics fm = g2.getFontMetrics(font);
        g2.drawString(text, cx - fm.stringWidth(text)/2, y);
    }

    private void drawDivider(Graphics2D g2, int cx, int y, int hw) {
        g2.setStroke(new BasicStroke(0.8f));
        g2.setPaint(new GradientPaint(cx-hw,y,new Color(201,168,76,0),cx,y,new Color(201,168,76,72)));
        g2.drawLine(cx-hw,y,cx-10,y);
        g2.setPaint(new GradientPaint(cx,y,new Color(201,168,76,72),cx+hw,y,new Color(201,168,76,0)));
        g2.drawLine(cx+10,y,cx+hw,y);
        g2.setColor(new Color(201,168,76,115));
        int ds=5; g2.fill(new Polygon(new int[]{cx,cx+ds,cx,cx-ds},new int[]{y-ds,y,y+ds,y},4));
    }

    private void drawRule(Graphics2D g2, int cx, int y, int hw) {
        g2.setStroke(new BasicStroke(0.8f));
        g2.setPaint(new GradientPaint(cx-hw,y,new Color(201,168,76,0),cx,y,new Color(201,168,76,55)));
        g2.drawLine(cx-hw,y,cx,y);
        g2.setPaint(new GradientPaint(cx,y,new Color(201,168,76,55),cx+hw,y,new Color(201,168,76,0)));
        g2.drawLine(cx,y,cx+hw,y);
    }

    private boolean isFontAvail(String name) {
        for (String f : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames())
            if (f.equalsIgnoreCase(name)) return true;
        return false;
    }

    // ── Spark ─────────────────────────────────────────────────────────
    static class Spark {
        float x, y, size, phase, speed;
        Spark(Random rng) {
            x = rng.nextFloat(); y = rng.nextFloat();
            size  = 1f + rng.nextFloat()*2.2f;
            phase = rng.nextFloat()*(float)Math.PI*2;
            speed = 0.004f + rng.nextFloat()*0.012f;
        }
        void update() { phase += speed; }
        void draw(Graphics2D g2, int w, int h) {
            float alpha = (float)(Math.sin(phase)*0.5+0.5)*0.55f;
            int a = (int)(alpha*180); if (a<=0) return;
            float px=x*w, py=y*h, s=size*(0.5f+alpha);
            g2.setColor(new Color(201,168,76,a));
            g2.fill(new Ellipse2D.Float(px-s/2,py-s/2,s,s));
            g2.setColor(new Color(201,168,76,a/5));
            float gs=s*3;
            g2.fill(new Ellipse2D.Float(px-gs/2,py-gs/2,gs,gs));
        }
    }
}