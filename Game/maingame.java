import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class maingame {
    public static void main(String[] args) {
        // --- ส่วนที่ 1: ตั้งค่า Font ภาษาไทย ---
        Font thaiFont = new Font("Tahoma", Font.PLAIN, 18);
        UIManager.put("Button.font", thaiFont);
        UIManager.put("Label.font", thaiFont);

        JFrame frame = new JFrame("HeartToHeart - Show Character");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // --- ส่วนที่ 2: พื้นที่แสดงรูป (JLabel) ---
        JLabel imageLabel = new JLabel("กดปุ่มด้านล่างเพื่อเริ่มการสนทนา", SwingConstants.CENTER);
        frame.add(imageLabel, BorderLayout.CENTER);

        // --- ส่วนที่ 3: ปุ่มกด ---
        JButton showButton = new JButton("พบเธอครั้งแรก");
        showButton.setPreferredSize(new Dimension(800, 60)); // กำหนดขนาดปุ่มให้ชัดเจน
        frame.add(showButton, BorderLayout.SOUTH);

        // --- ส่วนที่ 4: Action เมื่อกดปุ่ม ---
        showButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // 1. โหลดรูป (ตรวจสอบว่าไฟล์ชื่อ character.png อยู่ในโฟลเดอร์นอกสุดของโปรเจกต์)
                    ImageIcon icon = new ImageIcon("img/ahri.png");
                    
                    // 2. ปรับขนาดรูปให้พอดีหน้าจอ (สูง 450 pixels)
                    Image img = icon.getImage();
                    Image resizedImg = img.getScaledInstance(-1, 450, Image.SCALE_SMOOTH);
                    
                    // 3. นำรูปใส่ Label และล้างข้อความเก่าออก
                    imageLabel.setIcon(new ImageIcon(resizedImg));
                    imageLabel.setText(""); 
                    
                    // 4. สั่งให้โปรแกรมวาดหน้าจอใหม่ (สำคัญมาก!)
                    frame.revalidate();
                    frame.repaint();
                    
                    showButton.setText("ยินดีที่ได้รู้จัก!");
                    
                } catch (Exception ex) {
                    imageLabel.setText("หาไฟล์รูปไม่เจอ! ตรวจสอบชื่อไฟล์ ahri.png");
                }
            }
        });

        frame.setVisible(true);
    }
}