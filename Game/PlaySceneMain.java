import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PlaySceneMain extends JPanel {
    private int storyIndex = 0;
    private String fullText = "";
    private int charIndex = 0;
    
    private JLabel bgLayer, characterLayer, nameLabel, speechText;
    private JPanel dialogueGroup, whiteBox;
    private Timer typeTimer;

    public PlaySceneMain() {
        setLayout(null);
        setupUIComponents();
        
        // --- [จุดสำคัญ 1: ระบบ Responsive] ---
        // เพิ่ม Listener เพื่อคำนวณตำแหน่งใหม่ทุกครั้งที่มีการเปลี่ยนขนาดหน้าจอ
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateUIStyles(); // สั่งให้คำนวณพิกัดใหม่ตามขนาดจอจริง
            }
        });

        if (StoryData.SCENE_1.length > 0) {
            startStory(StoryData.SCENE_1[storyIndex]);
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleInteraction();
            }
        });
    }

    private void setupUIComponents() {
        // สร้างพื้นหลังและตัวละคร (กำหนด Bounds เริ่มต้นไปก่อน เดี๋ยว updateUIStyles จะจัดการให้)
        bgLayer = new JLabel();
        characterLayer = new JLabel();
        characterLayer.setHorizontalAlignment(SwingConstants.CENTER);
        
        // โหลดรูปภาพ (ตรวจสอบ Path ให้ถูกต้อง)
        bgLayer.setIcon(new ImageIcon("img/school_bg.jpg"));
        characterLayer.setIcon(new ImageIcon("img/ahri.png"));

        // สร้างโครงสร้าง Dialogue (ยังไม่กำหนด Bounds ตายตัว)
        dialogueGroup = new JPanel(null);
        dialogueGroup.setOpaque(false);

        nameLabel = new JLabel("อวี่เชิน", SwingConstants.CENTER);
        nameLabel.setOpaque(true);
        nameLabel.setBackground(new Color(40, 40, 40)); 
        nameLabel.setForeground(new Color(255, 215, 0)); 

        whiteBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 240));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2d.setColor(new Color(212, 175, 55));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 25, 25);
                g2d.dispose();
            }
        };
        whiteBox.setLayout(null);
        whiteBox.setOpaque(false);

        speechText = new JLabel("");
        speechText.setVerticalAlignment(SwingConstants.TOP);

        // ประกอบร่าง
        whiteBox.add(speechText);
        dialogueGroup.add(nameLabel);
        dialogueGroup.add(whiteBox);

        add(dialogueGroup);  
        add(characterLayer); 
        add(bgLayer);        
    }

    // --- [จุดสำคัญ 2: ฟังก์ชันคำนวณตำแหน่งตามสัดส่วนจอ] ---
    private void updateUIStyles() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        // 1. ปรับพื้นหลังและตัวละครให้เต็มจอเสมอ
        bgLayer.setBounds(0, 0, w, h);
        characterLayer.setBounds(0, 0, w, h);

        // 2. คำนวณขนาดกลุ่มข้อความ (กว้าง 85% ของจอ, สูง 25% ของจอ)
        int groupW = (int)(w * 0.85);
        int groupH = (int)(h * 0.25);
        int groupX = (w - groupW) / 2;
        int groupY = h - groupH - (int)(h * 0.05); // ลอยจากขอบล่าง 5%

        dialogueGroup.setBounds(groupX, groupY, groupW, groupH);

        // 3. ปรับขนาดป้ายชื่อ (Name Tag)
        int nameW = (int)(groupW * 0.18);
        int nameH = (int)(groupH * 0.22);
        nameLabel.setBounds(30, 0, nameW, nameH);
        nameLabel.setFont(new Font("Tahoma", Font.BOLD, Math.max(14, h / 35)));

        // 4. ปรับขนาดกล่องขาว (White Box)
        int boxY = (int)(nameH * 0.8); // ให้ป้ายชื่อทับกล่องนิดหน่อย
        whiteBox.setBounds(0, boxY, groupW, groupH - boxY);

        // 5. ปรับขนาดตัวหนังสือ (Speech Text)
        speechText.setBounds(30, 25, groupW - 60, groupH - boxY - 40);
        speechText.setFont(new Font("Tahoma", Font.PLAIN, Math.max(16, h / 28)));

        // บังคับให้วาดหน้าจอใหม่
        revalidate();
        repaint();
    }

    private void handleInteraction() {
        if (typeTimer != null && typeTimer.isRunning()) {
            typeTimer.stop();
            speechText.setText(formatHTML(fullText));
        } else {
            storyIndex++;
            if (storyIndex < StoryData.SCENE_1.length) {
                startStory(StoryData.SCENE_1[storyIndex]);
            }
        }
    }

    private void startStory(String text) {
        fullText = text;
        charIndex = 0;
        if (typeTimer != null) typeTimer.stop();
        typeTimer = new Timer(20, e -> {
            if (charIndex < fullText.length()) {
                charIndex++;
                speechText.setText(formatHTML(fullText.substring(0, charIndex)));
            } else { typeTimer.stop(); }
        });
        typeTimer.start();
    }

    private String formatHTML(String t) {
        // ใช้ width ใน HTML เพื่อให้ตัดคำอัตโนมัติเมื่อจอเล็ก
        return "<html><body style='width: " + (speechText.getWidth() - 20) + "px;'>" + t + "</body></html>";
    }
}