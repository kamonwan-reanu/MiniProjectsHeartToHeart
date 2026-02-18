package model;

public class StoryData {
    // ✅ SCENE_1: บทนำที่เป็นจุดไข่ปลา (รูปที่ 1 ของหนู)
    public static final Object[][] SCENE_1 = {
        {"", "ฟหกดฟกหดฟหกดฟหกดฟกหดฟหดก1", "", ""},
        {"", "ฟหกดฟกอแแอฟหกอฟหกดฟกหด2", "", ""},
        {"", "ฟหกดฟหกดฟหกดฟหกดฟหกดฟกหด3", "", ""},
    };

    // ✅ SCENE_2: อวี่เชินเริ่มพูด (แก้จาก String[] เป็น Object[][] เพื่อไม่ให้ Error)
    public static final Object[][] SCENE_2 = {
        {"อวี่เชิน", "วิชา ทฤษฎีสุนทรียศาสตร์เนี่ย...", "char/ahri_happy.png", "img_scene/scene1.jpg"},
        {"อวี่เชิน", "มันน่าสนใจกว่าที่คิดนะว่าไหม?", "char/ahri_smile.png", "img_scene/old_building.jpg"}
    };
    
    // ✅ SCENE_3: ตึกเรียนเก่า
    public static final Object[][] SCENE_3 = {
        {"อวี่เชิน", ".................................", "char/ahri_happy.png", "img_scene/scene1.jpg"},
        {"อวี่เชิน", "มันน่าสนใจกว่าที่คิดนะว่าไหม?", "char/ahri_smile.png", "img_scene/old_building.jpg"}
    };

    // ✅ SCENE_4: สวนหลังโรงเรียน
    public static final Object[][] SCENE_4 = {
        {"อวี่เชิน", "...............", "char/ahri_happy.png", "img_scene/scene1.jpg"},
        {"อวี่เชิน", "...............", "char/ahri_smile.png", "img_scene/old_building.jpg"}
    };
}