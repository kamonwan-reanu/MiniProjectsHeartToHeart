package core;

import javax.swing.*;
import UI_Screens.PlaySceneMain;
import model.StoryData;
import model.GameConstants;
import java.awt.*;

public class TestMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Testing PlaySceneMain");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            // ✅ ใช้ขนาด 800x600 ที่ Ahri ตั้งไว้เป็นค่ามาตรฐาน
            frame.setSize(800, 600);
            frame.setLayout(new BorderLayout());

            // ✅ สร้างหน้า PlaySceneMain โดยส่งข้อมูลทดสอบเข้าไป
            // หนูสามารถเปลี่ยนฉากหรือตัวละครตรงนี้เพื่อเช็คความเรียบร้อยได้เลยค่ะ
            PlaySceneMain testScene = new PlaySceneMain(
                StoryData.SCENE_1, 
                GameConstants.BG_SCHOOL, 
                GameConstants.CHAR_AHRI
            );

            frame.add(testScene, BorderLayout.CENTER);

            // ✅ เรียกคำสั่งจัด Layout ที่เราเขียนไว้เพื่อให้ UI แสดงผลถูกต้อง
            testScene.revalidate();
            
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
