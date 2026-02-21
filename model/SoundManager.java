package model;

import javax.sound.sampled.*;
import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.Timer;

public class SoundManager {
    private Clip bgmClip;
    private CopyOnWriteArrayList<Clip> activeSE = new CopyOnWriteArrayList<>();
    private float currentVolume = 0.5f;
    private String currentBGMPath;
    private Timer nextLoopTimer; // ตัวแปรสำหรับคุมเวลา 10 วิ

    public void setVolume(float volume) {
        this.currentVolume = Math.max(0f, Math.min(1f, volume));
        // คำนวณ dB ครั้งเดียว
        float dB = (float) (Math.log10(currentVolume <= 0 ? 0.0001 : currentVolume) * 20.0);

        // ✅ ปรับเสียงแบบ Real-time ไม่ต้องรอ Thread อื่น
        if (bgmClip != null && bgmClip.isOpen()) {
            applyGain(bgmClip, dB);
        }
        for (Clip clip : activeSE) {
            if (clip.isOpen()) {
                applyGain(clip, dB);
            }
        }
    }

    private void applyGain(Clip clip, float dB) {
        try {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB)));
        } catch (Exception ignored) {}
    }

    public void playBGM(String path) {
        // ถ้าเป็นเพลงเดิมที่เล่นอยู่ ไม่ต้องทำอะไร
        if (path.equals(currentBGMPath) && bgmClip != null && bgmClip.isRunning()) return;

        this.currentBGMPath = path;
        if (nextLoopTimer != null) nextLoopTimer.stop(); // เคลียร์คิวเล่นเพลงเดิม
        
        stopBGM();
        
        // แยกการโหลดไฟล์ออกไปทำงานข้างหลัง เพื่อไม่ให้หน้าจอ Setting ค้างตอนลาก Slider
        new Thread(() -> {
            try {
                File file = new File(path);
                if (!file.exists()) return;
                AudioInputStream ais = AudioSystem.getAudioInputStream(file);
                bgmClip = AudioSystem.getClip();
                bgmClip.open(ais);
                
                setVolume(currentVolume); // ตั้งเสียงทันทีที่เปิด

                bgmClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        // เช็คว่าจบเพลงจริงๆ (ไม่ใช่เรากดเปลี่ยนหน้าหรือกดหยุด)
                        if (bgmClip != null && event.getFramePosition() >= bgmClip.getFrameLength()) {
                            startDelayTimer(); 
                        }
                    }
                });

                bgmClip.start();
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void startDelayTimer() {
        if (nextLoopTimer != null) nextLoopTimer.stop();
        // ✅ รอ 10 วินาที แล้วเริ่มเพลงใหม่
        nextLoopTimer = new Timer(10000, e -> {
            if (currentBGMPath != null) {
                playBGM(currentBGMPath);
            }
        });
        nextLoopTimer.setRepeats(false);
        nextLoopTimer.start();
    }

    // ✅ เพิ่ม Method สำหรับเล่น Effect เสียง (ถ้ายังไม่มีในไฟล์เดิม)
    public void playSE(String path) {
        new Thread(() -> {
            try {
                File file = new File(path);
                if (!file.exists()) return;
                AudioInputStream ais = AudioSystem.getAudioInputStream(file);
                Clip seClip = AudioSystem.getClip();
                seClip.open(ais);
                
                float dB = (float) (Math.log10(currentVolume <= 0 ? 0.0001 : currentVolume) * 20.0);
                applyGain(seClip, dB);
                
                seClip.start();
                activeSE.add(seClip);
                seClip.addLineListener(e -> {
                    if (e.getType() == LineEvent.Type.STOP) {
                        seClip.close();
                        activeSE.remove(seClip);
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    public void stopBGM() {
        if (nextLoopTimer != null) nextLoopTimer.stop();
        if (bgmClip != null) {
            bgmClip.stop();
            bgmClip.close();
            bgmClip = null;
        }
    }
}