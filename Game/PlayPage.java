import javax.swing.*;
import java.awt.*;

public class PlayPage extends JPanel {
    public PlayPage(Font tFont) {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK); // ให้พื้นหลังเป็นสีดำเตรียมไว้

        JLabel label = new JLabel("--- เข้าสู่เนื้อเรื่อง ---", SwingConstants.CENTER);
        label.setFont(tFont);
        label.setForeground(Color.WHITE);
        add(label, BorderLayout.CENTER);
    }
}