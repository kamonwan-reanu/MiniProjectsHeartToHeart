package model;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class KeyConfig {

    public static final int KEY_NEXT        = KeyEvent.VK_SPACE;
    public static final int KEY_NEXT_ALT    = KeyEvent.VK_F;       // ✅ ปุ่มสำรอง
    public static final int KEY_CHOICE_1    = KeyEvent.VK_1;
    public static final int KEY_CHOICE_2    = KeyEvent.VK_2;
    public static final int KEY_CHOICE_3    = KeyEvent.VK_3;
    public static final int KEY_RELATION_UI = KeyEvent.VK_R;
    public static final int KEY_ESCAPE      = KeyEvent.VK_ESCAPE;

    private static int nextMsg    = KEY_NEXT;
    private static int nextMsgAlt = KEY_NEXT_ALT;   // ✅
    private static int choice1    = KEY_CHOICE_1;
    private static int choice2    = KEY_CHOICE_2;
    private static int choice3    = KEY_CHOICE_3;
    private static int relationUI = KEY_RELATION_UI;
    private static int escape     = KEY_ESCAPE;

    private static final Map<Integer, String> KEY_NAMES = new HashMap<>();

    static {
        for (int i = KeyEvent.VK_A; i <= KeyEvent.VK_Z; i++)
            KEY_NAMES.put(i, String.valueOf((char) i));
        for (int i = KeyEvent.VK_0; i <= KeyEvent.VK_9; i++)
            KEY_NAMES.put(i, String.valueOf((char) i));

        KEY_NAMES.put(KeyEvent.VK_SPACE,      "SPACE");
        KEY_NAMES.put(KeyEvent.VK_ENTER,      "ENTER");
        KEY_NAMES.put(KeyEvent.VK_ESCAPE,     "ESC");
        KEY_NAMES.put(KeyEvent.VK_TAB,        "TAB");
        KEY_NAMES.put(KeyEvent.VK_SHIFT,      "SHIFT");
        KEY_NAMES.put(KeyEvent.VK_CONTROL,    "CTRL");
        KEY_NAMES.put(KeyEvent.VK_ALT,        "ALT");
        KEY_NAMES.put(KeyEvent.VK_UP,         "↑");
        KEY_NAMES.put(KeyEvent.VK_DOWN,       "↓");
        KEY_NAMES.put(KeyEvent.VK_LEFT,       "←");
        KEY_NAMES.put(KeyEvent.VK_RIGHT,      "→");
        KEY_NAMES.put(KeyEvent.VK_BACK_SPACE, "BACKSPACE");
        KEY_NAMES.put(KeyEvent.VK_DELETE,     "DELETE");
        KEY_NAMES.put(KeyEvent.VK_HOME,       "HOME");
        KEY_NAMES.put(KeyEvent.VK_END,        "END");
        KEY_NAMES.put(KeyEvent.VK_PAGE_UP,    "PG UP");
        KEY_NAMES.put(KeyEvent.VK_PAGE_DOWN,  "PG DOWN");
        for (int i = KeyEvent.VK_F1; i <= KeyEvent.VK_F12; i++)
            KEY_NAMES.put(i, "F" + (i - KeyEvent.VK_F1 + 1));
    }

    // Getters
    public static int getNextMsg()    { return nextMsg; }
    public static int getNextMsgAlt() { return nextMsgAlt; }   // ✅
    public static int getChoice1()    { return choice1; }
    public static int getChoice2()    { return choice2; }
    public static int getChoice3()    { return choice3; }
    public static int getRelationUI() { return relationUI; }
    public static int getEscape()     { return escape; }

    // Setters
    public static void setNextMsg(int v)    { nextMsg = v; }
    public static void setNextMsgAlt(int v) { nextMsgAlt = v; }  // ✅
    public static void setChoice1(int v)    { choice1 = v; }
    public static void setChoice2(int v)    { choice2 = v; }
    public static void setChoice3(int v)    { choice3 = v; }
    public static void setRelationUI(int v) { relationUI = v; }
    public static void setEscape(int v)     { escape = v; }

    public static String getKeyText(int keyCode) {
        return KEY_NAMES.getOrDefault(keyCode, "KEY_" + keyCode);
    }

    public static boolean isDuplicate(int keyCode, String excludeKey) {
        switch (excludeKey) {
            case "NEXT_MSG":    return keyCode != nextMsg    && (keyCode == choice1 || keyCode == choice2 || keyCode == choice3 || keyCode == relationUI || keyCode == escape);
            case "CHOICE_1":    return keyCode != choice1    && (keyCode == nextMsg || keyCode == choice2 || keyCode == choice3 || keyCode == relationUI || keyCode == escape);
            case "CHOICE_2":    return keyCode != choice2    && (keyCode == nextMsg || keyCode == choice1 || keyCode == choice3 || keyCode == relationUI || keyCode == escape);
            case "CHOICE_3":    return keyCode != choice3    && (keyCode == nextMsg || keyCode == choice1 || keyCode == choice2 || keyCode == relationUI || keyCode == escape);
            case "RELATION_UI": return keyCode != relationUI && (keyCode == nextMsg || keyCode == choice1 || keyCode == choice2 || keyCode == choice3    || keyCode == escape);
            case "ESCAPE":      return keyCode != escape     && (keyCode == nextMsg || keyCode == choice1 || keyCode == choice2 || keyCode == choice3    || keyCode == relationUI);
            default: return false;
        }
    }

    public static void resetToDefaults() {
        nextMsg    = KEY_NEXT;
        nextMsgAlt = KEY_NEXT_ALT;
        choice1    = KEY_CHOICE_1;
        choice2    = KEY_CHOICE_2;
        choice3    = KEY_CHOICE_3;
        relationUI = KEY_RELATION_UI;
        escape     = KEY_ESCAPE;
    }

    public static int getKey(String keyName) {
        switch (keyName) {
            case "NEXT_MSG":    return nextMsg;
            case "NEXT_MSG_ALT":return nextMsgAlt;
            case "CHOICE_1":    return choice1;
            case "CHOICE_2":    return choice2;
            case "CHOICE_3":    return choice3;
            case "RELATION_UI": return relationUI;
            case "ESCAPE":      return escape;
            default: return -1;
        }
    }

    public static void setKey(String keyName, int keyCode) {
        switch (keyName) {
            case "NEXT_MSG":     nextMsg    = keyCode; break;
            case "NEXT_MSG_ALT": nextMsgAlt = keyCode; break;
            case "CHOICE_1":     choice1    = keyCode; break;
            case "CHOICE_2":     choice2    = keyCode; break;
            case "CHOICE_3":     choice3    = keyCode; break;
            case "RELATION_UI":  relationUI = keyCode; break;
            case "ESCAPE":       escape     = keyCode; break;
        }
    }
}