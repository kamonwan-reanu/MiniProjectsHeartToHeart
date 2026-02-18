package model;
import java.awt.Color;

public class GameConstants {
    public static final int SCREEN_WIDTH = 1920;
    public static final int SCREEN_HEIGHT = 1080;
    public static final int TYPEWRITER_SPEED = 50; 

    // ✅ ต้องเป็น public เพื่อให้ไฟล์อื่นดึงไปใช้ได้
    public static final String CHAR_PATH = "model/img_character/"; 
    public static final String SCENE_PATH = "model/img_scene/";

    public static final String CHAR_AHRI = CHAR_PATH + "ahri.jpg"; 
    public static final String BG_SCHOOL = SCENE_PATH + "scene1.jpg"; 

    public static final Color THEME_PINK = new Color(255, 105, 180);
}