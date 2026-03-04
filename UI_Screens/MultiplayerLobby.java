package UI_Screens;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.GradientPaint;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import model.GameClient;
import model.GameServer;
import model.GameConstants;
import model.Relation;
import core.Main;

public class MultiplayerLobby extends JPanel {

    private GameServer server;
    private GameClient client;

    private JPanel mainPanel, hostPanel, joinPanel, waitingPanel, leaderboardPanel;
    private JLabel  hostIPLabel, playerCountLabel, hostDisplay;
    private JPanel  playerListPanel;
    private JButton startGameBtn;
    private JTextField ipField, nameField;
    private JButton    connectBtn;
    private JLabel     joinStatusLabel;
    private JLabel waitingCountLabel;
    private JPanel waitingPlayerListPanel;

    private List<String> playerNames = new ArrayList<>();
    private String       hostName    = "Host";
    private PlaySceneMP  activeMPScene = null;

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

    // ════════════════════════════════════════════════════
    //  Panels
    // ════════════════════════════════════════════════════
    private void buildMainPanel() {
        mainPanel = new JPanel(null) {
            @Override public void doLayout() {
                for (Component c : getComponents()) c.setBounds(0,0,getWidth(),getHeight());
            }
        };
        mainPanel.setBackground(LIGHT_PINK);
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(LIGHT_PINK);
        JPanel box = roundBox(460,370); box.setLayout(new BoxLayout(box,BoxLayout.Y_AXIS)); box.setBorder(new EmptyBorder(38,50,38,50));
        JLabel title = lbl("[ เล่นหลายคน ]",F30B,PINK);
        JLabel sub   = lbl("แข่งกันว่าใครจีบ Ahri ได้คะแนนสูงกว่า!",F16,new Color(150,150,150));
        JButton hostBtn = mkBtn("สร้างห้อง  (Host)",PINK);
        hostBtn.addActionListener(e->showNameOverlay(content));
        JButton joinBtn = mkBtn("เข้าร่วมห้อง  (Join)",BLUE);
        joinBtn.addActionListener(e->goJoin());
        JButton backBtn = mkOutline("< กลับเมนูหลัก");
        backBtn.addActionListener(e->{resetAll(); Main.cardLayout.show(Main.mainContainer,"MENU");});
        box.add(title); box.add(gap(5)); box.add(sub);
        box.add(gap(28)); box.add(hostBtn); box.add(gap(13)); box.add(joinBtn); box.add(gap(17)); box.add(backBtn);
        content.add(box); mainPanel.add(content);
    }

    private void showNameOverlay(JPanel content) {
        JPanel dialogBox = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE); g2.fill(new RoundRectangle2D.Float(0,0,getWidth()-5,getHeight()-5,36,36));
                g2.setColor(PINK); g2.setStroke(new BasicStroke(2.5f)); g2.draw(new RoundRectangle2D.Float(1,1,getWidth()-7,getHeight()-7,36,36)); g2.dispose();
            }
        };
        dialogBox.setLayout(new BoxLayout(dialogBox,BoxLayout.Y_AXIS)); dialogBox.setOpaque(false);
        dialogBox.setBorder(new EmptyBorder(28,36,28,36)); dialogBox.setPreferredSize(new Dimension(360,220));
        JLabel title2=lbl("ใส่ชื่อของคุณ",F24B,PINK); JLabel sub2=lbl("ชื่อจะแสดงในห้องแข่งขัน",F14,new Color(180,180,180));
        JTextField nameInput=styledField(PINK);
        String def=(GameConstants.PLAYER_NAME!=null&&!GameConstants.PLAYER_NAME.isEmpty())?GameConstants.PLAYER_NAME:"Host";
        nameInput.setText(def); nameInput.selectAll();
        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.CENTER,14,0)); btnRow.setOpaque(false); btnRow.setAlignmentX(CENTER_ALIGNMENT);
        JButton cancelBtn=mkOutlineSmall("ยกเลิก"); JButton okBtn=mkBtnSmall("เริ่มเลย!",PINK);
        JPanel overlay=new JPanel(new GridBagLayout()){@Override public boolean isOpaque(){return false;} @Override protected void paintComponent(Graphics g){}};
        Runnable doOk=()->{
            String input=nameInput.getText().trim();
            if(input.isEmpty()){nameInput.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.RED,2),BorderFactory.createEmptyBorder(8,10,8,10)));return;}
            GameConstants.PLAYER_NAME=input; mainPanel.remove(overlay); mainPanel.revalidate(); mainPanel.repaint(); startHosting();
        };
        cancelBtn.addActionListener(e->{mainPanel.remove(overlay);mainPanel.revalidate();mainPanel.repaint();});
        okBtn.addActionListener(e->doOk.run());
        nameInput.addKeyListener(new KeyAdapter(){
            @Override public void keyPressed(KeyEvent e){
                if(e.getKeyCode()==KeyEvent.VK_ENTER) doOk.run();
                if(e.getKeyCode()==KeyEvent.VK_ESCAPE){mainPanel.remove(overlay);mainPanel.revalidate();mainPanel.repaint();}
            }
        });
        btnRow.add(cancelBtn); btnRow.add(okBtn);
        dialogBox.add(title2); dialogBox.add(gap(4)); dialogBox.add(sub2); dialogBox.add(gap(18)); dialogBox.add(nameInput); dialogBox.add(gap(18)); dialogBox.add(btnRow);
        overlay.add(dialogBox); mainPanel.add(overlay); mainPanel.setComponentZOrder(overlay,0); mainPanel.revalidate(); mainPanel.repaint();
        SwingUtilities.invokeLater(nameInput::requestFocusInWindow);
    }

    private void buildHostPanel() {
        hostPanel=new JPanel(new GridBagLayout()); hostPanel.setBackground(LIGHT_PINK);
        JPanel box=roundBox(500,540); box.setLayout(new BoxLayout(box,BoxLayout.Y_AXIS)); box.setBorder(new EmptyBorder(30,40,30,40));
        JLabel title=lbl("ห้องแข่งขัน",F30B,PINK);
        hostDisplay=lbl("หัวห้อง: -",F18B,PINK);
        hostIPLabel=new JLabel("กำลังเริ่ม...");
        hostIPLabel.setFont(new Font("Consolas",Font.BOLD,18)); hostIPLabel.setForeground(new Color(25,130,25));
        JButton copyBtn=new JButton("คัดลอก"){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg=getModel().isPressed()?new Color(100,175,100):new Color(110,185,110);
                g2.setColor(bg); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),14,14)); g2.dispose(); super.paintComponent(g);
            }
        };
        copyBtn.setFont(new Font("Tahoma",Font.BOLD,13)); copyBtn.setForeground(WHITE); copyBtn.setOpaque(false); copyBtn.setContentAreaFilled(false); copyBtn.setBorderPainted(false); copyBtn.setFocusPainted(false); copyBtn.setBorder(new EmptyBorder(6,12,6,12)); copyBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); copyBtn.setPreferredSize(new Dimension(112,34));
        copyBtn.addActionListener(e->{
            java.awt.datatransfer.StringSelection ss=new java.awt.datatransfer.StringSelection(hostIPLabel.getText());
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss,null);
            copyBtn.setText("คัดลอกแล้ว"); new Timer(2000,ev->{copyBtn.setText("คัดลอก");((Timer)ev.getSource()).stop();}).start();
        });
        JPanel ipCard=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(238,255,238)); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),20,20));
                g2.setColor(new Color(170,215,170)); g2.setStroke(new BasicStroke(1.5f)); g2.draw(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,20,20)); g2.dispose();
            }
        };
        ipCard.setLayout(new BorderLayout(10,0)); ipCard.setOpaque(false); ipCard.setBorder(new EmptyBorder(10,16,10,12)); ipCard.setMaximumSize(new Dimension(420,54)); ipCard.setAlignmentX(CENTER_ALIGNMENT);
        ipCard.add(hostIPLabel,BorderLayout.CENTER); ipCard.add(copyBtn,BorderLayout.EAST);
        playerCountLabel=lbl("ผู้เล่น: 1 / 3",F18B,new Color(120,120,120));
        playerListPanel=new JPanel(); playerListPanel.setLayout(new BoxLayout(playerListPanel,BoxLayout.Y_AXIS)); playerListPanel.setOpaque(false);
        JScrollPane scroll=new JScrollPane(playerListPanel); scroll.setBorder(BorderFactory.createLineBorder(new Color(255,182,193),2,true)); scroll.setOpaque(false); scroll.getViewport().setOpaque(false); scroll.setPreferredSize(new Dimension(420,140));
        startGameBtn=mkBtn("เริ่มเกม!",PINK); startGameBtn.setEnabled(false);
        startGameBtn.addActionListener(e->startGameAsHost());
        JButton cancelBtn2=mkOutline("ปิดห้อง"); cancelBtn2.addActionListener(e->{resetAll();goMain();});
        box.add(title); box.add(gap(8)); box.add(hostDisplay); box.add(gap(18));
        box.add(smallLbl("แชร์ IP นี้ให้เพื่อนเชื่อมต่อ:")); box.add(gap(6)); box.add(ipCard); box.add(gap(16));
        box.add(playerCountLabel); box.add(gap(6)); box.add(scroll); box.add(gap(18));
        box.add(startGameBtn); box.add(gap(8)); box.add(cancelBtn2);
        hostPanel.add(box);
    }

    private void buildJoinPanel() {
        joinPanel=new JPanel(new GridBagLayout()); joinPanel.setBackground(LIGHT_PINK);
        JPanel box=roundBox(490,420); box.setLayout(new BoxLayout(box,BoxLayout.Y_AXIS)); box.setBorder(new EmptyBorder(34,48,34,48));
        JLabel title=lbl("เข้าร่วมห้อง",F30B,BLUE); nameField=styledField(PINK); ipField=styledField(BLUE);
        KeyAdapter enter=new KeyAdapter(){@Override public void keyPressed(KeyEvent e){if(e.getKeyCode()==KeyEvent.VK_ENTER) connectBtn.doClick();}};
        nameField.addKeyListener(enter); ipField.addKeyListener(enter);
        joinStatusLabel=lbl(" ",F18B,Color.GRAY); connectBtn=mkBtn("เชื่อมต่อ",BLUE);
        connectBtn.addActionListener(e->connectToHost());
        JButton backBtn=mkOutline("< กลับ"); backBtn.addActionListener(e->goMain());
        box.add(title); box.add(gap(20)); box.add(smallLbl("ชื่อผู้เล่นของคุณ:")); box.add(gap(5)); box.add(nameField);
        box.add(gap(16)); box.add(smallLbl("IP:Port ของ Host:")); box.add(gap(5)); box.add(ipField);
        box.add(gap(10)); box.add(joinStatusLabel); box.add(gap(6)); box.add(connectBtn); box.add(gap(10)); box.add(backBtn);
        joinPanel.add(box);
    }

    private void buildWaitingPanel() {
        waitingPanel=new JPanel(new GridBagLayout()); waitingPanel.setBackground(LIGHT_PINK);
        JPanel box=roundBox(520,460); box.setLayout(new BoxLayout(box,BoxLayout.Y_AXIS)); box.setBorder(new EmptyBorder(28,40,28,40));
        JLabel title=lbl("ล็อบบี้",F30B,BLUE); JLabel status=lbl("เชื่อมต่อสำเร็จ! รอ Host เริ่มเกม...",F18B,GREEN);
        waitingCountLabel=lbl("ผู้เล่น: 1 / ?",F18B,new Color(100,100,100));
        waitingPlayerListPanel=new JPanel(); waitingPlayerListPanel.setLayout(new BoxLayout(waitingPlayerListPanel,BoxLayout.Y_AXIS)); waitingPlayerListPanel.setOpaque(false);
        JScrollPane scrollW=new JScrollPane(waitingPlayerListPanel); scrollW.setBorder(BorderFactory.createLineBorder(new Color(150,200,255),1)); scrollW.setOpaque(false); scrollW.getViewport().setOpaque(false); scrollW.setMaximumSize(new Dimension(440,200)); scrollW.setAlignmentX(CENTER_ALIGNMENT);
        JButton leaveBtn=mkOutline("< ออกจากห้อง");
        leaveBtn.addActionListener(e->{if(client!=null){client.disconnect();client=null;}playerNames.clear();goMain();});
        box.add(title); box.add(gap(10)); box.add(status); box.add(gap(6)); box.add(waitingCountLabel); box.add(gap(8));
        box.add(smallLbl("ผู้เล่นในห้อง:")); box.add(gap(4)); box.add(scrollW); box.add(gap(12));
        box.add(lbl("รอ Host กดเริ่มเกม...",F16,new Color(150,150,150))); box.add(gap(10)); box.add(leaveBtn);
        waitingPanel.add(box);
    }

    private void buildLeaderboardPanel() {
        leaderboardPanel=new JPanel(new GridBagLayout()); leaderboardPanel.setBackground(LIGHT_PINK);
    }

    // ════════════════════════════════════════════════════
    //  Leaderboard
    // ════════════════════════════════════════════════════
    public void showLeaderboard(Map<String,Integer> scores) {
        leaderboardPanel.removeAll();
        JPanel box=roundBox(520,500); box.setLayout(new BoxLayout(box,BoxLayout.Y_AXIS)); box.setBorder(new EmptyBorder(26,42,26,42));
        box.add(lbl("ผลการแข่งขัน",F30B,new Color(190,130,0))); box.add(gap(16));
        List<Map.Entry<String,Integer>> sorted=new ArrayList<>(scores.entrySet());
        sorted.sort((a,b)->b.getValue()-a.getValue());
        String[] ranks={"1.","2.","3."}; Color[] cols={new Color(190,140,0),new Color(120,120,120),new Color(150,90,40)};
        for(int i=0;i<sorted.size();i++){
            JPanel row=new JPanel(new BorderLayout(10,0)); row.setMaximumSize(new Dimension(430,55));
            row.setBackground(i==0?new Color(255,250,215):new Color(255,246,250));
            row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(255,200,220),1),BorderFactory.createEmptyBorder(9,12,9,12)));
            String rank=i<ranks.length?ranks[i]:(i+1)+".";
            JLabel nl=new JLabel(rank+"  "+sorted.get(i).getKey()); nl.setFont(F24B); nl.setForeground(i==0?new Color(150,95,0):Color.DARK_GRAY);
            JLabel sl=new JLabel(sorted.get(i).getValue()+" คะแนน"); sl.setFont(F24B); sl.setForeground(i<cols.length?cols[i]:cols[cols.length-1]); sl.setHorizontalAlignment(SwingConstants.RIGHT);
            row.add(nl,BorderLayout.WEST); row.add(sl,BorderLayout.EAST); row.setAlignmentX(CENTER_ALIGNMENT);
            box.add(row); box.add(gap(7));
        }
        box.add(gap(8));
        JButton menuBtn=mkBtn("กลับหน้าหลัก",PINK); JButton againBtn=mkOutline("เล่นอีกครั้ง");
        menuBtn.addActionListener(e->{resetAll();Main.cardLayout.show(Main.mainContainer,"MENU");});
        againBtn.addActionListener(e->{resetAll();goMain();});
        box.add(menuBtn); box.add(gap(10)); box.add(againBtn);
        leaderboardPanel.add(box); leaderboardPanel.revalidate(); leaderboardPanel.repaint();
        // ✅ Bug 2: switch main card กลับมาที่ Lobby ก่อน แล้วค่อย show LEADERBOARD
        Main.cardLayout.show(Main.mainContainer, "MULTIPLAYER");
        show("LEADERBOARD");
    }

    // ════════════════════════════════════════════════════
    //  Host Logic
    // ════════════════════════════════════════════════════
    private void startHosting() {
        String name=GameConstants.PLAYER_NAME; if(name==null||name.isEmpty()) name="Host";
        hostName=name; resetAll();
        hostDisplay.setText("หัวห้อง: "+name);
        playerNames.clear(); playerNames.add(name);
        refreshPlayerList(); playerCountLabel.setText("ผู้เล่น: 1 / 3"); show("HOST");

        // ✅ สุ่ม port เพื่อไม่ชนกับห้องอื่น
        int roomPort = GameServer.randomPort();
        server=new GameServer(roomPort);
        server.setHostName(name);
        server.setListener(new GameServer.ServerListener(){
            @Override public void onServerStarted(String ip,int port){
                SwingUtilities.invokeLater(()->hostIPLabel.setText(ip+":"+port));
            }
            @Override public void onPlayerJoined(String pName,int total){
                SwingUtilities.invokeLater(()->{
                    if(!playerNames.contains(pName)) playerNames.add(pName);
                    playerCountLabel.setText("ผู้เล่น: "+Math.min(playerNames.size(),3)+" / 3");
                    refreshPlayerList();
                    if(playerNames.size()>1) startGameBtn.setEnabled(true);
                });
            }
            @Override public void onPlayerLeft(String pName,int total){
                SwingUtilities.invokeLater(()->{
                    playerNames.remove(pName);
                    playerCountLabel.setText("ผู้เล่น: "+Math.min(playerNames.size(),3)+" / 3");
                    refreshPlayerList();
                    if(playerNames.size()<=1) startGameBtn.setEnabled(false);
                    // ✅ แจ้งเตือนในเกม
                    if (activeMPScene != null) activeMPScene.showPlayerLeftToast(pName);
                });
            }
            @Override public void onPlayerLeftDuringChoice(){
                SwingUtilities.invokeLater(()->{
                    // ✅ Bug 1: reset UI ให้ host กดได้ถ้า player ออกระหว่าง choice
                    if (activeMPScene != null) activeMPScene.resetWaitingState();
                });
            }
            @Override public void onScoreReceived(String n,int s){}
            @Override public void onAllPlayersFinished(Map<String,Integer> finalScores){
                finalScores.put(GameConstants.PLAYER_NAME,Relation.getInstance().getAffection("Ahri"));
                SwingUtilities.invokeLater(()->{
                    activeMPScene = null;
                    showLeaderboard(finalScores); // showLeaderboard จะ switch main card เองแล้ว
                });
            }
            @Override public void onServerError(String msg){
                SwingUtilities.invokeLater(()->{
                    JOptionPane.showMessageDialog(MultiplayerLobby.this,msg,"ข้อผิดพลาด",JOptionPane.ERROR_MESSAGE);
                    resetAll(); goMain();
                });
            }
            // ✅ 2-phase callbacks → ส่งต่อให้ PlaySceneMP (Host)
            @Override public void onPhaseRead(String scene,int sec,int total){
                SwingUtilities.invokeLater(()->{ if(activeMPScene!=null) activeMPScene.onHostPhaseRead(scene,sec,total); });
            }
            @Override public void onCountdown(int n){
                SwingUtilities.invokeLater(()->{ if(activeMPScene!=null) activeMPScene.onHostCountdown(n); });
            }
            @Override public void onPhaseChoice(String scene,int sec,int total){
                SwingUtilities.invokeLater(()->{ if(activeMPScene!=null) activeMPScene.onHostPhaseChoice(scene,sec,total); });
            }
            @Override public void onChoiceResult(String target,String charName,int score){
                SwingUtilities.invokeLater(()->{ if(activeMPScene!=null) activeMPScene.onHostChoiceResult(target,charName,score); });
            }
            @Override public void onForceNext(String scene,String target){
                SwingUtilities.invokeLater(()->{ if(activeMPScene!=null) activeMPScene.onHostForceNext(scene,target); });
            }
            @Override public void onTimerSync(int t){
                SwingUtilities.invokeLater(()->{ if(activeMPScene!=null) activeMPScene.onHostTimerSync(t); });
            }
            @Override public void onReadyCount(int ready,int total){
                SwingUtilities.invokeLater(()->{ if(activeMPScene!=null) activeMPScene.onHostReadyCount(ready,total); });
            }
        });
        server.start();
    }

    // ════════════════════════════════════════════════════
    //  Join Logic
    // ════════════════════════════════════════════════════
    private void connectToHost() {
        String name=nameField.getText().trim(); String ipPort=ipField.getText().trim();
        if(name.isEmpty()){setJoinStatus("กรุณาใส่ชื่อของคุณ",Color.RED);return;}
        if(!ipPort.contains(":")){setJoinStatus("รูปแบบ IP:Port เช่น 26.1.2.3:45621",Color.RED);return;}
        String[] parts=ipPort.split(":"); String ip=parts[0].trim(); int port;
        try{port=Integer.parseInt(parts[1].trim());}catch(NumberFormatException ex){setJoinStatus("Port ไม่ถูกต้อง",Color.RED);return;}
        GameConstants.PLAYER_NAME=name; Relation.getInstance().resetAll();
        connectBtn.setEnabled(false); setJoinStatus("กำลังเชื่อมต่อ...",new Color(80,80,200));
        if(client!=null){client.disconnect();client=null;}
        client=new GameClient(name);
        client.setListener(new GameClient.ClientListener(){
            @Override public void onConnected(String playerName){
                // ✅ อัปเดตชื่อจริงจาก Server (อาจต่างถ้าชื่อซ้ำ เช่น "ice(2)")
                GameConstants.PLAYER_NAME = playerName;
                SwingUtilities.invokeLater(()->{ playerNames.clear(); playerNames.add(playerName); refreshWaitingList(); show("WAITING"); });
            }
            @Override public void onPlayerListReceived(List<String> names){
                SwingUtilities.invokeLater(()->{
                    playerNames.clear(); playerNames.addAll(names);
                    if(!names.isEmpty()) hostName=names.get(0);
                    waitingCountLabel.setText("ผู้เล่น: "+playerNames.size()+" / ?");
                    refreshWaitingList();
                });
            }
            @Override public void onGameStart(int readSeconds){
                SwingUtilities.invokeLater(()->loadGameAndPlay(false,readSeconds));
            }
            @Override public void onPlayerJoined(String p,int t){}
            @Override public void onPlayerLeft(String p,int t){
                SwingUtilities.invokeLater(()->{
                    if (activeMPScene != null) activeMPScene.showPlayerLeftToast(p);
                });
            }
            @Override public void onScoreUpdate(String p,int s){}
            @Override public void onLeaderboard(Map<String,Integer> scores){
                SwingUtilities.invokeLater(()->showLeaderboard(scores));
            }
            @Override public void onChatMessage(String sender,String message){}
            @Override public void onDisconnected(String reason){
                SwingUtilities.invokeLater(()->{
                    if (client != null) { client.disconnect(); client = null; }
                    activeMPScene = null;
                    Main.cardLayout.show(Main.mainContainer, "MULTIPLAYER");
                    show("MAIN");
                    setJoinStatus("หัวห้องออกจากเกม", Color.RED);
                    connectBtn.setEnabled(true); connectBtn.setText("เชื่อมต่อ");
                    showHostLeftDialog();
                });
            }
            @Override public void onError(String message){
                SwingUtilities.invokeLater(()->{
                    setJoinStatus(message,Color.RED); connectBtn.setEnabled(true); connectBtn.setText("เชื่อมต่อ");
                });
            }
            // ✅ 2-phase callbacks → ส่งต่อให้ PlaySceneMP (Client)
            @Override public void onPhaseRead(String scene,int sec,int total){
                SwingUtilities.invokeLater(()->{ if(activeMPScene!=null) activeMPScene.onHostPhaseRead(scene,sec,total); });
            }
            @Override public void onCountdown(int n){
                SwingUtilities.invokeLater(()->{ if(activeMPScene!=null) activeMPScene.onHostCountdown(n); });
            }
            @Override public void onPhaseChoice(String scene,int sec,int total){
                SwingUtilities.invokeLater(()->{ if(activeMPScene!=null) activeMPScene.onHostPhaseChoice(scene,sec,total); });
            }
            @Override public void onChoiceResult(String target,String charName,int score){
                SwingUtilities.invokeLater(()->{ if(activeMPScene!=null) activeMPScene.onHostChoiceResult(target,charName,score); });
            }
            @Override public void onForceNext(String scene,String target){
                SwingUtilities.invokeLater(()->{ if(activeMPScene!=null) activeMPScene.onHostForceNext(scene,target); });
            }
            @Override public void onTimerSync(int t){
                SwingUtilities.invokeLater(()->{ if(activeMPScene!=null) activeMPScene.onHostTimerSync(t); });
            }
            @Override public void onReadyCount(int ready,int total){
                SwingUtilities.invokeLater(()->{ if(activeMPScene!=null) activeMPScene.onHostReadyCount(ready,total); });
            }
        });
        client.connect(ip,port);
    }

    private void startGameAsHost() {
        int totalPlayers = Math.min(playerNames.size(), 3);
        server.setExpectedPlayers(totalPlayers);
        server.lockRoom(); // ✅ lock หลัง setExpectedPlayers เพื่อให้ lockedTotal ถูกต้อง
        server.broadcast("START_GAME:" + GameServer.READ_SECONDS);
        loadGameAndPlay(true, GameServer.READ_SECONDS);
    }

    // ════════════════════════════════════════════════════
    //  Load Game
    // ════════════════════════════════════════════════════
    private void loadGameAndPlay(boolean asHost, int readSeconds) {
        Relation.getInstance().resetAll();
        UI_Components.RelationUI.getInstance().setVisible(false); // ซ่อนใน MP เสมอ
        activeMPScene = findOrCreateMPScene();
        activeMPScene.startMPGame(asHost ? server : null, asHost ? null : client);
        String myName = GameConstants.PLAYER_NAME.isEmpty() ? "Player" : GameConstants.PLAYER_NAME;

        if (asHost) {
            // Host ส่งคะแนนตัวเอง → Server รวบรวม → broadcast LEADERBOARD → onAllPlayersFinished
            activeMPScene.setOnGameFinished(() ->
                server.receiveHostScore(myName, Relation.getInstance().getAffection("Ahri")));
        } else {
            // Client ส่งคะแนน → Server รับ → ครบทุกคน → broadcast LEADERBOARD
            // Lobby client listener มี onLeaderboard → showLeaderboard() อยู่แล้ว
            activeMPScene.setOnGameFinished(() -> {
                if (client != null) client.sendScore(Relation.getInstance().getAffection("Ahri"));
            });
        }

        Main.cardLayout.show(Main.mainContainer, "PLAY_SCENE_MP");
    }

    private PlaySceneMP findOrCreateMPScene() {
        for (Component c : Main.mainContainer.getComponents())
            if (c instanceof PlaySceneMP) return (PlaySceneMP)c;
        PlaySceneMP mp = new PlaySceneMP();
        Main.mainContainer.add(mp,"PLAY_SCENE_MP");
        return mp;
    }

    private void showHostLeftDialog() {
        // ── overlay ──────────────────────────────────────────
        JPanel overlay = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 160));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        overlay.setOpaque(false);

        // ── dialog box ───────────────────────────────────────
        JPanel box = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // shadow
                g2.setColor(new Color(0, 0, 0, 100));
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(6, 6, getWidth()-4, getHeight()-4, 24, 24));
                // bg
                g2.setColor(new Color(12, 16, 30));
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth()-6, getHeight()-6, 24, 24));
                // border
                g2.setColor(new Color(220, 60, 60));
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new java.awt.geom.RoundRectangle2D.Float(1, 1, getWidth()-8, getHeight()-8, 24, 24));
                // top accent line
                g2.setStroke(new BasicStroke(2.5f));
                g2.setPaint(new GradientPaint(30, 0, new Color(220, 60, 60), getWidth()-30, 0, new Color(0,0,0,0)));
                g2.drawLine(30, 1, getWidth()-30, 1);
                g2.dispose();
            }
        };
        box.setOpaque(false);
        box.setPreferredSize(new Dimension(420, 260));
        box.setBorder(BorderFactory.createEmptyBorder(28, 32, 24, 32));

        // ── icon + title ─────────────────────────────────────
        JLabel icon  = new JLabel("⚠", SwingConstants.CENTER);
        icon.setFont(new Font("Dialog", Font.PLAIN, 44));
        icon.setForeground(new Color(220, 60, 60));

        JLabel title = new JLabel("หัวห้องออกจากเกม", SwingConstants.CENTER);
        title.setFont(new Font("Tahoma", Font.BOLD, 22));
        title.setForeground(new Color(240, 245, 255));

        JLabel sub = new JLabel("คุณถูกนำกลับมายังล็อบบี้", SwingConstants.CENTER);
        sub.setFont(new Font("Tahoma", Font.PLAIN, 16));
        sub.setForeground(new Color(160, 175, 210));

        // ── OK button ─────────────────────────────────────────
        JButton okBtn = new JButton("รับทราบ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isRollover() ? new Color(240, 80, 80) : new Color(200, 50, 50);
                g2.setColor(c);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        okBtn.setFont(new Font("Tahoma", Font.BOLD, 16));
        okBtn.setForeground(Color.WHITE);
        okBtn.setOpaque(false); okBtn.setContentAreaFilled(false);
        okBtn.setBorderPainted(false); okBtn.setFocusPainted(false);
        okBtn.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        okBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // ── layout ───────────────────────────────────────────
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        icon .setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub  .setAlignmentX(Component.CENTER_ALIGNMENT);
        okBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(icon);
        center.add(Box.createRigidArea(new Dimension(0, 10)));
        center.add(title);
        center.add(Box.createRigidArea(new Dimension(0, 8)));
        center.add(sub);
        center.add(Box.createRigidArea(new Dimension(0, 24)));
        center.add(okBtn);
        box.add(center, BorderLayout.CENTER);
        overlay.add(box);

        // ── mount บน LayeredPane ──────────────────────────────
        JLayeredPane lp = Main.mainFrame.getLayeredPane();
        overlay.setBounds(0, 0, lp.getWidth(), lp.getHeight());
        lp.add(overlay, JLayeredPane.MODAL_LAYER);
        lp.revalidate(); lp.repaint();

        okBtn.addActionListener(e -> {
            lp.remove(overlay);
            lp.revalidate(); lp.repaint();
        });
    }

    // ════════════════════════════════════════════════════
    //  Reset / Nav
    // ════════════════════════════════════════════════════
    private void resetAll() {
        if(server!=null){server.stop();server=null;}
        if(client!=null){client.disconnect();client=null;}
        playerNames.clear(); hostName="Host"; activeMPScene=null;
    }

    private void refreshPlayerList() {
        if(playerListPanel==null) return;
        playerListPanel.removeAll(); String me=GameConstants.PLAYER_NAME;
        for(int i=0;i<playerNames.size();i++){
            String n=playerNames.get(i);
            playerListPanel.add(playerCard(n,n.equals(me),i==0));
            playerListPanel.add(Box.createRigidArea(new Dimension(0,6)));
        }
        playerListPanel.revalidate(); playerListPanel.repaint();
    }

    private void refreshWaitingList() {
        if(waitingPlayerListPanel==null) return;
        waitingPlayerListPanel.removeAll(); String me=GameConstants.PLAYER_NAME;
        for(String n:playerNames){
            waitingPlayerListPanel.add(playerCard(n,n.equals(me),n.equals(hostName)));
            waitingPlayerListPanel.add(Box.createRigidArea(new Dimension(0,6)));
        }
        waitingPlayerListPanel.revalidate(); waitingPlayerListPanel.repaint();
    }

    private JPanel playerCard(String name,boolean isMe,boolean isHost){
        JPanel card=new JPanel(new BorderLayout(8,0)); card.setMaximumSize(new Dimension(440,46));
        card.setBackground(isMe?new Color(255,240,250):WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(isHost?PINK:BLUE,2),BorderFactory.createEmptyBorder(8,14,8,14)));
        JLabel dot=new JLabel("*"); dot.setFont(F18B); dot.setForeground(GREEN);
        String clean=name.replace("(Host)","").replace("(คุณ)","").trim();
        String display=clean+(isHost?" (Host)":"")+(isMe?" (คุณ)":"");
        JLabel nameLbl=new JLabel(display); nameLbl.setFont(F18B); nameLbl.setForeground(isHost?PINK:new Color(60,100,180));
        card.add(dot,BorderLayout.WEST); card.add(nameLbl,BorderLayout.CENTER); card.setAlignmentX(CENTER_ALIGNMENT);
        return card;
    }

    private void goMain() { show("MAIN"); }
    private void goJoin() {
        nameField.setText(""); ipField.setText(""); joinStatusLabel.setText(" ");
        connectBtn.setEnabled(true); connectBtn.setText("เชื่อมต่อ"); show("JOIN");
    }
    private void show(String card){ ((CardLayout)getLayout()).show(this,card); }
    private void setJoinStatus(String msg,Color c){joinStatusLabel.setText(msg);joinStatusLabel.setForeground(c);}

    // ── UI helpers ─────────────────────────────────────────
    private JPanel roundBox(int w,int h){
        JPanel p=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),40,40));
                g2.setColor(PINK); g2.setStroke(new BasicStroke(2.5f)); g2.draw(new RoundRectangle2D.Float(1,1,getWidth()-2,getHeight()-2,40,40)); g2.dispose();
            }
        };
        p.setPreferredSize(new Dimension(w,h)); return p;
    }
    private JButton mkBtn(String t,Color bg){JButton b=new JButton(t);b.setFont(F18B);b.setBackground(bg);b.setForeground(WHITE);b.setBorder(BorderFactory.createEmptyBorder(11,24,11,24));b.setFocusPainted(false);b.setAlignmentX(CENTER_ALIGNMENT);b.setMaximumSize(new Dimension(330,50));b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));return b;}
    private JButton mkBtnSmall(String t,Color bg){JButton b=new JButton(t);b.setFont(F18B);b.setBackground(bg);b.setForeground(WHITE);b.setBorder(BorderFactory.createEmptyBorder(9,20,9,20));b.setFocusPainted(false);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));return b;}
    private JButton mkOutline(String t){JButton b=new JButton(t);b.setFont(F18B);b.setBackground(WHITE);b.setForeground(PINK);b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(PINK,2),BorderFactory.createEmptyBorder(7,18,7,18)));b.setFocusPainted(false);b.setAlignmentX(CENTER_ALIGNMENT);b.setMaximumSize(new Dimension(330,48));b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));return b;}
    private JButton mkOutlineSmall(String t){JButton b=new JButton(t);b.setFont(F18B);b.setBackground(WHITE);b.setForeground(new Color(150,150,150));b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200,200,200),2),BorderFactory.createEmptyBorder(7,18,7,18)));b.setFocusPainted(false);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));return b;}
    private JLabel lbl(String t,Font f,Color c){JLabel l=new JLabel(t,SwingConstants.CENTER);l.setFont(f);l.setForeground(c);l.setAlignmentX(CENTER_ALIGNMENT);return l;}
    private JLabel smallLbl(String t){JLabel l=new JLabel(t);l.setFont(F18B);l.setForeground(new Color(120,120,120));l.setAlignmentX(CENTER_ALIGNMENT);return l;}
    private JTextField styledField(Color border){JTextField f=new JTextField("");f.setFont(new Font("Tahoma",Font.BOLD,18));f.setHorizontalAlignment(JTextField.CENTER);f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,2),BorderFactory.createEmptyBorder(8,10,8,10)));f.setMaximumSize(new Dimension(380,48));f.setAlignmentX(CENTER_ALIGNMENT);return f;}
    private Component gap(int h){return Box.createRigidArea(new Dimension(0,h));}
}