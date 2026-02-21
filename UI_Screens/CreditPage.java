package UI_Screens;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import core.Main; 

public class CreditPage extends JPanel {
    public CreditPage(Font tFont, Font bFont) {
        setLayout(new BorderLayout());
        setBackground(new Color(230, 230, 250)); // ✅ สีเดียวกับ SettingPage
        setBorder(new EmptyBorder(50, 50, 50, 50));

        // --- 1. หัวข้อหน้า (จัดกลาง) ---
        JLabel titleLabel = new JLabel("เกี่ยวกับคนสร้าง", JLabel.CENTER);
        titleLabel.setFont(tFont);
        titleLabel.setForeground(new Color(50, 50, 50));
        add(titleLabel, BorderLayout.NORTH);

        // --- 2. ส่วนเนื้อหา (ใช้ Wrapper เพื่อล็อคให้อยู่ตรงกลาง) ---
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        JPanel contentCard = new JPanel(new GridBagLayout());
        contentCard.setOpaque(false);
        contentCard.setPreferredSize(new Dimension(650, 400)); // ✅ ล็อคขนาดไม่ให้ยืดตามหน้าจอ

        setupCreditLogic(contentCard);
        centerWrapper.add(contentCard);
        add(centerWrapper, BorderLayout.CENTER);

        // --- 3. ส่วนปุ่มย้อนกลับ (จัดกลาง) ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        
        JButton backBtn = new JButton("ย้อนกลับ");
        backBtn.setFont(new Font("Tahoma", Font.BOLD, 22)); 
        backBtn.setPreferredSize(new Dimension(200, 60));   
        backBtn.setFocusable(false);
        backBtn.setBackground(Color.WHITE);
        
        backBtn.addActionListener(e -> Main.cardLayout.show(Main.mainContainer, "MENU"));
        bottomPanel.add(backBtn);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupCreditLogic(JPanel content) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 20, 15, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Tahoma", Font.BOLD, 22);
        Font valueFont = new Font("Tahoma", Font.PLAIN, 22);

        // แถวที่ 1: สร้างโดย
        gbc.gridy = 0; gbc.gridx = 0; gbc.weightx = 0.4;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel creatorLabel = new JLabel("สร้างโดย:");
        creatorLabel.setFont(labelFont);
        content.add(creatorLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.6;
        JLabel creatorName = new JLabel("Ahri"); 
        creatorName.setFont(valueFont);
        content.add(creatorName, gbc);

        // แถวที่ 2: เวอร์ชันเกม
        gbc.gridy = 1; gbc.gridx = 0; gbc.weightx = 0.4;
        JLabel versionLabel = new JLabel("เวอร์ชันเกม:");
        versionLabel.setFont(labelFont);
        content.add(versionLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.6;
        JLabel versionValue = new JLabel("0.1 Alpha");
        versionValue.setFont(valueFont);
        content.add(versionValue, gbc);

        // แถวที่ 3: ติดต่อ
        gbc.gridy = 2; gbc.gridx = 0; gbc.weightx = 0.4;
        JLabel contactLabel = new JLabel("ติดต่อ:");
        contactLabel.setFont(labelFont);
        content.add(contactLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.6;
        JLabel contactValue = new JLabel("yourname@email.com");
        contactValue.setFont(valueFont);
        content.add(contactValue, gbc);
    }
}