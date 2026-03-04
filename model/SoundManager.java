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
    private Timer nextLoopTimer;

    public void setVolume(float volume) {
        this.currentVolume = Math.max(0f, Math.min(1f, volume));
        // ✅ square root ทำให้ slider รู้สึกสม่ำเสมอตลอดช่วง
        // เดิม log10(volume) ทำให้ครึ่งแรกเงียบมาก ครึ่งหลังดังพรวด
        double linear = Math.sqrt(currentVolume);
        float dB = (float) (Math.log10(linear <= 0 ? 0.0001 : linear) * 20.0);

        if (bgmClip != null && bgmClip.isOpen()) applyGain(bgmClip, dB);
        for (Clip clip : activeSE) if (clip.isOpen()) applyGain(clip, dB);
    }

    private void applyGain(Clip clip, float dB) {
        try {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB)));
        } catch (Exception ignored) {}
    }

    public void playBGM(String path) {
        if (path.equals(currentBGMPath) && bgmClip != null && bgmClip.isRunning()) return;

        this.currentBGMPath = path;
        if (nextLoopTimer != null) nextLoopTimer.stop();
        stopBGM();

        new Thread(() -> {
            try {
                File file = new File(path);
                if (!file.exists()) return;
                AudioInputStream ais = AudioSystem.getAudioInputStream(file);
                bgmClip = AudioSystem.getClip();
                bgmClip.open(ais);
                setVolume(currentVolume);

                bgmClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
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
        nextLoopTimer = new Timer(10000, e -> {
            if (currentBGMPath != null) playBGM(currentBGMPath);
        });
        nextLoopTimer.setRepeats(false);
        nextLoopTimer.start();
    }

    public void playSE(String path) {
        new Thread(() -> {
            try {
                File file = new File(path);
                if (!file.exists()) return;
                AudioInputStream ais = AudioSystem.getAudioInputStream(file);
                Clip seClip = AudioSystem.getClip();
                seClip.open(ais);

                double linear = Math.sqrt(currentVolume);
                float dB = (float) (Math.log10(linear <= 0 ? 0.0001 : linear) * 20.0);
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