package model;

import java.util.HashMap;
import java.util.Map;

/**
 * ระบบจัดการค่าความสัมพันธ์ (Relationship System)
 * ใช้ Singleton Pattern เพื่อให้เรียกใช้ได้จากทุกที่ในโปรเจค
 * รองรับตัวละครหลายตัวแบบ Dynamic ด้วย Map<String, Integer>
 */
public class Relation {
    private static Relation instance;
    private Map<String, Integer> affectionPoints;
    
    // Private constructor สำหรับ Singleton Pattern
    private Relation() {
        affectionPoints = new HashMap<>();
        // กำหนดค่าเริ่มต้นให้ตัวละครหลัก
        initializeDefaultCharacters();
    }
    
    /**
     * ดึง Instance ของ Relation (Singleton Pattern)
     */
    public static Relation getInstance() {
        if (instance == null) {
            instance = new Relation();
        }
        return instance;
    }
    
    /**
     * กำหนดค่าเริ่มต้นให้ตัวละครหลัก
     */
    private void initializeDefaultCharacters() {
        affectionPoints.put("Ahri", 0);
        affectionPoints.put("Theerapat", 0);
        affectionPoints.put("Kirin", 0);
        affectionPoints.put("Cream", 0);
    }
    
    /**
     * เพิ่มค่าความสัมพันธ์ให้กับตัวละคร
     * @param characterName ชื่อตัวละคร
     * @param amount จำนวนคะแนนที่จะเพิ่ม (สามารถเป็นลบได้)
     */
    public void addAffection(String characterName, int amount) {
        if (characterName == null || characterName.trim().isEmpty()) {
            return;
        }
        
        String name = characterName.trim();
        int currentPoints = affectionPoints.getOrDefault(name, 0);
        affectionPoints.put(name, currentPoints + amount);
        
        System.out.println("ความสัมพันธ์กับ " + name + ": " + (currentPoints + amount) + " (+ " + amount + ")");
    }
    
    /**
     * ดึงค่าความสัมพันธ์ปัจจุบันของตัวละคร
     * @param characterName ชื่อตัวละคร
     * @return ค่าความสัมพันธ์ปัจจุบัน ถ้าไม่พบจะคืนค่า 0
     */
    public int getAffection(String characterName) {
        if (characterName == null || characterName.trim().isEmpty()) {
            return 0;
        }
        
        return affectionPoints.getOrDefault(characterName.trim(), 0);
    }
    
    /**
     * กำหนดค่าความสัมพันธ์โดยตรง
     * @param characterName ชื่อตัวละคร
     * @param amount ค่าที่ต้องการกำหนด
     */
    public void setAffection(String characterName, int amount) {
        if (characterName == null || characterName.trim().isEmpty()) {
            return;
        }
        
        affectionPoints.put(characterName.trim(), amount);
    }
    
    /**
     * ตรวจสอบว่ามีตัวละครนี้ในระบบหรือไม่
     * @param characterName ชื่อตัวละคร
     * @return true ถ้ามี, false ถ้าไม่มี
     */
    public boolean hasCharacter(String characterName) {
        if (characterName == null || characterName.trim().isEmpty()) {
            return false;
        }
        
        return affectionPoints.containsKey(characterName.trim());
    }
    
    /**
     * ดึงข้อมูลความสัมพันธ์ทั้งหมด
     * @return Map ของชื่อตัวละครและค่าความสัมพันธ์
     */
    public Map<String, Integer> getAllAffection() {
        return new HashMap<>(affectionPoints);
    }
    
    /**
     * รีเซ็ตค่าความสัมพันธ์ทั้งหมด
     */
    public void resetAll() {
        affectionPoints.clear();
        initializeDefaultCharacters();
    }
    
    /**
     * แสดงสถานะความสัมพันธ์ทั้งหมด (สำหรับ Debug)
     */
    public void printAllStatus() {
        System.out.println("=== สถานะความสัมพันธ์ปัจจุบัน ===");
        for (Map.Entry<String, Integer> entry : affectionPoints.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("========================");
    }
}
