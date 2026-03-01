package UI_Components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class PauseMenuUI extends JPanel {

    private static PauseMenuUI instance;

    private Container parentContainer;
    private boolean   menuVisible = false;

    private JButton resumeBtn, saveBtn, mainMenuBtn, exitBtn;
    private JPanel  menuPanel;

    // ── Palette ──────────────────────────────────────────────
    private static final Color BG_CARD  = new Color(15,  20, 35, 245);
    private static final Color GOLD     = new Color(212, 175, 55, 220);
    private static final Color PINK     = new Color(255, 105, 180);
    private static final Color PINK_HOV = new Color(255, 150, 210);
    private static final Color RED      = new Color(220, 53,  69);
    private static final Color RED_HOV  = new Color(240, 73,  89);
    private static final Color WHITE    = Color.WHITE;

    private PauseMenuUI() {
        setLayout(null);
        setOpaque(false);
        setVisible(false);
        setFocusable(true);
        buildMenuPanel();
        buildButtons();

        // ✅ ESC ปิด pause menu
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) hideMenu();
            }
        });
    }

    public static PauseMenuUI getInstance() {
        if (instance == null) instance = new PauseMenuUI();
        return instance;
    }

    // ════════════════════════════════════════════════════════
    //  Menu panel
    // ════════════════════════════════════════════════════════
    private void buildMenuPanel() {
        menuPanel = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                // shadow
                g2.setColor(new Color(0,0,0,90));
                g2.fill(new RoundRectangle2D.Float(6,6,w-4,h-4,24,24));
                // body
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0,0,w-6,h-6,22,22));
                // gold border
                g2.setColor(GOLD);
                g2.setStroke(new BasicStroke(2.5f));
                g2.draw(new RoundRectangle2D.Float(1,1,w-8,h-8,22,22));
                // title
                g2.setColor(WHITE);
                g2.setFont(new Font("Tahoma", Font.BOLD, 30));
                FontMetrics fm = g2.getFontMetrics();
                String t = "PAUSE";
                g2.drawString(t, (w - fm.stringWidth(t)) / 2, 62);
                // divider
                g2.setColor(GOLD);
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(35, 78, w-40, 78);
                g2.dispose();
            }
        };
        menuPanel.setOpaque(false);
        add(menuPanel);
    }

    // ════════════════════════════════════════════════════════
    //  Buttons
    // ════════════════════════════════════════════════════════
    private void buildButtons() {
        Font f = new Font("Tahoma", Font.BOLD, 18);

        resumeBtn   = styledBtn("RESUME",    f, PINK,    PINK_HOV);
        saveBtn     = styledBtn("SAVE GAME", f, PINK,    PINK_HOV);
        mainMenuBtn = styledBtn("MAIN MENU", f, PINK,    PINK_HOV);
        exitBtn     = styledBtn("EXIT",      f, RED,     RED_HOV);

        resumeBtn.addActionListener(e -> hideMenu());

        saveBtn.addActionListener(e -> {
            if (parentContainer instanceof JPanel) {
                SaveSystemUI.getInstance().showSave((JPanel) parentContainer);
            }
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

    // ════════════════════════════════════════════════════════
    //  Show / Hide
    // ════════════════════════════════════════════════════════
    public void showMenu(Container parent) {
        if (parent == null) return;
        this.parentContainer = parent;
        layoutChildren(parent.getWidth(), parent.getHeight());

        boolean already = false;
        for (Component c : parent.getComponents())
            if (c instanceof PauseMenuUI) { already = true; break; }
        if (!already) parent.add(this);

        parent.setComponentZOrder(this, 0);
        setVisible(true);
        menuVisible = true;
        parent.revalidate();
        parent.repaint();
        // ✅ ขอ focus เพื่อรับ KeyEvent
        SwingUtilities.invokeLater(() -> requestFocusInWindow());
    }

    public void hideMenu() {
        if (!menuVisible) return;
        setVisible(false);
        menuVisible = false;
        if (parentContainer != null) {
            parentContainer.revalidate();
            parentContainer.repaint();
            parentContainer.requestFocusInWindow();
        }
    }

    public boolean isMenuVisible() { return menuVisible; }

    // ════════════════════════════════════════════════════════
    //  Layout — ขยายขนาดให้ใหญ่ขึ้น
    // ════════════════════════════════════════════════════════
    private void layoutChildren(int pw, int ph) {
        setBounds(0, 0, pw, ph);

        // ✅ ขนาดใหญ่ขึ้น: 400x460 จากเดิม 340x380
        int mw = 400, mh = 460;
        menuPanel.setBounds((pw - mw) / 2, (ph - mh) / 2, mw, mh);

        int bw = 310, bh = 56;   // ✅ ปุ่มใหญ่ขึ้น
        int bx = (mw - bw) / 2;
        int startY = 100, gap = 72;  // ✅ เว้นระยะมากขึ้น

        resumeBtn  .setBounds(bx, startY,           bw, bh);
        saveBtn    .setBounds(bx, startY + gap,      bw, bh);
        mainMenuBtn.setBounds(bx, startY + gap * 2,  bw, bh);
        exitBtn    .setBounds(bx, startY + gap * 3,  bw, bh);
    }

    @Override protected void paintComponent(Graphics g) {
        if (menuVisible) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0,0,0,170));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    // ════════════════════════════════════════════════════════
    //  Button factory
    // ════════════════════════════════════════════════════════
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
                // shadow
                g2.setColor(new Color(0,0,0,50));
                g2.fill(new RoundRectangle2D.Float(2,3,w-2,h-1,12,12));
                // fill
                g2.setColor(cur);
                g2.fill(new RoundRectangle2D.Float(0,0,w-2,h-2,12,12));
                // border
                g2.setColor(new Color(255,255,255,90));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1,1,w-4,h-4,12,12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(font);
        btn.setForeground(WHITE);
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}