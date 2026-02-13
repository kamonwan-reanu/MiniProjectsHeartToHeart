import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainMenu extends JPanel {
    public MainMenu(Font titleFont, Font menuFont) {
        setLayout(new BorderLayout());
        setBackground(new Color(255, 230, 240));
        setBorder(new EmptyBorder(50, 50, 50, 50));

        JLabel gameName = new JLabel("HeartToHeart", SwingConstants.CENTER);
        gameName.setFont(titleFont);
        gameName.setForeground(new Color(255, 105, 180));
        add(gameName, BorderLayout.NORTH);

        JPanel menuButtonPanel = new JPanel(new GridBagLayout());
        menuButtonPanel.setOpaque(false);
        JPanel buttonBox = new JPanel(new GridLayout(5, 1, 0, 15));
        buttonBox.setOpaque(false);

        JButton[] buttons = {
            new JButton("เริ่มเกม"), new JButton("บันทึกเกม"), 
            new JButton("ตั้งค่า"), new JButton("เกี่ยวกับคนสร้าง"), 
            new JButton("ออกจากเกม")
        };

        for (JButton btn : buttons) {
            btn.setFont(menuFont);
            btn.setPreferredSize(new Dimension(350, 60));
            btn.setBackground(Color.WHITE);
            btn.setFocusable(false);
            buttonBox.add(btn);
        }

        buttons[2].addActionListener(e -> Main.cardLayout.show(Main.mainContainer, "SETTING"));
        buttons[3].addActionListener(e -> Main.cardLayout.show(Main.mainContainer, "CREDIT"));
        buttons[4].addActionListener(e -> showCustomExitDialog());

        menuButtonPanel.add(buttonBox);
        add(menuButtonPanel, BorderLayout.CENTER);
    }

    private void showCustomExitDialog() {
        JDialog exitDialog = new JDialog(Main.mainFrame, "ยืนยัน", true);
        exitDialog.setUndecorated(true);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 240, 245));
        panel.setBorder(BorderFactory.createLineBorder(new Color(255, 105, 180), 3));

        JLabel label = new JLabel("คุณต้องการออกจากเกมใช่ไหม?", SwingConstants.CENTER);
        label.setFont(new Font("Tahoma", Font.BOLD, 18));
        label.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(label, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        btnPanel.setOpaque(false);
        JButton yes = new JButton("ใช่"); JButton no = new JButton("ไม่");
        yes.addActionListener(e -> System.exit(0));
        no.addActionListener(e -> exitDialog.dispose());
        
        btnPanel.add(yes); btnPanel.add(no);
        panel.add(btnPanel, BorderLayout.CENTER);
        exitDialog.add(panel);
        exitDialog.pack();
        exitDialog.setLocationRelativeTo(Main.mainFrame);
        exitDialog.setVisible(true);
    }
}