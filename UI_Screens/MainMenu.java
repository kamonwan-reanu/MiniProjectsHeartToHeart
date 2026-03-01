package UI_Screens;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;
import core.Main;
import model.GameConstants;
import model.Relation;
import model.StoryData;

public class MainMenu extends JPanel {

    private static final Color PINK        = new Color(255, 105, 180);
    private static final Color PINK_LIGHT  = new Color(255, 182, 213);
    private static final Color PINK_SOFT   = new Color(255, 235, 245);
    private static final Color PINK_MED    = new Color(255, 160, 200);
    private static final Color WHITE       = Color.WHITE;
    private static final Color TEXT_DARK   = new Color(90, 30, 60);
    private static final Color TEXT_EXIT   = new Color(190, 50, 80);
    private static final Font  F_BTN       = new Font("Tahoma", Font.BOLD, 16);
    private static final Font  F_THAI      = new Font("Tahoma", Font.PLAIN, 13);

    private JLabel       gameName;
    private JButton[]    buttons;
    private Timer        bgmLoopTimer;
    private JLayeredPane layeredPane;
    private JPanel       mainContentPanel;
    private JPanel       registerOverlay;
    private JTextField   inputField;
    private JLabel       warningLabel;

    private static final int N = 18;
    private final float[] hx = new float[N], hy = new float[N], hsize = new float[N];
    private final float[] hspd = new float[N], halpha = new float[N], hdrift = new float[N];
    private final Random rng = new Random();
    private boolean heartsInited = false;
    private Timer particleTimer;
    private JPanel bgPanel;

    public MainMenu(Font titleFont, Font menuFont) {
        setLayout(new BorderLayout());
        setBackground(PINK_SOFT);

        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        add(layeredPane, BorderLayout.CENTER);

        bgPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,new Color(255,240,248),0,getHeight(),new Color(255,210,232)));
                g2.fillRect(0,0,getWidth(),getHeight());
                paintBlob(g2,getWidth()*0.12f,getHeight()*0.18f,200,new Color(255,170,210,50));
                paintBlob(g2,getWidth()*0.88f,getHeight()*0.55f,170,new Color(255,140,190,40));
                paintBlob(g2,getWidth()*0.50f,getHeight()*0.88f,150,new Color(255,200,230,55));
                if (heartsInited) for (int i=0;i<N;i++) paintHeart(g2,i);
                g2.dispose();
            }
        };
        bgPanel.setOpaque(false);
        layeredPane.add(bgPanel, Integer.valueOf(0));

        mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setOpaque(false);
        buildContent(mainContentPanel);
        layeredPane.add(mainContentPanel, Integer.valueOf(1));

        setupRegisterOverlay();

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { sizeAllLayers(); applyResponsiveFont(); }
        });

        particleTimer = new Timer(32, e -> { tickHearts(); bgPanel.repaint(); });
        particleTimer.start();
        startMenuMusic();
    }

    private void sizeAllLayers() {
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;
        bgPanel.setBounds(0,0,w,h);
        mainContentPanel.setBounds(0,0,w,h);
        if (registerOverlay != null) {
            registerOverlay.setBounds(0,0,w,h);
            if (registerOverlay.getComponentCount() >= 1) {
                int bw=500,bh=320;
                registerOverlay.getComponent(0).setBounds((w-bw)/2,(h-bh)/2,bw,bh);
            }
        }
        if (!heartsInited) { initHearts(w,h); heartsInited=true; }
    }

    private void applyResponsiveFont() {
        int w=getWidth(),h=getHeight();
        if (w<=0||h<=0||gameName==null||buttons==null) return;
        gameName.setFont(new Font("Tahoma",Font.BOLD,Math.max(28,(int)Math.min(w*0.075f,60f))));
        int btnW=Math.min((int)(w*0.45f),400), btnH=Math.max((int)(h*0.075f),54);
        Font bf=new Font("Tahoma",Font.BOLD,Math.max(14,(int)Math.min(w*0.028f,18f)));
        for (JButton btn:buttons) { btn.setMaximumSize(new Dimension(btnW,btnH)); btn.setFont(bf); }
        mainContentPanel.setBorder(new EmptyBorder((int)(h*0.06f),Math.max((int)(w*0.22f),60),(int)(h*0.06f),Math.max((int)(w*0.22f),60)));
        mainContentPanel.revalidate();
    }

    private void buildContent(JPanel content) {
        JPanel titlePanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,255,255,130));
                g2.fill(new RoundRectangle2D.Float(10,6,getWidth()-20,getHeight()-12,28,28));
                g2.dispose();
            }
        };
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel,BoxLayout.Y_AXIS));
        titlePanel.setBorder(new EmptyBorder(18,20,16,20));

        gameName = new JLabel("HeartToHeart",SwingConstants.CENTER);
        gameName.setFont(new Font("Tahoma",Font.BOLD,54));
        gameName.setForeground(PINK);
        gameName.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("~ เรื่องราวของหัวใจ ~",SwingConstants.CENTER);
        subtitle.setFont(new Font("Tahoma",Font.ITALIC,15));
        subtitle.setForeground(PINK_MED);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        titlePanel.add(gameName);
        titlePanel.add(Box.createRigidArea(new Dimension(0,3)));
        titlePanel.add(subtitle);

        JPanel menuWrap=new JPanel(new GridBagLayout());
        menuWrap.setOpaque(false);
        JPanel col=new JPanel();
        col.setLayout(new BoxLayout(col,BoxLayout.Y_AXIS));
        col.setOpaque(false);

        String[] labels={"เริ่มเกม","เล่นหลายคน","โหลดเกม","ตั้งค่า","เกี่ยวกับคนสร้าง","ออกจากเกม"};
        buttons=new JButton[labels.length];
        for (int i=0;i<labels.length;i++) {
            buttons[i]=makeMenuBtn(labels[i], i==labels.length-1);
            col.add(buttons[i]);
            if (i<labels.length-1) col.add(Box.createRigidArea(new Dimension(0,10)));
        }

        buttons[0].addActionListener(e->showRegisterUI());
        buttons[1].addActionListener(e->Main.cardLayout.show(Main.mainContainer,"MULTIPLAYER"));
        buttons[2].addActionListener(e->openLoadScreen());  // ✅ ใช้ SaveSystemUI
        buttons[3].addActionListener(e->Main.cardLayout.show(Main.mainContainer,"SETTING"));
        buttons[4].addActionListener(e->Main.cardLayout.show(Main.mainContainer,"CREDIT"));
        buttons[5].addActionListener(e->showExitDialog());

        menuWrap.add(col);

        JLabel ver=new JLabel("v1.0",SwingConstants.RIGHT);
        ver.setFont(new Font("Tahoma",Font.PLAIN,11));
        ver.setForeground(new Color(255,150,190,140));
        JPanel foot=new JPanel(new BorderLayout());
        foot.setOpaque(false);
        foot.setBorder(new EmptyBorder(0,0,6,12));
        foot.add(ver,BorderLayout.EAST);

        content.add(titlePanel,BorderLayout.NORTH);
        content.add(menuWrap,BorderLayout.CENTER);
        content.add(foot,BorderLayout.SOUTH);
    }

    // ✅ เปิดหน้าโหลดผ่าน SaveSystemUI
    private void openLoadScreen() {
        UI_Components.SaveSystemUI saveUI = UI_Components.SaveSystemUI.getInstance();
        saveUI.setOnLoadSuccess(this::stopMenuMusic);
        saveUI.showLoad(mainContentPanel);
    }

    private JButton makeMenuBtn(String text, boolean isExit) {
        JButton btn = new JButton(text) {
            private float t=0f;
            private Timer anim;
            {
                anim=new Timer(14,null);
                anim.addActionListener(ev->{
                    boolean over=getModel().isRollover();
                    t=over?Math.min(1f,t+0.14f):Math.max(0f,t-0.14f);
                    repaint();
                    if ((over&&t>=1f)||(!over&&t<=0f)) anim.stop();
                });
                addMouseListener(new MouseAdapter(){
                    @Override public void mouseEntered(MouseEvent e){anim.start();}
                    @Override public void mouseExited(MouseEvent e){anim.start();}
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int w=getWidth(),h=getHeight();
                g2.setColor(new Color(200,60,110,(int)(35*t)));
                g2.fill(new RoundRectangle2D.Float(3,4,w-4,h-2,16,16));
                Color fill=isExit?blend(new Color(255,255,255,210),new Color(255,200,210,230),t)
                                 :blend(new Color(255,255,255,220),PINK,t);
                g2.setColor(fill);
                g2.fill(new RoundRectangle2D.Float(0,0,w-3,h-3,14,14));
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(isExit?new Color(220,100,130,180):PINK_LIGHT);
                g2.draw(new RoundRectangle2D.Float(0,0,w-4,h-4,14,14));
                if (!isExit&&t>0.05f){
                    g2.setColor(new Color(255,255,255,(int)(180*t)));
                    g2.fill(new RoundRectangle2D.Float(0,(h-24)/2f,4,24,3,3));
                }
                g2.dispose();
                setForeground(isExit?TEXT_EXIT:(t>0.55f?WHITE:TEXT_DARK));
                super.paintComponent(g);
            }
        };
        btn.setFont(F_BTN);
        btn.setForeground(isExit?TEXT_EXIT:TEXT_DARK);
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(13,28,13,28));
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(400,60));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ✅ ไม่มี dimmer ชมพูเลย — แค่ dialog box ลอยอยู่กลาง
    private void setupRegisterOverlay() {
        registerOverlay = new JPanel(null) {
            @Override public boolean contains(int x, int y) {
                if (!isVisible()) return false;
                if (getComponentCount()>0) return getComponent(0).getBounds().contains(x,y);
                return false;
            }
        };
        registerOverlay.setOpaque(false);
        registerOverlay.setVisible(false);

        MouseAdapter block=new MouseAdapter(){
            @Override public void mouseClicked(MouseEvent e){e.consume();}
            @Override public void mousePressed(MouseEvent e){e.consume();}
            @Override public void mouseReleased(MouseEvent e){e.consume();}
        };

        JPanel box=new JPanel(null){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(220,80,140,55));
                g2.fill(new RoundRectangle2D.Float(6,8,getWidth()-5,getHeight()-5,36,36));
                g2.setColor(new Color(255,252,255));
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth()-6,getHeight()-6,36,36));
                g2.setColor(PINK); g2.setStroke(new BasicStroke(2.5f));
                g2.draw(new RoundRectangle2D.Float(1,1,getWidth()-8,getHeight()-8,36,36));
                g2.setPaint(new GradientPaint(0,0,PINK,(getWidth()-6)*0.65f,0,new Color(255,105,180,0)));
                g2.fillRoundRect(2,2,getWidth()-9,5,3,3);
                g2.dispose();
            }
        };
        box.addMouseListener(block); box.addMouseMotionListener(block);

        JLabel icon=new JLabel("♡",SwingConstants.CENTER);
        icon.setFont(new Font("Tahoma",Font.BOLD,34)); icon.setForeground(PINK);
        icon.setBounds(0,22,500,42);

        JLabel title=new JLabel("ใส่ชื่อของคุณ",SwingConstants.CENTER);
        title.setFont(new Font("Tahoma",Font.BOLD,24)); title.setForeground(PINK);
        title.setBounds(0,68,500,36);

        JLabel subLbl=new JLabel("ชื่อจะแสดงตลอดการผจญภัย",SwingConstants.CENTER);
        subLbl.setFont(F_THAI); subLbl.setForeground(new Color(180,130,160));
        subLbl.setBounds(0,106,500,22);

        warningLabel=new JLabel("กรุณาใส่ชื่อก่อนเริ่มต้น",SwingConstants.CENTER);
        warningLabel.setFont(new Font("Tahoma",Font.BOLD,13));
        warningLabel.setForeground(new Color(210,50,80));
        warningLabel.setBounds(0,130,500,22); warningLabel.setVisible(false);

        inputField=new JTextField();
        inputField.setFont(new Font("Tahoma",Font.PLAIN,20));
        inputField.setHorizontalAlignment(JTextField.CENTER);
        inputField.setBackground(new Color(255,248,252)); inputField.setForeground(TEXT_DARK);
        inputField.setCaretColor(PINK);
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PINK_LIGHT,2),BorderFactory.createEmptyBorder(6,10,6,10)));
        inputField.setBounds(80,158,340,46);
        inputField.addKeyListener(new KeyAdapter(){
            @Override public void keyPressed(KeyEvent e){
                if (e.getKeyCode()==KeyEvent.VK_ENTER) startGameAction();
                if (e.getKeyCode()==KeyEvent.VK_ESCAPE) closeRegisterOverlay();
            }
        });

        JButton confirmBtn=new JButton("เริ่มต้นการเดินทาง"){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()?PINK.brighter():PINK);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),14,14));
                g2.dispose(); super.paintComponent(g);
            }
        };
        confirmBtn.setFont(new Font("Tahoma",Font.BOLD,17)); confirmBtn.setForeground(WHITE);
        confirmBtn.setOpaque(false); confirmBtn.setContentAreaFilled(false);
        confirmBtn.setBorderPainted(false); confirmBtn.setFocusPainted(false);
        confirmBtn.setBorder(new EmptyBorder(10,20,10,20));
        confirmBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        confirmBtn.setBounds(130,224,240,48);
        confirmBtn.addActionListener(e->startGameAction());

        JButton cancelBtn=new JButton("< ยกเลิก");
        cancelBtn.setFont(new Font("Tahoma",Font.PLAIN,13));
        cancelBtn.setForeground(new Color(180,140,160));
        cancelBtn.setOpaque(false); cancelBtn.setContentAreaFilled(false);
        cancelBtn.setBorderPainted(false); cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.setBounds(185,279,130,26);
        cancelBtn.addActionListener(e->closeRegisterOverlay());

        box.add(icon); box.add(title); box.add(subLbl);
        box.add(warningLabel); box.add(inputField);
        box.add(confirmBtn); box.add(cancelBtn);

        registerOverlay.add(box);  // ✅ เพิ่มแค่ box ไม่มี dimmer
        layeredPane.add(registerOverlay, Integer.valueOf(300));
    }

    private void showRegisterUI() {
        inputField.setText("");
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PINK_LIGHT,2),BorderFactory.createEmptyBorder(6,10,6,10)));
        warningLabel.setVisible(false);
        sizeAllLayers();
        registerOverlay.setVisible(true);
        SwingUtilities.invokeLater(()->inputField.requestFocusInWindow());
    }

    private void closeRegisterOverlay() {
        registerOverlay.setVisible(false);
        Main.mainFrame.requestFocusInWindow();
    }

    private void startGameAction() {
        String name=inputField.getText().trim();
        if (name.isEmpty()) {
            warningLabel.setVisible(true);
            inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210,50,80),2),BorderFactory.createEmptyBorder(6,10,6,10)));
            registerOverlay.repaint(); return;
        }
        GameConstants.PLAYER_NAME=name;
        Relation.getInstance().resetAll();
        UI_Components.RelationUI.getInstance().updateAllScores();
        closeRegisterOverlay();  // ✅ ปิดก่อน fade
        for (Component c:core.Main.mainContainer.getComponents())
            if (c instanceof PlaySceneMain) { ((PlaySceneMain)c).loadNewScene(StoryData.SCENE_1,"SCENE_1"); break; }
        startFadeOut();
    }

    private void startFadeOut() {
        stopMenuMusic();
        final float[] alpha={0f};
        Timer t=new Timer(20,null);
        t.addActionListener(e->{
            alpha[0]=Math.min(1f,alpha[0]+0.05f);
            Main.brightnessAlpha=alpha[0]; Main.repaintBrightness();
            if (alpha[0]>=1f) {
                t.stop(); Main.brightnessAlpha=0f; Main.repaintBrightness();
                Main.cardLayout.show(Main.mainContainer,"PLAY_PAGE");
            }
        });
        t.start();
    }

    private void showExitDialog() {
        JDialog d=new JDialog(Main.mainFrame,true);
        d.setUndecorated(true);
        JPanel p=new JPanel(null){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,250,253));
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),28,28));
                g2.setColor(PINK); g2.setStroke(new BasicStroke(2.5f));
                g2.draw(new RoundRectangle2D.Float(1,1,getWidth()-2,getHeight()-2,28,28));
                g2.dispose();
            }
        };
        p.setPreferredSize(new Dimension(360,180));
        JLabel lbl=new JLabel("คุณต้องการออกจากเกมใช่ไหม?",SwingConstants.CENTER);
        lbl.setFont(new Font("Tahoma",Font.BOLD,17)); lbl.setForeground(TEXT_DARK);
        lbl.setBounds(0,38,360,30);
        JButton yes=dialogBtn("ออกจากเกม",new Color(210,60,90),WHITE);
        JButton no=dialogBtn("ยังอยู่ต่อ",WHITE,PINK);
        yes.setBounds(40,104,130,42); no.setBounds(190,104,130,42);
        yes.addActionListener(e->System.exit(0)); no.addActionListener(e->d.dispose());
        p.add(lbl); p.add(yes); p.add(no);
        d.add(p); d.pack(); d.setLocationRelativeTo(Main.mainFrame); d.setVisible(true);
    }

    private JButton dialogBtn(String text,Color bg,Color fg){
        JButton b=new JButton(text){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()?bg.brighter():bg);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),12,12));
                if (bg.equals(WHITE)){ g2.setColor(PINK_LIGHT); g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,12,12)); }
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(new Font("Tahoma",Font.BOLD,15)); b.setForeground(fg);
        b.setOpaque(false); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void initHearts(int w,int h){ for(int i=0;i<N;i++) resetHeart(i,w,h,true); }
    private void resetHeart(int i,int w,int h,boolean scatter){
        hx[i]=rng.nextInt(Math.max(w,1)); hy[i]=scatter?rng.nextInt(Math.max(h,1)):h+20;
        hsize[i]=8+rng.nextFloat()*16f; hspd[i]=0.35f+rng.nextFloat()*0.65f;
        halpha[i]=0.06f+rng.nextFloat()*0.14f; hdrift[i]=(rng.nextFloat()-0.5f)*0.5f;
    }
    private void tickHearts(){
        int w=getWidth(),h=getHeight(); if(w<=0||h<=0) return;
        for(int i=0;i<N;i++){ hy[i]-=hspd[i]; hx[i]+=hdrift[i]; if(hy[i]<-30) resetHeart(i,w,h,false); }
    }
    private void paintBlob(Graphics2D g2,float cx,float cy,float r,Color c){
        g2.setColor(c); g2.fillOval((int)(cx-r),(int)(cy-r),(int)(r*2),(int)(r*2));
    }
    private void paintHeart(Graphics2D g2,int i){
        Composite prev=g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,halpha[i]));
        g2.setColor(PINK);
        double s=hsize[i]/10.0,x=hx[i],y=hy[i];
        Path2D path=new Path2D.Double();
        path.moveTo(x,y); path.curveTo(x,y-s*3,x-s*5,y-s*3,x-s*5,y);
        path.curveTo(x-s*5,y+s*3,x,y+s*5,x,y+s*8);
        path.curveTo(x,y+s*5,x+s*5,y+s*3,x+s*5,y);
        path.curveTo(x+s*5,y-s*3,x,y-s*3,x,y);
        g2.fill(path); g2.setComposite(prev);
    }
    private Color blend(Color a,Color b,float t){
        return new Color(clamp((int)(a.getRed()+(b.getRed()-a.getRed())*t)),
            clamp((int)(a.getGreen()+(b.getGreen()-a.getGreen())*t)),
            clamp((int)(a.getBlue()+(b.getBlue()-a.getBlue())*t)),
            clamp((int)(a.getAlpha()+(b.getAlpha()-a.getAlpha())*t)));
    }
    private int clamp(int v){ return Math.max(0,Math.min(255,v)); }
    private void startMenuMusic(){ Main.soundManager.playBGM(GameConstants.SOUND_PATH+"music_mainmenu.wav"); }
    public void stopMenuMusic(){ if(bgmLoopTimer!=null) bgmLoopTimer.stop(); Main.soundManager.stopBGM(); }
}