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
        // ✅ เพิ่มเงื่อนไข: ถ้าไม่มีรูป (เช่น ในซีนบรรยาย) ก็ไม่ต้องวาดอะไรเลย
        if (characterImage != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            // ✅ เปิดโหมดภาพเนียน (Interpolation)
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
            // ปรับขนาดรูปให้เหมาะสมกับความสูงหน้าจอ (ตัวอย่าง: ให้สูง 80% ของจอ)
            int newHeight = (int) (getHeight() * 0.8);
            int newWidth = (int) (characterImage.getWidth(null) * ((double) newHeight / characterImage.getHeight(null)));
            
            // วาดตัวละครลงบนจอ
            int x = (getWidth() - newWidth) / 2;
            int y = getHeight() - newHeight;
            g2d.drawImage(characterImage, x, y, newWidth, newHeight, null);
            
            g2d.dispose();
        }
        // super.paintComponent(g); // ไม่จำเป็นต้องใช้ถ้าเราวาดเองหมดแล้วค่ะ
    }

    // ✅ เมธอดสำหรับเปลี่ยนรูปตัวละคร (อัปเกรดให้ซ่อนได้)
    public void updateCharacter(String newPath) {
        // ✅ 1. ถ้าส่ง "none" หรือค่าว่างมา ให้เคลียร์รูปออกและซ่อน Component
        if (newPath == null || newPath.isEmpty() || newPath.equalsIgnoreCase("none")) {
            this.characterImage = null;
            this.currentPath = "none";
            this.setVisible(false); 
            repaint();
            return;
        }

        // 2. ถ้าเป็นรูปเดิมไม่ต้องโหลดใหม่
        if (newPath.equals(currentPath)) {
            this.setVisible(true); // มั่นใจว่าเปิดการมองเห็นไว้
            return;
        }

        try {
            // ✅ โหลดรูปจาก Resource 
            java.net.URL imgURL = getClass().getClassLoader().getResource(newPath);
            if (imgURL != null) {
                this.characterImage = new ImageIcon(imgURL).getImage();
                this.currentPath = newPath;
                this.setVisible(true); // แสดงตัวละครขึ้นมา
                repaint(); 
            } else {
                System.err.println("หาไฟล์รูปไม่เจอจ้า Ahri: " + newPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}