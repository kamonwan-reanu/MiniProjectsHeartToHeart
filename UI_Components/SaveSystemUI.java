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

    // ── Palette ───────────────────────────────────────────
    private static final Color CARD_NORM = new Color(18, 24, 40);
    private static final Color CARD_NEW  = new Color(22, 30, 50);
    private static final Color CARD_SEL  = new Color(30, 45, 75);
    private static final Color BORDER_N  = new Color(40, 55, 90);
    private static final Color BORDER_S  = new Color(100, 160, 255);
    private static final Color ACCENT    = new Color(100, 200, 255);
    private static final Color GOLD      = new Color(212, 175, 55);
    private static final Color GREEN     = new Color(80,  200, 120);
    private static final Color RED_DEL   = new Color(220, 60,  60);
    private static final Color TEXT_HI   = new Color(240, 245, 255);
    private static final Color TEXT_MID  = new Color(160, 175, 210);
    private static final Color TEXT_DIM  = new Color(90,  105, 140);
    private static final Font  F_TITLE   = new Font("Tahoma", Font.BOLD,  22);
    private static final Font  F_QUEST   = new Font("Tahoma", Font.BOLD,  17);
    private static final Font  F_SLOT    = new Font("Tahoma", Font.BOLD,  13);
    private static final Font  F_META    = new Font("Tahoma", Font.PLAIN, 12);
    private static final Font  F_BTN     = new Font("Tahoma", Font.BOLD,  14);

    private static final int    MAX_SLOTS = 4;
    private static final String SAVE_DIR  = System.getProperty("user.dir") + File.separator;

    // ── Data Model ────────────────────────────────────────
    public static class SaveSlot {
        public int     slotIndex;
        public String  questName, location, timestamp, playtime;
        public String  sceneName;
        public int     storyIndex;
        public String  playerName;
        public int     ahriAffection;
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
                    "questName=" + questName + "\n" +
                    "location=" + location + "\n" +
                    "timestamp=" + timestamp + "\n" +
                    "playtime=" + playtime + "\n" +
                    "sceneName=" + sceneName + "\n" +
                    "storyIndex=" + storyIndex + "\n" +
                    "playerName=" + playerName + "\n" +
                    "ahriAffection=" + ahriAffection + "\n" +
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
    private boolean isSaveMode = true;
    private int selectedSlot  = -1;

    private JPanel  slotContainer;
    private JLabel  titleLabel;
    private JButton confirmBtn;
    private JButton cancelBtn;
    private Runnable onLoadSuccess;
    public void setOnLoadSuccess(Runnable r) { this.onLoadSuccess = r; }

    // ── parent ref สำหรับ hide ────────────────────────────
    private Container mountedParent = null;

    private SaveSystemUI() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(new Color(0, 0, 0, 0));
        buildUI();
    }

    private void reloadSlotsFromFiles() {
        slots.clear();
        for (int i = 0; i < MAX_SLOTS; i++) slots.add(SaveSlot.loadFromFile(i));
    }

    // ════════════════════════════════════════════════════════
    //  Build UI — ใช้ JLayeredPane เพื่อควบคุม Z-order
    // ════════════════════════════════════════════════════════
    private void buildUI() {
        // Dim background
        JPanel dimBg = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0,0,new Color(5,8,18,220),
                    0,getHeight(),new Color(12,18,35,200));
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        dimBg.setOpaque(false);
        dimBg.setLayout(new GridBagLayout());

        // ── Dialog box ─────────────────────────────────────
        JPanel dialog = new JPanel(new BorderLayout(0,0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0,0,0,120));
                g2.fill(new RoundRectangle2D.Float(6,6,getWidth()-4,getHeight()-4,20,20));
                g2.setColor(new Color(12,16,30));
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth()-6,getHeight()-6,20,20));
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(new Color(50,70,120));
                g2.draw(new RoundRectangle2D.Float(0,0,getWidth()-7,getHeight()-7,20,20));
                g2.dispose();
            }
        };
        dialog.setOpaque(false);
        dialog.setPreferredSize(new Dimension(700, 600));

        // Title
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        titleBar.setBorder(new EmptyBorder(20,24,14,24));

        titleLabel = new JLabel("บันทึกเกม", JLabel.LEFT);
        titleLabel.setFont(F_TITLE);
        titleLabel.setForeground(TEXT_HI);

        JPanel divLine = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                GradientPaint gp = new GradientPaint(0,0,ACCENT,getWidth(),0,new Color(0,0,0,0));
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),1); g2.dispose();
            }
        };
        divLine.setPreferredSize(new Dimension(0,1)); divLine.setOpaque(false);
        titleBar.add(titleLabel, BorderLayout.CENTER);
        titleBar.add(divLine, BorderLayout.SOUTH);

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
        topSection.add(titleBar, BorderLayout.NORTH);

        // Slot list
        slotContainer = new JPanel();
        slotContainer.setLayout(new BoxLayout(slotContainer, BoxLayout.Y_AXIS));
        slotContainer.setOpaque(false);
        slotContainer.setBorder(new EmptyBorder(8,20,8,20));

        JScrollPane scroll = new JScrollPane(slotContainer);
        scroll.setOpaque(false); scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.getVerticalScrollBar().setOpaque(false);

        // Bottom buttons — ใช้ absolute position เพื่อให้กดได้ชัวร์
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 14));
        btnBar.setOpaque(false);
        btnBar.setBorder(new EmptyBorder(4,20,8,20));

        cancelBtn = makeBtn("ยกเลิก", new Color(60,72,110), TEXT_MID);
        confirmBtn = makeBtn("บันทึก", ACCENT, new Color(10,20,35));
        confirmBtn.setEnabled(false);

        // ✅ ปุ่มยกเลิกต้องกดได้เสมอ
        cancelBtn.addActionListener(e -> hide());
        confirmBtn.addActionListener(e -> doConfirm());

        btnBar.add(cancelBtn);
        btnBar.add(confirmBtn);

        dialog.add(topSection, BorderLayout.NORTH);
        dialog.add(scroll,     BorderLayout.CENTER);
        dialog.add(btnBar,     BorderLayout.SOUTH);

        dimBg.add(dialog);
        add(dimBg, BorderLayout.CENTER);

        // ✅ ESC ปิด UI ได้
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) hide();
            }
        });
    }

    // ════════════════════════════════════════════════════════
    //  Slot Cards
    // ════════════════════════════════════════════════════════
    private void refreshSlots() {
        slotContainer.removeAll();
        for (SaveSlot slot : slots) {
            slotContainer.add(buildSlotCard(slot));
            slotContainer.add(Box.createRigidArea(new Dimension(0,3)));
        }
        slotContainer.revalidate();
        slotContainer.repaint();
    }

    private JPanel buildSlotCard(SaveSlot slot) {
        JPanel card = new JPanel(new BorderLayout(8,0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                boolean sel = selectedSlot == slot.slotIndex;
                g2.setColor(sel ? CARD_SEL : (slot.isEmpty ? CARD_NEW : CARD_NORM));
                g2.fillRect(0,0,getWidth(),getHeight());
                if (sel) {
                    g2.setColor(BORDER_S); g2.setStroke(new BasicStroke(2f));
                    g2.drawLine(0,0,getWidth(),0);
                    g2.drawLine(0,getHeight()-1,getWidth(),getHeight()-1);
                    g2.setColor(ACCENT); g2.fillRect(0,0,4,getHeight());
                } else {
                    g2.setColor(BORDER_N); g2.setStroke(new BasicStroke(1f));
                    g2.drawLine(0,getHeight()-1,getWidth(),getHeight()-1);
                }
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14,22,14,22));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (slot.isEmpty) {
            JLabel empty = new JLabel("  — ช่องว่าง  (Slot " + (slot.slotIndex+1) + ")");
            empty.setFont(F_SLOT); empty.setForeground(TEXT_DIM);
            card.add(empty, BorderLayout.CENTER);
            card.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (isSaveMode) selectSlot(slot.slotIndex);
                }
            });
        } else {
            // LEFT
            JPanel left = new JPanel();
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            left.setOpaque(false);
            JLabel questLbl = new JLabel(slot.questName);
            questLbl.setFont(F_QUEST); questLbl.setForeground(TEXT_HI);
            JLabel locLbl = new JLabel(slot.location);
            locLbl.setFont(F_META); locLbl.setForeground(TEXT_DIM);
            left.add(questLbl);
            left.add(Box.createRigidArea(new Dimension(0,3)));
            left.add(locLbl);

            // RIGHT
            JPanel right = new JPanel();
            right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
            right.setOpaque(false);
            JLabel timeLbl = new JLabel(slot.timestamp, JLabel.RIGHT);
            timeLbl.setFont(F_SLOT); timeLbl.setForeground(GOLD);
            timeLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);
            JLabel sceneLbl = new JLabel(slot.sceneName, JLabel.RIGHT);
            sceneLbl.setFont(F_META); sceneLbl.setForeground(ACCENT);
            sceneLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);
            right.add(timeLbl);
            right.add(Box.createRigidArea(new Dimension(0,3)));
            right.add(sceneLbl);

            // ✅ ปุ่มลบ — แก้ด้วย JLayeredPane ใน card
            JButton delBtn = new JButton("ลบ") {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isRollover()
                        ? new Color(240,70,70) : new Color(180,40,40));
                    g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),8,8));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            delBtn.setFont(new Font("Tahoma", Font.BOLD, 11));
            delBtn.setForeground(Color.WHITE);
            delBtn.setOpaque(false); delBtn.setContentAreaFilled(false);
            delBtn.setBorderPainted(false); delBtn.setFocusPainted(false);
            delBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            delBtn.setPreferredSize(new Dimension(44, 26));
            delBtn.setVisible(false);
            delBtn.addActionListener(e -> deleteSlot(slot.slotIndex));

            // ✅ ใช้ mouseEntered/Exited บน card แต่ตรวจว่า mouse ยังอยู่บน card ก่อนซ่อน
            card.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    delBtn.setVisible(true); card.repaint();
                }
                @Override public void mouseExited(MouseEvent e) {
                    // เช็คว่า mouse ออกจาก card จริงๆ ไม่ใช่แค่เข้า delBtn
                    Component c = SwingUtilities.getDeepestComponentAt(
                        card, e.getX(), e.getY());
                    if (c == null || !SwingUtilities.isDescendingFrom(c, card)) {
                        delBtn.setVisible(false); card.repaint();
                    }
                }
                @Override public void mouseClicked(MouseEvent e) {
                    selectSlot(slot.slotIndex);
                }
            });

            // ✅ เพิ่ม listener บน delBtn เองด้วยเพื่อให้ card รู้
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

    // ── Select / Confirm / Delete ─────────────────────────
    private void selectSlot(int idx) {
        selectedSlot = idx;
        confirmBtn.setEnabled(true);
        confirmBtn.setText(isSaveMode ? "บันทึก" : "โหลด");
        refreshSlots();
    }

    private void doConfirm() {
        if (isSaveMode) performSave();
        else            performLoad();
    }

    private void performSave() {
        if (selectedSlot < 0) return;
        int idx = selectedSlot;

        String ts         = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
        String sceneName  = "SCENE_1";
        int    storyIdx   = 0;
        String playerName = model.GameConstants.PLAYER_NAME;
        int    affection  = 0;

        try {
            for (Component c : Main.mainContainer.getComponents()) {
                if (c instanceof UI_Screens.PlaySceneMain) {
                    UI_Screens.PlaySceneMain ps = (UI_Screens.PlaySceneMain) c;
                    java.lang.reflect.Field fScene = ps.getClass().getDeclaredField("sceneName");
                    java.lang.reflect.Field fIndex = ps.getClass().getDeclaredField("storyIndex");
                    fScene.setAccessible(true); fIndex.setAccessible(true);
                    sceneName = (String) fScene.get(ps);
                    storyIdx  = (int)   fIndex.get(ps);
                    break;
                }
            }
        } catch (Exception ignored) {}

        try { affection = model.Relation.getInstance().getAffection("Ahri"); }
        catch (Exception ignored) {}

        String questName = "บันทึกของ " + (playerName.isEmpty() ? "ผู้เล่น" : playerName);
        SaveSlot newSlot = new SaveSlot(idx, questName, sceneName, ts, "—",
            sceneName, storyIdx, playerName, affection);
        newSlot.saveToFile();

        while (slots.size() <= idx) slots.add(new SaveSlot(slots.size()));
        slots.set(idx, newSlot);
        selectedSlot = -1;
        confirmBtn.setEnabled(false);
        refreshSlots();

        String prev = titleLabel.getText();
        titleLabel.setForeground(GREEN);
        titleLabel.setText("บันทึกสำเร็จ! (Slot " + (idx+1) + ")");
        new Timer(1500, e -> {
            titleLabel.setText(prev); titleLabel.setForeground(TEXT_HI);
            ((Timer)e.getSource()).stop();
        }).start();
    }

    private void performLoad() {
        if (selectedSlot < 0 || selectedSlot >= slots.size()) return;
        SaveSlot slot = slots.get(selectedSlot);
        if (slot.isEmpty) return;

        titleLabel.setText("กำลังโหลด...");
        titleLabel.setForeground(ACCENT);
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
                        java.util.Map<String,Object[][]> map =
                            (java.util.Map<String,Object[][]>) fMap.get(ps);
                        Object[][] sceneData = map.get(slot.sceneName);
                        if (sceneData != null) {
                            ps.loadNewScene(sceneData, slot.sceneName);
                            java.lang.reflect.Field fIdx = ps.getClass().getDeclaredField("storyIndex");
                            fIdx.setAccessible(true);
                            int idx = Math.min(slot.storyIndex, sceneData.length-1);
                            fIdx.set(ps, idx);
                            ps.updateScene(sceneData[idx]);
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

    private void deleteSlot(int idx) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "ต้องการลบ Save นี้ใช่ไหม?", "ยืนยันการลบ",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            SaveSlot.deleteFile(idx);
            slots.set(idx, new SaveSlot(idx));
            if (selectedSlot == idx) { selectedSlot = -1; confirmBtn.setEnabled(false); }
            refreshSlots();
        }
    }

    // ── Public API ────────────────────────────────────────
    public void showSave(JPanel parent) {
        isSaveMode = true;
        titleLabel.setText("บันทึกเกม"); titleLabel.setForeground(TEXT_HI);
        confirmBtn.setText("บันทึก"); selectedSlot = -1; confirmBtn.setEnabled(false);
        reloadSlotsFromFiles(); refreshSlots();
        mountOnto(parent);
    }

    public void showLoad(JPanel parent) {
        isSaveMode = false;
        titleLabel.setText("โหลดเกม"); titleLabel.setForeground(TEXT_HI);
        confirmBtn.setText("โหลด"); selectedSlot = -1; confirmBtn.setEnabled(false);
        reloadSlotsFromFiles(); refreshSlots();
        mountOnto(parent);
    }

    // ✅ ใช้ null layout + setBounds แทน OverlayLayout เพื่อไม่บิดเบี้ยว
    private void mountOnto(JPanel parent) {
        if (mountedParent != null && mountedParent != parent) {
            mountedParent.remove(this);
        }
        mountedParent = parent;

        // ถ้ายังไม่ได้ add
        boolean found = false;
        for (Component c : parent.getComponents()) if (c == this) { found = true; break; }
        if (!found) parent.add(this);

        // ตั้งขนาดให้เต็ม parent
        setBounds(0, 0, parent.getWidth(), parent.getHeight());
        parent.setComponentZOrder(this, 0);
        setVisible(true);
        parent.revalidate();
        parent.repaint();
        SwingUtilities.invokeLater(() -> { requestFocusInWindow(); });
    }

    @Override public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
    }

    public void hide() {
        setVisible(false);
        if (mountedParent != null) {
            mountedParent.remove(this);
            mountedParent.revalidate();
            mountedParent.repaint();
            mountedParent = null;
        }
    }

    // ── Styled button ─────────────────────────────────────
    private JButton makeBtn(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isRollover() ? bg.brighter() : bg;
                if (!isEnabled()) c = new Color(35,45,70);
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),12,12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(F_BTN); b.setForeground(fg);
        b.setOpaque(false); b.setContentAreaFilled(false);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10,28,10,28));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}