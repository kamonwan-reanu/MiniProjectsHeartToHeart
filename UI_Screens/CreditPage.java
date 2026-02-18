package UI_Screens; // 1. ระบุตำแหน่งโฟลเดอร์

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
// 2. Import Main มาจาก package core
import core.Main; 

public class CreditPage extends JPanel {
    public CreditPage(Font tFont, Font bFont) {
        setLayout(new BorderLayout());
        setBackground(new Color(235, 245, 255)); 
        setBorder(new EmptyBorder(50, 50, 50, 50));

        // 1. หัวข้อหน้า
        JLabel label = new JLabel("เกี่ยวกับคนสร้าง");
        label.setFont(tFont);
        label.setForeground(new Color(50, 50, 50));
        add(label, BorderLayout.NORTH);

        // 2. ส่วนเนื้อหา
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 20, 15, 20);
        gbc.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("Tahoma", Font.BOLD, 22);
        Font valueFont = new Font("Tahoma", Font.PLAIN, 22);

        // แถวที่ 1: ชื่อผู้สร้าง
        gbc.gridy = 0; gbc.gridx = 0;
        JLabel creatorLabel = new JLabel("สร้างโดย:");
        creatorLabel.setFont(labelFont);
        content.add(creatorLabel, gbc);
        
        gbc.gridx = 1;
        JLabel creatorName = new JLabel("Ahri"); // เปลี่ยนเป็นชื่อคุณได้เลยนะคะ
        creatorName.setFont(valueFont);
        content.add(creatorName, gbc);

        // แถวที่ 2: เวอร์ชัน
        gbc.gridy = 1; gbc.gridx = 0;
        JLabel versionLabel = new JLabel("เวอร์ชันเกม:");
        versionLabel.setFont(labelFont);
        content.add(versionLabel, gbc);
        
        gbc.gridx = 1;
        JLabel versionValue = new JLabel("0.1 Alpha");
        versionValue.setFont(valueFont);
        content.add(versionValue, gbc);

        // แถวที่ 3: ช่องทางติดต่อ
        gbc.gridy = 2; gbc.gridx = 0;
        JLabel contactLabel = new JLabel("ติดต่อ:");
        contactLabel.setFont(labelFont);
        content.add(contactLabel, gbc);
        
        gbc.gridx = 1;
        JLabel contactValue = new JLabel("yourname@email.com");
        contactValue.setFont(valueFont);
        content.add(contactValue, gbc);

        add(content, BorderLayout.CENTER);

        // 3. ปุ่มย้อนกลับ
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setOpaque(false);
        
        JButton backBtn = new JButton("ย้อนกลับ");
        backBtn.setFont(new Font("Tahoma", Font.BOLD, 22)); 
        backBtn.setPreferredSize(new Dimension(200, 60));   
        backBtn.setFocusable(false);
        backBtn.setBackground(new Color(240, 248, 255));    
        
        backBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 200, 220), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

        // แก้ไขให้เรียกผ่าน Main.cardLayout
        backBtn.addActionListener(e -> Main.cardLayout.show(Main.mainContainer, "MENU"));
        bottomPanel.add(backBtn);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
}