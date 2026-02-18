package UI_Components;

import javax.swing.*;
import java.awt.*;

public class CharacterSprite extends JLabel {
    private Image characterImage;
    private String currentPath; // ✅ เก็บ Path ปัจจุบันไว้เช็ค

    public CharacterSprite(String imagePath) {
        // ✅ เรียกใช้เมธอดโหลดรูปตั้งแต่สร้าง Object
        updateCharacter(imagePath);
        
        // ตั้งค่าให้ตัวละครอยู่กึ่งกลาง
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (characterImage != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            // ✅ เปิดโหมดภาพเนียน (Interpolation)
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
            // ปรับขนาดรูปให้เหมาะสมกับความสูงหน้าจอ (80% ของจอ)
            int newHeight = (int) (getHeight() * 0.8);
            int newWidth = (int) (characterImage.getWidth(null) * ((double) newHeight / characterImage.getHeight(null)));
            
            // วาดตัวละครลงบนจอ
            int x = (getWidth() - newWidth) / 2;
            int y = getHeight() - newHeight;
            g2d.drawImage(characterImage, x, y, newWidth, newHeight, null);
            
            g2d.dispose();
        }
        // ไม่ต้อง super.paintComponent(g) ก็ได้ถ้าเราวาดเองหมดแล้วค่ะ
    }

    // ✅ เมธอดสำหรับเปลี่ยนรูปตัวละคร
    public void updateCharacter(String newPath) {
        if (newPath == null || newPath.equals(currentPath)) return; // ถ้าเป็นรูปเดิมไม่ต้องโหลดใหม่

        try {
            // ✅ โหลดรูปจาก Resource (ใช้ getClass().getClassLoader().getResource)
            java.net.URL imgURL = getClass().getClassLoader().getResource(newPath);
            if (imgURL != null) {
                this.characterImage = new ImageIcon(imgURL).getImage();
                this.currentPath = newPath;
                repaint(); // สั่งให้วาดรูปใหม่ลงบนจอทันที
            } else {
                System.err.println("หาไฟล์รูปไม่เจอจ้า Ahri: " + newPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}