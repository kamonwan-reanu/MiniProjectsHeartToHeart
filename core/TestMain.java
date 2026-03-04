package core;

import javax.swing.*;
import UI_Screens.PlaySceneMain;
import model.StoryData;
import model.GameConstants;
import model.GameState;

import java.awt.*;

public class TestMain {
    public static void main(String[] args) {

        GameState.reset();           
        GameConstants.PLAYER_NAME = "Ahri";
        
        // ✅ สำคัญมาก: กันคะแนนค้างเวลาเทสฉากซ้ำ
        model.GameState.reset();

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Scene Testing Tool - Ahri");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLayout(new BorderLayout());

            // เลือกซีนที่ต้องการทดสอบตรงนี้ (เช่น SCENE_5)
            PlaySceneMain testScene = new PlaySceneMain(
                StoryData.SCENE_13, 
                null, 
                "SCENE_13"
            );

            frame.add(testScene, BorderLayout.CENTER);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // บังคับกระตุ้นระบบหลังจาก Frame โชว์แล้ว
            SwingUtilities.invokeLater(() -> {
                testScene.loadNewScene(StoryData.SCENE_13, "SCENE_13");
                testScene.revalidate();
                testScene.repaint();
                testScene.requestFocusInWindow();
            });
        });
    }
}