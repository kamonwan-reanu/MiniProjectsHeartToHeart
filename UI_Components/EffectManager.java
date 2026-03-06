package UI_Components;

import javax.swing.*;
import java.awt.*;

/**
 * EffectManager v2 — แก้:
 * FIX A: เพิ่ม setMultiplayerBypass() → ใน MP mode ไม่ซ่อน dialogueBox เลย
 * FIX B: FADE_WHITE_OPEN ไม่ซ่อน UI อีกต่อไป (เป็น visual only)
 */
public class EffectManager {
    private JPanel targetPanel;
    private DialogueBox dialogueBox;
    private String currentEffect = "none";
    private float alpha = 0.0f;
    private int shakeX = 0, shakeY = 0;
    private int shakeIntensity = 0;
    private boolean isPlaying = false;

    // FIX A: โหมด Multiplayer — ไม่ซ่อน dialogueBox เด็ดขาด
    private boolean multiplayerBypass = false;

    private Timer fadeTimer;
    private Timer shakeTimer;

    public EffectManager(JPanel panel, DialogueBox dialogueBox) {
        this.targetPanel = panel;
        this.dialogueBox = dialogueBox;
    }

    /** FIX A: เรียกจาก PlaySceneMain.setMultiplayerEffectBypass() */
    public void setMultiplayerBypass(boolean bypass) {
        this.multiplayerBypass = bypass;
    }

    public boolean isPlaying() { return isPlaying; }

    public void stopAll() {
        if (fadeTimer  != null) fadeTimer.stop();
        if (shakeTimer != null) shakeTimer.stop();

        // คืน dialogueBox เสมอ
        if (dialogueBox != null) dialogueBox.setVisible(true);

        currentEffect = "none";
        alpha   = 0.0f;
        shakeX  = 0;
        shakeY  = 0;
        isPlaying = false;

        if (targetPanel != null) targetPanel.repaint();
    }

    public void play(String effectName) {
        stopAll();

        if (effectName == null || effectName.isEmpty() || effectName.equalsIgnoreCase("none")) return;

        currentEffect = effectName.toUpperCase();
        isPlaying     = true;

        switch (currentEffect) {

            case "WHITE_FADE_OUT":
                alpha = 1.0f;
                // FIX B: ซ่อน dialogueBox เฉพาะ Single Player เท่านั้น
                if (!multiplayerBypass && dialogueBox != null) dialogueBox.setVisible(false);
                startFadeOut(0.01f);
                break;

            case "FADE_WHITE_OPEN":
                // FIX B: visual effect เท่านั้น — ไม่ซ่อน UI
                alpha = 0.0f;
                startFadeIn(0.015f);
                break;

            case "FADE_WHITE":
            case "FADE_BLACK":
                alpha = 0.0f;
                startFadeIn(0.02f);
                break;

            case "SHAKE":
                startShake(15, 600);
                break;

            case "FLASH":
                startFlash();
                break;

            default:
                isPlaying = false;
                break;
        }
    }

    // fade เข้า (alpha 0→1) แล้วหยุด
    private void startFadeIn(float speed) {
        fadeTimer = new Timer(20, e -> {
            alpha += speed;
            if (alpha >= 1.0f) {
                alpha = 1.0f;
                ((Timer) e.getSource()).stop();
                isPlaying = false;
                // คืน dialogueBox กรณี Single Player
                if (dialogueBox != null) dialogueBox.setVisible(true);
            }
            if (targetPanel != null) targetPanel.repaint();
        });
        fadeTimer.start();
    }

    // fade ออก (alpha 1→0)
    private void startFadeOut(float speed) {
        fadeTimer = new Timer(20, e -> {
            alpha -= speed;
            if (alpha <= 0.0f) {
                alpha = 0.0f;
                ((Timer) e.getSource()).stop();
                isPlaying     = false;
                currentEffect = "none";
                // คืน dialogueBox เสมอ
                if (dialogueBox != null) dialogueBox.setVisible(true);
            }
            if (targetPanel != null) targetPanel.repaint();
        });
        fadeTimer.start();
    }

    private void startShake(int intensity, int duration) {
        shakeIntensity = intensity;
        long t0 = System.currentTimeMillis();
        shakeTimer = new Timer(20, e -> {
            if (System.currentTimeMillis() - t0 < duration) {
                shakeX = (int)(Math.random() * shakeIntensity * 2) - shakeIntensity;
                shakeY = (int)(Math.random() * shakeIntensity * 2) - shakeIntensity;
            } else {
                shakeX = shakeY = 0;
                ((Timer) e.getSource()).stop();
                isPlaying = false;
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
                ((Timer) e.getSource()).stop();
                isPlaying     = false;
                currentEffect = "none";
            }
            if (targetPanel != null) targetPanel.repaint();
        });
        fadeTimer.start();
    }

    public void drawEffects(Graphics2D g2d, int w, int h) {
        if (currentEffect.equals("none") || currentEffect.equals("SHAKE")) return;
        int alphaVal = Math.max(0, Math.min(255, (int)(alpha * 255)));
        if (alphaVal <= 0) return;
        if (currentEffect.contains("WHITE") || currentEffect.equals("FLASH")) {
            g2d.setColor(new Color(255, 255, 255, alphaVal));
        } else if (currentEffect.contains("BLACK")) {
            g2d.setColor(new Color(0, 0, 0, alphaVal));
        } else {
            return;
        }
        g2d.fillRect(0, 0, w, h);
    }

    public float getAlpha()  { return alpha; }
    public int   getShakeX() { return shakeX; }
    public int   getShakeY() { return shakeY; }
}