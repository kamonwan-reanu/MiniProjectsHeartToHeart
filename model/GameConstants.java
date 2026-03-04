package model;
import java.awt.Color;

public class GameConstants {
    // 🖥️ กำหนดขนาดหน้าจอมาตรฐาน

    public static final String PACKET_READ_DONE = "READ_DONE:";
    public static final String PACKET_SHOW_CHOICES = "SHOW_CHOICES:";
    
    public static final int SCREEN_WIDTH = 1920;
    public static final int SCREEN_HEIGHT = 1080;
    
    // ⌨️ ความเร็วตัวอักษร
    public static final int TYPEWRITER_SPEED = 80; 

    // 📁 ระบบ Path 
    public static final String BASE_PATH = "model/"; 
    
    public static final String CHAR_PATH = BASE_PATH + "img_character/"; 
    public static final String SCENE_PATH = BASE_PATH + "img_scene/";
    public static final String SOUND_PATH = BASE_PATH + "sound_effect/";

    // 🎭 ไฟล์พื้นฐาน
    public static final String CHAR_AHRI = CHAR_PATH + "ahri.png"; 

    // 🎨 สี
    public static final Color THEME_PINK = new Color(255, 105, 180);
    public static final Color DIALOGUE_BG = new Color(0, 0, 0, 180); 
    public static final Color TEXT_WHITE = Color.WHITE;

    // ✨ ตัวแปรเก็บชื่อผู้เล่น (ตั้งเป็นค่าว่าง เพื่อให้ผู้เล่นกรอกเอง)
    // ใช้ static (ไม่มี final) เพื่อให้สามารถเปลี่ยนค่าได้เมื่อผู้เล่นพิมพ์ชื่อเข้ามาค่ะ
    public static String PLAYER_NAME = "";
    
    public static final String UI_PATH = BASE_PATH + "img_ui/";
}