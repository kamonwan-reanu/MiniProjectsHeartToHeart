import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CreditPage extends JPanel {
    public CreditPage(Font tFont, Font bFont) {
        setLayout(new BorderLayout());
        setBackground(new Color(235, 245, 255)); // สีพื้นหลังโทนเดียวกับหน้าตั้งค่า
        setBorder(new EmptyBorder(50, 50, 50, 50));

        // 1. หัวข้อหน้า (ชิดซ้ายบน)
        JLabel label = new JLabel("เกี่ยวกับคนสร้าง");
        label.setFont(tFont);
        label.setForeground(new Color(50, 50, 50));
        add(label, BorderLayout.NORTH);

        // 2. ส่วนเนื้อหา (ใช้ GridBagLayout ให้เหมือนหน้า Setting)
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 20, 15, 20);
        gbc.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("Tahoma", Font.BOLD, 22);
        Font valueFont = new Font("Tahoma", Font.PLAIN, 22);

        // แถวที่ 1: ชื่อผู้สร้าง
        gbc.gridy = 0; gbc.gridx = 0;
        content.add(new JLabel("สร้างโดย:") {{ setFont(labelFont); }}, gbc);
        gbc.gridx = 1;
        content.add(new JLabel("[ชื่อของคุณ]") {{ setFont(valueFont); }}, gbc);

        // แถวที่ 2: เวอร์ชัน
        gbc.gridy = 1; gbc.gridx = 0;
        content.add(new JLabel("เวอร์ชันเกม:") {{ setFont(labelFont); }}, gbc);
        gbc.gridx = 1;
        content.add(new JLabel("0.1 Alpha") {{ setFont(valueFont); }}, gbc);

        // แถวที่ 3: ช่องทางติดต่อ/เครดิตอื่นๆ
        gbc.gridy = 2; gbc.gridx = 0;
        content.add(new JLabel("ติดต่อ:") {{ setFont(labelFont); }}, gbc);
        gbc.gridx = 1;
        content.add(new JLabel("yourname@email.com") {{ setFont(valueFont); }}, gbc);

        add(content, BorderLayout.CENTER);

        // 3. ปุ่มย้อนกลับ (ปรับแต่งใหม่ให้เหมือนหน้าตั้งค่า)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setOpaque(false);
        
        JButton backBtn = new JButton("ย้อนกลับ");
        
        // --- [ส่วนที่ปรับปรุงใหม่] ---
        backBtn.setFont(new Font("Tahoma", Font.BOLD, 22)); // ปรับฟอนต์ให้ใหญ่ขึ้น
        backBtn.setPreferredSize(new Dimension(200, 60));   // ปรับขนาดปุ่มให้ใหญ่และยาวขึ้น
        backBtn.setFocusable(false);
        backBtn.setBackground(new Color(240, 248, 255));    // เปลี่ยนเป็นสีขาวนวล/ฟ้าอ่อน
        
        // เพิ่มเส้นขอบ (Border) ให้ดูมีมิติเหมือนในรูป
        backBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 200, 220), 1), // เส้นขอบนอก
            BorderFactory.createEmptyBorder(5, 15, 5, 15)                // ระยะห่างข้อความข้างใน
        ));
        // ---------------------------

        backBtn.addActionListener(e -> Main.cardLayout.show(Main.mainContainer, "MENU"));
        bottomPanel.add(backBtn);
        
        add(bottomPanel, BorderLayout.SOUTH); // วางปุ่มไว้ที่ขอบล่างของหน้าจอ
    }
}