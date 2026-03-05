package model;

public class GameState {
    public static int teerHeart = 0;
    public static int kirinHeart = 0;
    public static int tianHeart = 0;

    // กันกดซ้ำในฉากเก็บใจเดียวกัน
    public static boolean picked13 = false;
    public static boolean picked17 = false;
    public static boolean picked22 = false;

    public static void reset() {
        teerHeart = 0;
        kirinHeart = 0;
        tianHeart = 0;

        picked13 = false;
        picked17 = false;
        picked22 = false;
    }
}