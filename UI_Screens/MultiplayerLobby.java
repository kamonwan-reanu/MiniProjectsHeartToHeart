package UI_Screens;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import model.GameClient;
import model.GameServer;
import model.GameConstants;
import model.Relation;
import model.StoryData;
import core.Main;

public class MultiplayerLobby extends JPanel {

    private GameServer server;
    private GameClient client;
    private boolean isHost = false;

    // Panels
    private JPanel mainPanel, hostPanel, joinPanel, waitingPanel, leaderboardPanel;

    // Host UI
    private JLabel hostIPLabel, playerCountLabel;
    private JPanel playerListPanel;
    private JButton startGameBtn;

    // Join UI
    private JTextField ipField, nameField;
    private JButton connectBtn;
    private JLabel joinStatusLabel;

    // Waiting (Client) UI
    private JLabel waitingStatusLabel, waitingCountLabel;
    private JPanel waitingPlayerListPanel;

    // ชื่อผู้เล่นในห้อง
    private List<String> playerNames = new ArrayList<>();

    private JLabel hostDisplay;

    // ✨ ชื่อ Host — set ตั้งแต่ฝั่ง Host สร้างห้อง ส่งมาถึง Client ผ่าน PLAYER_LIST
    private String hostName = "Host";

    // Colors & Fonts
    private static final Color PINK       = new Color(255, 105, 180);
    private static final Color LIGHT_PINK = new Color(255, 230, 240);
    private static final Color BLUE       = new Color(100, 180, 255);
    private static final Color GREEN      = new Color(60, 170, 60);
    private static final Color WHITE      = Color.WHITE;
    private static final Font F30B = new Font("Tahoma", Font.BOLD, 30);
    private static final Font F24B = new Font("Tahoma", Font.BOLD, 24);
    private static final Font F18B = new Font("Tahoma", Font.BOLD, 18);
    private static final Font F16  = new Font("Tahoma", Font.PLAIN, 16);
    private static final Font F14  = new Font("Tahoma", Font.PLAIN, 14);

    public MultiplayerLobby() {
        setLayout(new CardLayout());
        setBackground(LIGHT_PINK);
        buildMainPanel();
        buildHostPanel();
        buildJoinPanel();
        buildWaitingPanel();
        buildLeaderboardPanel();
        add(mainPanel,        "MAIN");
        add(hostPanel,        "HOST");
        add(joinPanel,        "JOIN");
        add(waitingPanel,     "WAITING");
        add(leaderboardPanel, "LEADERBOARD");
    }

    // -------------------------------------------------------
    // หน้าหลัก — null layout เพื่อรองรับ overlay
    // -------------------------------------------------------
    private void buildMainPanel() {
        mainPanel = new JPanel(null) {
            @Override public void doLayout() {
                for (Component c : getComponents()) c.setBounds(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBackground(LIGHT_PINK);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(LIGHT_PINK);

        JPanel box = roundBox(460, 370);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(38, 50, 38, 50));

        JLabel title = lbl("[ เล่นหลายคน ]", F30B, PINK);
        JLabel sub   = lbl("แข่งกันว่าใครจีบได้คะแนนสูงกว่า!", F16, new Color(150,150,150));

        JButton hostBtn = mkBtn("สร้างห้อง  (Host)", PINK);
        hostBtn.addActionListener(e -> showNameOverlay(content));

        JButton joinBtn = mkBtn("เข้าร่วมห้อง  (Join)", BLUE);
        joinBtn.addActionListener(e -> goJoin());

        JButton backBtn = mkOutline("< กลับเมนูหลัก");
        backBtn.addActionListener(e -> { resetAll(); Main.cardLayout.show(Main.mainContainer,"MENU"); });

        box.add(title); box.add(gap(5)); box.add(sub);
        box.add(gap(28)); box.add(hostBtn);
        box.add(gap(13)); box.add(joinBtn);
        box.add(gap(17)); box.add(backBtn);
        content.add(box);
        mainPanel.add(content);
    }

    // -------------------------------------------------------
    // ✨ FIX #1: Overlay ถามชื่อ — ลงบน mainPanel โดยตรง
    //    ไม่มีพื้นดำ ไม่ตกขอบ ไม่ใช้ JDialog
    // -------------------------------------------------------
    private void showNameOverlay(JPanel content) {
        JPanel dialogBox = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 150, 200, 60));
                g2.fill(new RoundRectangle2D.Float(5, 5, getWidth()-4, getHeight()-4, 36, 36));
                g2.setColor(WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-5, getHeight()-5, 36, 36));
                g2.setColor(PINK);
                g2.setStroke(new BasicStroke(2.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-7, getHeight()-7, 36, 36));
                g2.dispose();
            }
        };
        dialogBox.setLayout(new BoxLayout(dialogBox, BoxLayout.Y_AXIS));
        dialogBox.setOpaque(false);
        dialogBox.setBorder(new EmptyBorder(28, 36, 28, 36));
        dialogBox.setPreferredSize(new Dimension(360, 248));

        JLabel icon  = lbl("💖", new Font("Segoe UI Emoji", Font.PLAIN, 34), PINK);
        JLabel title = lbl("ใส่ชื่อของคุณ", F24B, PINK);
        JLabel sub   = lbl("ชื่อจะแสดงให้ผู้เล่นอื่นเห็น", F14, new Color(180, 180, 180));

        JTextField nameInput = styledField(PINK);
        String def = (GameConstants.PLAYER_NAME != null && !GameConstants.PLAYER_NAME.isEmpty())
                      ? GameConstants.PLAYER_NAME : "Host";
        nameInput.setText(def);
        nameInput.selectAll();

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(CENTER_ALIGNMENT);

        JButton cancelBtn = mkOutlineSmall("ยกเลิก");
        JButton okBtn     = mkBtnSmall("✔  เริ่มเลย!", PINK);

        JPanel overlay = new JPanel(new GridBagLayout()) {
            @Override public boolean isOpaque() { return false; }
            @Override protected void paintComponent(Graphics g) { /* โปร่งใสสนิท */ }
        };

        Runnable doOk = () -> {
            String input = nameInput.getText().trim();
            if (input.isEmpty()) {
                nameInput.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.RED, 2),
                    BorderFactory.createEmptyBorder(8,10,8,10)));
                return;
            }
            GameConstants.PLAYER_NAME = input;
            mainPanel.remove(overlay);
            mainPanel.revalidate(); mainPanel.repaint();
            startHosting();
        };

        cancelBtn.addActionListener(e -> {
            mainPanel.remove(overlay);
            mainPanel.revalidate(); mainPanel.repaint();
        });
        okBtn.addActionListener(e -> doOk.run());
        nameInput.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)  doOk.run();
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    mainPanel.remove(overlay);
                    mainPanel.revalidate(); mainPanel.repaint();
                }
            }
        });

        btnRow.add(cancelBtn); btnRow.add(okBtn);

        dialogBox.add(icon);      dialogBox.add(gap(2));
        dialogBox.add(title);     dialogBox.add(gap(4));
        dialogBox.add(sub);       dialogBox.add(gap(18));
        dialogBox.add(nameInput); dialogBox.add(gap(18));
        dialogBox.add(btnRow);

        overlay.add(dialogBox);
        mainPanel.add(overlay);
        mainPanel.setComponentZOrder(overlay, 0);
        mainPanel.revalidate(); mainPanel.repaint();
        SwingUtilities.invokeLater(nameInput::requestFocusInWindow);
    }

    // -------------------------------------------------------
    // หน้า Host — ✨ FIX #2: ปุ่มคัดลอก style ใหม่
    // -------------------------------------------------------
    private void buildHostPanel() {
        hostPanel = new JPanel(new GridBagLayout());
        hostPanel.setBackground(LIGHT_PINK);

        JPanel box = roundBox(500, 580);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel title = lbl("ตั้งค่าห้องของคุณ", F30B, PINK);
        hostDisplay = lbl("หัวห้อง: ", F18B, PINK);

        // ✨ IP card พร้อมปุ่มคัดลอก style ใหม่
        JPanel ipCard = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(238, 255, 238));
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),20,20));
                g2.setColor(new Color(170, 215, 170));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,20,20));
                g2.dispose();
            }
        };
        ipCard.setLayout(new BorderLayout(10, 0));
        ipCard.setOpaque(false);
        ipCard.setBorder(new EmptyBorder(10, 16, 10, 12));
        ipCard.setMaximumSize(new Dimension(420, 54));
        ipCard.setAlignmentX(CENTER_ALIGNMENT);

        hostIPLabel = new JLabel("กำลังเริ่ม...");
        hostIPLabel.setFont(new Font("Consolas", Font.BOLD, 18));
        hostIPLabel.setForeground(new Color(25, 130, 25));

        JButton copyBtn = new JButton("📋 คัดลอก") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed()  ? new Color(100, 175, 100)
                         : getModel().isRollover() ? new Color(80, 165, 80)
                         : new Color(110, 185, 110);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        copyBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
        copyBtn.setForeground(WHITE);
        copyBtn.setOpaque(false);
        copyBtn.setContentAreaFilled(false);
        copyBtn.setBorderPainted(false);
        copyBtn.setFocusPainted(false);
        copyBtn.setBorder(new EmptyBorder(6, 12, 6, 12));
        copyBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        copyBtn.setPreferredSize(new Dimension(112, 34));
        copyBtn.addActionListener(e -> {
            java.awt.datatransfer.StringSelection ss =
                new java.awt.datatransfer.StringSelection(hostIPLabel.getText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
            copyBtn.setText("✔ คัดลอกแล้ว");
            new Timer(2000, ev -> { copyBtn.setText("📋 คัดลอก"); ((Timer)ev.getSource()).stop(); }).start();
        });

        ipCard.add(hostIPLabel, BorderLayout.CENTER);
        ipCard.add(copyBtn, BorderLayout.EAST);

        playerCountLabel = lbl("ผู้เล่น: 1 / 4", F18B, new Color(120,120,120));

        playerListPanel = new JPanel();
        playerListPanel.setLayout(new BoxLayout(playerListPanel, BoxLayout.Y_AXIS));
        playerListPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(playerListPanel);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(255,182,193), 2, true));
        scroll.setOpaque(false); scroll.getViewport().setOpaque(false);
        scroll.setPreferredSize(new Dimension(420, 180));

        startGameBtn = mkBtn("เริ่มเกม!", PINK);
        startGameBtn.setEnabled(false);
        startGameBtn.addActionListener(e -> startGameAsHost());

        JButton cancelBtn = mkOutline("X  ปิดห้อง");
        cancelBtn.addActionListener(e -> { resetAll(); goMain(); });

        box.add(title);        box.add(gap(10));
        box.add(hostDisplay);  box.add(gap(20));
        box.add(smallLbl("แชร์ IP นี้ให้เพื่อนเชื่อมต่อ:")); box.add(gap(8));
        box.add(ipCard);       box.add(gap(20));
        box.add(playerCountLabel); box.add(gap(8));
        box.add(scroll);       box.add(gap(20));
        box.add(startGameBtn); box.add(gap(10));
        box.add(cancelBtn);
        hostPanel.add(box);
    }

    // -------------------------------------------------------
    // หน้า Join
    // -------------------------------------------------------
    private void buildJoinPanel() {
        joinPanel = new JPanel(new GridBagLayout());
        joinPanel.setBackground(LIGHT_PINK);
        JPanel box = roundBox(490, 420);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(34, 48, 34, 48));

        JLabel title = lbl("เข้าร่วมห้อง", F30B, BLUE);
        nameField = styledField(PINK);
        ipField   = styledField(BLUE);

        KeyAdapter enter = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) connectBtn.doClick();
            }
        };
        nameField.addKeyListener(enter);
        ipField.addKeyListener(enter);

        joinStatusLabel = lbl(" ", F18B, Color.GRAY);
        connectBtn = mkBtn("เชื่อมต่อ", BLUE);
        connectBtn.addActionListener(e -> connectToHost());
        JButton backBtn = mkOutline("< กลับ");
        backBtn.addActionListener(e -> goMain());

        box.add(title);
        box.add(gap(20)); box.add(smallLbl("ชื่อผู้เล่นของคุณ:")); box.add(gap(5)); box.add(nameField);
        box.add(gap(16)); box.add(smallLbl("IP:Port ของ Host  (เช่น 26.x.x.x:45621):")); box.add(gap(5)); box.add(ipField);
        box.add(gap(10)); box.add(joinStatusLabel); box.add(gap(6));
        box.add(connectBtn); box.add(gap(10)); box.add(backBtn);
        joinPanel.add(box);
    }

    // -------------------------------------------------------
    // หน้า Waiting — Client รอ Host
    // -------------------------------------------------------
    private void buildWaitingPanel() {
        waitingPanel = new JPanel(new GridBagLayout());
        waitingPanel.setBackground(LIGHT_PINK);
        JPanel box = roundBox(520, 480);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(28, 40, 28, 40));

        JLabel title = lbl("ล็อบบี้", F30B, BLUE);
        waitingStatusLabel = lbl("เชื่อมต่อสำเร็จ! รอ Host เริ่มเกม...", F18B, GREEN);
        waitingCountLabel  = lbl("ผู้เล่น: 1 / ?", F18B, new Color(100,100,100));

        waitingPlayerListPanel = new JPanel();
        waitingPlayerListPanel.setLayout(new BoxLayout(waitingPlayerListPanel, BoxLayout.Y_AXIS));
        waitingPlayerListPanel.setOpaque(false);
        JScrollPane scrollW = new JScrollPane(waitingPlayerListPanel);
        scrollW.setBorder(BorderFactory.createLineBorder(new Color(150,200,255),1));
        scrollW.setOpaque(false); scrollW.getViewport().setOpaque(false);
        scrollW.setMaximumSize(new Dimension(440, 220));
        scrollW.setAlignmentX(CENTER_ALIGNMENT);

        JLabel hint = lbl("รอ Host กดเริ่มเกม...", F16, new Color(150,150,150));

        JButton leaveBtn = mkOutline("< ออกจากห้อง");
        leaveBtn.addActionListener(e -> {
            if (client != null) { client.disconnect(); client = null; }
            playerNames.clear();
            goMain();
        });

        box.add(title);              box.add(gap(10));
        box.add(waitingStatusLabel); box.add(gap(6));
        box.add(waitingCountLabel);  box.add(gap(8));
        box.add(smallLbl("ผู้เล่นในห้อง:")); box.add(gap(4));
        box.add(scrollW);            box.add(gap(12));
        box.add(hint);               box.add(gap(10));
        box.add(leaveBtn);
        waitingPanel.add(box);
    }

    // -------------------------------------------------------
    // Leaderboard
    // -------------------------------------------------------
    private void buildLeaderboardPanel() {
        leaderboardPanel = new JPanel(new GridBagLayout());
        leaderboardPanel.setBackground(LIGHT_PINK);
    }

    public void showLeaderboard(Map<String, Integer> scores) {
        leaderboardPanel.removeAll();
        JPanel box = roundBox(520, 500);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(26, 42, 26, 42));

        box.add(lbl("ผลการแข่งขัน", F30B, new Color(190,130,0)));
        box.add(gap(16));

        List<Map.Entry<String,Integer>> sorted = new ArrayList<>(scores.entrySet());
        sorted.sort((a,b) -> b.getValue() - a.getValue());
        String[] ranks = {"1.", "2.", "3.", "4."};
        Color[]  cols  = {new Color(190,140,0), new Color(120,120,120), new Color(150,90,40), new Color(90,90,90)};

        for (int i = 0; i < sorted.size(); i++) {
            String rank = i < ranks.length ? ranks[i] : (i+1)+".";
            JPanel row = new JPanel(new BorderLayout(10,0));
            row.setMaximumSize(new Dimension(430,55));
            row.setBackground(i==0 ? new Color(255,250,215) : new Color(255,246,250));
            row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255,200,220),1),
                BorderFactory.createEmptyBorder(9,12,9,12)));
            JLabel nl = new JLabel(rank+"  "+sorted.get(i).getKey()); nl.setFont(F24B);
            nl.setForeground(i==0 ? new Color(150,95,0) : Color.DARK_GRAY);
            JLabel sl = new JLabel(sorted.get(i).getValue()+" คะแนน"); sl.setFont(F24B);
            sl.setForeground(i < cols.length ? cols[i] : cols[3]);
            sl.setHorizontalAlignment(SwingConstants.RIGHT);
            row.add(nl, BorderLayout.WEST); row.add(sl, BorderLayout.EAST);
            row.setAlignmentX(CENTER_ALIGNMENT);
            box.add(row); box.add(gap(7));
        }

        box.add(gap(8));
        JButton menuBtn = mkBtn("กลับหน้าหลัก", PINK);
        menuBtn.addActionListener(e -> { resetAll(); Main.cardLayout.show(Main.mainContainer,"MENU"); });
        JButton againBtn = mkOutline("เล่นอีกครั้ง");
        againBtn.addActionListener(e -> { resetAll(); goMain(); });
        box.add(menuBtn); box.add(gap(10)); box.add(againBtn);

        leaderboardPanel.add(box);
        leaderboardPanel.revalidate(); leaderboardPanel.repaint();
        show("LEADERBOARD");
    }

    // -------------------------------------------------------
    // Logic: Host
    // -------------------------------------------------------
    private void startHosting() {
        String finalName = GameConstants.PLAYER_NAME;
        if (finalName == null || finalName.trim().isEmpty()) finalName = "Host";

        hostName = finalName;
        resetAll();
        isHost = true;

        hostDisplay.setText("หัวห้อง: " + finalName);
        playerNames.clear();
        playerNames.add(finalName);
        refreshPlayerList();
        playerCountLabel.setText("ผู้เล่น: 1 / 4");
        show("HOST");

        server = new GameServer(GameServer.DEFAULT_PORT);
        server.setHostName(finalName); // ✨ บอก Server ว่าใครคือ Host (index 0)

        server.setListener(new GameServer.ServerListener() {
            @Override public void onServerStarted(String ip, int port) {
                SwingUtilities.invokeLater(() -> hostIPLabel.setText(ip + ":" + port));
            }
            @Override public void onPlayerJoined(String pName, int total) {
                SwingUtilities.invokeLater(() -> {
                    if (!playerNames.contains(pName)) playerNames.add(pName);
                    playerCountLabel.setText("ผู้เล่น: " + total + " / 4");
                    refreshPlayerList();
                    if (playerNames.size() > 1) startGameBtn.setEnabled(true);
                    // ✨ GameServer จัดการ broadcast PLAYER_LIST ให้อัตโนมัติแล้ว
                });
            }
            @Override public void onPlayerLeft(String pName, int total) {
                SwingUtilities.invokeLater(() -> {
                    playerNames.remove(pName);
                    playerCountLabel.setText("ผู้เล่น: " + total + " / 4");
                    refreshPlayerList();
                    if (playerNames.size() <= 1) startGameBtn.setEnabled(false);
                });
            }
            @Override public void onScoreReceived(String name, int score) {}
            @Override public void onAllPlayersFinished(Map<String,Integer> finalScores) {
                finalScores.put(GameConstants.PLAYER_NAME, model.Relation.getInstance().getAffection("Ahri"));
                SwingUtilities.invokeLater(() -> showLeaderboard(finalScores));
            }
            @Override public void onServerError(String msg) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(MultiplayerLobby.this, msg, "ข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
                    resetAll(); goMain();
                });
            }
        });
        server.start();
    }

    // -------------------------------------------------------
    // Logic: Connect
    // -------------------------------------------------------
    private void connectToHost() {
        String name   = nameField.getText().trim();
        String ipPort = ipField.getText().trim();

        if (name.isEmpty())        { setJoinStatus("กรุณาใส่ชื่อของคุณ", Color.RED); return; }
        if (!ipPort.contains(":")) { setJoinStatus("รูปแบบควรเป็น IP:Port  เช่น 26.1.2.3:45621", Color.RED); return; }

        String[] parts = ipPort.split(":");
        String ip = parts[0].trim();
        int port;
        try { port = Integer.parseInt(parts[1].trim()); }
        catch (NumberFormatException ex) { setJoinStatus("Port ไม่ถูกต้อง", Color.RED); return; }

        GameConstants.PLAYER_NAME = name;
        model.Relation.getInstance().resetAll();
        connectBtn.setEnabled(false);
        setJoinStatus("กำลังเชื่อมต่อ...", new Color(80,80,200));

        if (client != null) { client.disconnect(); client = null; }

        client = new GameClient(name);
        client.setListener(new GameClient.ClientListener() {

            @Override public void onConnected(String playerName) {
                SwingUtilities.invokeLater(() -> {
                    // รอ onPlayerListReceived มาแทน — ไม่ต้อง add เอง
                    playerNames.clear();
                    playerNames.add(playerName);
                    refreshWaitingList();
                    show("WAITING");
                });
            }

            // ✨ FIX #3: รับรายชื่อจาก Server — index 0 = Host เสมอ
            @Override public void onPlayerListReceived(List<String> names) {
                SwingUtilities.invokeLater(() -> {
                    playerNames.clear();
                    playerNames.addAll(names);
                    if (!names.isEmpty()) hostName = names.get(0); // Host อยู่ index 0
                    waitingCountLabel.setText("ผู้เล่น: " + playerNames.size() + " / ?");
                    refreshWaitingList();
                });
            }

            @Override public void onGameStart(int readSeconds) {
                SwingUtilities.invokeLater(() -> loadGameAndPlay(false, readSeconds));
            }
            @Override public void onPlayerJoined(String pName, int total) {
                SwingUtilities.invokeLater(() -> {
                    // PLAYER_LIST จะมาทีหลังเสมอ ไม่ต้องจัดการที่นี่
                });
            }
            @Override public void onPlayerLeft(String pName, int total) {
                SwingUtilities.invokeLater(() -> {
                    // PLAYER_LIST จะมาทีหลังเสมอ ไม่ต้องจัดการที่นี่
                });
            }
            @Override public void onScoreUpdate(String pName, int score) {}
            @Override public void onLeaderboard(Map<String,Integer> scores) {
                SwingUtilities.invokeLater(() -> showLeaderboard(scores));
            }
            @Override public void onChatMessage(String sender, String message) {}
            @Override public void onDisconnected(String reason) {
                SwingUtilities.invokeLater(() -> {
                    setJoinStatus("ตัดการเชื่อมต่อ: "+reason, Color.RED);
                    connectBtn.setEnabled(true); connectBtn.setText("เชื่อมต่อ");
                    goJoin();
                });
            }
            @Override public void onError(String message) {
                SwingUtilities.invokeLater(() -> {
                    setJoinStatus(message, Color.RED);
                    connectBtn.setEnabled(true); connectBtn.setText("เชื่อมต่อ");
                });
            }
        });
        client.connect(ip, port);
    }

    // -------------------------------------------------------
    // เริ่มเกม
    // -------------------------------------------------------
    private void startGameAsHost() {
        server.lockRoom();
        int readSec = 30;
        server.setExpectedPlayers(server.getPlayerCount() + 1);
        server.broadcast("START_GAME:" + readSec);
        loadGameAndPlay(true, readSec);
    }

    private void loadGameAndPlay(boolean asHost, int readSeconds) {
        model.Relation.getInstance().resetAll();
        UI_Components.RelationUI.getInstance().updateAllScores();

        for (Component comp : Main.mainContainer.getComponents()) {
            if (comp instanceof PlaySceneMain) {
                PlaySceneMain scene = (PlaySceneMain) comp;
                Object[][] mpScene = StoryData.SCENE_MP != null ? StoryData.SCENE_MP : StoryData.SCENE_2;
                scene.loadNewScene(mpScene, "SCENE_MP");
                scene.setMultiplayerMode(true, readSeconds,
                    asHost ? server : null,
                    asHost ? null   : client);
                scene.setMultiplayerEffectBypass(true);

                String myHostName = GameConstants.PLAYER_NAME.isEmpty() ? "Host" : GameConstants.PLAYER_NAME;
                if (asHost) {
                    scene.setOnGameFinished(() ->
                        server.receiveHostScore(myHostName, model.Relation.getInstance().getAffection("Ahri")));
                } else {
                    scene.setOnGameFinished(() -> {
                        if (client != null) client.sendScore(model.Relation.getInstance().getAffection("Ahri"));
                    });
                }
                break;
            }
        }
        Main.cardLayout.show(Main.mainContainer, "PLAY_SCENE");
    }

    // -------------------------------------------------------
    // reset ทุกอย่าง
    // -------------------------------------------------------
    private void resetAll() {
        if (server != null) { server.stop(); server = null; }
        if (client != null) { client.disconnect(); client = null; }
        isHost = false;
        playerNames.clear();
        hostName = "Host";
        for (Component comp : Main.mainContainer.getComponents()) {
            if (comp instanceof PlaySceneMain) {
                ((PlaySceneMain)comp).setMultiplayerMode(false, 30, null, null);
                ((PlaySceneMain)comp).setMultiplayerEffectBypass(false);
                break;
            }
        }
    }

    // -------------------------------------------------------
    // อัปเดตรายชื่อ
    // -------------------------------------------------------
    private void refreshPlayerList() {
        if (playerListPanel == null) return;
        playerListPanel.removeAll();
        String me = GameConstants.PLAYER_NAME;
        for (int i = 0; i < playerNames.size(); i++) {
            String n = playerNames.get(i);
            playerListPanel.add(playerCard(n, n.equals(me), i == 0));
            playerListPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        }
        playerListPanel.revalidate(); playerListPanel.repaint();
    }

    private void refreshWaitingList() {
        if (waitingPlayerListPanel == null) return;
        waitingPlayerListPanel.removeAll();
        String me = GameConstants.PLAYER_NAME;
        for (String n : playerNames) {
            boolean isHostPlayer = n.equals(hostName);
            waitingPlayerListPanel.add(playerCard(n, n.equals(me), isHostPlayer));
            waitingPlayerListPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        }
        waitingPlayerListPanel.revalidate();
        waitingPlayerListPanel.repaint();
    }

    private JPanel playerCard(String name, boolean isMe, boolean isHostCard) {
        JPanel card = new JPanel(new BorderLayout(8, 0));
        card.setMaximumSize(new Dimension(440, 46));
        Color borderColor = isHostCard ? PINK : BLUE;
        card.setBackground(isMe ? new Color(255,240,250) : WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, 2),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)));

        JLabel dot = new JLabel("●");
        dot.setFont(F18B);
        dot.setForeground(GREEN);

        String cleanName = name.replace("(Host)", "").replace("(คุณ)", "").trim();
        if (cleanName.isEmpty() && isHostCard) cleanName = "Host";
        String displayName = cleanName + (isHostCard ? " (Host)" : "") + (isMe ? " (คุณ)" : "");

        JLabel nameLbl = new JLabel(displayName);
        nameLbl.setFont(F18B);
        nameLbl.setForeground(isHostCard ? PINK : new Color(60,100,180));

        card.add(dot, BorderLayout.WEST);
        card.add(nameLbl, BorderLayout.CENTER);
        card.setAlignmentX(CENTER_ALIGNMENT);
        return card;
    }

    // -------------------------------------------------------
    // Navigation
    // -------------------------------------------------------
    private void goMain() { show("MAIN"); }
    private void goJoin() {
        nameField.setText(""); ipField.setText("");
        joinStatusLabel.setText(" ");
        connectBtn.setEnabled(true); connectBtn.setText("เชื่อมต่อ");
        show("JOIN");
    }
    private void show(String card) { ((CardLayout)getLayout()).show(this, card); }
    private void setJoinStatus(String msg, Color c) { joinStatusLabel.setText(msg); joinStatusLabel.setForeground(c); }

    // -------------------------------------------------------
    // UI Helpers
    // -------------------------------------------------------
    private JPanel roundBox(int w, int h) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),40,40));
                g2.setColor(PINK); g2.setStroke(new BasicStroke(2.5f));
                g2.draw(new RoundRectangle2D.Float(1,1,getWidth()-2,getHeight()-2,40,40));
                g2.dispose();
            }
        };
        p.setPreferredSize(new Dimension(w, h));
        return p;
    }
    private JButton mkBtn(String text, Color bg) {
        JButton b = new JButton(text); b.setFont(F18B); b.setBackground(bg); b.setForeground(WHITE);
        b.setBorder(BorderFactory.createEmptyBorder(11,24,11,24)); b.setFocusPainted(false);
        b.setAlignmentX(CENTER_ALIGNMENT); b.setMaximumSize(new Dimension(330,50));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
    private JButton mkBtnSmall(String text, Color bg) {
        JButton b = new JButton(text); b.setFont(F18B); b.setBackground(bg); b.setForeground(WHITE);
        b.setBorder(BorderFactory.createEmptyBorder(9,20,9,20)); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
    private JButton mkOutline(String text) {
        JButton b = new JButton(text); b.setFont(F18B); b.setBackground(WHITE); b.setForeground(PINK);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PINK,2), BorderFactory.createEmptyBorder(7,18,7,18)));
        b.setFocusPainted(false); b.setAlignmentX(CENTER_ALIGNMENT); b.setMaximumSize(new Dimension(330,48));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
    private JButton mkOutlineSmall(String text) {
        JButton b = new JButton(text); b.setFont(F18B); b.setBackground(WHITE); b.setForeground(new Color(150,150,150));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200,200,200),2), BorderFactory.createEmptyBorder(7,18,7,18)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
    private JLabel lbl(String text, Font font, Color color) {
        JLabel l = new JLabel(text, SwingConstants.CENTER); l.setFont(font); l.setForeground(color);
        l.setAlignmentX(CENTER_ALIGNMENT); return l;
    }
    private JLabel smallLbl(String text) {
        JLabel l = new JLabel(text); l.setFont(F18B); l.setForeground(new Color(120,120,120));
        l.setAlignmentX(CENTER_ALIGNMENT); return l;
    }
    private JTextField styledField(Color border) {
        JTextField f = new JTextField(""); f.setFont(new Font("Tahoma",Font.BOLD,18));
        f.setHorizontalAlignment(JTextField.CENTER);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(border,2), BorderFactory.createEmptyBorder(8,10,8,10)));
        f.setMaximumSize(new Dimension(380,48)); f.setAlignmentX(CENTER_ALIGNMENT); return f;
    }
    private Component gap(int h) { return Box.createRigidArea(new Dimension(0, h)); }
}