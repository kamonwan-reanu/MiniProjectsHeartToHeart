package UI_Components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import core.Main;

public class SaveSystemUI extends JPanel {

    private static SaveSystemUI instance;
    public static SaveSystemUI getInstance() {
        if (instance == null) instance = new SaveSystemUI();
        return instance;
    }

    private static final Color CARD_NORM = new Color(18, 24, 40);
    private static final Color CARD_NEW  = new Color(22, 30, 50);
    private static final Color CARD_SEL  = new Color(30, 45, 75);
    private static final Color BORDER_N  = new Color(40, 55, 90);
    private static final Color BORDER_S  = new Color(100, 160, 255);
    private static final Color ACCENT    = new Color(100, 200, 255);
    private static final Color GOLD      = new Color(212, 175, 55);
    private static final Color GREEN     = new Color(80, 200, 120);
    private static final Color RED       = new Color(220, 60, 60);
    private static final Color TEXT_HI   = new Color(240, 245, 255);
    private static final Color TEXT_MID  = new Color(160, 175, 210);
    private static final Color TEXT_DIM  = new Color(90, 105, 140);
    private static final Font  F_TITLE   = new Font("Tahoma", Font.BOLD,  22);
    private static final Font  F_QUEST   = new Font("Tahoma", Font.BOLD,  17);
    private static final Font  F_SLOT    = new Font("Tahoma", Font.BOLD,  13);
    private static final Font  F_META    = new Font("Tahoma", Font.PLAIN, 12);
    private static final Font  F_BTN     = new Font("Tahoma", Font.BOLD,  15);

    private static final int    MAX_SLOTS = 4;
    private static final String SAVE_DIR  = System.getProperty("user.dir") + File.separator;

    // ── Data Model ────────────────────────────────────────
    public static class SaveSlot {
        public int slotIndex;
        public String questName, location, timestamp, playtime, sceneName, playerName;
        public int storyIndex, ahriAffection;
        public boolean isEmpty;

        public SaveSlot(int idx) { this.slotIndex = idx; this.isEmpty = true; }

        public SaveSlot(int idx, String quest, String loc, String time, String play,
                        String scene, int story, String player, int affection) {
            this.slotIndex = idx; this.questName = quest; this.location = loc;
            this.timestamp = time; this.playtime = play; this.sceneName = scene;
            this.storyIndex = story; this.playerName = player;
            this.ahriAffection = affection; this.isEmpty = false;
        }

        public void saveToFile() {
            try {
                String path = SAVE_DIR + "saveslot_" + slotIndex + ".dat";
                String sb = "slotIndex=" + slotIndex + "\n" +
                    "questName=" + questName + "\n" + "location=" + location + "\n" +
                    "timestamp=" + timestamp + "\n" + "playtime=" + playtime + "\n" +
                    "sceneName=" + sceneName + "\n" + "storyIndex=" + storyIndex + "\n" +
                    "playerName=" + playerName + "\n" + "ahriAffection=" + ahriAffection + "\n" +
                    "isEmpty=false\n";
                Files.write(Paths.get(path), sb.getBytes("UTF-8"));
            } catch (Exception e) { e.printStackTrace(); }
        }

        public static SaveSlot loadFromFile(int idx) {
            try {
                String path = SAVE_DIR + "saveslot_" + idx + ".dat";
                if (!Files.exists(Paths.get(path))) return new SaveSlot(idx);
                String content = new String(Files.readAllBytes(Paths.get(path)), "UTF-8");
                String quest="", loc="", time="", play="", scene="SCENE_1", player="";
                int story=0, affection=0;
                for (String ln : content.split("\n")) {
                    if      (ln.startsWith("questName="))     quest     = ln.substring(9).trim();
                    else if (ln.startsWith("location="))      loc       = ln.substring(9).trim();
                    else if (ln.startsWith("timestamp="))     time      = ln.substring(10).trim();
                    else if (ln.startsWith("playtime="))      play      = ln.substring(9).trim();
                    else if (ln.startsWith("sceneName="))     scene     = ln.substring(10).trim();
                    else if (ln.startsWith("storyIndex="))    story     = Integer.parseInt(ln.substring(11).trim());
                    else if (ln.startsWith("playerName="))    player    = ln.substring(11).trim();
                    else if (ln.startsWith("ahriAffection=")) affection = Integer.parseInt(ln.substring(14).trim());
                }
                return new SaveSlot(idx, quest, loc, time, play, scene, story, player, affection);
            } catch (Exception e) { return new SaveSlot(idx); }
        }

        public static void deleteFile(int idx) {
            try { Files.deleteIfExists(Paths.get(SAVE_DIR + "saveslot_" + idx + ".dat")); }
            catch (Exception ignored) {}
        }
    }

    // ── State ─────────────────────────────────────────────
    private final List<SaveSlot> slots = new ArrayList<>();
    private boolean isSaveMode   = true;
    private int     selectedSlot = -1;
    private String  returnCard   = null;
    private boolean isHiding     = false;

    private JPanel   slotContainer;
    private JLabel   titleLabel;
    private JButton  confirmBtn;
    private Runnable onLoadSuccess;
    private Runnable onBack;
    public void setOnLoadSuccess(Runnable r) { this.onLoadSuccess = r; }
    public void setOnBack(Runnable r)        { this.onBack = r; }

    private JPanel dialogPanel;

    private SaveSystemUI() {
        setLayout(null);
        setOpaque(false);
        buildUI();
    }

    private void reloadSlotsFromFiles() {
        slots.clear();
        for (int i = 0; i < MAX_SLOTS; i++) slots.add(SaveSlot.loadFromFile(i));
    }

    // ════════════════════════════════════════════════════════
    //  Build UI
    // ════════════════════════════════════════════════════════
    private void buildUI() {
        dialogPanel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 120));
                g2.fill(new RoundRectangle2D.Float(6, 6, getWidth()-4, getHeight()-4, 24, 24));
                g2.setColor(new Color(12, 16, 30));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-6, getHeight()-6, 24, 24));
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(new Color(50, 70, 120));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-7, getHeight()-7, 24, 24));
                g2.setStroke(new BasicStroke(2f));
                g2.setPaint(new GradientPaint(30, 0, ACCENT, getWidth()-30, 0, new Color(0,0,0,0)));
                g2.drawLine(30, 1, getWidth()-30, 1);
                g2.dispose();
            }
        };
        dialogPanel.setOpaque(false);

        // Title bar
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        titleBar.setBorder(new EmptyBorder(20, 24, 14, 24));
        titleLabel = new JLabel("บันทึกเกม", JLabel.LEFT);
        titleLabel.setFont(F_TITLE);
        titleLabel.setForeground(TEXT_HI);
        JPanel divLine = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, ACCENT, getWidth(), 0, new Color(0,0,0,0)));
                g2.fillRect(0, 0, getWidth(), 1); g2.dispose();
            }
        };
        divLine.setPreferredSize(new Dimension(0, 1));
        divLine.setOpaque(false);
        titleBar.add(titleLabel, BorderLayout.CENTER);
        titleBar.add(divLine, BorderLayout.SOUTH);

        // Slot list
        slotContainer = new JPanel();
        slotContainer.setLayout(new BoxLayout(slotContainer, BoxLayout.Y_AXIS));
        slotContainer.setOpaque(false);
        slotContainer.setBorder(new EmptyBorder(8, 20, 8, 20));
        JScrollPane scroll = new JScrollPane(slotContainer);
        scroll.setOpaque(false); scroll.getViewport().setOpaque(false);
        scroll.setBorder(null); scroll.getVerticalScrollBar().setUnitIncrement(20);

        // Bottom bar
        JPanel btnBar = new JPanel(new BorderLayout());
        btnBar.setOpaque(false);
        btnBar.setBorder(new EmptyBorder(8, 20, 18, 20));

        JButton backBtn = makeOutlineBtn("← ย้อนกลับ");
        backBtn.addActionListener(e -> doBack());

        confirmBtn = makeConfirmBtn("บันทึก");
        confirmBtn.setEnabled(false);
        confirmBtn.addActionListener(e -> doConfirm());

        btnBar.add(backBtn,    BorderLayout.WEST);
        btnBar.add(confirmBtn, BorderLayout.EAST);

        dialogPanel.add(titleBar, BorderLayout.NORTH);
        dialogPanel.add(scroll,   BorderLayout.CENTER);
        dialogPanel.add(btnBar,   BorderLayout.SOUTH);

        add(dialogPanel);

        setFocusable(true);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "save_esc");
        getActionMap().put("save_esc", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (isVisible()) doBack();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getSource() == SaveSystemUI.this) doBack();
            }
        });
    }

    // ── ปุ่ม ย้อนกลับ (outline style) ──────────────────────
    private JButton makeOutlineBtn(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(50, 65, 100));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                }
                g2.setColor(getModel().isRollover() ? TEXT_MID : new Color(60, 80, 120));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-3, getHeight()-3, 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(F_BTN); b.setForeground(TEXT_MID);
        b.setOpaque(false); b.setContentAreaFilled(false);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 22, 10, 22));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ── ปุ่ม บันทึก/โหลด (gradient + glow style) ────────────
    private JButton makeConfirmBtn(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                if (!isEnabled()) {
                    g2.setColor(new Color(30, 40, 65));
                    g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));
                    g2.setColor(new Color(60, 75, 110));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new RoundRectangle2D.Float(1, 1, w-3, h-3, 14, 14));
                } else {
                    // glow shadow
                    Color glowC = getModel().isRollover() ? new Color(80, 180, 255, 60) : new Color(60, 150, 230, 40);
                    g2.setColor(glowC);
                    g2.fill(new RoundRectangle2D.Float(-3, -3, w+6, h+6, 18, 18));
                    // gradient fill
                    Color top = getModel().isRollover() ? new Color(130, 215, 255) : new Color(100, 200, 255);
                    Color bot = getModel().isRollover() ? new Color(60, 150, 230)  : new Color(50, 130, 210);
                    g2.setPaint(new GradientPaint(0, 0, top, 0, h, bot));
                    g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));
                    // top highlight
                    g2.setPaint(new GradientPaint(0, 0, new Color(255,255,255,60), 0, h/2f, new Color(255,255,255,0)));
                    g2.fill(new RoundRectangle2D.Float(2, 2, w-4, h/2, 12, 12));
                    // border
                    g2.setColor(new Color(150, 220, 255, 180));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new RoundRectangle2D.Float(1, 1, w-3, h-3, 14, 14));
                }
                g2.dispose();
                // สี text ตาม state
                setForeground(isEnabled() ? new Color(10, 20, 40) : new Color(80, 100, 140));
                super.paintComponent(g);
            }
        };
        b.setFont(F_BTN);
        b.setOpaque(false); b.setContentAreaFilled(false);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 32, 10, 32));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ════════════════════════════════════════════════════════
    //  ย้อนกลับ
    // ════════════════════════════════════════════════════════
    private void doBack() {
        if (isHiding) return;
        hide();
        if (onBack != null) {
            Runnable cb = onBack; onBack = null;
            cb.run();
        } else if (returnCard != null && !returnCard.isEmpty()) {
            Main.cardLayout.show(Main.mainContainer, returnCard);
        }
    }

    // ════════════════════════════════════════════════════════
    //  Dialog sizing
    // ════════════════════════════════════════════════════════
    private void resizeDialog() {
        int pw = getWidth(), ph = getHeight();
        if (pw <= 0 || ph <= 0) {
            if (Main.mainFrame != null) { pw = Main.mainFrame.getWidth(); ph = Main.mainFrame.getHeight(); }
        }
        if (pw <= 0 || ph <= 0) return;
        int dw = Math.max(Math.min(700, (int)(pw*0.90)), 360);
        int dh = Math.max(Math.min(560, (int)(ph*0.85)), 320);
        dialogPanel.setBounds((pw-dw)/2, (ph-dh)/2, dw, dh);
    }

    @Override public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        if (isVisible()) resizeDialog();
    }

    // ════════════════════════════════════════════════════════
    //  Slot Cards
    // ════════════════════════════════════════════════════════
    private void refreshSlots() {
        slotContainer.removeAll();
        for (SaveSlot slot : slots) {
            slotContainer.add(buildSlotCard(slot));
            slotContainer.add(Box.createRigidArea(new Dimension(0, 6)));
        }
        slotContainer.revalidate(); slotContainer.repaint();
    }

    private JPanel buildSlotCard(SaveSlot slot) {
        JPanel card = new JPanel(new BorderLayout(8, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean sel = selectedSlot == slot.slotIndex;
                // rounded bg
                g2.setColor(sel ? CARD_SEL : (slot.isEmpty ? CARD_NEW : CARD_NORM));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-4, getHeight()-2, 10, 10));
                if (sel) {
                    // left accent bar
                    g2.setColor(ACCENT);
                    g2.fill(new RoundRectangle2D.Float(0, 0, 4, getHeight()-2, 4, 4));
                    // border glow
                    g2.setColor(new Color(100, 160, 255, 120));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-6, getHeight()-3, 10, 10));
                } else {
                    g2.setColor(BORDER_N);
                    g2.setStroke(new BasicStroke(1f));
                    g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-5, getHeight()-3, 10, 10));
                }
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 22, 14, 18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (slot.isEmpty) {
            JPanel inner = new JPanel(new BorderLayout());
            inner.setOpaque(false);
            JLabel icon  = new JLabel("＋");
            icon.setFont(new Font("Dialog", Font.PLAIN, 20));
            icon.setForeground(new Color(60, 80, 120));
            JLabel empty = new JLabel("  ช่องว่าง  —  Slot " + (slot.slotIndex+1));
            empty.setFont(F_SLOT); empty.setForeground(TEXT_DIM);
            inner.add(icon,  BorderLayout.WEST);
            inner.add(empty, BorderLayout.CENTER);
            card.add(inner, BorderLayout.CENTER);
            card.addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    if (isSaveMode) selectSlot(slot.slotIndex);
                }
            });
        } else {
            JPanel left = new JPanel(new GridLayout(2,1,0,4)); left.setOpaque(false);
            JLabel questLbl = new JLabel(slot.questName);
            questLbl.setFont(F_QUEST); questLbl.setForeground(TEXT_HI);
            JLabel locLbl = new JLabel("📍 " + slot.location);
            locLbl.setFont(F_META); locLbl.setForeground(TEXT_DIM);
            left.add(questLbl); left.add(locLbl);

            JPanel right = new JPanel(new GridLayout(2,1,0,4)); right.setOpaque(false);
            JLabel timeLbl  = new JLabel(slot.timestamp, JLabel.RIGHT);
            timeLbl.setFont(F_SLOT); timeLbl.setForeground(GOLD);
            JLabel sceneLbl = new JLabel(slot.sceneName, JLabel.RIGHT);
            sceneLbl.setFont(F_META); sceneLbl.setForeground(ACCENT);
            right.add(timeLbl); right.add(sceneLbl);

            // ── ปุ่มลบ (icon trash) ─────────────────────────
            JButton delBtn = new JButton("✕") {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (getModel().isRollover()) {
                        g2.setColor(new Color(200, 40, 40, 200));
                        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            delBtn.setFont(new Font("Tahoma", Font.BOLD, 14));
            delBtn.setForeground(new Color(200, 60, 60));
            delBtn.setOpaque(false); delBtn.setContentAreaFilled(false);
            delBtn.setBorderPainted(false); delBtn.setFocusPainted(false);
            delBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            delBtn.setPreferredSize(new Dimension(36, 36));
            delBtn.setVisible(false);
            delBtn.setToolTipText("ลบ Save นี้");
            delBtn.addActionListener(e -> showDeleteConfirm(slot.slotIndex));

            card.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { delBtn.setVisible(true);  card.repaint(); }
                @Override public void mouseExited(MouseEvent e) {
                    Component c = SwingUtilities.getDeepestComponentAt(card, e.getX(), e.getY());
                    if (c == null || !SwingUtilities.isDescendingFrom(c, card)) { delBtn.setVisible(false); card.repaint(); }
                }
                @Override public void mousePressed(MouseEvent e) { selectSlot(slot.slotIndex); }
            });
            delBtn.addMouseListener(new MouseAdapter() {
                @Override public void mouseExited(MouseEvent e) {
                    Point p = SwingUtilities.convertPoint(delBtn, e.getPoint(), card);
                    if (!card.contains(p)) { delBtn.setVisible(false); card.repaint(); }
                }
            });

            card.add(left,   BorderLayout.CENTER);
            card.add(right,  BorderLayout.EAST);
            card.add(delBtn, BorderLayout.WEST);
        }
        return card;
    }

    // ════════════════════════════════════════════════════════
    //  Custom Delete Confirm Dialog
    // ════════════════════════════════════════════════════════
    private void showDeleteConfirm(int idx) {
        JLayeredPane lp = Main.mainFrame.getLayeredPane();
        int lpW = lp.getWidth(), lpH = lp.getHeight();

        // ── dim panel (block mouse แต่ไม่มี children) ────────
        JPanel dim = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 180));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
            @Override public boolean contains(int x, int y) { return true; }
        };
        dim.setOpaque(false);
        MouseAdapter blocker = new MouseAdapter() {};
        dim.addMouseListener(blocker);
        dim.addMouseMotionListener(blocker);
        dim.setBounds(0, 0, lpW, lpH);
        lp.add(dim, JLayeredPane.DRAG_LAYER);

        // ── dialog box (อยู่เหนือ dim) ────────────────────────
        JPanel box = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 100));
                g2.fill(new RoundRectangle2D.Float(6, 6, getWidth()-4, getHeight()-4, 20, 20));
                g2.setColor(new Color(14, 18, 34));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-6, getHeight()-6, 20, 20));
                g2.setColor(new Color(180, 40, 40));
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-8, getHeight()-8, 20, 20));
                g2.setStroke(new BasicStroke(2.5f));
                g2.setPaint(new GradientPaint(30, 0, new Color(220, 60, 60), getWidth()-30, 0, new Color(0,0,0,0)));
                g2.drawLine(30, 1, getWidth()-30, 1);
                g2.dispose();
            }
        };
        int bw = 380, bh = 230;
        box.setOpaque(false);
        box.setBorder(new EmptyBorder(24, 28, 22, 28));
        box.setBounds((lpW - bw) / 2, (lpH - bh) / 2, bw, bh);
        lp.add(box, JLayeredPane.DRAG_LAYER);
        lp.setComponentZOrder(box, 0); // box อยู่เหนือ dim

        JLabel icon  = new JLabel("✕", SwingConstants.CENTER);
        icon.setFont(new Font("Tahoma", Font.BOLD, 42));
        icon.setForeground(new Color(220, 60, 60));

        JLabel title = new JLabel("ลบ Save นี้?", SwingConstants.CENTER);
        title.setFont(new Font("Tahoma", Font.BOLD, 20));
        title.setForeground(TEXT_HI);

        JLabel sub = new JLabel("การกระทำนี้ไม่สามารถเลิกทำได้", SwingConstants.CENTER);
        sub.setFont(new Font("Tahoma", Font.PLAIN, 14));
        sub.setForeground(TEXT_DIM);

        // ปุ่มคู่
        JButton cancelBtn = new JButton("ยกเลิก") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(50, 65, 100) : new Color(35, 45, 75));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(new Color(70, 90, 130));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-3, getHeight()-3, 10, 10));
                g2.dispose(); super.paintComponent(g);
            }
        };
        cancelBtn.setFont(F_BTN); cancelBtn.setForeground(TEXT_MID);
        cancelBtn.setOpaque(false); cancelBtn.setContentAreaFilled(false);
        cancelBtn.setBorderPainted(false); cancelBtn.setFocusPainted(false);
        cancelBtn.setBorder(new EmptyBorder(10, 28, 10, 28));
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton confirmDelBtn = new JButton("ลบเลย") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color top = getModel().isRollover() ? new Color(255, 80, 80) : new Color(200, 50, 50);
                Color bot = getModel().isRollover() ? new Color(200, 30, 30) : new Color(160, 30, 30);
                g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bot));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(new Color(255, 120, 120, 120));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-3, getHeight()-3, 10, 10));
                g2.dispose(); super.paintComponent(g);
            }
        };
        confirmDelBtn.setFont(F_BTN); confirmDelBtn.setForeground(Color.WHITE);
        confirmDelBtn.setOpaque(false); confirmDelBtn.setContentAreaFilled(false);
        confirmDelBtn.setBorderPainted(false); confirmDelBtn.setFocusPainted(false);
        confirmDelBtn.setBorder(new EmptyBorder(10, 28, 10, 28));
        confirmDelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setOpaque(false);
        btnRow.add(cancelBtn); btnRow.add(confirmDelBtn);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        for (JComponent c : new JComponent[]{icon, title, sub})
            c.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(icon);
        center.add(Box.createRigidArea(new Dimension(0, 8)));
        center.add(title);
        center.add(Box.createRigidArea(new Dimension(0, 6)));
        center.add(sub);
        center.add(Box.createRigidArea(new Dimension(0, 20)));
        center.add(btnRow);
        box.add(center, BorderLayout.CENTER);

        lp.revalidate(); lp.repaint();

        cancelBtn.addActionListener(e -> {
            lp.remove(dim); lp.remove(box);
            lp.revalidate(); lp.repaint();
        });
        confirmDelBtn.addActionListener(e -> {
            lp.remove(dim); lp.remove(box);
            lp.revalidate(); lp.repaint();
            SaveSlot.deleteFile(idx);
            slots.set(idx, new SaveSlot(idx));
            if (selectedSlot == idx) { selectedSlot = -1; confirmBtn.setEnabled(false); }
            refreshSlots();
        });
    }

    private void selectSlot(int idx) {
        selectedSlot = idx;
        confirmBtn.setEnabled(true);
        confirmBtn.setText(isSaveMode ? "บันทึก" : "โหลด");
        // repaint เฉพาะ card ไม่ต้อง rebuild ทั้งหมด
        slotContainer.repaint();
    }

    private void doConfirm() {
        if (isSaveMode) performSave();
        else            performLoad();
    }

    private void performSave() {
        if (selectedSlot < 0) return;
        int idx = selectedSlot;
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
        String sceneName = "SCENE_1"; int storyIdx = 0;
        String playerName = model.GameConstants.PLAYER_NAME; int affection = 0;
        try {
            for (Component c : Main.mainContainer.getComponents()) {
                if (c instanceof UI_Screens.PlaySceneMain) {
                    UI_Screens.PlaySceneMain ps = (UI_Screens.PlaySceneMain) c;
                    java.lang.reflect.Field fScene = ps.getClass().getDeclaredField("sceneName");
                    java.lang.reflect.Field fIndex = ps.getClass().getDeclaredField("storyIndex");
                    fScene.setAccessible(true); fIndex.setAccessible(true);
                    sceneName = (String) fScene.get(ps); storyIdx = (int) fIndex.get(ps); break;
                }
            }
        } catch (Exception ignored) {}
        try { affection = model.Relation.getInstance().getAffection("Ahri"); } catch (Exception ignored) {}

        String questName = "บันทึกของ " + (playerName.isEmpty() ? "ผู้เล่น" : playerName);
        SaveSlot newSlot = new SaveSlot(idx, questName, sceneName, ts, "—", sceneName, storyIdx, playerName, affection);
        newSlot.saveToFile();
        while (slots.size() <= idx) slots.add(new SaveSlot(slots.size()));
        slots.set(idx, newSlot);
        selectedSlot = -1; confirmBtn.setEnabled(false);
        refreshSlots();

        String prev = titleLabel.getText();
        titleLabel.setForeground(GREEN);
        titleLabel.setText("✓  บันทึกสำเร็จ!  (Slot " + (idx+1) + ")");
        new Timer(1800, e -> { titleLabel.setText(prev); titleLabel.setForeground(TEXT_HI); ((Timer)e.getSource()).stop(); }).start();
    }

    private void performLoad() {
        if (selectedSlot < 0 || selectedSlot >= slots.size()) return;
        SaveSlot slot = slots.get(selectedSlot);
        if (slot.isEmpty) return;
        titleLabel.setText("กำลังโหลด..."); titleLabel.setForeground(ACCENT);
        new Timer(500, e -> {
            ((Timer)e.getSource()).stop();
            hide();
            try {
                model.GameConstants.PLAYER_NAME = slot.playerName;
                model.Relation.getInstance().setAffection("Ahri", slot.ahriAffection);
                RelationUI.getInstance().updateAllScores();
                for (Component c : Main.mainContainer.getComponents()) {
                    if (c instanceof UI_Screens.PlaySceneMain) {
                        UI_Screens.PlaySceneMain ps = (UI_Screens.PlaySceneMain) c;
                        java.lang.reflect.Field fMap = ps.getClass().getDeclaredField("storyMap");
                        fMap.setAccessible(true);
                        @SuppressWarnings("unchecked")
                        java.util.Map<String,Object[][]> map = (java.util.Map<String,Object[][]>) fMap.get(ps);
                        Object[][] sceneData = map.get(slot.sceneName);
                        if (sceneData != null) {
                            ps.loadNewScene(sceneData, slot.sceneName);
                            java.lang.reflect.Field fIdx = ps.getClass().getDeclaredField("storyIndex");
                            fIdx.setAccessible(true);
                            fIdx.set(ps, Math.min(slot.storyIndex, sceneData.length-1));
                            ps.updateScene(sceneData[Math.min(slot.storyIndex, sceneData.length-1)]);
                        }
                        break;
                    }
                }
                Main.cardLayout.show(Main.mainContainer, "PLAY_SCENE");
                if (onLoadSuccess != null) onLoadSuccess.run();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "โหลดล้มเหลว: " + ex.getMessage());
            }
        }).start();
    }

    // ════════════════════════════════════════════════════════
    //  Public API
    // ════════════════════════════════════════════════════════
    public void showSave(JPanel parent, String returnTo) {
        isSaveMode = true; returnCard = returnTo;
        titleLabel.setText("บันทึกเกม"); titleLabel.setForeground(TEXT_HI);
        confirmBtn.setText("บันทึก"); selectedSlot = -1; confirmBtn.setEnabled(false);
        reloadSlotsFromFiles(); refreshSlots(); mountOnLayeredPane();
    }

    public void showLoad(JPanel parent, String returnTo) {
        isSaveMode = false; returnCard = returnTo;
        titleLabel.setText("โหลดเกม"); titleLabel.setForeground(TEXT_HI);
        confirmBtn.setText("โหลด"); selectedSlot = -1; confirmBtn.setEnabled(false);
        reloadSlotsFromFiles(); refreshSlots(); mountOnLayeredPane();
    }

    public void showSave(JPanel parent) { showSave(parent, null); }
    public void showLoad(JPanel parent) { showLoad(parent, null); }

    private void mountOnLayeredPane() {
        JLayeredPane lp = Main.mainFrame.getLayeredPane();
        lp.remove(this);
        setBounds(0, 0, lp.getWidth(), lp.getHeight());
        lp.add(this, JLayeredPane.MODAL_LAYER);
        setVisible(true);
        resizeDialog();
        lp.revalidate(); lp.repaint();
    }

    public void hide() {
        if (isHiding) return;
        isHiding = true;
        setVisible(false);
        JLayeredPane lp = Main.mainFrame.getLayeredPane();
        lp.remove(this);
        lp.revalidate(); lp.repaint();
        isHiding = false;
    }

    private JButton makeBtn(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = !isEnabled() ? new Color(35,45,70) : getModel().isRollover() ? bg.brighter() : bg;
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(F_BTN); b.setForeground(fg);
        b.setOpaque(false); b.setContentAreaFilled(false);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 22, 10, 22));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}