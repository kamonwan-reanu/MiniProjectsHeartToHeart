package UI_Components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class PauseMenuUI extends JPanel {

    private static PauseMenuUI instance;
    public static PauseMenuUI getInstance() {
        if (instance == null) instance = new PauseMenuUI();
        return instance;
    }

    private Container parentContainer;
    private boolean   menuVisible    = false;
    private boolean   isSinglePlayer = true;

    private JButton resumeBtn, saveBtn, mainMenuBtn, exitBtn;
    private JPanel  menuPanel;

    private static final Color BG_CARD  = new Color(15,  20, 35, 240);
    private static final Color GOLD     = new Color(212, 175, 55, 200);
    private static final Color PINK     = new Color(255, 105, 180);
    private static final Color PINK_HOV = new Color(255, 140, 200);
    private static final Color RED      = new Color(220, 53,  69);
    private static final Color RED_HOV  = new Color(240, 73,  89);
    private static final Color WHITE    = Color.WHITE;

    private PauseMenuUI() {
        setLayout(null);
        setOpaque(false);
        setVisible(false);

        // Block mouse ไม่ให้ลอดไปด้านหลัง
        MouseAdapter blocker = new MouseAdapter() {
            @Override public void mouseClicked (MouseEvent e) { e.consume(); }
            @Override public void mousePressed (MouseEvent e) { e.consume(); }
            @Override public void mouseReleased(MouseEvent e) { e.consume(); }
        };
        addMouseListener(blocker);

        buildMenuPanel();
        buildButtons();

        setFocusable(false); // PauseMenuUI ไม่จัดการ ESC เอง — scene จัดการผ่าน InputMap
    }

    private void buildMenuPanel() {
        menuPanel = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(new Color(0,0,0,80));
                g2.fill(new RoundRectangle2D.Float(5,5,w-3,h-3,22,22));
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0,0,w-5,h-5,20,20));
                g2.setColor(GOLD); g2.setStroke(new BasicStroke(2.5f));
                g2.draw(new RoundRectangle2D.Float(1,1,w-7,h-7,20,20));
                g2.setColor(WHITE);
                g2.setFont(new Font("Tahoma", Font.BOLD, 26));
                FontMetrics fm = g2.getFontMetrics();
                String t = "PAUSE";
                g2.drawString(t, (w - fm.stringWidth(t)) / 2, 52);
                g2.setColor(GOLD); g2.setStroke(new BasicStroke(1f));
                g2.drawLine(30, 65, w-35, 65);
                g2.dispose();
            }
        };
        menuPanel.setOpaque(false);
        add(menuPanel);
    }

    private void buildButtons() {
        Font f = new Font("Tahoma", Font.BOLD, 17);

        resumeBtn   = styledBtn("RESUME",    f, PINK, PINK_HOV);
        saveBtn     = styledBtn("SAVE GAME", f, PINK, PINK_HOV);
        mainMenuBtn = styledBtn("MAIN MENU", f, PINK, PINK_HOV);
        exitBtn     = styledBtn("EXIT",      f, RED,  RED_HOV);

        resumeBtn.addActionListener(e -> hideMenu());

        saveBtn.addActionListener(e -> {
            // ไม่ hideMenu ก่อน — ให้ SaveSystemUI ซ้อนทับ PauseMenu
            // เมื่อกด ย้อนกลับ ใน SaveSystemUI จะ showMenu อีกครั้ง
            SaveSystemUI save = SaveSystemUI.getInstance();
            save.setOnBack(() -> showMenu(parentContainer, isSinglePlayer));
            save.showSave(null, null);
        });

        mainMenuBtn.addActionListener(e -> {
            hideMenu();
            core.Main.cardLayout.show(core.Main.mainContainer, "MENU");
        });

        exitBtn.addActionListener(e -> System.exit(0));

        menuPanel.add(resumeBtn);
        menuPanel.add(saveBtn);
        menuPanel.add(mainMenuBtn);
        menuPanel.add(exitBtn);
    }

    // ✅ showMenu(parent, singlePlayer) — ตัวหลัก
    public void showMenu(Container parent, boolean singlePlayer) {
        if (parent == null) return;
        this.parentContainer = parent;
        this.isSinglePlayer  = singlePlayer;
        saveBtn.setVisible(isSinglePlayer); // ✅ MP ไม่มีปุ่ม Save

        layoutChildren(parent.getWidth(), parent.getHeight());

        boolean already = false;
        for (Component c : parent.getComponents())
            if (c == this) { already = true; break; }
        if (!already) parent.add(this);

        parent.setComponentZOrder(this, 0);
        setVisible(true);
        menuVisible = true;
        parent.revalidate();
        parent.repaint();
    }

    // ✅ showMenu(parent) — default SP=true
    public void showMenu(Container parent) { showMenu(parent, true); }

    public void hideMenu() {
        if (!menuVisible) return;
        setVisible(false);
        menuVisible = false;
        if (parentContainer != null) {
            parentContainer.revalidate();
            parentContainer.repaint();
        }
    }

    public boolean isMenuVisible() { return menuVisible; }

    private void layoutChildren(int pw, int ph) {
        setBounds(0, 0, pw, ph);

        int bw = 260, bh = 48, gap = 14;
        int numBtns = isSinglePlayer ? 4 : 3; // MP ไม่มี Save
        int titleH  = 80, padding = 24;
        int mw      = 340;
        int mh      = titleH + numBtns * bh + (numBtns - 1) * gap + padding * 2;

        menuPanel.setBounds((pw - mw) / 2, (ph - mh) / 2, mw, mh);

        int bx   = (mw - bw) / 2;
        int curY = titleH + padding;

        resumeBtn.setBounds(bx, curY, bw, bh); curY += bh + gap;
        if (isSinglePlayer) {
            saveBtn.setBounds(bx, curY, bw, bh); curY += bh + gap;
        }
        mainMenuBtn.setBounds(bx, curY, bw, bh); curY += bh + gap;
        exitBtn    .setBounds(bx, curY, bw, bh);
    }

    @Override protected void paintComponent(Graphics g) {
        if (menuVisible) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0,0,0,160));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    @Override public boolean contains(int x, int y) {
        return menuVisible;
    }

    private JButton styledBtn(String text, Font font, Color normal, Color hover) {
        JButton btn = new JButton(text) {
            private Color cur = normal;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { cur = hover;  repaint(); }
                    @Override public void mouseExited (MouseEvent e) { cur = normal; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(new Color(0,0,0,40));
                g2.fill(new RoundRectangle2D.Float(2,3,w-2,h-1,10,10));
                g2.setColor(cur);
                g2.fill(new RoundRectangle2D.Float(0,0,w-2,h-2,10,10));
                g2.setColor(new Color(255,255,255,80));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1,1,w-4,h-4,10,10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(font); btn.setForeground(WHITE);
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}