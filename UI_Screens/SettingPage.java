package UI_Screens;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import core.Main;
import model.KeyConfig;

public class SettingPage extends JPanel {

    private JButton[] keyButtons;
    private String[] keyNames;
    private String[] keyDescriptions;

    private boolean isListening = false;
    private JButton listeningButton = null;

    private static final Color BG       = new Color(248, 235, 248);
    private static final Color CARD     = new Color(255, 248, 254);
    private static final Color BORDER   = new Color(235, 180, 215);
    private static final Color ACCENT   = new Color(220, 70,  150);
    private static final Color ACCENT2  = new Color(100, 140, 220);
    private static final Color GOLD     = new Color(160, 80,  30);
    private static final Color TEXT_HI  = new Color(70,  25,  55);
    private static final Color TEXT_LO  = new Color(150, 90,  130);
    private static final Font  TITLE_F  = new Font("Tahoma", Font.BOLD, 28);
    private static final Font  LABEL_F  = new Font("Tahoma", Font.BOLD, 15);
    private static final Font  VALUE_F  = new Font("Tahoma", Font.BOLD, 13);
    private static final Font  KEY_F    = new Font("Tahoma", Font.BOLD, 12);

    public SettingPage(Font tFont, Font bFont) {
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 24, 0));
        JLabel title = new JLabel("ตั้งค่าระบบ", JLabel.LEFT);
        title.setFont(TITLE_F);
        title.setForeground(TEXT_HI);
        JPanel divider = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, ACCENT, getWidth()*0.6f, 0, new Color(0,0,0,0)));
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

    private JScrollPane buildCenter() {
        initializeKeyData();

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(8, 0, 8, 0);
        g.weightx = 1.0;

        // ── ระดับแสง ──────────────────────────────────────────
        g.gridy = 0;
        JSlider brightSlider = makeSlider(0, 200, 100, ACCENT);
        JLabel  brightVal    = makeValueLabel("50%");
        brightSlider.addChangeListener(e -> {
            int v = brightSlider.getValue();
            brightVal.setText((v / 2) + "%");
            updateBrightness(v);
        });
        grid.add(makeSettingRow("ระดับแสง", brightSlider, brightVal), g);

        // ── ระดับเสียง (✅ แก้ delay) ────────────────────────
        g.gridy = 1;
        JSlider volSlider = makeSlider(0, 100, 50, ACCENT2);
        JLabel  volVal    = makeValueLabel("50%");
        volSlider.addChangeListener(e -> {
            int v = volSlider.getValue();
            volVal.setText(v + "%");
            // ✅ อัปเดตทันทีไม่ throttle
            Main.soundManager.setVolume(v / 100f);
        });
        grid.add(makeSettingRow("ระดับเสียง", volSlider, volVal), g);

        // ── ขนาดหน้าจอ ────────────────────────────────────────
        g.gridy = 2;
        JComboBox<String> resBox = makeCombo("1920x1080", "1280x720", "800x600");
        resBox.addActionListener(e -> {
            String r = (String) resBox.getSelectedItem();
            if ("800x600".equals(r))       Main.mainFrame.setSize(800, 600);
            else if ("1280x720".equals(r)) Main.mainFrame.setSize(1280, 720);
            else                           Main.mainFrame.setSize(1920, 1080);
            Main.mainFrame.setLocationRelativeTo(null);
        });
        grid.add(makeComboRow("ขนาดหน้าจอ", resBox), g);

        // ── รูปแบบหน้าจอ ──────────────────────────────────────
        g.gridy = 3;
        JComboBox<String> modeBox = makeCombo("Windowed", "Borderless", "Full Screen");
        modeBox.addActionListener(e -> {
            String mode = (String) modeBox.getSelectedItem();
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            Main.mainFrame.dispose();
            if ("Full Screen".equals(mode)) {
                Main.mainFrame.setUndecorated(true);
                gd.setFullScreenWindow(Main.mainFrame);
            } else {
                gd.setFullScreenWindow(null);
                Main.mainFrame.setUndecorated("Borderless".equals(mode));
                if ("Borderless".equals(mode)) Main.mainFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
                else Main.mainFrame.setSize(1920, 1080);
                Main.mainFrame.setLocationRelativeTo(null);
                Main.mainFrame.setVisible(true);
            }
        });
        grid.add(makeComboRow("รูปแบบหน้าจอ", modeBox), g);

        // ── ปุ่มควบคุม ────────────────────────────────────────
        g.gridy = 4;
        grid.add(makeKeyBindingCard(), g);

        // ✅ Custom scrollbar สวยงาม
        JScrollPane sp = new JScrollPane(grid);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor      = new Color(220, 70, 150, 180);
                trackColor      = new Color(245, 220, 238);
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                if (r.isEmpty()) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // glow
                g2.setColor(new Color(220, 70, 150, 40));
                g2.fill(new RoundRectangle2D.Float(r.x - 2, r.y - 2, r.width + 4, r.height + 4, 12, 12));
                // thumb gradient
                g2.setPaint(new GradientPaint(r.x, r.y, new Color(240, 100, 180), r.x, r.y + r.height, new Color(200, 50, 130)));
                g2.fill(new RoundRectangle2D.Float(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 10, 10));
                // highlight
                g2.setColor(new Color(255, 255, 255, 80));
                g2.fill(new RoundRectangle2D.Float(r.x + 3, r.y + 3, r.width - 6, (r.height - 6) / 2, 8, 8));
                g2.dispose();
            }
            @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(240, 218, 235));
                g2.fill(new RoundRectangle2D.Float(r.x + 3, r.y, r.width - 6, r.height, 6, 6));
                g2.setColor(new Color(220, 180, 210, 80));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(r.x + 3, r.y, r.width - 7, r.height - 1, 6, 6));
                g2.dispose();
            }
            private JButton zeroBtn() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                b.setMinimumSize(new Dimension(0, 0));
                b.setMaximumSize(new Dimension(0, 0));
                return b;
            }
        });
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        return sp;
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 0, 0, 0));
        JButton back = makePrimaryBtn("ย้อนกลับ", ACCENT);
        back.addActionListener(e -> Main.cardLayout.show(Main.mainContainer, "MENU"));
        p.add(back);
        return p;
    }

    private JPanel makeSettingRow(String labelText, JSlider slider, JLabel valLabel) {
        JPanel card = makeCard();
        card.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(12, 16, 12, 16);
        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(LABEL_F); lbl.setForeground(TEXT_HI);
        lbl.setPreferredSize(new Dimension(160, 24));
        card.add(lbl, g);
        g.gridx = 1; g.weightx = 1.0; g.fill = GridBagConstraints.HORIZONTAL;
        card.add(slider, g);
        g.gridx = 2; g.weightx = 0; g.fill = GridBagConstraints.NONE;
        card.add(valLabel, g);
        return card;
    }

    private JPanel makeComboRow(String labelText, JComboBox<String> combo) {
        JPanel card = makeCard();
        card.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(12, 16, 12, 16);
        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(LABEL_F); lbl.setForeground(TEXT_HI);
        lbl.setPreferredSize(new Dimension(160, 24));
        card.add(lbl, g);
        g.gridx = 1; g.weightx = 1.0; g.fill = GridBagConstraints.HORIZONTAL;
        card.add(combo, g);
        return card;
    }

    private JPanel makeKeyBindingCard() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout(0, 0));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(14, 16, 10, 16));
        JLabel title = new JLabel("ปุ่มควบคุม");
        title.setFont(LABEL_F); title.setForeground(TEXT_HI);
        header.add(title, BorderLayout.WEST);

        JButton resetBtn = new JButton("คืนค่าเริ่มต้น") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(200,40,60) : new Color(180,30,50));
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),8,8));
                g2.dispose(); super.paintComponent(g);
            }
        };
        resetBtn.setFont(new Font("Tahoma", Font.BOLD, 11));
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setOpaque(false); resetBtn.setContentAreaFilled(false);
        resetBtn.setBorderPainted(false); resetBtn.setFocusPainted(false);
        resetBtn.setBorder(new EmptyBorder(5, 12, 5, 12));
        resetBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        resetBtn.addActionListener(e -> resetKeyBindings());
        header.add(resetBtn, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        JPanel keyGrid = new JPanel(new GridBagLayout());
        keyGrid.setOpaque(false);
        keyGrid.setBorder(new EmptyBorder(0, 16, 14, 16));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 0, 4, 0);
        g.fill = GridBagConstraints.HORIZONTAL;
        for (int i = 0; i < keyNames.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 1.0;
            JLabel desc = new JLabel(keyDescriptions[i]);
            desc.setFont(new Font("Tahoma", Font.PLAIN, 13));
            desc.setForeground(TEXT_LO);
            keyGrid.add(desc, g);
            g.gridx = 1; g.weightx = 0;
            JButton btn = createKeyButton(keyNames[i]);
            keyButtons[i] = btn;
            keyGrid.add(btn, g);
        }
        card.add(keyGrid, BorderLayout.CENTER);
        return card;
    }

    private JPanel makeCard() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(200, 150, 190, 40));
                g2.fill(new RoundRectangle2D.Float(3, 4, getWidth()-2, getHeight()-2, 16, 16));
                g2.setColor(CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-3, getHeight()-4, 16, 16));
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-4, getHeight()-5, 16, 16));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    private JSlider makeSlider(int min, int max, int val, Color trackColor) {
        JSlider s = new JSlider(min, max, val) {
            @Override public void updateUI() {
                setUI(new BasicSliderUI(this) {
                    @Override public void paintTrack(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        Rectangle t = trackRect;
                        int mid = t.y + t.height / 2;
                        g2.setColor(new Color(220, 190, 210));
                        g2.fill(new RoundRectangle2D.Float(t.x, mid-3, t.width, 6, 6, 6));
                        int filled = thumbRect.x - t.x + thumbRect.width / 2;
                        if (filled > 0) {
                            g2.setColor(trackColor);
                            g2.fill(new RoundRectangle2D.Float(t.x, mid-3, filled, 6, 6, 6));
                        }
                        g2.dispose();
                    }
                    @Override public void paintThumb(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        int cx = thumbRect.x + thumbRect.width / 2;
                        int cy = thumbRect.y + thumbRect.height / 2;
                        // shadow
                        g2.setColor(new Color(0, 0, 0, 25));
                        g2.fillOval(cx - 10, cy - 8, 20, 20);
                        // outer circle
                        g2.setColor(trackColor);
                        g2.fillOval(cx - 10, cy - 10, 20, 20);
                        // inner white dot
                        g2.setColor(new Color(255, 255, 255, 220));
                        g2.fillOval(cx - 5, cy - 5, 10, 10);
                        g2.dispose();
                    }
                    // ✅ ไม่วาด focus outline
                    @Override public void paintFocus(Graphics g) {}
                });
            }
        };
        s.setOpaque(false);
        s.setPreferredSize(new Dimension(220, 36));
        return s;
    }

    private JLabel makeValueLabel(String text) {
        JLabel l = new JLabel(text, JLabel.RIGHT);
        l.setFont(VALUE_F); l.setForeground(GOLD);
        l.setPreferredSize(new Dimension(52, 24));
        return l;
    }

    private JComboBox<String> makeCombo(String... items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(VALUE_F);
        c.setBackground(new Color(255, 240, 250));
        c.setForeground(TEXT_HI);
        c.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        c.setFocusable(false);
        return c;
    }

    private JButton makePrimaryBtn(String text, Color bg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(new Font("Tahoma", Font.BOLD, 16));
        b.setForeground(Color.WHITE);
        b.setOpaque(false); b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(12, 40, 12, 40));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void initializeKeyData() {
        keyNames = new String[]{"NEXT_MSG","CHOICE_1","CHOICE_2","CHOICE_3","RELATION_UI","ESCAPE"};
        keyDescriptions = new String[]{
            "ข้ามบทสนทนา / ต่อไป",
            "เลือกตัวเลือกที่ 1",
            "เลือกตัวเลือกที่ 2",
            "เลือกตัวเลือกที่ 3",
            "ดูสถานะความสัมพันธ์",
            "กลับเมนู / ปิดหน้าต่าง"
        };
        keyButtons = new JButton[keyNames.length];
    }

    private JButton createKeyButton(String keyName) {
        int currentKey = KeyConfig.getKey(keyName);
        JButton btn = new JButton(KeyConfig.getKeyText(currentKey)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = (Color) getClientProperty("bgColor");
                if (bg == null) bg = new Color(240, 220, 235);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 10, 10));
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.putClientProperty("bgColor", new Color(240, 220, 235));
        btn.setFont(KEY_F); btn.setForeground(TEXT_HI);
        btn.setOpaque(false); btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(90, 30));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!isListening) { btn.putClientProperty("bgColor", ACCENT); btn.setForeground(Color.WHITE); btn.repaint(); }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!isListening) { btn.putClientProperty("bgColor", new Color(240,220,235)); btn.setForeground(TEXT_HI); btn.repaint(); }
            }
        });
        btn.addActionListener(e -> { if (!isListening) startKeyListening(keyName, btn); });
        return btn;
    }

    private void startKeyListening(String keyName, JButton btn) {
        isListening = true;
        listeningButton = btn;
        btn.setText("[ กดปุ่ม... ]");
        btn.putClientProperty("bgColor", new Color(255, 200, 80)); btn.repaint();
        btn.setForeground(Color.BLACK);
        KeyListener tmp = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                int newKey = e.getKeyCode();
                if (KeyConfig.isDuplicate(newKey, keyName)) {
                    btn.setText("ซ้ำ!");
                    btn.putClientProperty("bgColor", new Color(200, 50, 50)); btn.repaint();
                    btn.setForeground(Color.WHITE);
                    new Timer(1000, ev -> {
                        btn.setText(KeyConfig.getKeyText(KeyConfig.getKey(keyName)));
                        btn.putClientProperty("bgColor", new Color(240,220,235)); btn.repaint();
                        btn.setForeground(TEXT_HI); stopKeyListening();
                        ((Timer)ev.getSource()).stop();
                    }).start();
                    return;
                }
                KeyConfig.setKey(keyName, newKey);
                btn.setText(KeyConfig.getKeyText(newKey));
                btn.putClientProperty("bgColor", new Color(40, 180, 100)); btn.repaint();
                btn.setForeground(Color.WHITE);
                new Timer(500, ev -> {
                    btn.putClientProperty("bgColor", new Color(240,220,235)); btn.repaint();
                    btn.setForeground(TEXT_HI); stopKeyListening();
                    ((Timer)ev.getSource()).stop();
                }).start();
            }
        };
        Main.mainFrame.addKeyListener(tmp);
        Main.mainFrame.setFocusable(true);
        Main.mainFrame.requestFocus();
        btn.putClientProperty("tempListener", tmp);
    }

    private void stopKeyListening() {
        if (isListening && listeningButton != null) {
            KeyListener tmp = (KeyListener) listeningButton.getClientProperty("tempListener");
            if (tmp != null) Main.mainFrame.removeKeyListener(tmp);
            isListening = false; listeningButton = null;
        }
    }

    private void resetKeyBindings() {
        KeyConfig.resetToDefaults();
        for (int i = 0; i < keyNames.length; i++) {
            if (keyButtons[i] != null) {
                keyButtons[i].setText(KeyConfig.getKeyText(KeyConfig.getKey(keyNames[i])));
                keyButtons[i].putClientProperty("bgColor", new Color(240,220,235));
                keyButtons[i].setForeground(TEXT_HI);
                keyButtons[i].repaint();
            }
        }
        JOptionPane.showMessageDialog(this, "คืนค่าปุ่มควบคุมเป็นค่าเริ่มต้นเรียบร้อยแล้ว",
            "รีเซ็ตสำเร็จ", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateBrightness(int val) {
        if (val < 100) { Main.overlayColor = Color.BLACK; Main.brightnessAlpha = (100 - val) / 125.0f; }
        else { Main.overlayColor = Color.WHITE; Main.brightnessAlpha = (val - 100) / 250.0f; }
        Main.repaintBrightness();
    }
}