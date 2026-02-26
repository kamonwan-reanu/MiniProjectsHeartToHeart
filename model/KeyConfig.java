package model;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * KeyConfig - ระบบจัดการปุ่มควบคุมแบบ Centralized
 * ใช้สำหรับเก็บค่าปุ่มและแปลงรหัสเป็นชื่อที่อ่านง่าย
 */
public class KeyConfig {
    
    // === ค่าปุ่มเริ่มต้นแบบ Static Constants ===
    public static final int KEY_NEXT = KeyEvent.VK_SPACE;
    public static final int KEY_CHOICE_1 = KeyEvent.VK_1;
    public static final int KEY_CHOICE_2 = KeyEvent.VK_2;
    public static final int KEY_CHOICE_3 = KeyEvent.VK_3;
    public static final int KEY_RELATION_UI = KeyEvent.VK_R;
    public static final int KEY_ESCAPE = KeyEvent.VK_ESCAPE;
    
    // === ตัวแปรเก็บค่าปุ่มปัจจุบัน ===
    private static int nextMsg = KEY_NEXT;
    private static int choice1 = KEY_CHOICE_1;
    private static int choice2 = KEY_CHOICE_2;
    private static int choice3 = KEY_CHOICE_3;
    private static int relationUI = KEY_RELATION_UI;
    private static int escape = KEY_ESCAPE;
    
    // === แมพสำหรับแปลงรหัสปุ่มเป็นชื่อ ===
    private static final Map<Integer, String> KEY_NAMES = new HashMap<>();
    
    static {
        // ตัวอักษรและตัวเลข
        for (int i = KeyEvent.VK_A; i <= KeyEvent.VK_Z; i++) {
            KEY_NAMES.put(i, String.valueOf((char) i));
        }
        for (int i = KeyEvent.VK_0; i <= KeyEvent.VK_9; i++) {
            KEY_NAMES.put(i, String.valueOf((char) i));
        }
        
        // ปุ่มพิเศษสำคัญ
        KEY_NAMES.put(KeyEvent.VK_SPACE, "SPACE");
        KEY_NAMES.put(KeyEvent.VK_ENTER, "ENTER");
        KEY_NAMES.put(KeyEvent.VK_ESCAPE, "ESC");
        KEY_NAMES.put(KeyEvent.VK_TAB, "TAB");
        KEY_NAMES.put(KeyEvent.VK_SHIFT, "SHIFT");
        KEY_NAMES.put(KeyEvent.VK_CONTROL, "CTRL");
        KEY_NAMES.put(KeyEvent.VK_ALT, "ALT");
        
        // ปุ่มลูกศร
        KEY_NAMES.put(KeyEvent.VK_UP, "↑");
        KEY_NAMES.put(KeyEvent.VK_DOWN, "↓");
        KEY_NAMES.put(KeyEvent.VK_LEFT, "←");
        KEY_NAMES.put(KeyEvent.VK_RIGHT, "→");
        
        // ปุ่มฟังก์ชัน
        for (int i = KeyEvent.VK_F1; i <= KeyEvent.VK_F12; i++) {
            KEY_NAMES.put(i, "F" + (i - KeyEvent.VK_F1 + 1));
        }
        
        // อื่นๆ
        KEY_NAMES.put(KeyEvent.VK_BACK_SPACE, "BACKSPACE");
        KEY_NAMES.put(KeyEvent.VK_DELETE, "DELETE");
        KEY_NAMES.put(KeyEvent.VK_HOME, "HOME");
        KEY_NAMES.put(KeyEvent.VK_END, "END");
        KEY_NAMES.put(KeyEvent.VK_PAGE_UP, "PG UP");
        KEY_NAMES.put(KeyEvent.VK_PAGE_DOWN, "PG DOWN");
    }
    
    // === Getter Methods ===
    public static int getNextMsg() { return nextMsg; }
    public static int getChoice1() { return choice1; }
    public static int getChoice2() { return choice2; }
    public static int getChoice3() { return choice3; }
    public static int getRelationUI() { return relationUI; }
    public static int getEscape() { return escape; }
    
    // === Setter Methods ===
    public static void setNextMsg(int value) { nextMsg = value; }
    public static void setChoice1(int value) { choice1 = value; }
    public static void setChoice2(int value) { choice2 = value; }
    public static void setChoice3(int value) { choice3 = value; }
    public static void setRelationUI(int value) { relationUI = value; }
    public static void setEscape(int value) { escape = value; }
    
    /**
     * แปลงรหัสปุ่มเป็นชื่อที่อ่านง่าย
     * @param keyCode รหัสปุ่มจาก KeyEvent
     * @return ชื่อปุ่ม (เช่น "SPACE", "A", "F1")
     */
    public static String getKeyText(int keyCode) {
        return KEY_NAMES.getOrDefault(keyCode, "KEY_" + keyCode);
    }
    
    /**
     * ตรวจสอบว่าปุ่มซ้ำกับปุ่มอื่นหรือไม่
     * @param keyCode รหัสปุ่มที่ต้องการตรวจสอบ
     * @param excludeKey ชื่อปุ่มที่ไม่ต้องตรวจสอบ (เพื่อไม่ตรวจกับตัวเอง)
     * @return true ถ้าซ้ำ, false ถ้าไม่ซ้ำ
     */
    public static boolean isDuplicate(int keyCode, String excludeKey) {
        switch (excludeKey) {
            case "NEXT_MSG": return keyCode != nextMsg && (keyCode == choice1 || keyCode == choice2 || keyCode == choice3 || keyCode == relationUI || keyCode == escape);
            case "CHOICE_1": return keyCode != choice1 && (keyCode == nextMsg || keyCode == choice2 || keyCode == choice3 || keyCode == relationUI || keyCode == escape);
            case "CHOICE_2": return keyCode != choice2 && (keyCode == nextMsg || keyCode == choice1 || keyCode == choice3 || keyCode == relationUI || keyCode == escape);
            case "CHOICE_3": return keyCode != choice3 && (keyCode == nextMsg || keyCode == choice1 || keyCode == choice2 || keyCode == relationUI || keyCode == escape);
            case "RELATION_UI": return keyCode != relationUI && (keyCode == nextMsg || keyCode == choice1 || keyCode == choice2 || keyCode == choice3 || keyCode == escape);
            case "ESCAPE": return keyCode != escape && (keyCode == nextMsg || keyCode == choice1 || keyCode == choice2 || keyCode == choice3 || keyCode == relationUI);
            default: return false;
        }
    }
    
    /**
     * รีเซ็ตทุกปุ่มกลับเป็นค่าเริ่มต้น
     */
    public static void resetToDefaults() {
        nextMsg = KEY_NEXT;
        choice1 = KEY_CHOICE_1;
        choice2 = KEY_CHOICE_2;
        choice3 = KEY_CHOICE_3;
        relationUI = KEY_RELATION_UI;
        escape = KEY_ESCAPE;
    }
    
    /**
     * ดึงค่าปุ่มตามชื่อ
     * @param keyName ชื่อปุ่ม (เช่น "NEXT_MSG", "CHOICE_1")
     * @return รหัสปุ่ม
     */
    public static int getKey(String keyName) {
        switch (keyName) {
            case "NEXT_MSG": return nextMsg;
            case "CHOICE_1": return choice1;
            case "CHOICE_2": return choice2;
            case "CHOICE_3": return choice3;
            case "RELATION_UI": return relationUI;
            case "ESCAPE": return escape;
            default: return -1;
        }
    }
    
    /**
     * ตั้งค่าปุ่มตามชื่อ
     * @param keyName ชื่อปุ่ม (เช่น "NEXT_MSG", "CHOICE_1")
     * @param keyCode รหัสปุ่มใหม่
     */
    public static void setKey(String keyName, int keyCode) {
        switch (keyName) {
            case "NEXT_MSG": nextMsg = keyCode; break;
            case "CHOICE_1": choice1 = keyCode; break;
            case "CHOICE_2": choice2 = keyCode; break;
            case "CHOICE_3": choice3 = keyCode; break;
            case "RELATION_UI": relationUI = keyCode; break;
            case "ESCAPE": escape = keyCode; break;
        }
    }
}
