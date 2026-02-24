package UI_Components;

import javax.swing.*;
import java.awt.*;

public class EffectManager {
    private JPanel targetPanel;
    private DialogueBox dialogueBox; 
    private String currentEffect = "none";
    private float alpha = 0.0f;
    private int shakeX = 0, shakeY = 0;
    private int shakeIntensity = 0;
    private boolean isPlaying = false;
    
    private Timer fadeTimer;
    private Timer shakeTimer;

    public EffectManager(JPanel panel, DialogueBox dialogueBox) {
        this.targetPanel = panel;
        this.dialogueBox = dialogueBox;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    // ✨ ปรับปรุง stopAll ให้ล้างสถานะเกลี้ยงจริงๆ
    public void stopAll() {
        if (fadeTimer != null) fadeTimer.stop();
        if (shakeTimer != null) shakeTimer.stop();
        
        if (dialogueBox != null) dialogueBox.setVisible(true);

        this.currentEffect = "none";
        this.alpha = 0.0f;
        this.shakeX = 0;
        this.shakeY = 0;
        this.isPlaying = false;
        
        if (targetPanel != null) {
            targetPanel.repaint();
        }
    }

    public void play(String effectName) {
        // เคลียร์ของเก่าก่อนเริ่มใหม่เสมอ
        stopAll();
        
        if (effectName == null || effectName.isEmpty() || effectName.equalsIgnoreCase("none")) {
            return;
        }

        this.currentEffect = effectName.toUpperCase();
        this.isPlaying = true;

        switch (currentEffect) {
            case "WHITE_FADE_OUT": 
                alpha = 1.0f;
                if (dialogueBox != null) dialogueBox.setVisible(false);
                startFadeOutWithUI(0.01f);
                break;

            case "FADE_WHITE_OPEN":
                alpha = 0.0f;
                startFade(0.01f);
                break;

            case "FADE_WHITE":
            case "FADE_BLACK":
                alpha = 0.0f;
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
                this.isPlaying = false; // ปลดล็อคให้ PlayPage กดต่อได้
            }
            if (targetPanel != null) targetPanel.repaint();
        });
        fadeTimer.start();
    }

    private void startFadeOutWithUI(float speed) {
        fadeTimer = new Timer(20, e -> {
            alpha -= speed;
            
            // ❌ ลบ If (alpha <= 0.5f...) ตรงนี้ทิ้งให้หมดเลยค่ะ! 
            // เราจะไม่ให้กล่องโผล่จนกว่าจะจางหายไปจริงๆ ใน PlaySceneMain

            if (alpha <= 0.0f) {
                alpha = 0.0f;
                fadeTimer.stop();
                this.isPlaying = false; 
                this.currentEffect = "none";
            }
            if (targetPanel != null) targetPanel.repaint();
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
                shakeX = 0; 
                shakeY = 0;
                shakeTimer.stop();
                this.isPlaying = false;
            }
            if (targetPanel != null) targetPanel.repaint();
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
                this.isPlaying = false;
                this.currentEffect = "none";
            }
            if (targetPanel != null) targetPanel.repaint();
        });
        fadeTimer.start();
    }

    public void drawEffects(Graphics2D g2d, int w, int h) {
        if (currentEffect.equals("none") || currentEffect.equals("SHAKE")) return;

        int alphaValue = Math.max(0, Math.min(255, (int)(alpha * 255)));
        
        if (alphaValue > 0) {
            // ✨ ใช้ความแม่นยำในการเช็คคำสั่ง
            if (currentEffect.contains("WHITE") || currentEffect.equals("FLASH")) {
                g2d.setColor(new Color(255, 255, 255, alphaValue));
            } else if (currentEffect.contains("BLACK")) { // เปลี่ยนเป็น contains เพื่อให้ครอบคลุม FADE_BLACK
                g2d.setColor(new Color(0, 0, 0, alphaValue));
            }
            g2d.fillRect(0, 0, w, h);
        }
    }

    public float getAlpha() {
        return this.alpha;
    }       

    public int getShakeX() { return shakeX; }
    public int getShakeY() { return shakeY; }
}