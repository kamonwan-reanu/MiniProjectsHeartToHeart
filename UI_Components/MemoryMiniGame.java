package UI_Components;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.*;

public class MemoryMiniGame extends JPanel {
    private String[] emojis = {"❤️", "💙", "💛", "💚", "❤️", "💙", "💛", "💚"}; 
    private ArrayList<JButton> buttons = new ArrayList<>();
    private JButton firstClicked = null;
    private JButton secondClicked = null;
    private int pairsFound = 0;
    private Runnable onWinAction;

    public MemoryMiniGame(Runnable onWin) {
        this.onWinAction = onWin;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(500, 400));
        setBackground(new Color(255, 240, 245));
        setBorder(BorderFactory.createLineBorder(new Color(255, 105, 180), 5));

        JLabel title = new JLabel("จับคู่หัวใจให้ธีร์", SwingConstants.CENTER);
        title.setFont(new Font("Tahoma", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 4, 10, 10));
        grid.setOpaque(false);

        ArrayList<String> emojiList = new ArrayList<>();
        for (String s : emojis) emojiList.add(s);
        Collections.shuffle(emojiList);

        for (int i = 0; i < 8; i++) {
            JButton btn = new JButton("?");
            btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 30));
            String emoji = emojiList.get(i);
            btn.addActionListener(e -> {
                if (btn == firstClicked || btn.getText().equals("") || secondClicked != null) return;
                btn.setText(emoji);
                if (firstClicked == null) { firstClicked = btn; } 
                else { secondClicked = btn; checkMatch(); }
            });
            buttons.add(btn);
            grid.add(btn);
        }
        add(grid, BorderLayout.CENTER);
    }

    private void checkMatch() {
        Timer timer = new Timer(500, e -> {
            if (firstClicked.getText().equals(secondClicked.getText())) {
                firstClicked.setEnabled(false);
                secondClicked.setEnabled(false);
                pairsFound++;
                if (pairsFound == 4) onWinAction.run();
            } else {
                firstClicked.setText("?");
                secondClicked.setText("?");
            }
            firstClicked = null;
            secondClicked = null;
        });
        timer.setRepeats(false);
        timer.start();
    }
}