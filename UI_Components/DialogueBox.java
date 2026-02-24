package UI_Components;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DialogueBox extends JPanel {
    private JLabel nameLabel;
    private JTextArea speechText;
    private JScrollPane scrollPane;
    private JPanel mainBox;
    private JPanel nextButton;
    private Runnable onNextRequested;
    private int lastW = 0, lastH = 0;

    public DialogueBox() {
        setLayout(null);
        setOpaque(false);
        initUI();
        setupClickHandler();
    }

    public void setOnNextRequested(Runnable action) {
        this.onNextRequested = action;
    }

    private void initUI() {
        mainBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) { drawBackground((Graphics2D) g); }
        };
        mainBox.setLayout(null);
        mainBox.setOpaque(false);

        // 🎨 ชื่อตัวละคร
        nameLabel = new JLabel("");
        nameLabel.setForeground(new Color(100, 210, 255)); 
        nameLabel.setFont(new Font("Tahoma", Font.BOLD, 22));
        nameLabel.setVisible(false);

        // 📝 ข้อความคำพูด
        speechText = new JTextArea("");
        speechText.setEditable(false);
        speechText.setOpaque(false);
        speechText.setFocusable(false);
        speechText.setLineWrap(true);      
        speechText.setWrapStyleWord(true);
        speechText.setForeground(new Color(245, 245, 245));
        speechText.setFont(new Font("Tahoma", Font.PLAIN, 20));

        // ✨ ScrollPane พร้อมชุดแต่ง
        scrollPane = new JScrollPane(speechText);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // 🎨 ใส่ UI ScrollBar สีทองที่แต่งใหม่
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0)); 
        scrollPane.getVerticalScrollBar().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // 🔘 ปุ่ม Next
        nextButton = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) { drawNextButton((Graphics2D) g); }
        };
        nextButton.setOpaque(false);

        mainBox.add(nameLabel);
        mainBox.add(scrollPane); 
        mainBox.add(nextButton);
        add(mainBox);
    }

    private void drawBackground(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = mainBox.getWidth();
        int h = mainBox.getHeight();
        g2d.setColor(new Color(15, 20, 35, 230));
        g2d.fillRect(0, 0, w, h);
        g2d.setColor(new Color(212, 175, 55, 180)); 
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawRect(5, 5, w - 10, h - 10);
    }

    private void drawNextButton(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w = nextButton.getWidth();
        int h = nextButton.getHeight();
        int centerX = w / 2;
        int centerY = h / 2;

        // 🎨 1. วาดวงกลมขอบนอกสุด (เส้นบางๆ สีฟ้าอ่อน)
        g2d.setColor(new Color(150, 230, 255, 255)); // สีฟ้าสว่าง
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval(2, 2, w - 5, h - 5);

        // 🎨 2. วาดพื้นหลังวงกลมด้านใน (สีฟ้าเข้มกึ่งโปร่งใส)
        g2d.setColor(new Color(80, 180, 230, 150)); 
        g2d.fillOval(5, 5, w - 11, h - 11);

        // 🎨 3. วาดเส้นขอบวงกลมชั้นใน (เพื่อให้ดูมีมิติ)
        g2d.setColor(new Color(180, 240, 255, 200));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawOval(5, 5, w - 11, h - 11);

        // 🎨 4. วาดลูกศรคู่ (>>) สีเข้ม
        g2d.setColor(new Color(20, 40, 60, 230)); // สีน้ำเงินเข้มเกือบดำตามรูป
        
        // ลูกศรตัวซ้าย
        int[] x1 = {centerX - 6, centerX - 6, centerX}; 
        int[] y1 = {centerY - 7, centerY + 7, centerY};
        g2d.fillPolygon(x1, y1, 3);
        
        // ลูกศรตัวขวา
        int[] x2 = {centerX, centerX, centerX + 6}; 
        int[] y2 = {centerY - 7, centerY + 7, centerY};
        g2d.fillPolygon(x2, y2, 3);
    }

    private void setupClickHandler() {
        MouseAdapter listener = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { if (onNextRequested != null) onNextRequested.run(); }
        };
        this.addMouseListener(listener);
        mainBox.addMouseListener(listener);
        speechText.addMouseListener(listener);
    }

    private void updateInsideLayout(int width, int height) {
        if (nameLabel.isVisible()) {
            nameLabel.setBounds(30, 15, width - 120, 30);
            // ✨ ปรับความสูงให้รองรับ 3 บรรทัด (height - 95)
            scrollPane.setBounds(30, 50, width - 130, height - 95); 
        } else {
            scrollPane.setBounds(30, 30, width - 130, height - 75);
        }
        
        // ✅ ย้ายมาตั้งค่าตรงนี้แทน เพราะ nextButton อยู่ในคลาสนี้
        // วางไว้มุมขวาล่างของกล่อง (width - 75, height - 70)
        nextButton.setBounds(width - 75, height - 70, 50, 50);
    }

    public void updateLayout(int groupW, int groupH, int parentH) {
    if (getParent() == null) return;
    this.lastW = groupW; this.lastH = groupH;
    this.setBounds(0, 0, getParent().getWidth(), parentH);
    int x = (getParent().getWidth() - groupW) / 2;
    int y = parentH - groupH - 30; 
    mainBox.setBounds(x, y, groupW, groupH);
    updateInsideLayout(groupW, groupH);

}

    public void moveTo(int groupW, int groupH, int targetY) {
    if (getParent() == null) return;
    this.lastW = groupW; this.lastH = groupH;
    this.setBounds(0, 0, getParent().getWidth(), getParent().getHeight());
    int x = (getParent().getWidth() - groupW) / 2;
    mainBox.setBounds(x, targetY, groupW, groupH);
    updateInsideLayout(groupW, groupH);

    // ✨ แผนแทรกซึม (ย้ายมาไว้ใน moveTo เพราะ PlaySceneMain เรียกใช้ตัวนี้ตลอดเวลา)
    Container parent = getParent(); 
    UI_Components.RelationUI relUI = UI_Components.RelationUI.getInstance();
    
    boolean isPresent = false;
    for (Component c : parent.getComponents()) {
        if (c instanceof UI_Components.RelationUI) { 
            isPresent = true; 
            break; 
        }
    }
    
    if (!isPresent) {
        parent.add(relUI);
        // ดันมาหน้าสุด (Index 0) เพื่อให้ทับเลเยอร์ Effect ของเพื่อน
        parent.setComponentZOrder(relUI, 0); 
    }
    
    // บังคับให้หลอดอัปเดตตำแหน่งตามขนาดจอ
    relUI.updateBounds(parent.getWidth(), parent.getHeight());
    relUI.setVisible(true);

    revalidate(); 
    repaint();
    parent.repaint(); // สั่งให้ PlaySceneMain (หน้าจอเพื่อน) วาดหลอดออกมา
}

    public void setText(String name, String text) {
        speechText.setText(text);
        speechText.setCaretPosition(speechText.getDocument().getLength());

        if (name != null && !name.trim().isEmpty() && !name.equalsIgnoreCase("none")) {
            nameLabel.setText(name.trim().toUpperCase());
            nameLabel.setVisible(true);
        } else {
            nameLabel.setVisible(false);
        }
        nextButton.setVisible(true);
        if (lastW > 0) updateInsideLayout(lastW, lastH);
        repaint();
    }
    
}

