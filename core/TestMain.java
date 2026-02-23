package core;

import javax.swing.*;
import UI_Screens.PlaySceneMain;
import model.StoryData;
import model.GameConstants;
import java.awt.*;

public class TestMain {
    public static void main(String[] args) {
        GameConstants.PLAYER_NAME = "Ahri"; 

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Scene Testing Tool - Ahri");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLayout(new BorderLayout());

            // เลือกซีนที่ต้องการทดสอบตรงนี้ (เช่น SCENE_5)
            PlaySceneMain testScene = new PlaySceneMain(
                StoryData.SCENE_5, 
                null, 
                "SCENE_5"
            );

            frame.add(testScene, BorderLayout.CENTER);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // บังคับกระตุ้นระบบหลังจาก Frame โชว์แล้ว
            SwingUtilities.invokeLater(() -> {
                testScene.loadNewScene(StoryData.SCENE_5, "SCENE_5");
                testScene.revalidate();
                testScene.repaint();
                testScene.requestFocusInWindow();
            });
        });
    }
}