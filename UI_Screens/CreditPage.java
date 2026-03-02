package UI_Screens;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import core.Main;

public class CreditPage extends JPanel {

    private static final Color BG      = new Color(248, 235, 248);
    private static final Color CARD    = new Color(255, 248, 254);
    private static final Color BORDER  = new Color(235, 180, 215);
    private static final Color ACCENT  = new Color(220, 70,  150);
    private static final Color TEXT_HI = new Color(70,  25,  55);
    private static final Color TEXT_LO = new Color(150, 90,  130);
    private static final Color PINK    = new Color(255, 105, 180);
    private static final Color WHITE   = Color.WHITE;

    public CreditPage(Font tFont, Font bFont) {
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(new EmptyBorder(36, 48, 28, 48));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    // ── Header ──────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 24, 0));

        JLabel title = new JLabel("เกี่ยวกับคนสร้าง", JLabel.LEFT);
        title.setFont(new Font("Tahoma", Font.BOLD, 28));
        title.setForeground(TEXT_HI);

        JPanel divider = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new java.awt.GradientPaint(0, 0, ACCENT, getWidth() * 0.6f, 0, new Color(0,0,0,0)));
                g2.fillRect(0, 0, getWidth(), 2);
                g2.dispose();
            }
        };
        divider.setPreferredSize(new Dimension(0, 2));
        divider.setOpaque(false);

        p.add(title, BorderLayout.CENTER);
        p.add(divider, BorderLayout.SOUTH);
        return p;
    }

    // ── Center ──────────────────────────────────────────────────
    private JPanel buildCenter() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // shadow
                g2.setColor(new Color(200, 150, 190, 40));
                g2.fill(new RoundRectangle2D.Float(3, 4, getWidth()-2, getHeight()-2, 20, 20));
                // body
                g2.setColor(CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-3, getHeight()-4, 20, 20));
                // border
                g2.setColor(BORDER);
                g2.setStroke(new java.awt.BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-4, getHeight()-5, 20, 20));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(660, 420));
        card.setBorder(new EmptyBorder(30, 40, 30, 40));

        buildRows(card);
        wrapper.add(card);
        return wrapper;
    }

    private void buildRows(JPanel card) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(14, 20, 14, 20);

        Font labelFont = new Font("Tahoma", Font.BOLD, 20);
        Font valueFont = new Font("Tahoma", Font.PLAIN, 20);

        String[][] rows = {
            { "สร้างโดย",  "Kamonwan Reanu" },
            { "เวอร์ชัน",   "0.1 Alpha" },
            { "เครื่องมือ", "Java Swing" },
            { "ติดต่อ",     "yourname@email.com" },
            { "ขอบคุณ",    "ทุกคนที่ช่วยทดสอบ 💖" },
        };

        for (int i = 0; i < rows.length; i++) {
            // separator
            if (i > 0) {
                gbc.gridy = i * 2 - 1; gbc.gridx = 0; gbc.gridwidth = 2;
                JPanel sep = new JPanel() {
                    @Override protected void paintComponent(Graphics g) {
                        g.setColor(new Color(235, 200, 220));
                        g.fillRect(0, 0, getWidth(), 1);
                    }
                };
                sep.setOpaque(false);
                sep.setPreferredSize(new Dimension(0, 1));
                card.add(sep, gbc);
                gbc.gridwidth = 1;
            }

            gbc.gridy = i * 2; gbc.gridx = 0; gbc.weightx = 0.35;
            gbc.anchor = GridBagConstraints.WEST;
            JLabel lbl = new JLabel(rows[i][0] + "  :");
            lbl.setFont(labelFont);
            lbl.setForeground(ACCENT);
            card.add(lbl, gbc);

            gbc.gridx = 1; gbc.weightx = 0.65;
            JLabel val = new JLabel(rows[i][1]);
            val.setFont(valueFont);
            val.setForeground(TEXT_HI);
            card.add(val, gbc);
        }
    }

    // ── Footer ──────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(22, 0, 0, 0));

        JButton backBtn = new JButton("ย้อนกลับ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? PINK.brighter() : PINK);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        backBtn.setFont(new Font("Tahoma", Font.BOLD, 18));
        backBtn.setForeground(WHITE);
        backBtn.setOpaque(false); backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false); backBtn.setFocusPainted(false);
        backBtn.setBorder(new EmptyBorder(12, 48, 12, 48));
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> Main.cardLayout.show(Main.mainContainer, "MENU"));
        p.add(backBtn);
        return p;
    }
}