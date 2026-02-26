package UI_Components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import model.Relation;
import model.GameConstants;

public class SaveSystemUI extends JPanel {
    private static SaveSystemUI instance;
    private Timer fadeTimer;
    private float alpha = 0.0f;
    private JLabel saveMessage;
    
    private SaveSystemUI() {
        setLayout(null);
        setOpaque(false);
        setVisible(false);
        setupSaveMessage();
    }
    
    public static SaveSystemUI getInstance() {
        if (instance == null) {
            instance = new SaveSystemUI();
        }
        return instance;
    }
    
    private void setupSaveMessage() {
        // สร้างข้อความ "Saved!" สำหรับแสดงผล
        saveMessage = new JLabel("✅ Saved!", SwingConstants.CENTER);
        saveMessage.setForeground(new Color(255, 255, 255));
        saveMessage.setFont(new Font("Tahoma", Font.BOLD, 14));
        saveMessage.setBounds(0, -25, 120, 20);
        saveMessage.setVisible(false);
        add(saveMessage);
    }
    
    public void triggerSave() {
        saveGame();
    }
    
    private void saveGame() {
        try {
            // ลองหาจาก Parent ก่อน
            Container parent = getParent();
            UI_Screens.PlaySceneMain scene = null;
            while (parent != null) {
                if (parent instanceof UI_Screens.PlaySceneMain) {
                    scene = (UI_Screens.PlaySceneMain) parent;
                    break;
                }
                parent = parent.getParent();
            }
            
            // ถ้าหายังไม่เจอ ให้ลองหาผ่าน Main.mainContainer (สำหรับตอนรันเกมจริง)
            if (scene == null && core.Main.mainContainer != null) {
                for (Component comp : core.Main.mainContainer.getComponents()) {
                    if (comp instanceof UI_Screens.PlaySceneMain) {
                        scene = (UI_Screens.PlaySceneMain) comp;
                        break;
                    }
                }
            }
            
            // ถ้ายังไม่เจออีก ให้ลองหาจากทุก JFrame
            if (scene == null) {
                Frame[] frames = Frame.getFrames();
                for (Frame frame : frames) {
                    if (frame instanceof JFrame) {
                        JFrame jframe = (JFrame) frame;
                        // ค้นหาจาก content pane
                        searchInContainer(jframe.getContentPane());
                    }
                }
            }
            
            if (scene != null) {
                System.out.println("พบ PlaySceneMain แล้ว!");
                
                // ใช้ Reflection แบบปลอดภัย - วนลูปหา field แทนการเรียกตรงๆ
                Class<?> sceneClass = scene.getClass();
                Field[] fields = sceneClass.getDeclaredFields();
                
                int storyIndex = 0;
                String sceneName = "SCENE_1";
                
                for (Field field : fields) {
                    field.setAccessible(true); // ให้เข้าถึง private field ได้
                    
                    if (field.getName().equals("storyIndex")) {
                        storyIndex = field.getInt(scene);
                        System.out.println("พบ storyIndex: " + storyIndex);
                    } else if (field.getName().equals("sceneName")) {
                        sceneName = (String) field.get(scene);
                        System.out.println("พบ sceneName: " + sceneName);
                    }
                }
                
                // ดึงค่าความสัมพันธ์
                int ahriAffection = 0;
                try {
                    ahriAffection = Relation.getInstance().getAffection("Ahri");
                    System.out.println("พบความสัมพันธ์ Ahri: " + ahriAffection);
                } catch (Exception ex) {
                    System.err.println("ไม่สามารถดึงค่าความสัมพันธ์ Ahri: " + ex.getMessage());
                }
                
                // สร้างข้อมูลการบันทึก
                StringBuilder saveData = new StringBuilder();
                saveData.append("storyIndex=").append(storyIndex).append("\n");
                saveData.append("sceneName=").append(sceneName).append("\n");
                saveData.append("playerName=").append(GameConstants.PLAYER_NAME != null ? GameConstants.PLAYER_NAME : "").append("\n");
                saveData.append("ahriAffection=").append(ahriAffection).append("\n");
                
                // บันทึกลงไฟล์
                String projectPath = System.getProperty("user.dir");
                String savePath = projectPath + File.separator + "savegame.dat";
                System.out.println("บันทึกไฟล์ที่: " + savePath);
                
                Files.write(Paths.get(savePath), saveData.toString().getBytes("UTF-8"));
                System.out.println("บันทึกข้อมูลสำเร็จ!");
                
                // แสดงข้อความ "Saved!" และ Fade out
                showSaveMessage();
                
                // สร้างตัวแปร final สำหรับใช้ใน SwingUtilities.invokeLater
                final int finalStoryIndex = storyIndex;
                final String finalSceneName = sceneName;
                final int finalAhriAffection = ahriAffection;
                
                // เพิ่ม Popup แจ้งเตือนผู้เล่น
                SwingUtilities.invokeLater(() -> {
                    Font thaiFont = new Font("Tahoma", Font.BOLD, 14);
                    UIManager.put("OptionPane.messageFont", thaiFont);
                    UIManager.put("OptionPane.buttonFont", thaiFont);
                    
                    JOptionPane.showMessageDialog(null, 
                        "เซฟเกมสำเร็จแล้ว!\n" +
                        "ซีน: " + finalSceneName + "\n" +
                        "ดัชนี: " + finalStoryIndex + "\n" +
                        "ความสัมพันธ์ Ahri: " + finalAhriAffection,
                        "System", 
                        JOptionPane.INFORMATION_MESSAGE);
                });
                
            } else {
                System.err.println("ไม่พบซีนเกมสำหรับบันทึก - หา PlaySceneMain ไม่เจอจากทุกทาง");
                
                // แสดง Popup แจ้งเตือนแทนการพิมพ์ลง console
                SwingUtilities.invokeLater(() -> {
                    Font thaiFont = new Font("Tahoma", Font.BOLD, 14);
                    UIManager.put("OptionPane.messageFont", thaiFont);
                    UIManager.put("OptionPane.buttonFont", thaiFont);
                    
                    JOptionPane.showMessageDialog(null, 
                        "❌ ไม่สามารถบันทึกเกมได้\nไม่พบซีนเกมที่กำลังเล่นอยู่", 
                        "ข้อผิดพลาด", 
                        JOptionPane.ERROR_MESSAGE);
                });
            }
            
        } catch (Exception ex) {
            System.err.println("ไม่สามารถบันทึกเกมได้: " + ex.getMessage());
            ex.printStackTrace();
            
            // แสดง Popup แจ้งเตือนแทนการพิมพ์ลง console
            SwingUtilities.invokeLater(() -> {
                Font thaiFont = new Font("Tahoma", Font.BOLD, 14);
                UIManager.put("OptionPane.messageFont", thaiFont);
                UIManager.put("OptionPane.buttonFont", thaiFont);
                
                JOptionPane.showMessageDialog(null, 
                    "❌ ไม่สามารถบันทึกเกมได้: " + ex.getMessage(), 
                    "ข้อผิดพลาด", 
                    JOptionPane.ERROR_MESSAGE);
            });
        }
    }
    
    // เมธอดช่วยในการค้นหา PlaySceneMain ใน container ซ้อนๆ
    private UI_Screens.PlaySceneMain searchInContainer(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof UI_Screens.PlaySceneMain) {
                return (UI_Screens.PlaySceneMain) comp;
            } else if (comp instanceof Container) {
                UI_Screens.PlaySceneMain result = searchInContainer((Container) comp);
                if (result != null) return result;
            }
        }
        return null;
    }
    
    private void showSaveMessage() {
        // แสดงข้อความ "Saved!"
        saveMessage.setVisible(true);
        
        // Fade out หลังจากการบันทึก
        if (fadeTimer != null && fadeTimer.isRunning()) {
            fadeTimer.stop();
        }
        
        fadeTimer = new Timer(50, e -> {
            alpha += 0.05f;
            if (alpha >= 1.0f) {
                alpha = 1.0f;
                fadeTimer.stop();
                // ซ่อนตัวเองหลังจากแสดงข้อความ
                SwingUtilities.invokeLater(() -> {
                    saveMessage.setVisible(false);
                    alpha = 0.0f;
                });
            }
            repaint();
        });
        fadeTimer.start();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w = getWidth();
        int h = getHeight();
        
        // วาดพื้นหลังกล่อง (สำหรับแสดงข้อความ Saved!)
        RoundRectangle2D background = new RoundRectangle2D.Float(0, 0, w, h, 10, 10);
        g2d.setColor(new Color(15, 20, 35, (int)(210 * alpha)));
        g2d.fill(background);
        
        // วาดขอบ
        g2d.setColor(new Color(212, 175, 55, (int)(180 * alpha)));
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(background);
        
        g2d.dispose();
    }
}
