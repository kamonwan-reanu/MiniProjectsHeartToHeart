import javax.swing.*;
import java.awt.*;

public class CreditPage extends JPanel {
    public CreditPage(Font tFont, Font bFont) {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 255, 240));
        
        JLabel info = new JLabel("<html><div style='text-align: center;'>สร้างโดย: [ชื่อของคุณ]<br>เวอร์ชัน: 0.1 Alpha</div></html>", SwingConstants.CENTER);
        info.setFont(new Font("Tahoma", Font.PLAIN, 30));
        add(info, BorderLayout.CENTER);

        JButton backBtn = new JButton("ย้อนกลับ");
        backBtn.setFont(bFont);
        backBtn.addActionListener(e -> Main.cardLayout.show(Main.mainContainer, "MENU"));
        add(backBtn, BorderLayout.SOUTH);
    }
}