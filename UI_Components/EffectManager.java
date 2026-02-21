package UI_Components;

import javax.swing.*;
import java.awt.*;

public class EffectManager {
    private JPanel targetPanel;
    private DialogueBox dialogueBox; // สำหรับคุมการซ่อน/แสดงกล่องข้อความ
    private String currentEffect = "none";
    private float alpha = 0.0f;
    private int shakeX = 0, shakeY = 0;
    private int shakeIntensity = 0;
    private boolean isPlaying = false;
    
    private Timer fadeTimer;
    private Timer shakeTimer;

    // รับ DialogueBox เข้ามาเพื่อจัดการ UI ในไฟล์เดียว
    public EffectManager(JPanel panel, DialogueBox dialogueBox) {
        this.targetPanel = panel;
        this.dialogueBox = dialogueBox;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void stopAll() {
        if (fadeTimer != null && fadeTimer.isRunning()) fadeTimer.stop();
        if (shakeTimer != null && shakeTimer.isRunning()) shakeTimer.stop();
        
        // คืนค่า DialogueBox ให้แสดงผลปกติเสมอเมื่อหยุด Effect
        if (dialogueBox != null) dialogueBox.setVisible(true);

        this.currentEffect = "none";
        this.alpha = 0.0f;
        this.shakeX = 0;
        this.shakeY = 0;
        this.isPlaying = false;
        if (targetPanel != null) targetPanel.repaint();
    }

    public void play(String effectName) {
    stopAll();
    if (effectName == null || effectName.isEmpty() || effectName.equalsIgnoreCase("none")) return;

    this.currentEffect = effectName.toUpperCase();
    this.isPlaying = true;

    switch (currentEffect) {
        // ✨ เปลี่ยนชื่อเป็น WHITE_FADE_OUT ตามที่ Ahri ต้องการ
        case "WHITE_FADE_OUT": 
            alpha = 1.0f; // เริ่มที่ขาวสนิท
            if (dialogueBox != null) dialogueBox.setVisible(false); // ซ่อนกล่องข้อความ
            startFadeOutWithUI(0.01f); // เริ่มการจางออกพร้อมเปิด UI
            break;

        case "FADE_WHITE_OPEN":
            alpha = 0.0f;
            startFade(0.01f);
            break;

        case "FADE_WHITE":
        case "FADE_BLACK":
            startFade(0.02f);
            break;

        case "SHAKE":
            startShake(15, 600);
            break;

        case "FLASH":
            startFlash();
            break;

        default:
            this.isPlaying = false;
            break;
    }
}

    private void startFade(float speed) {
        fadeTimer = new Timer(20, e -> {
            alpha += speed;
            if (alpha >= 1.0f) {
                alpha = 1.0f;
                fadeTimer.stop();
            }
            targetPanel.repaint();
        });
        fadeTimer.start();
    }

    // ✨ เมธอดที่ Ahri ต้องการ: จางหายขาว -> โชว์กล่องข้อความ
    private void startFadeOutWithUI(float speed) {
        fadeTimer = new Timer(20, e -> {
            alpha -= speed;
            
            // เมื่อจอเริ่มจางลง (เห็นพื้นหลังบ้างแล้ว) ค่อยโชว์กล่องข้อความ
            if (alpha <= 0.5f && dialogueBox != null && !dialogueBox.isVisible()) {
                dialogueBox.setVisible(true);
            }

            if (alpha <= 0.0f) {
                alpha = 0.0f;
                fadeTimer.stop();
                isPlaying = false;
                currentEffect = "none";
            }
            targetPanel.repaint();
        });
        fadeTimer.start();
    }

    private void startShake(int intensity, int duration) {
        this.shakeIntensity = intensity;
        long startTime = System.currentTimeMillis();
        shakeTimer = new Timer(20, e -> {
            if (System.currentTimeMillis() - startTime < duration) {
                shakeX = (int) (Math.random() * shakeIntensity * 2) - shakeIntensity;
                shakeY = (int) (Math.random() * shakeIntensity * 2) - shakeIntensity;
            } else {
                shakeX = 0; shakeY = 0;
                shakeTimer.stop();
                isPlaying = false;
            }
            targetPanel.repaint();
        });
        shakeTimer.start();
    }

    private void startFlash() {
        alpha = 0.9f;
        fadeTimer = new Timer(30, e -> {
            alpha -= 0.1f;
            if (alpha <= 0) {
                alpha = 0;
                fadeTimer.stop();
                isPlaying = false;
                currentEffect = "none";
            }
            targetPanel.repaint();
        });
        fadeTimer.start();
    }

    public void drawEffects(Graphics2D g2d, int w, int h) {
        if (currentEffect.equals("none") || currentEffect.equals("SHAKE")) return;

        int alphaValue = Math.max(0, Math.min(255, (int)(alpha * 255)));

        if (currentEffect.contains("WHITE") || currentEffect.equals("FLASH")) {
            g2d.setColor(new Color(255, 255, 255, alphaValue));
            g2d.fillRect(0, 0, w, h);
        } else if (currentEffect.equals("FADE_BLACK")) {
            g2d.setColor(new Color(0, 0, 0, alphaValue));
            g2d.fillRect(0, 0, w, h);
        }
    }

    public int getShakeX() { return shakeX; }
    public int getShakeY() { return shakeY; }
}