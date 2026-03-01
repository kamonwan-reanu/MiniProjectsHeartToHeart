package UI_Screens;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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

    // Waiting (Client) UI — FIX #1 + #5
    private JLabel waitingStatusLabel, waitingCountLabel;
    private JPanel waitingPlayerListPanel;

    // ชื่อผู้เล่นในห้อง
    private List<String> playerNames = new ArrayList<>();

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
    // หน้าหลัก
    // -------------------------------------------------------
    private void buildMainPanel() {
        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(LIGHT_PINK);
        JPanel box = roundBox(460, 370);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(38, 50, 38, 50));

        JLabel title = lbl("[ เล่นหลายคน ]", F30B, PINK);
        JLabel sub   = lbl("แข่งกันว่าใครจีบได้คะแนนสูงกว่า!", F16, new Color(150,150,150));

        JButton hostBtn = mkBtn("สร้างห้อง  (Host)", PINK);
        hostBtn.addActionListener(e -> startHosting());
        JButton joinBtn = mkBtn("เข้าร่วมห้อง  (Join)", BLUE);
        joinBtn.addActionListener(e -> goJoin());
        JButton backBtn = mkOutline("< กลับเมนูหลัก");
        backBtn.addActionListener(e -> { resetAll(); Main.cardLayout.show(Main.mainContainer,"MENU"); });

        box.add(title); box.add(gap(5)); box.add(sub);
        box.add(gap(28)); box.add(hostBtn);
        box.add(gap(13)); box.add(joinBtn);
        box.add(gap(17)); box.add(backBtn);
        mainPanel.add(box);
    }

    // -------------------------------------------------------
    // หน้า Host — แสดง IP + รายชื่อผู้เล่น card style (FIX #2)
    // -------------------------------------------------------
    private void buildHostPanel() {
        hostPanel = new JPanel(new GridBagLayout());
        hostPanel.setBackground(LIGHT_PINK);
        JPanel box = roundBox(580, 560);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(24, 34, 24, 34));

        JLabel title = lbl("ห้องของคุณ", F30B, PINK);

        // IP row
        JPanel ipRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        ipRow.setOpaque(false);
        ipRow.setMaximumSize(new Dimension(510, 52));
        hostIPLabel = new JLabel("กำลังเริ่ม...");
        hostIPLabel.setFont(new Font("Consolas", Font.BOLD, 20));
        hostIPLabel.setForeground(new Color(30, 140, 30));
        JButton copyBtn = new JButton("คัดลอก");
        copyBtn.setFont(F14); copyBtn.setBackground(new Color(215,255,215));
        copyBtn.setBorder(BorderFactory.createLineBorder(GREEN, 2));
        copyBtn.setFocusPainted(false);
        copyBtn.addActionListener(e -> {
            java.awt.datatransfer.StringSelection ss =
                new java.awt.datatransfer.StringSelection(hostIPLabel.getText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
            copyBtn.setText("คัดลอกแล้ว!");
            Timer t = new Timer(2000, ev -> copyBtn.setText("คัดลอก"));
            t.setRepeats(false); t.start();
        });
        ipRow.add(hostIPLabel); ipRow.add(copyBtn);

        playerCountLabel = lbl("ผู้เล่น: 1 / 4", F18B, new Color(100,100,100));

        // รายชื่อผู้เล่น (ไม่ใช่ log — FIX #2)
        playerListPanel = new JPanel();
        playerListPanel.setLayout(new BoxLayout(playerListPanel, BoxLayout.Y_AXIS));
        playerListPanel.setOpaque(false);
        JScrollPane scroll = new JScrollPane(playerListPanel);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(255,182,193),1));
        scroll.setOpaque(false); scroll.getViewport().setOpaque(false);
        scroll.setMaximumSize(new Dimension(500, 200));
        scroll.setAlignmentX(CENTER_ALIGNMENT);

        startGameBtn = mkBtn("เริ่มเกม!", PINK);
        startGameBtn.setEnabled(false);
        startGameBtn.addActionListener(e -> startGameAsHost());
        JButton cancelBtn = mkOutline("X  ปิดห้อง");
        cancelBtn.addActionListener(e -> { resetAll(); goMain(); });

        box.add(title); box.add(gap(10));
        box.add(smallLbl("แชร์ IP นี้ให้เพื่อนเชื่อมต่อ:")); box.add(gap(4));
        box.add(ipRow); box.add(gap(8));
        box.add(playerCountLabel); box.add(gap(6));
        box.add(smallLbl("ผู้เล่นในห้อง:")); box.add(gap(4));
        box.add(scroll); box.add(gap(14));
        box.add(startGameBtn); box.add(gap(8)); box.add(cancelBtn);
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
    // หน้า Waiting — Client รอ Host (FIX #1 + หน้า Lobby #5)
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

        box.add(title); box.add(gap(10));
        box.add(waitingStatusLabel); box.add(gap(6));
        box.add(waitingCountLabel); box.add(gap(8));
        box.add(smallLbl("ผู้เล่นในห้อง:")); box.add(gap(4));
        box.add(scrollW); box.add(gap(12));
        box.add(hint); box.add(gap(10)); box.add(leaveBtn);
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
        resetAll(); // FIX #3
        isHost = true;
        String hostName = GameConstants.PLAYER_NAME.isEmpty() ? "Host" : GameConstants.PLAYER_NAME;
        playerNames.clear();
        playerNames.add(hostName);
        refreshPlayerList();
        show("HOST");

        server = new GameServer(GameServer.DEFAULT_PORT);
        server.setListener(new GameServer.ServerListener() {
            @Override public void onServerStarted(String ip, int port) {
                SwingUtilities.invokeLater(() -> hostIPLabel.setText(ip+":"+port));
            }
            @Override public void onPlayerJoined(String name, int total) {
                SwingUtilities.invokeLater(() -> {
                    if (!playerNames.contains(name)) playerNames.add(name);
                    playerCountLabel.setText("ผู้เล่น: "+playerNames.size()+" / 4");
                    refreshPlayerList();
                    startGameBtn.setEnabled(true);
                });
            }
            @Override public void onPlayerLeft(String name, int total) {
                SwingUtilities.invokeLater(() -> {
                    playerNames.remove(name);
                    playerCountLabel.setText("ผู้เล่น: "+playerNames.size()+" / 4");
                    refreshPlayerList();
                    if (playerNames.size() <= 1) startGameBtn.setEnabled(false);
                });
            }
            @Override public void onScoreReceived(String name, int score) {}
            @Override public void onAllPlayersFinished(Map<String,Integer> finalScores) {
                String hn = GameConstants.PLAYER_NAME.isEmpty() ? "Host" : GameConstants.PLAYER_NAME;
                finalScores.put(hn, model.Relation.getInstance().getAffection("Ahri"));
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

        if (client != null) { client.disconnect(); client = null; } // FIX #3

        client = new GameClient(name);
        client.setListener(new GameClient.ClientListener() {
            @Override public void onConnected(String playerName) {
                SwingUtilities.invokeLater(() -> {
                    // FIX #1: ย้ายไปหน้า WAITING
                    playerNames.clear();
                    playerNames.add(playerName);
                    refreshWaitingList();
                    waitingCountLabel.setText("ผู้เล่น: "+playerNames.size()+" / ?");
                    waitingStatusLabel.setText("เชื่อมต่อสำเร็จ! รอ Host เริ่มเกม...");
                    waitingStatusLabel.setForeground(GREEN);
                    show("WAITING");
                });
            }
            @Override public void onGameStart(int readSeconds) {
                SwingUtilities.invokeLater(() -> loadGameAndPlay(false, readSeconds));
            }
            @Override public void onPlayerJoined(String pName, int total) {
                SwingUtilities.invokeLater(() -> {
                    if (!playerNames.contains(pName)) playerNames.add(pName);
                    waitingCountLabel.setText("ผู้เล่น: "+playerNames.size()+" / ?");
                    refreshWaitingList();
                });
            }
            @Override public void onPlayerLeft(String pName, int total) {
                SwingUtilities.invokeLater(() -> {
                    playerNames.remove(pName);
                    waitingCountLabel.setText("ผู้เล่น: "+playerNames.size()+" / ?");
                    refreshWaitingList();
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
                scene.setMultiplayerEffectBypass(true); // FIX #4

                String hostName = GameConstants.PLAYER_NAME.isEmpty() ? "Host" : GameConstants.PLAYER_NAME;
                if (asHost) {
                    scene.setOnGameFinished(() ->
                        server.receiveHostScore(hostName, model.Relation.getInstance().getAffection("Ahri")));
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
    // FIX #3: reset ทุกอย่าง
    // -------------------------------------------------------
    private void resetAll() {
        if (server != null) { server.stop(); server = null; }
        if (client != null) { client.disconnect(); client = null; }
        isHost = false;
        playerNames.clear();
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
        String me = GameConstants.PLAYER_NAME.isEmpty() ? "Host" : GameConstants.PLAYER_NAME;
        for (int i = 0; i < playerNames.size(); i++) {
            String n = playerNames.get(i);
            // ✨ ส่งเพิ่ม: n.equals(me) คือตัวเรา, i == 0 คือหัวห้อง
            playerListPanel.add(playerCard(n, n.equals(me), i == 0));
            playerListPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        }
        playerListPanel.revalidate(); playerListPanel.repaint();
    }

    private void refreshWaitingList() {
        if (waitingPlayerListPanel == null) return;
        waitingPlayerListPanel.removeAll();
        
        // ✨ ถ้าในลิสต์ยังไม่มีใครเลย (เพราะรอ Data) ให้ใส่ Slot ของ Host ไว้รอ
        if (playerNames.isEmpty()) {
            waitingPlayerListPanel.add(playerCard("กำลังโหลดชื่อหัวห้อง...", false, true));
        }

        String me = GameConstants.PLAYER_NAME;
        for (int i = 0; i < playerNames.size(); i++) {
            String n = playerNames.get(i);
            waitingPlayerListPanel.add(playerCard(n, n.equals(me), i == 0));
            waitingPlayerListPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        }
        waitingPlayerListPanel.revalidate(); 
        waitingPlayerListPanel.repaint();
    }

    private JPanel playerCard(String name, boolean isMe, boolean isHost) {
        JPanel card = new JPanel(new BorderLayout(8, 0));
        card.setMaximumSize(new Dimension(440, 46));
        
        // ✨ สีขอบ: หัวห้องสีชมพูเสมอ คนทั่วไปสีฟ้า
        Color borderColor = isHost ? PINK : BLUE;
        
        card.setBackground(isMe ? new Color(255,240,250) : new Color(245,250,255));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, 2), // ใช้สีตามสถานะ Host
            BorderFactory.createEmptyBorder(8, 14, 8, 14)));
            
        JLabel dot = new JLabel("●"); dot.setFont(F18B); dot.setForeground(GREEN);
        
        // ✨ จัดการชื่อให้สะอาด:
        // 1. ตัดคำว่า (Host) หรือ Host เดิมออกก่อนเพื่อล้างค่าซ้ำ
        String cleanName = name.replace("(Host)", "").replace("Host", "").trim();
        if (cleanName.isEmpty() && isHost) cleanName = "Host"; // ถ้าชื่อว่างแต่เป็น Host ให้ใช้คำว่า Host
        
        // 2. ประกอบร่างใหม่ให้สวยงาม
        String displayName = cleanName + (isHost ? " (Host)" : "") + (isMe ? " (คุณ)" : "");
        
        JLabel nameLbl = new JLabel(displayName); 
        nameLbl.setFont(F18B);
        nameLbl.setForeground(isHost ? PINK : new Color(60,100,180));
        
        card.add(dot, BorderLayout.WEST); card.add(nameLbl, BorderLayout.CENTER);
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
    private JButton mkOutline(String text) {
        JButton b = new JButton(text); b.setFont(F18B); b.setBackground(WHITE); b.setForeground(PINK);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PINK,2), BorderFactory.createEmptyBorder(7,18,7,18)));
        b.setFocusPainted(false); b.setAlignmentX(CENTER_ALIGNMENT); b.setMaximumSize(new Dimension(330,48));
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