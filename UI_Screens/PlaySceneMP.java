package UI_Screens;

import UI_Components.CharacterSprite;
import UI_Components.DialogueBox;
import UI_Components.EffectManager;
import UI_Components.PauseMenuUI;
import UI_Components.RelationUI;
import UI_Components.MultiplayerTimerBar;
<<<<<<< HEAD
// ✅ เพิ่ม import มินิเกม
=======
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
import UI_Components.MemoryMatchMiniGame;
import UI_Components.RockPaperScissorsMiniGame;

import model.GameConstants;
import model.GameServer;
import model.GameClient;
import model.KeyConfig;
import model.SoundManager;
import model.StoryDataMP;
import model.Relation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

<<<<<<< HEAD
/**
 * PlaySceneMP — 2-phase TFT timer + countdown + random choice on timeout
 * + MiniGame integration
 */
=======
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
public class PlaySceneMP extends JPanel {

    private final Map<String, Object[][]> storyMap = new HashMap<>();
    private Object[][] currentScene;
    private String     sceneName     = "MP_INTRO";
    private int        sceneIndex    = 0;
    private Runnable   onGameFinished;

    private String  currentBG      = "";
    private String  currentChar    = "";
    private String  currentSpeaker = "";
    private String  fullText       = "";
    private int     charIndex      = 0;
    private boolean isChoiceMode   = false;
    private Object[][] pendingChoices  = null;
    private boolean sentSceneReady    = false;
    private boolean waitingForResult   = false;
<<<<<<< HEAD

    // ✅ เพิ่มตัวแปรสำหรับมินิเกม
    private JPanel activeMiniGamePanel = null;
    private boolean isInMiniGame = false;
    private String currentMiniGameType = "";
    private boolean miniGameFinished = false;
=======
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef

    // MiniGame
    private JPanel activeMiniGamePanel = null;
    private boolean isInMiniGame = false;
    private String currentMiniGameType = "";
    private boolean miniGameFinished = false;

    // UI
    private JLabel              bgLayer;
    private CharacterSprite     characterLayer;
    private JPanel              effectLayer;
    private JPanel              choiceLayer;
    private DialogueBox         dialogueBox;
    private Timer               typeTimer;
    private MultiplayerTimerBar timerBar;

    private final SoundManager soundManager  = new SoundManager();
    private EffectManager      effectManager;

    private GameServer mpServer = null;
    private GameClient mpClient = null;
    private final Random random = new Random();
    private java.util.function.Consumer<java.util.Map<String,Integer>> onLeaderboardCb = null;

    public PlaySceneMP() {
        setLayout(null);
        setOpaque(true);
        setBackground(Color.BLACK);

        dialogueBox   = new DialogueBox();
        effectManager = new EffectManager(this, dialogueBox);
        effectManager.setMultiplayerBypass(true);

        initStoryMap();
        setupUI();

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { relayout(); }
            @Override public void componentShown(ComponentEvent e) {
                relayout();
                if (!isInMiniGame && currentScene != null && sceneIndex < currentScene.length)
                    renderLine(currentScene[sceneIndex]);
            }
        });
        dialogueBox.setOnNextRequested(this::onNext);
        setupKeys();
    }

    public void startMPGame(GameServer server, GameClient client) {
        this.mpServer = server;
        this.mpClient = client;

        Relation.getInstance().resetAll();
        RelationUI.getInstance().setVisible(false);
        effectManager.stopAll();
        timerBar.resetAndHide();
<<<<<<< HEAD
        
        // ✅ ปิดมินิเกมถ้ามีค้างอยู่
=======
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
        closeMiniGame();

        if (server != null) registerNextScenes(server);
        if (client != null) client.setListener(buildClientListener());

        loadScene("MP_INTRO");
        relayout();
    }

    public void setOnGameFinished(Runnable r)  { this.onGameFinished = r; }
    public void setOnLeaderboard(java.util.function.Consumer<java.util.Map<String,Integer>> cb) { this.onLeaderboardCb = cb; }

    // ── Host callbacks ────────────────────────────────────
    public void onHostPhaseRead(String scene, int sec, int total) {
        SwingUtilities.invokeLater(() -> {
            RelationUI.getInstance().setVisible(false);
<<<<<<< HEAD
            // ✅ ถ้ากำลังอยู่ในมินิเกม ให้ข้าม
            if (isInMiniGame) return;
            
=======
            if (isInMiniGame) return;
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
            if (mpClient != null) {
                if (!scene.equals(sceneName) || currentScene == null) loadScene(scene);
                // ✅ FIX: แจ้ง server ว่าโหลด scene เสร็จ ป้องกัน checkAllSceneLoaded ค้าง
                mpClient.notifySceneLoaded(scene);
            }
            timerBar.startReadPhase(scene, sec, total);
            relayout();
        });
    }
<<<<<<< HEAD
    
    public void onHostCountdown(int n) {
        SwingUtilities.invokeLater(() -> {
            if (!isInMiniGame) timerBar.showCountdown(n);
        });
    }
    
    public void onHostPhaseChoice(String scene, int sec, int total) {
        SwingUtilities.invokeLater(() -> {
            if (isInMiniGame) return;
            
=======

    public void onHostCountdown(int n) {
        SwingUtilities.invokeLater(() -> { if (!isInMiniGame) timerBar.showCountdown(n); });
    }

    public void onHostPhaseChoice(String scene, int sec, int total) {
        SwingUtilities.invokeLater(() -> {
            if (isInMiniGame) return;
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
            timerBar.startChoicePhase(scene, sec, total);
            unlockChoiceButtons();
            if (!isChoiceMode || countChoiceButtons() == 0) skipToChoice(true);
            relayout();
        });
    }
<<<<<<< HEAD
    
    public void onHostChoiceResult(String target, String charName, int score) {
        SwingUtilities.invokeLater(() -> {
            if (isInMiniGame) return;
            applyChoiceResult(target, charName, score);
        });
    }
    
    public void onHostForceNext(String scene, String targetScene) {
        SwingUtilities.invokeLater(() -> {
            if (isInMiniGame) return;
            doForceNext(targetScene);
        });
    }
    
    public void onHostTimerSync(int t) {
        SwingUtilities.invokeLater(() -> {
            if (!isInMiniGame) timerBar.setTimeLeft(t);
        });
    }
    
    public void onHostReadyCount(int ready, int total) {
        SwingUtilities.invokeLater(() -> {
            if (!isInMiniGame) timerBar.setReadyCount(ready, total);
        });
    }

    // ✅ เพิ่ม callback สำหรับมินิเกม
    public void onHostStartMiniGame(String gameType, int timeSeconds) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("🎮 Host starting minigame: " + gameType);
            startMiniGame(gameType, timeSeconds);
        });
    }

    public void onHostMiniGameResult(String playerName, boolean won, int points) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("🏆 Minigame result: " + playerName + " " + (won ? "won" : "lost") + " +" + points);
            showMiniGameResult(playerName, won, points);
        });
    }

    // ════════════════════════════════════════════════════
    //  MiniGame Methods
    // ════════════════════════════════════════════════════

    /**
     * เริ่มมินิเกมตามประเภท
     */
    private void startMiniGame(String gameType, int timeSeconds) {
        if (activeMiniGamePanel != null) {
            System.out.println("⚠️ Minigame already running");
            return;
        }

        isInMiniGame = true;
        miniGameFinished = false;
        currentMiniGameType = gameType;
        
        System.out.println("🎮 Starting " + gameType + " minigame for " + timeSeconds + "s");
        
        // ซ่อน UI ปกติ
=======

    public void onHostChoiceResult(String target, String charName, int score) {
        SwingUtilities.invokeLater(() -> { if (!isInMiniGame) applyChoiceResult(target, charName, score); });
    }

    public void onHostForceNext(String scene, String targetScene) {
        SwingUtilities.invokeLater(() -> { if (!isInMiniGame) doForceNext(targetScene); });
    }

    public void onHostTimerSync(int t) {
        SwingUtilities.invokeLater(() -> { if (!isInMiniGame) timerBar.setTimeLeft(t); });
    }

    public void onHostReadyCount(int ready, int total) {
        SwingUtilities.invokeLater(() -> { if (!isInMiniGame) timerBar.setReadyCount(ready, total); });
    }

    // ════════════════════════════════════════════════════
    //  MiniGame
    // ════════════════════════════════════════════════════
    private void startMiniGame(String gameType, int timeSeconds) {
        if (activeMiniGamePanel != null) return;
        isInMiniGame = true;
        miniGameFinished = false;
        currentMiniGameType = gameType;

>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
        dialogueBox.setVisible(false);
        choiceLayer.setVisible(false);
        timerBar.setVisible(false);

<<<<<<< HEAD
        Runnable winAction = new Runnable() {
            @Override
            public void run() {
                if (miniGameFinished) return;
                miniGameFinished = true;
                
                // ชนะได้ 5 คะแนน ตามบท
                int points = 5;
                String playerName = GameConstants.PLAYER_NAME;
                
                System.out.println("🏆 " + playerName + " won the minigame! +" + points);
                
                if (mpServer != null) {
                    mpServer.broadcastMiniGameResult(playerName, true, points);
                } else if (mpClient != null) {
                    mpClient.sendMiniGameResult(true, points);
                }
                
                // เพิ่มคะแนนให้ตัวเองทันที
                Relation.getInstance().addAffection("Jes", points);
                
                // รอ 2 วินาทีแล้วค่อยปิดเกม
                Timer closeTimer = new Timer(2000, e -> {
                    closeMiniGameAndContinue();
                });
                closeTimer.setRepeats(false);
                closeTimer.start();
            }
        };

        Runnable failAction = new Runnable() {
            @Override
            public void run() {
                if (miniGameFinished) return;
                miniGameFinished = true;
                
                // แพ้ได้ 2 คะแนน ตามบท
                int points = 2;
                String playerName = GameConstants.PLAYER_NAME;
                
                System.out.println("💔 " + playerName + " lost the minigame! +" + points);
                
                if (mpServer != null) {
                    mpServer.broadcastMiniGameResult(playerName, false, points);
                } else if (mpClient != null) {
                    mpClient.sendMiniGameResult(false, points);
                }
                
                Relation.getInstance().addAffection("Jes", points);
                
                Timer closeTimer = new Timer(2000, e -> {
                    closeMiniGameAndContinue();
                });
                closeTimer.setRepeats(false);
                closeTimer.start();
            }
        };

        // สร้างมินิเกมตามประเภท
        try {
            switch (gameType.toUpperCase()) {
                case "MATCH":
                case "MEMORY":
                    activeMiniGamePanel = new MemoryMatchMiniGame(winAction, failAction, true);
                    break;
                    
                case "RPS":
                case "ROCKPAPERSCISSORS":
                    activeMiniGamePanel = new RockPaperScissorsMiniGame(winAction, failAction, true, timeSeconds);
                    break;
                    
                default:
                    System.err.println("❌ Unknown minigame type: " + gameType);
                    isInMiniGame = false;
                    return;
            }

            if (activeMiniGamePanel != null) {
                showMiniGame();
            }
        } catch (Exception e) {
            System.err.println("❌ Error creating minigame: " + e.getMessage());
            e.printStackTrace();
            isInMiniGame = false;
        }
    }

    /**
     * แสดงมินิเกมบนหน้าจอ
     */
    private void showMiniGame() {
        int w = getWidth();
        int h = getHeight();
        
        if (w <= 0 || h <= 0) {
            w = 1000;
            h = 700;
        }
        
        int gameW = Math.min(900, w - 100);
        int gameH = Math.min(700, h - 100);
        int gameX = (w - gameW) / 2;
        int gameY = (h - gameH) / 2;
        
        activeMiniGamePanel.setBounds(gameX, gameY, gameW, gameH);
        add(activeMiniGamePanel);
        setComponentZOrder(activeMiniGamePanel, 0);
        
        revalidate();
        repaint();
        
        System.out.println("✅ Minigame displayed");
    }

    /**
     * ปิดมินิเกม
     */
    private void closeMiniGame() {
        if (activeMiniGamePanel != null) {
            remove(activeMiniGamePanel);
            activeMiniGamePanel = null;
        }
        isInMiniGame = false;
        miniGameFinished = false;
        currentMiniGameType = "";
    }

    /**
     * ปิดมินิเกมและไปต่อ
     */
    private void closeMiniGameAndContinue() {
        closeMiniGame();
        
        // แสดง UI กลับมา
        dialogueBox.setVisible(true);
        timerBar.setVisible(true);
        
        // ไปซีนต่อไปตามบท
        if (mpServer != null) {
            // Host สั่งให้ทุกคนไปซีนต่อไป
            String nextScene = getNextSceneAfterMinigame(currentMiniGameType);
            if (nextScene != null && !nextScene.isEmpty()) {
                System.out.println("📢 Host broadcasting force next to: " + nextScene);
                mpServer.broadcastForceNext(sceneName, nextScene);
            } else {
                // จบเกม
                System.out.println("🏁 Game finished, broadcasting leaderboard");
                mpServer.broadcastLeaderboard();
            }
        } else {
            System.out.println("⏳ Client waiting for host to continue...");
        }
        
        revalidate();
        repaint();
    }

    /**
     * หาชื่อซีนถัดไปหลังจากมินิเกมตามบท
     */
    private String getNextSceneAfterMinigame(String gameType) {
        if ("MATCH".equals(gameType)) {
            return "MP_SCENE_3D"; // หลังจบเกมจับคู่ ไปฉาก 3D
        } else if ("RPS".equals(gameType)) {
            return "MP_SCENE_4C"; // หลังจบเกมเป่ายิ้งฉุบ ไปฉาก 4C
        }
        return null;
    }

    /**
     * แสดงผลมินิเกม (ใครได้คะแนนเท่าไหร่)
     */
    private void showMiniGameResult(String playerName, boolean won, int points) {
        JLabel resultLabel = new JLabel(
            (won ? "🏆 " : "💔 ") + playerName + 
            (won ? " ชนะ! " : " แพ้ ") + 
            "ได้ " + points + " คะแนน",
            SwingConstants.CENTER
        );
        resultLabel.setFont(new Font("Tahoma", Font.BOLD, 18));
        resultLabel.setForeground(won ? new Color(255, 215, 0) : Color.WHITE);
        resultLabel.setBackground(new Color(0, 0, 0, 180));
        resultLabel.setOpaque(true);
        resultLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        int w = getWidth();
        int h = getHeight();
        int labelW = 400;
        int labelH = 50;
        resultLabel.setBounds((w - labelW) / 2, h - 150, labelW, labelH);
        
        add(resultLabel);
        setComponentZOrder(resultLabel, 0);
        repaint();
        
        Timer hideTimer = new Timer(2000, e -> {
            remove(resultLabel);
            repaint();
        });
        hideTimer.setRepeats(false);
        hideTimer.start();
    }

    /**
     * ตรวจสอบว่าซีนนี้ต้องเล่นมินิเกมหรือไม่ ตามบท
     */
    private void checkForMiniGameTrigger() {
        if (sceneName == null) return;
        
        System.out.println("🔍 Checking scene: " + sceneName);
        
        // ตามบท: ฉาก 3C -> MINIGAME_MATCH, ฉาก 4B -> MINIGAME_RPS
        if ("MP_SCENE_3C".equals(sceneName)) {
            System.out.println("🎮 Triggering MEMORY minigame from scene 3C");
            if (mpServer != null) {
                mpServer.broadcastStartMiniGame("MATCH", 45);
            } else {
                startMiniGame("MATCH", 45);
            }
        } else if ("MP_SCENE_4B".equals(sceneName)) {
            System.out.println("🎮 Triggering RPS minigame from scene 4B");
            if (mpServer != null) {
                mpServer.broadcastStartMiniGame("RPS", 20);
            } else {
                startMiniGame("RPS", 20);
            }
        }
=======
        Runnable winAction = () -> {
            if (miniGameFinished) return;
            miniGameFinished = true;
            Relation.getInstance().addAffection("Jes", 5);
            new Timer(2000, e -> { ((Timer)e.getSource()).stop(); closeMiniGameAndContinue(); }).start();
        };
        Runnable failAction = () -> {
            if (miniGameFinished) return;
            miniGameFinished = true;
            Relation.getInstance().addAffection("Jes", 2);
            new Timer(2000, e -> { ((Timer)e.getSource()).stop(); closeMiniGameAndContinue(); }).start();
        };

        try {
            switch (gameType.toUpperCase()) {
                case "MATCH": case "MEMORY":
                    activeMiniGamePanel = new MemoryMatchMiniGame(winAction, failAction, true);
                    break;
                case "RPS": case "ROCKPAPERSCISSORS":
                    activeMiniGamePanel = new RockPaperScissorsMiniGame(winAction, failAction, true, timeSeconds);
                    break;
                default:
                    isInMiniGame = false; return;
            }
            if (activeMiniGamePanel != null) {
                showMiniGame();
                // ✅ FIX: แจ้ง server ว่าโหลด minigame scene เสร็จแล้ว
                if (mpServer != null && mpServer.hasPendingScene(sceneName))
                    mpServer.hostSceneLoaded(sceneName);
                else if (mpClient != null)
                    mpClient.notifySceneLoaded(sceneName);
            }
        } catch (Exception e) {
            e.printStackTrace(); isInMiniGame = false;
        }
    }

    private void showMiniGame() {
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) { w = 1000; h = 700; }
        int gameW = Math.min(900, w - 100), gameH = Math.min(700, h - 100);
        activeMiniGamePanel.setBounds((w-gameW)/2, (h-gameH)/2, gameW, gameH);
        add(activeMiniGamePanel);
        setComponentZOrder(activeMiniGamePanel, 0);
        revalidate(); repaint();
    }

    private void closeMiniGame() {
        if (activeMiniGamePanel != null) { remove(activeMiniGamePanel); activeMiniGamePanel = null; }
        isInMiniGame = false; miniGameFinished = false; currentMiniGameType = "";
    }

    private void closeMiniGameAndContinue() {
        // ✅ FIX: บันทึก type และ sceneName ก่อน closeMiniGame จะ clear
        String savedType = currentMiniGameType;
        String savedScene = sceneName;
        closeMiniGame();
        dialogueBox.setVisible(true);
        timerBar.setVisible(true);
        if (mpServer != null) {
            String next = getNextSceneAfterMinigame(savedType);
            if (next != null && !next.isEmpty()) {
                mpServer.broadcast("FORCE_NEXT:" + savedScene + ":" + next);
                mpServer.startReadPhase(next);
                loadScene(next);
            }
        } else if (mpClient != null) {
            // ✅ client รอ FORCE_NEXT จาก server — จะ doForceNext → loadScene เอง
        } else {
            // Singleplayer
            String next = getNextSceneAfterMinigame(savedType);
            if (next != null) loadScene(next);
        }
        revalidate(); repaint();
    }

    private String getNextSceneAfterMinigame(String gameType) {
        if ("MATCH".equals(gameType)) return "MP_SCENE_3D";
        if ("RPS".equals(gameType))   return "MP_SCENE_4C";
        return null;
    }

    private void checkForMiniGameTrigger() {
        // ✅ ไม่ใช้แล้ว — loadScene intercept MINIGAME_MATCH/MINIGAME_RPS โดยตรง
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
    }

    // ════════════════════════════════════════════════════
    //  Client Listener
    // ════════════════════════════════════════════════════
    private GameClient.ClientListener buildClientListener() {
        return new GameClient.ClientListener() {
            @Override public void onConnected(String n) {}
            @Override public void onPlayerListReceived(java.util.List<String> n) {}
            @Override public void onGameStart(int s) {}
            @Override public void onPlayerJoined(String n, int t) {}
            @Override public void onPlayerLeft(String n, int t) {}
            @Override public void onScoreUpdate(String n, int s) {}
            @Override public void onChatMessage(String s, String m) {}
            @Override public void onDisconnected(String r) {}
            @Override public void onError(String m) {}
            @Override public void onPhaseRead(String scene, int sec, int total) {
<<<<<<< HEAD
                SwingUtilities.invokeLater(() -> { 
                    if (!isInMiniGame) {
                        timerBar.startReadPhase(scene, sec, total); 
                        relayout();
                    }
=======
                SwingUtilities.invokeLater(() -> {
                    if (isInMiniGame) return;
                    if (!scene.equals(sceneName) || currentScene == null) loadScene(scene);
                    timerBar.startReadPhase(scene, sec, total);
                    // ✅ แจ้ง server ว่าโหลด scene เสร็จแล้ว (ป้องกัน checkAllSceneLoaded ค้าง)
                    if (mpClient != null) mpClient.notifySceneLoaded(scene);
                    relayout();
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
                });
            }
            
            @Override public void onCountdown(int n) {
<<<<<<< HEAD
                SwingUtilities.invokeLater(() -> {
                    if (!isInMiniGame) timerBar.showCountdown(n);
                });
=======
                SwingUtilities.invokeLater(() -> { if (!isInMiniGame) timerBar.showCountdown(n); });
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
            }
            
            @Override public void onPhaseChoice(String scene, int sec, int total) {
                SwingUtilities.invokeLater(() -> {
                    if (isInMiniGame) return;
<<<<<<< HEAD
                    
=======
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
                    timerBar.startChoicePhase(scene, sec, total);
                    unlockChoiceButtons();
                    if (!isChoiceMode || countChoiceButtons() == 0) skipToChoice(true);
                    relayout();
                });
            }
            
            @Override public void onChoiceResult(String target, String charName, int score) {
<<<<<<< HEAD
                SwingUtilities.invokeLater(() -> {
                    if (!isInMiniGame) applyChoiceResult(target, charName, score);
                });
=======
                SwingUtilities.invokeLater(() -> { if (!isInMiniGame) applyChoiceResult(target, charName, score); });
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
            }
            
            @Override public void onForceNext(String scene, String targetScene) {
<<<<<<< HEAD
                SwingUtilities.invokeLater(() -> {
                    if (!isInMiniGame) doForceNext(targetScene);
                });
=======
                SwingUtilities.invokeLater(() -> { if (!isInMiniGame) doForceNext(targetScene); });
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
            }
            
            @Override public void onTimerSync(int t) {
<<<<<<< HEAD
                SwingUtilities.invokeLater(() -> {
                    if (!isInMiniGame) timerBar.setTimeLeft(t);
                });
=======
                SwingUtilities.invokeLater(() -> { if (!isInMiniGame) timerBar.setTimeLeft(t); });
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
            }
            
            @Override public void onReadyCount(int ready, int total) {
<<<<<<< HEAD
                SwingUtilities.invokeLater(() -> {
                    if (!isInMiniGame) timerBar.setReadyCount(ready, total);
                });
            }

            // ✅ เพิ่ม listener สำหรับมินิเกม
            @Override public void onStartMiniGame(String gameType, int timeSeconds) {
                SwingUtilities.invokeLater(() -> {
                    System.out.println("📱 Client received start minigame: " + gameType);
                    startMiniGame(gameType, timeSeconds);
                });
            }

            @Override public void onMiniGameResult(String playerName, boolean won, int points) {
                SwingUtilities.invokeLater(() -> {
                    System.out.println("📱 Client received minigame result: " + playerName);
                    showMiniGameResult(playerName, won, points);
                    
                    if (playerName.equals(GameConstants.PLAYER_NAME)) {
                        Relation.getInstance().addAffection("Jes", points);
                    }
                });
            }

            @Override public void onLeaderboard(java.util.Map<String,Integer> scores) {
                SwingUtilities.invokeLater(() -> {
                    timerBar.resetAndHide();
                    closeMiniGame();
=======
                SwingUtilities.invokeLater(() -> { if (!isInMiniGame) timerBar.setReadyCount(ready, total); });
            }
            @Override public void onLeaderboard(java.util.Map<String,Integer> scores) {
                SwingUtilities.invokeLater(() -> {
                    timerBar.resetAndHide(); closeMiniGame();
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
                    if (onLeaderboardCb != null) onLeaderboardCb.accept(scores);
                });
            }
        };
    }

    // ════════════════════════════════════════════════════
    //  Force Next
    // ════════════════════════════════════════════════════
    private void doForceNext(String targetScene) {
        if (isInMiniGame) return;
<<<<<<< HEAD
        
=======
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
        timerBar.stopTimer();

        if (isChoiceMode && pendingChoices != null && pendingChoices.length > 0) {
            int pick = random.nextInt(pendingChoices.length);
            Object[] chosen = pendingChoices[pick];
            String pickedTarget = (String) chosen[1];
            String cn = chosen.length >= 4 ? (String) chosen[2] : null;
            int    cs = chosen.length >= 4 ? (Integer) chosen[3] : 0;
            if (countChoiceButtons() > 0) highlightRandomPick(pick);
<<<<<<< HEAD

            if (cn != null && !cn.isEmpty() && cs != 0) {
                Relation.getInstance().addAffection(cn, cs);
                RelationUI.getInstance().setVisible(false);
            }

            if (mpServer != null) {
                new Timer(500, e -> {
                    ((Timer)e.getSource()).stop();
=======
            if (cn != null && !cn.isEmpty() && cs != 0) Relation.getInstance().addAffection(cn, cs);

            if (mpServer != null) {
                new Timer(500, e -> { ((Timer)e.getSource()).stop();
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
                    isChoiceMode = false; pendingChoices = null;
                    choiceLayer.removeAll(); choiceLayer.setVisible(false);
                    timerBar.resetAndHide(); mpServer.forceChoiceTimeout(pickedTarget);
                }).start();
            } else if (mpClient != null) {
<<<<<<< HEAD
                new Timer(500, e -> {
                    ((Timer)e.getSource()).stop();
=======
                new Timer(500, e -> { ((Timer)e.getSource()).stop();
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
                    isChoiceMode = false; pendingChoices = null;
                    choiceLayer.removeAll(); choiceLayer.setVisible(false);
                    lockChoiceButtons(); timerBar.showWaitingForPlayers();
                    mpClient.sendPlayerChoice(pickedTarget, cn, cs);
                }).start();
            } else {
<<<<<<< HEAD
                new Timer(500, e -> {
                    ((Timer)e.getSource()).stop();
                    applyChoiceResult(pickedTarget, cn, cs);
                }).start();
=======
                new Timer(500, e -> { ((Timer)e.getSource()).stop(); applyChoiceResult(pickedTarget, cn, cs); }).start();
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
            }
            return;
        }

<<<<<<< HEAD
        isChoiceMode   = false;
        pendingChoices = null;
        sentSceneReady = false;
        choiceLayer.removeAll();
        choiceLayer.setVisible(false);
        timerBar.resetAndHide();

        if (targetScene != null && !targetScene.isEmpty() && storyMap.containsKey(targetScene)) {
            if (mpServer != null) {
                mpServer.broadcastChoiceResult(targetScene, null, 0);
            } else if (mpClient != null) {
                // รอ server
            } else {
                loadScene(targetScene);
            }
=======
        isChoiceMode = false; pendingChoices = null; sentSceneReady = false;
        choiceLayer.removeAll(); choiceLayer.setVisible(false); timerBar.resetAndHide();

        if (targetScene != null && !targetScene.isEmpty() && storyMap.containsKey(targetScene)) {
            if (mpServer != null) mpServer.broadcastChoiceResult(targetScene, null, 0);
            else if (mpClient == null) loadScene(targetScene);
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
        } else {
            if (mpServer != null) nextScene();
            else if (mpClient == null) nextScene();
        }
    }

    private void highlightRandomPick(int index) {
        int i = 0;
        for (Component c : choiceLayer.getComponents()) {
            if (c instanceof UI_Components.ChoiceButton) {
                c.setBackground(i == index ? new Color(255, 180, 0) : new Color(60, 60, 80)); i++;
            }
        }
        repaint();
    }

<<<<<<< HEAD
    // ════════════════════════════════════════════════════
    //  Register next scenes
    // ════════════════════════════════════════════════════
=======
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
    private void registerNextScenes(GameServer server) {
        String[] order = StoryDataMP.SCENE_ORDER;
        for (int i = 0; i < order.length - 1; i++) server.registerNextScene(order[i], order[i+1]);
    }

    // ════════════════════════════════════════════════════
    //  UI Setup
    // ════════════════════════════════════════════════════
    private void initStoryMap() {
        for (String name : StoryDataMP.SCENE_ORDER) {
            Object[][] data = StoryDataMP.getScene(name);
            if (data != null) storyMap.put(name, data);
        }
    }

    private void setupUI() {
        bgLayer = new JLabel() {
            @Override protected void paintComponent(Graphics g) { super.paintComponent(g); drawBG((Graphics2D)g); }
        };
        characterLayer = new CharacterSprite("") {
            @Override protected void paintComponent(Graphics g) { drawChar((Graphics2D)g); }
        };
        effectLayer = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g.create();
                if (effectManager != null) effectManager.drawEffects(g2, getWidth(), getHeight());
                g2.dispose();
            }
        };
        effectLayer.setOpaque(false);
        choiceLayer = new JPanel();
        choiceLayer.setLayout(new BoxLayout(choiceLayer, BoxLayout.Y_AXIS));
        choiceLayer.setOpaque(false);
        timerBar = new MultiplayerTimerBar();

        add(timerBar); add(choiceLayer); add(dialogueBox);
        add(effectLayer); add(characterLayer); add(bgLayer);
        fixZOrder();
    }

    private void fixZOrder() {
        try {
            int z = 0;
            if (activeMiniGamePanel != null) setComponentZOrder(activeMiniGamePanel, z++);
            if (timerBar != null && timerBar.isVisible()) setComponentZOrder(timerBar, z++);
            setComponentZOrder(choiceLayer,    z++);
            setComponentZOrder(dialogueBox,    z++);
            setComponentZOrder(effectLayer,    z++);
            setComponentZOrder(characterLayer, z++);
            setComponentZOrder(bgLayer,        z);
        } catch (Exception ignored) {}
    }

    private void drawBG(Graphics2D g2) {
        if (effectManager != null) g2.translate(effectManager.getShakeX(), effectManager.getShakeY());
        if (currentBG == null || currentBG.isEmpty() || currentBG.equals("none")) return;
        try {
            Image img = new ImageIcon(currentBG).getImage();
            if (img == null || img.getWidth(null) <= 0) return;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int pw = getWidth(), ph = getHeight();
            double s = Math.max((double)pw/img.getWidth(null),(double)ph/img.getHeight(null)) + 0.15;
            int dw = (int)(img.getWidth(null)*s), dh = (int)(img.getHeight(null)*s);
            g2.drawImage(img, (pw-dw)/2, (ph-dh)/2, dw, dh, this);
        } catch (Exception ignored) {}
    }

    private void drawChar(Graphics2D g2) {
        if (effectManager != null) g2.translate(effectManager.getShakeX(), effectManager.getShakeY());
        if (currentChar == null || currentChar.isEmpty() || currentChar.equals("none")) return;
        try {
            Image img = new ImageIcon(currentChar).getImage();
            if (img == null || img.getWidth(null) <= 0) return;
            int ph = getHeight();
            double r = (double)(ph+30)/img.getHeight(null);
            int dw = (int)(img.getWidth(null)*r);
            g2.drawImage(img, (getWidth()-dw)/2, 0, dw, ph+30, this);
        } catch (Exception ignored) {}
    }

    private void relayout() {
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;
<<<<<<< HEAD

        RelationUI.getInstance().setVisible(false);

        // ✅ ถ้ากำลังเล่นมินิเกม ให้จัดขนาดมินิเกม
        if (isInMiniGame && activeMiniGamePanel != null) {
            int gameW = Math.min(900, w - 100);
            int gameH = Math.min(700, h - 100);
            int gameX = (w - gameW) / 2;
            int gameY = (h - gameH) / 2;
            activeMiniGamePanel.setBounds(gameX, gameY, gameW, gameH);
            revalidate();
            repaint();
            return;
        }

        bgLayer.setBounds(0, 0, w, h);
        characterLayer.setBounds(0, 0, w, h);
        effectLayer.setBounds(0, 0, w, h);
=======
        RelationUI.getInstance().setVisible(false);

        if (isInMiniGame && activeMiniGamePanel != null) {
            int gW = Math.min(900, w-100), gH = Math.min(700, h-100);
            activeMiniGamePanel.setBounds((w-gW)/2, (h-gH)/2, gW, gH);
            revalidate(); repaint(); return;
        }
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef

        bgLayer.setBounds(0,0,w,h); characterLayer.setBounds(0,0,w,h); effectLayer.setBounds(0,0,w,h);
        int tw = Math.min((int)(w*0.70), 560);
        timerBar.setBounds((w-tw)/2, 8, tw, 72);

        int gw = (int)(w*0.70), gh = (int)(h*0.25);
        int btnCount = countChoiceButtons();
        int dialogueY = (isChoiceMode && btnCount > 0) ? h-gh-(btnCount*65)-70 : h-gh-70;
        dialogueY = Math.max(dialogueY, 72+16);

        dialogueBox.moveTo(gw, gh, dialogueY);
        layoutChoiceButtons(gw, dialogueY+gh+10);
        revalidate(); repaint();
    }

    private void layoutChoiceButtons(int gw, int startY) {
        int w = getWidth(), cx = (w-gw)/2, btnH = 55, gap = 10, y = 0;
        for (Component c : choiceLayer.getComponents()) {
            if (c instanceof UI_Components.ChoiceButton) { c.setBounds(0, y, gw, btnH); y += btnH+gap; }
        }
        choiceLayer.setBounds(cx, startY, gw, Math.max(y, 50));
    }

    private int countChoiceButtons() {
        int n = 0;
        for (Component c : choiceLayer.getComponents()) if (c instanceof UI_Components.ChoiceButton) n++;
        return n;
    }

    public void showPlayerLeftToast(String playerName) {
        SwingUtilities.invokeLater(() -> {
            Window parent = SwingUtilities.getWindowAncestor(this);
            JWindow popup = new JWindow(parent);
            JPanel panel = new JPanel(new BorderLayout(8, 0));
            panel.setBackground(new Color(30,30,30,220));
            panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200,60,60),2),
                BorderFactory.createEmptyBorder(10,14,10,14)));
            JLabel icon = new JLabel("✖ "); icon.setFont(new Font("Dialog",Font.BOLD,14)); icon.setForeground(new Color(255,80,80));
            JLabel msg = new JLabel(playerName+" ออกจากห้องแล้ว"); msg.setFont(new Font("Tahoma",Font.PLAIN,14)); msg.setForeground(Color.WHITE);
            panel.add(icon, BorderLayout.WEST); panel.add(msg, BorderLayout.CENTER);
            popup.add(panel); popup.pack();
            if (parent != null) popup.setLocation(parent.getX()+parent.getWidth()-popup.getWidth()-14, parent.getY()+parent.getHeight()-popup.getHeight()-14);
            popup.setOpacity(0.92f); popup.setVisible(true);
            new Timer(3000, e -> { ((Timer)e.getSource()).stop(); popup.dispose(); }).start();
        });
    }

    public void resetWaitingState() {
        SwingUtilities.invokeLater(() -> { timerBar.resetWaitingForPlayers(); unlockChoiceButtons(); });
    }

    private void lockChoiceButtons() {
        for (Component c : choiceLayer.getComponents()) if (c instanceof UI_Components.ChoiceButton) c.setEnabled(false); repaint();
    }
    
    private void unlockChoiceButtons() {
        for (Component c : choiceLayer.getComponents()) if (c instanceof UI_Components.ChoiceButton) c.setEnabled(true); repaint();
    }

    // ════════════════════════════════════════════════════
    //  Scene Logic
    // ════════════════════════════════════════════════════
    private void loadScene(String name) {
        // ✅ FIX: intercept MINIGAME scenes → เปิด minigame โดยตรง
        if ("MINIGAME_MATCH".equals(name)) {
            sceneName = name;
            startMiniGame("MATCH", 45);
            return;
        }
        if ("MINIGAME_RPS".equals(name)) {
            sceneName = name;
            startMiniGame("RPS", 20);
            return;
        }

        if (isInMiniGame) return;
        Object[][] data = storyMap.get(name);
        if (data == null) { 
            System.out.println("❌ Scene not found: " + name);
            finishGame(); 
            return; 
        }
        
        System.out.println("📖 Loading scene: " + name);
        effectManager.stopAll();
        currentScene = data; sceneName = name;
        sceneIndex = 0; sentSceneReady = false; waitingForResult = false;
        isChoiceMode = false; pendingChoices = null;
        choiceLayer.removeAll(); choiceLayer.setVisible(false);

        if (mpServer != null) {
            if (mpServer.hasPendingScene(name)) mpServer.hostSceneLoaded(name);
            else mpServer.startReadPhase(name);
        } else if (mpClient != null) {
            timerBar.resetAndHide();
        } else {
                            if (!isChoiceMode || countChoiceButtons() == 0) skipToChoice(true);
                            relayout();
        }
        renderLine(currentScene[sceneIndex]);
        checkForMiniGameTrigger();
    }

    private void renderLine(Object[] line) {
        if (line == null || line.length < 2 || isInMiniGame) return;
<<<<<<< HEAD
        
        choiceLayer.removeAll(); 
        choiceLayer.setVisible(false);
        isChoiceMode = false; 
        Object sp = line[0];
        if (sp != null) {
            if (s.equalsIgnoreCase("PLAYER") || s.equals(GameConstants.PLAYER_NAME)) currentSpeaker = GameConstants.PLAYER_NAME;
            else if (!s.isEmpty() && !s.equalsIgnoreCase("none")) currentSpeaker = s;
        }
        dialogueBox.setText(currentSpeaker, "");
        fullText = line[1].toString().replace("[PLAYER]", GameConstants.PLAYER_NAME);

        if (line.length >= 3) { String fn=(String)line[2]; currentChar=(fn==null||fn.isEmpty()||fn.equals("none"))?"":fn.startsWith("model/")?fn:GameConstants.CHAR_PATH+fn; characterLayer.updateCharacter(currentChar); }
        if (line.length >= 4) { String bg=(String)line[3]; currentBG=(bg==null||bg.isEmpty()||bg.equals("none"))?"":bg.startsWith("model/")?bg:GameConstants.SCENE_PATH+bg; }

        for (int i = 4; i < line.length; i++) {
            Object slot = line[i];
<<<<<<< HEAD
            if (slot instanceof Object[][]) {
                pendingChoices = (Object[][]) slot; 
                isChoiceMode = true;
            } else if (slot instanceof String) {
                String val = (String)slot;
                if (val==null||val.isEmpty()||val.equalsIgnoreCase("none")) continue;
                String up = val.toUpperCase();
                if (up.contains("FADE")||up.contains("WHITE")||up.contains("BLACK")
                        ||up.equals("SHAKE")||up.equals("FLASH")) {
                    effectManager.stopAll(); 
                    effectManager.play(val);
                } else {
                    String path = GameConstants.SOUND_PATH+val+".wav";
                    if (up.startsWith("BGM")) soundManager.playBGM(path); 
                    else soundManager.playSE(path);
                }
            }
        }
        dialogueBox.setVisible(true);
        relayout(); 
        startTypewriter();
=======
            if (slot instanceof Object[][]) { pendingChoices = (Object[][])slot; isChoiceMode = true; }
            else if (slot instanceof String) {
                String val = (String)slot;
                if (val==null||val.isEmpty()||val.equalsIgnoreCase("none")) continue;
                String up = val.toUpperCase();
                if (up.contains("FADE")||up.contains("WHITE")||up.contains("BLACK")||up.equals("SHAKE")||up.equals("FLASH")) { effectManager.stopAll(); effectManager.play(val); }
                else { String path = GameConstants.SOUND_PATH+val+".wav"; if (up.startsWith("BGM")) soundManager.playBGM(path); else soundManager.playSE(path); }
            }
        }
        dialogueBox.setVisible(true); relayout(); startTypewriter();
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
    }

    private void showChoices(Object[][] choices, boolean alreadyUnlocked) {
        if (choices == null || isInMiniGame) return;
<<<<<<< HEAD
        
        isChoiceMode = true; 
        choiceLayer.removeAll();
=======
        isChoiceMode = true; choiceLayer.removeAll();
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef

        for (int i = 0; i < choices.length; i++) {
            final String target=(String)choices[i][1];
            final String cn=choices[i].length>=4?(String)choices[i][2]:null;
            final int    cs=choices[i].length>=4?(Integer)choices[i][3]:0;
            final String text=(String)choices[i][0];
            final String num=String.valueOf(i+1);
            UI_Components.ChoiceButton btn = new UI_Components.ChoiceButton(num, text, () -> {
                if (timerBar.isChoiceLocked() || isInMiniGame) return;
                doChoice(target, cn, cs);
            });
            btn.setEnabled(alreadyUnlocked); choiceLayer.add(btn);
        }
<<<<<<< HEAD

        relayout(); 
        choiceLayer.setVisible(true); 
        fixZOrder();
        
=======
        relayout(); choiceLayer.setVisible(true); fixZOrder();
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
        if (alreadyUnlocked) unlockChoiceButtons();
        if (!alreadyUnlocked && !sentSceneReady) {
            sentSceneReady = true;
            if (mpClient != null) mpClient.notifySceneReady(sceneName);
            if (mpServer != null) mpServer.hostSceneReady(sceneName);
        }
    }

    private void skipToChoice(boolean unlock) {
        if (isInMiniGame) return;
<<<<<<< HEAD
        
=======
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
        if (typeTimer != null) typeTimer.stop();
        if (isChoiceMode && countChoiceButtons() > 0) { if (unlock) unlockChoiceButtons(); return; }
        if (currentScene == null) return;
        
        for (int i = sceneIndex; i < currentScene.length; i++) {
            Object[] line = currentScene[i];
            if (line == null || line.length < 2) continue;
            
            for (int j = 4; j < line.length; j++) {
                if (!(line[j] instanceof Object[][])) continue;
                
                sceneIndex = i; 
                pendingChoices = (Object[][])line[j]; 
                isChoiceMode = true;
                
                currentSpeaker = "";
<<<<<<< HEAD
                if (line[0] != null) {
                    String s = line[0].toString().trim();
                    if (s.equalsIgnoreCase("PLAYER")||s.equals(GameConstants.PLAYER_NAME))
                        currentSpeaker = GameConstants.PLAYER_NAME;
                    else if (!s.isEmpty() && !s.equalsIgnoreCase("none"))
                        currentSpeaker = s;
                }
                
                fullText = line[1].toString().replace("[PLAYER]", GameConstants.PLAYER_NAME);
                dialogueBox.setText(currentSpeaker, fullText);
                
                if (line.length>=3) { 
                    String fn=(String)line[2]; 
                    currentChar=(fn==null||fn.isEmpty()||fn.equals("none"))?"":fn.startsWith("model/")?fn:GameConstants.CHAR_PATH+fn; 
                    characterLayer.updateCharacter(currentChar); 
                }
                
                if (line.length>=4) { 
                    String bg=(String)line[3]; 
                    currentBG=(bg==null||bg.isEmpty()||bg.equals("none"))?"":bg.startsWith("model/")?bg:GameConstants.SCENE_PATH+bg; 
                }
                
                dialogueBox.setVisible(true);
                showChoices(pendingChoices, unlock);
                return;
=======
                if (line[0] != null) { String s=line[0].toString().trim(); if (s.equalsIgnoreCase("PLAYER")||s.equals(GameConstants.PLAYER_NAME)) currentSpeaker=GameConstants.PLAYER_NAME; else if (!s.isEmpty()&&!s.equalsIgnoreCase("none")) currentSpeaker=s; }
                fullText = line[1].toString().replace("[PLAYER]", GameConstants.PLAYER_NAME);
                dialogueBox.setText(currentSpeaker, fullText);
                if (line.length>=3) { String fn=(String)line[2]; currentChar=(fn==null||fn.isEmpty()||fn.equals("none"))?"":fn.startsWith("model/")?fn:GameConstants.CHAR_PATH+fn; characterLayer.updateCharacter(currentChar); }
                if (line.length>=4) { String bg=(String)line[3]; currentBG=(bg==null||bg.isEmpty()||bg.equals("none"))?"":bg.startsWith("model/")?bg:GameConstants.SCENE_PATH+bg; }
                dialogueBox.setVisible(true); showChoices(pendingChoices, unlock); return;
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
            }
        }
        nextScene();
    }

    private void doChoice(String target, String charName, int score) {
        if (isInMiniGame) return;
<<<<<<< HEAD
        
        if (charName != null && !charName.isEmpty() && score != 0) {
            Relation.getInstance().addAffection(charName, score);
            RelationUI.getInstance().setVisible(false);
        }

        isChoiceMode = false; 
        pendingChoices = null; 
        sentSceneReady = false;
        choiceLayer.removeAll(); 
        choiceLayer.setVisible(false);
        timerBar.stopTimer();
        lockChoiceButtons();

        if (mpServer != null) {
            waitingForResult = true;
            mpServer.broadcastChoiceResult(target, charName, score);
            timerBar.showWaitingForPlayers();
        } else if (mpClient != null) {
            waitingForResult = true;
            mpClient.sendPlayerChoice(target, charName, score);
            timerBar.showWaitingForPlayers();
        } else {
            timerBar.resetAndHide();
            if (target != null && storyMap.containsKey(target)) loadScene(target);
            else finishGame();
        }
=======
        if (charName != null && !charName.isEmpty() && score != 0) { Relation.getInstance().addAffection(charName, score); RelationUI.getInstance().setVisible(false); }
        isChoiceMode = false; pendingChoices = null; sentSceneReady = false;
        choiceLayer.removeAll(); choiceLayer.setVisible(false); timerBar.stopTimer(); lockChoiceButtons();

        if (mpServer != null) { waitingForResult = true; mpServer.broadcastChoiceResult(target, charName, score); timerBar.showWaitingForPlayers(); }
        else if (mpClient != null) { waitingForResult = true; mpClient.sendPlayerChoice(target, charName, score); timerBar.showWaitingForPlayers(); }
        else { timerBar.resetAndHide(); if (target != null && storyMap.containsKey(target)) loadScene(target); else finishGame(); }
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
    }

    private void applyChoiceResult(String target, String charName, int score) {
        if (isInMiniGame) return;
<<<<<<< HEAD
        
        if (isChoiceMode && pendingChoices != null && pendingChoices.length > 0) {
            int pick = random.nextInt(pendingChoices.length);
            Object[] chosen = pendingChoices[pick];
            String cn = chosen.length >= 4 ? (String) chosen[2] : null;
            int    cs = chosen.length >= 4 ? (Integer) chosen[3] : 0;
            if (cn != null && !cn.isEmpty() && cs != 0) {
                Relation.getInstance().addAffection(cn, cs);
                RelationUI.getInstance().setVisible(false);
            }
        }

        isChoiceMode = false; 
        pendingChoices = null; 
        sentSceneReady = false;
        waitingForResult = false;
        choiceLayer.removeAll(); 
        choiceLayer.setVisible(false);
        timerBar.resetAndHide();

        if (target == null || target.isEmpty() || !storyMap.containsKey(target)) {
            finishGame(); 
            return;
        }

        if (mpServer != null) {
            loadScene(target);
        } else if (mpClient != null) {
            loadScene(target);
            mpClient.notifySceneLoaded(target);
        } else {
            loadScene(target);
=======
        if (isChoiceMode && pendingChoices != null && pendingChoices.length > 0) {
            int pick = random.nextInt(pendingChoices.length);
            Object[] chosen = pendingChoices[pick];
            String cn=chosen.length>=4?(String)chosen[2]:null; int cs=chosen.length>=4?(Integer)chosen[3]:0;
            if (cn!=null&&!cn.isEmpty()&&cs!=0) { Relation.getInstance().addAffection(cn, cs); RelationUI.getInstance().setVisible(false); }
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
        }
        isChoiceMode = false; pendingChoices = null; sentSceneReady = false; waitingForResult = false;
        choiceLayer.removeAll(); choiceLayer.setVisible(false); timerBar.resetAndHide();
        if (target==null||target.isEmpty()||!storyMap.containsKey(target)) { finishGame(); return; }
        if (mpClient != null) { loadScene(target); mpClient.notifySceneLoaded(target); } else loadScene(target);
    }

    private void onNext() {
        if (waitingForResult || isInMiniGame) return;
<<<<<<< HEAD

        if (typeTimer != null && typeTimer.isRunning()) {
            typeTimer.stop(); 
            charIndex = fullText.length();
            dialogueBox.setText(currentSpeaker, fullText); 
            return;
        }
        if (isChoiceMode && countChoiceButtons() == 0 && pendingChoices != null) {
            showChoices(pendingChoices, !timerBar.isChoiceLocked()); 
            return;
        }
=======
        if (typeTimer != null && typeTimer.isRunning()) { typeTimer.stop(); charIndex = fullText.length(); dialogueBox.setText(currentSpeaker, fullText); return; }
        if (isChoiceMode && countChoiceButtons() == 0 && pendingChoices != null) { showChoices(pendingChoices, !timerBar.isChoiceLocked()); return; }
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
        if (isChoiceMode && countChoiceButtons() > 0) return;
        sceneIndex++;
        if (sceneIndex < currentScene.length) renderLine(currentScene[sceneIndex]); else nextScene();
    }

    private void nextScene() {
        timerBar.stopTimer();
        if ("MP_END".equals(sceneName)) { 
            finishGame(); 
            return; 
        }
        
        String[] order = StoryDataMP.SCENE_ORDER;
<<<<<<< HEAD
        for (int i = 0; i < order.length - 1; i++) {
            if (order[i].equals(sceneName)) { 
                loadScene(order[i+1]); 
                return; 
            }
        }
        finishGame();
    }

    private void finishGame() {
        timerBar.resetAndHide();
        closeMiniGame();
        if (onGameFinished != null) onGameFinished.run();
    }

    private void startTypewriter() {
        if (isInMiniGame) return;
        
=======
        for (int i = 0; i < order.length-1; i++) { if (order[i].equals(sceneName)) { loadScene(order[i+1]); return; } }
        finishGame();
    }

    private void finishGame() { timerBar.resetAndHide(); closeMiniGame(); if (onGameFinished != null) onGameFinished.run(); }

    private void startTypewriter() {
        if (isInMiniGame) return;
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
        charIndex = 0;
        if (typeTimer != null) typeTimer.stop();
        dialogueBox.setVisible(true);
        typeTimer = new Timer(GameConstants.TYPEWRITER_SPEED, e -> {
<<<<<<< HEAD
            if (charIndex < fullText.length()) {
                charIndex++;
                dialogueBox.setText(currentSpeaker, fullText.substring(0, charIndex));
            } else { 
                ((Timer)e.getSource()).stop(); 
            }
=======
            if (charIndex < fullText.length()) { charIndex++; dialogueBox.setText(currentSpeaker, fullText.substring(0, charIndex)); }
            else ((Timer)e.getSource()).stop();
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
        });
        typeTimer.start();
    }

    // ════════════════════════════════════════════════════
    //  Keys — ✅ SPACE + F (next), 1/2/3 (choice), ESC (pause)
    // ════════════════════════════════════════════════════
    private void setupKeys() {
        setFocusable(false);
<<<<<<< HEAD

=======
>>>>>>> d8bd9ab18a98ae6d1f3954d7f7c53d0c290ad0ef
        InputMap  im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        // NEXT — ปุ่มหลัก + F สำรองเสมอ
        im.put(KeyStroke.getKeyStroke(KeyConfig.getNextMsg(), 0), "mp_next");
        im.put(KeyStroke.getKeyStroke(KeyConfig.getNextMsgAlt(), 0), "mp_next");
        am.put("mp_next", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { onNext(); }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "mp_escape");
        am.put("mp_escape", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { handleEsc(); }
        });

        // Choice 1/2/3 — ทำงานเฉพาะตอน isChoiceMode
        int[] choiceKeys = { KeyConfig.getChoice1(), KeyConfig.getChoice2(), KeyConfig.getChoice3() };
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            im.put(KeyStroke.getKeyStroke(choiceKeys[i], 0), "mp_ch" + i);
            am.put("mp_ch" + i, new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    if (!isChoiceMode || pendingChoices == null || idx >= pendingChoices.length) return;
                    if (timerBar.isChoiceLocked() || isInMiniGame) return;
                    Object[] ch = pendingChoices[idx];
                    String target=(String)ch[1];
                    String cn=ch.length>=4?(String)ch[2]:null;
                    int    cs=ch.length>=4?(Integer)ch[3]:0;
                    doChoice(target, cn, cs);
                }
            });
        }
    }

    private void handleEsc() {
        PauseMenuUI pause = PauseMenuUI.getInstance();
        if (pause.isMenuVisible()) { 
            pause.hideMenu(); 
            return; 
        }
        pause.showMenu(PlaySceneMP.this, false);
    }
}