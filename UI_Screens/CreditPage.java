package UI_Screens;

import core.Main;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * CreditPage — หน้า "เกี่ยวกับผู้สร้าง" (เข้าจากเมนูหลัก)
 * ธีม: dark cinematic + gold ให้เข้ากับ DialogueBox
 */
public class CreditPage extends JPanel {

    // ── Color palette (เดียวกับ DialogueBox / EndCredits) ────────────
    private static final Color BG          = new Color(10,  7,   5);
    private static final Color GOLD        = new Color(201, 168, 76);
    private static final Color GOLD_DIM    = new Color(201, 168, 76, 110);
    private static final Color GOLD_BORDER = new Color(201, 168, 76, 85);
    private static final Color CREAM       = new Color(245, 238, 216);
    private static final Color CREAM_DIM   = new Color(185, 178, 158);
    private static final Color CARD_BG     = new Color(18,  12,  8,  235);

    // ── สมาชิกในทีม ──────────────────────────────────────────────────
    private static final String[][] MEMBERS = {
        { "Project Manager",          "นางสาวปริยากร ธารพรศรี",         "68543210078-0" },
        { "Scenario Writer",          "นางสาวนภัสประภา กุลสุทธิเสถียร", "68543210029-3" },
        { "UI Designer",              "นายนรินทร์ ไสวจำเนียรกูล",       "68543210030-1" },
        { "Asset & Resource Manager", "นางสาวกมลวรรณ เรณู",             "68543210060-8" },
        { "Tester",                   "นายกฤตภาส มงคลคลี",              "68543210061-6" },
        { "Lead Developer",           "นายนิรันดร์รักษ์ อนุสนธิ์",      "68543210075-6" },
    };

    // ── Sparks ────────────────────────────────────────────────────────
    private final ArrayList<EndCredits.Spark> sparks = new ArrayList<>();
    private final Random rng = new Random();
    private Timer sparkTimer;

    // ─────────────────────────────────────────────────────────────────
    public CreditPage(Font tFont, Font bFont) {
        setLayout(new BorderLayout());
        setBackground(BG);
        for (int i = 0; i < 36; i++) sparks.add(new EndCredits.Spark(rng));

        // bg layer (sparks + gradient)
        JPanel bg = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w=getWidth(), h=getHeight();
                // dark gradient
                g2.setPaint(new GradientPaint(0,0,new Color(15,10,7),0,h,BG));
                g2.fillRect(0,0,w,h);
                // crimson top
                g2.setPaint(new RadialGradientPaint(new Point2D.Float(w/2f,0), w*0.55f,
                    new float[]{0f,1f}, new Color[]{new Color(120,20,20,22), new Color(0,0,0,0)}));
                g2.fillRect(0,0,w,h);
                // gold bottom
                g2.setPaint(new RadialGradientPaint(new Point2D.Float(w/2f,h), w*0.45f,
                    new float[]{0f,1f}, new Color[]{new Color(201,168,76,10), new Color(0,0,0,0)}));
                g2.fillRect(0,0,w,h);
                // sparks
                for (EndCredits.Spark s : sparks) s.draw(g2, w, h);
                // vignette
                g2.setPaint(new RadialGradientPaint(new Point2D.Float(w/2f,h/2f),
                    Math.max(w,h)*0.72f, new float[]{0.3f,1f},
                    new Color[]{new Color(0,0,0,0), new Color(0,0,0,155)}));
                g2.fillRect(0,0,w,h);
                g2.dispose();
            }
        };
        bg.setOpaque(false);

        bg.add(buildCard());
        add(bg, BorderLayout.CENTER);

        // spark animation
        sparkTimer = new Timer(16, e -> {
            for (EndCredits.Spark s : sparks) s.update();
            bg.repaint();
        });
        addComponentListener(new ComponentAdapter() {
            @Override public void componentShown (ComponentEvent e) { sparkTimer.start(); }
            @Override public void componentHidden(ComponentEvent e) { sparkTimer.stop();  }
        });
    }

    // ── Main card ─────────────────────────────────────────────────────
    private JPanel buildCard() {
        boolean hasThai = isFontAvail("TH Sarabun New");

        JPanel card = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w=getWidth(), h=getHeight();
                // shadow
                g2.setColor(new Color(0,0,0,90));
                g2.fill(new RoundRectangle2D.Float(5,7,w-5,h-5,22,22));
                // body
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0,0,w-4,h-5,20,20));
                // gold border
                g2.setColor(GOLD_BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1,1,w-6,h-7,18,18));
                // top shimmer line
                g2.setPaint(new GradientPaint(40,0,new Color(201,168,76,0),w/2f,0,new Color(201,168,76,90)));
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(40,1,w/2,1);
                g2.setPaint(new GradientPaint(w/2f,0,new Color(201,168,76,90),w-40f,0,new Color(201,168,76,0)));
                g2.drawLine(w/2,1,w-40,1);
                g2.dispose();
            }
        };
        card.setOpaque(false);

        int cw = 740, ch = 590;
        card.setPreferredSize(new Dimension(cw, ch));

        // ── Header ──
        JLabel ornament = lbl("✦  HeartToHeart  ✦", new Font("Tahoma",Font.BOLD,17),
            new Color(201,168,76,195), SwingConstants.CENTER);
        JLabel title = lbl("ทีมผู้สร้าง", new Font("Tahoma",Font.BOLD,32),
            CREAM, SwingConstants.CENTER);
        JLabel subtitle = lbl("CS Project  •  2025",
            new Font("Tahoma",Font.PLAIN,12), GOLD_DIM, SwingConstants.CENTER);

        ornament.setBounds(0, 22, cw, 26);
        title   .setBounds(0, 52, cw, 44);
        subtitle.setBounds(0, 96, cw, 20);
        card.add(ornament); card.add(title); card.add(subtitle);

        // divider
        card.add(hRule(60, 118, cw-120, 1));

        // ── Member grid (2 columns) ──
        Font roleF = new Font("Tahoma", Font.PLAIN, 11);
        Font nameF = hasThai ? new Font("TH Sarabun New", Font.BOLD,  21)
                             : new Font("Tahoma",         Font.BOLD,  16);
        Font idF   = new Font("Tahoma", Font.PLAIN, 11);

        int rowH  = 76;
        int colW  = (cw - 80) / 2;
        int startY= 132;

        for (int i = 0; i < MEMBERS.length; i++) {
            int col = i % 2, row = i / 2;
            int x = 36 + col*(colW+16);
            int y = startY + row*rowH;
            JPanel tile = memberTile(MEMBERS[i], roleF, nameF, idF, colW, 66);
            tile.setBounds(x, y, colW, 66);
            card.add(tile);
        }

        // ── Lower divider ──
        int lowerY = startY + ((MEMBERS.length+1)/2)*rowH + 4;
        card.add(hRule(60, lowerY, cw-120, 1));

        // ── Info row ──
        JLabel infoLbl = lbl("สร้างด้วย Java Swing  •  Version 1.0",
            new Font("Tahoma",Font.PLAIN,13), new Color(185,178,158,155), SwingConstants.CENTER);
        infoLbl.setBounds(0, lowerY+10, cw, 22);
        card.add(infoLbl);

        // ── Quote ──
        JLabel quote = lbl("\u201c ขอบคุณที่ร่วมเป็นส่วนหนึ่งของเรื่องราวนี้ \u201d",
            new Font("Tahoma",Font.ITALIC,14), new Color(201,168,76,120), SwingConstants.CENTER);
        quote.setBounds(0, lowerY+38, cw, 22);
        card.add(quote);

        // ── Buttons ──
        JButton backBtn = goldBtn("← ย้อนกลับ", false);
        backBtn.setBounds((cw - 200) / 2, lowerY+74, 200, 44);
        backBtn.addActionListener(e -> Main.cardLayout.show(Main.mainContainer, "MENU"));
        card.add(backBtn);

        // hint
        JLabel hint = lbl("ESC — กลับเมนูหลัก",
            new Font("Tahoma",Font.PLAIN,11), new Color(201,168,76,45), SwingConstants.RIGHT);
        hint.setBounds(0, ch-30, cw-18, 18);
        card.add(hint);

        // ── fix card height dynamically ──
        ch = lowerY + 140;
        card.setPreferredSize(new Dimension(cw, ch));

        return card;
    }

    // ── Member tile ───────────────────────────────────────────────────
    private JPanel memberTile(String[] data, Font roleF, Font nameF, Font idF, int w, int h) {
        JPanel p = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(201,168,76,14));
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),8,8));
                g2.setColor(new Color(201,168,76,50));
                g2.setStroke(new BasicStroke(0.8f));
                g2.draw(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,8,8));
                g2.dispose();
            }
        };
        p.setOpaque(false);

        JLabel rl = lbl(data[0].toUpperCase(), roleF, new Color(201,168,76,148), SwingConstants.CENTER);
        JLabel nl = lbl(data[1], nameF, CREAM, SwingConstants.CENTER);
        JLabel il = lbl(data[2], idF,   new Color(201,168,76,95),  SwingConstants.CENTER);

        rl.setBounds(0,  4, w, 15);
        nl.setBounds(0, 22, w, 28);
        il.setBounds(0, 50, w, 14);

        p.add(rl); p.add(nl); p.add(il);
        return p;
    }

    // ── Helpers ───────────────────────────────────────────────────────
    /** เพิ่ม component แบบ absolute bounds (สำหรับ add แบบสั้น) */
    private void addAt(JPanel parent, JComponent c, int x, int y, int w, int h) {
        c.setBounds(x,y,w,h); parent.add(c);
    }

    /** shortcut add with bounds */
    private JPanel add(JPanel parent, JPanel child, int x, int y, int w, int h) {
        child.setBounds(x,y,w,h); parent.add(child); return child;
    }

    private JLabel lbl(String text, Font font, Color color, int align) {
        JLabel l = new JLabel(text, align);
        l.setFont(font); l.setForeground(color); l.setOpaque(false);
        return l;
    }

    private JPanel hRule(int x, int y, int w, int h) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setPaint(new GradientPaint(0,0,new Color(201,168,76,0),getWidth()/2f,0,new Color(201,168,76,60)));
                g2.fillRect(0,0,getWidth()/2,1);
                g2.setPaint(new GradientPaint(getWidth()/2f,0,new Color(201,168,76,60),getWidth(),0,new Color(201,168,76,0)));
                g2.fillRect(getWidth()/2,0,getWidth()/2,1);
                g2.dispose();
            }
        };
        p.setOpaque(false); p.setBounds(x,y,w,h); return p;
    }

    private JButton goldBtn(String text, boolean filled) {
        JButton b = new JButton(text) {
            boolean hov=false;
            { addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){hov=true; repaint();}
                public void mouseExited (MouseEvent e){hov=false;repaint();}
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color gc = hov ? new Color(232,201,122) : GOLD;
                if (filled) {
                    g2.setColor(gc);
                    g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),10,10));
                    g2.setColor(new Color(10,7,5));
                } else {
                    g2.setColor(new Color(0,0,0,0));
                    g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),10,10));
                    if (hov){ g2.setColor(new Color(201,168,76,20)); g2.fill(new RoundRectangle2D.Float(2,2,getWidth()-4,getHeight()-4,8,8)); }
                    g2.setColor(gc);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new RoundRectangle2D.Float(1,1,getWidth()-3,getHeight()-3,8,8));
                }
                g2.setFont(getFont());
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        b.setFont(new Font("Tahoma",Font.BOLD,15));
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private boolean isFontAvail(String name) {
        for (String f : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames())
            if (f.equalsIgnoreCase(name)) return true;
        return false;
    }
}