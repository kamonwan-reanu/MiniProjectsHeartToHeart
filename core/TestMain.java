package core;

import UI_Screens.PlaySceneMain;
import java.awt.*;
import javax.swing.*;
import model.StoryData;

public class TestMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Testing PlaySceneMain");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            // ✅ ใช้ขนาด 800x600 มาตรฐาน
            frame.setSize(800, 600);
            frame.setLayout(new BorderLayout());

            // ✅ แก้ไขตรงนี้: ลบวงเล็บปิดที่เกินมา และลบ "PLAY_SCENE" ออก
            // เพราะในไฟล์ทดสอบนี้เราแอดลง BorderLayout โดยตรง ไม่ได้ใช้ CardLayout ค่ะ
            // ใน TestMain.java
            PlaySceneMain testScene = new PlaySceneMain(
                StoryData.SCENE_3,      // ✅ เริ่มที่ฉาก 2
                null,                   // ✅ ไม่ต้องระบุชื่อตัวละครในฉากนี้
                "SCENE_3"               // ✅ ระบุชื่อว่าเป็นฉาก 
            );

            frame.add(testScene, BorderLayout.CENTER);

            // ✅ เรียกคำสั่งจัด Layout ให้แสดงผลถูกต้อง
            testScene.revalidate();
            
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}