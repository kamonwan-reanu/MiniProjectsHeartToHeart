package model;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GameServer — 2-phase timer + countdown
 *
 * Phase 1 READ (50วิ):
 *   PHASE_READ:scene:sec:total
 *   TIMER_SYNC:t  ทุก 1 วิ
 *   READY_COUNT:n:total
 *   ครบ/หมดเวลา → countdown 3 วิ
 *
 * Countdown (3วิ ก่อน choice):
 *   COUNTDOWN:3 / COUNTDOWN:2 / COUNTDOWN:1
 *   → PHASE_CHOICE:scene:sec:total
 *
 * Phase 2 CHOICE (10วิ):
 *   PHASE_CHOICE:scene:sec:total
 *   TIMER_SYNC:t
 *   หมด → FORCE_NEXT:scene:targetScene (ทุกคนไปพร้อมกัน)
 */
public class GameServer {

    public static final int MAX_PLAYERS     = 3;
    public static final int READ_SECONDS    = 50;
    public static final int COUNTDOWN_SECS  = 3;
    public static final int CHOICE_SECONDS  = 10;

    // สุ่ม port 40000–49999 เพื่อไม่ชนกับเกมอื่น
    public static int randomPort() {
        return 40000 + new Random().nextInt(9999);
    }

    public interface ServerListener {
        void onPlayerJoined(String playerName, int total);
        void onPlayerLeft(String playerName, int total);
        void onScoreReceived(String playerName, int score);
        void onAllPlayersFinished(Map<String, Integer> finalScores);
        /** แจ้ง Host ว่ามี player ออกระหว่าง choice phase — ให้ reset waitingForPlayers UI */
        default void onPlayerLeftDuringChoice() {}
        void onServerError(String message);
        void onServerStarted(String ip, int port);

        default void onPhaseRead(String scene, int sec, int total) {}
        default void onCountdown(int n) {}                          // 3,2,1
        default void onPhaseChoice(String scene, int sec, int total) {}
        default void onForceNext(String scene, String targetScene) {}
        /** Host เลือก choice → ทุกคนไปซีนเดียวกัน */
        default void onChoiceResult(String targetScene, String charName, int score) {}
        default void onTimerSync(int t) {}
        default void onReadyCount(int ready, int total) {}
    }

    private ServerSocket   serverSocket;
    private final List<ClientHandler>  clients      = new CopyOnWriteArrayList<>();
    private final Map<String,Integer>  playerScores = new ConcurrentHashMap<>();
    private final Set<String>          readySet     = ConcurrentHashMap.newKeySet();
    private final Set<String>          finishedSet  = ConcurrentHashMap.newKeySet();
    private final Set<String>          clientReadyForScene = ConcurrentHashMap.newKeySet();
    private String  pendingScene = "";
    private int     sceneLoadedCount = 0;
    // ✅ เก็บ choice ของแต่ละคน (รอให้ครบก่อน broadcast)
    private final Map<String,String[]> playerChoices = new ConcurrentHashMap<>();
    private String  hostChoiceTarget = null;

    private ServerListener listener;
    private boolean running  = false;
    private boolean locked   = false;
    private int     port;
    private int     expectedPlayers = 2;
    private int     lockedTotal     = 0;  // ✅ FIX 2: จำนวนผู้เล่นที่ lock ไว้ตอนเริ่มเกม
    private String  hostName        = "Host";
    private String  currentScene    = "";
    private boolean inChoicePhase   = false;

    private java.util.Timer serverTimer = null;
    private final AtomicInteger timerSec = new AtomicInteger(0);

    // เก็บ targetScene ต่อซีน (set โดย StoryDataMP หรือ PlaySceneMP)
    private final Map<String, String> sceneNextMap = new ConcurrentHashMap<>();

    public GameServer(int port)               { this.port = port; }
    public void setListener(ServerListener l) { this.listener = l; }
    public void setExpectedPlayers(int n)     { this.expectedPlayers = Math.min(n, MAX_PLAYERS); }
    public void lockRoom() {
        locked = true;
        lockedTotal = clients.size() + 1;
        if (expectedPlayers < lockedTotal) expectedPlayers = lockedTotal;
        finishedSet.clear();
    }
    public void setHostName(String name)      { this.hostName = name; }
    public int  getPlayerCount()              { return clients.size(); }
    public boolean isRunning()                { return running; }

    /** Host บันทึก nextScene ของแต่ละซีน (เรียกจาก PlaySceneMP ตอนเริ่ม) */
    public void registerNextScene(String scene, String next) {
        if (next != null) sceneNextMap.put(scene, next);
    }

    public void receiveHostScore(String name, int score) {
        playerScores.put(name, score);
        finishedSet.add(name); // ✅ FIX 2
        broadcast("SCORE_UPDATE:" + name + ":" + score);
        if (listener != null) listener.onScoreReceived(name, score);
        checkAllFinished();
    }

    // ════════════════════════════════════════════════
    //  Phase 1 READ
    // ════════════════════════════════════════════════
    public void startReadPhase(String sceneName) {
        stopTimer();
        readySet.clear();
        playerChoices.clear();
        hostChoiceTarget = null;
        currentScene  = sceneName;
        inChoicePhase = false;
        // ✅ FIX 3: ใช้ lockedTotal ถ้ามี เพื่อให้ ready count ไม่เปลี่ยนกะทันหัน
        int total = lockedTotal > 0 ? lockedTotal : clients.size() + 1;
        timerSec.set(READ_SECONDS);

        broadcast("PHASE_READ:" + sceneName + ":" + READ_SECONDS + ":" + total);
        if (listener != null) listener.onPhaseRead(sceneName, READ_SECONDS, total);

        serverTimer = new java.util.Timer("ReadTimer", true);
        serverTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                int t = timerSec.decrementAndGet();
                broadcast("TIMER_SYNC:" + t);
                if (listener != null) listener.onTimerSync(t);
                if (t <= 0) { stopTimer(); startCountdown(sceneName); }
            }
        }, 1000, 1000);
    }

    // ════════════════════════════════════════════════
    //  Countdown 3,2,1 ก่อน Phase 2
    // ════════════════════════════════════════════════
    private void startCountdown(String sceneName) {
        stopTimer();
        final int[] count = {COUNTDOWN_SECS};

        // broadcast countdown เริ่มต้น
        broadcast("COUNTDOWN:" + count[0]);
        if (listener != null) listener.onCountdown(count[0]);

        serverTimer = new java.util.Timer("Countdown", true);
        serverTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                count[0]--;
                if (count[0] > 0) {
                    broadcast("COUNTDOWN:" + count[0]);
                    if (listener != null) listener.onCountdown(count[0]);
                } else {
                    stopTimer();
                    startChoicePhase(sceneName);
                }
            }
        }, 1000, 1000);
    }

    // ════════════════════════════════════════════════
    //  Phase 2 CHOICE
    // ════════════════════════════════════════════════
    private void startChoicePhase(String sceneName) {
        stopTimer();
        readySet.clear();
        inChoicePhase = true;
        int total = clients.size() + 1;
        timerSec.set(CHOICE_SECONDS);

        broadcast("PHASE_CHOICE:" + sceneName + ":" + CHOICE_SECONDS + ":" + total);
        if (listener != null) listener.onPhaseChoice(sceneName, CHOICE_SECONDS, total);

        serverTimer = new java.util.Timer("ChoiceTimer", true);
        serverTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                int t = timerSec.decrementAndGet();
                broadcast("TIMER_SYNC:" + t);
                if (listener != null) listener.onTimerSync(t);
                if (t <= 0) {
                    stopTimer();
                    // หา targetScene — ถ้าไม่มีให้ใช้ "" (PlaySceneMP จะ nextScene เอง)
                    String target = sceneNextMap.getOrDefault(sceneName, "");
                    broadcast("FORCE_NEXT:" + sceneName + ":" + target);
                    if (listener != null) listener.onForceNext(sceneName, target);
                }
            }
        }, 1000, 1000);
    }

    private void stopTimer() {
        if (serverTimer != null) { serverTimer.cancel(); serverTimer = null; }
    }

    // ════════════════════════════════════════════════
    //  Host ส่งผล choice ให้ทุกคน
    //  เรียกแทน loadScene โดยตรง เพื่อให้ทุกคนไปพร้อมกัน
    // ════════════════════════════════════════════════
    /**
     * HOST ส่ง choice มาเก็บ แล้วรอให้ clients ส่งครบก่อน broadcast
     */
    public void broadcastChoiceResult(String targetScene, String charName, int score) {
        if (!inChoicePhase) return;
        hostChoiceTarget = targetScene;
        playerChoices.put(hostName, new String[]{targetScene, charName != null ? charName : "", String.valueOf(score)});
        checkAllChosen();
    }

    /** Client ส่ง choice มาเก็บ แล้วเช็คว่าครบหรือยัง */
    public void receiveClientChoice(String playerName, String target, String charName, int score) {
        if (!inChoicePhase) return;
        playerChoices.put(playerName, new String[]{target, charName != null ? charName : "", String.valueOf(score)});
        checkAllChosen();
    }

    /** ครบทุกคนแล้ว (ทั้ง host และ clients) → broadcast */
    private void checkAllChosen() {
        int total = lockedTotal > 0 ? lockedTotal : clients.size() + 1;
        // ถ้า host ยังไม่กด → รอก่อน (ยกเว้นถ้า host เป็นคนเดียวที่ยังไม่กดและ timeout จะจัดการเอง)
        if (hostChoiceTarget == null) return;
        // ทุกคนกดแล้ว (playerChoices รวม host ด้วย)
        if (playerChoices.size() >= total) {
            doFinalBroadcastChoice(hostChoiceTarget);
        }
    }

    /** เรียกหลัง player disconnect ระหว่าง choice — recheck กับ count ใหม่ */
    private void recheckAfterDisconnect() {
        if (!inChoicePhase) return;
        int total = lockedTotal > 0 ? lockedTotal : clients.size() + 1;

        // ถ้า host กดแล้ว (hostChoiceTarget มีค่า) → เช็คว่าครบหรือยัง
        if (hostChoiceTarget != null && !hostChoiceTarget.isEmpty()) {
            if (total <= 1 || playerChoices.size() >= total) {
                doFinalBroadcastChoice(hostChoiceTarget);
            }
        }
        // ถ้า host ยังไม่กด → host กดได้ตามปกติ (UI ถูก reset โดย onPlayerLeftDuringChoice แล้ว)
    }

    /** เวลาหมด → ใช้ choice ที่มีอยู่ หรือ fallback target ที่ส่งมา */
    public void forceChoiceTimeout(String fallbackTarget) {
        if (!inChoicePhase) return;
        String target = (hostChoiceTarget != null && !hostChoiceTarget.isEmpty())
            ? hostChoiceTarget : fallbackTarget;
        doFinalBroadcastChoice(target);
    }

    /** broadcast CHOICE_RESULT ให้ทุกคน navigate พร้อมกัน */
    private void doFinalBroadcastChoice(String targetScene) {
        inChoicePhase = false;
        playerChoices.clear();
        hostChoiceTarget = null;
        clientReadyForScene.clear();
        pendingScene = targetScene;
        sceneLoadedCount = 0; // reset นับคนที่ load scene เสร็จ

        broadcast("CHOICE_RESULT:" + targetScene + "::");
        if (listener != null) listener.onChoiceResult(targetScene, "", 0);
        // ไม่มี safety timeout แล้ว — ใช้ hostSceneLoaded + clientSceneLoaded แทน
    }

    /** ตรวจว่ากำลังรอ clients โหลดซีนนี้อยู่หรือเปล่า */
    public boolean hasPendingScene(String scene) {
        return scene != null && scene.equals(pendingScene) && !pendingScene.isEmpty();
    }

    /** Host บอกว่า navigate ไปซีนใหม่เสร็จแล้ว (เรียกจาก PlaySceneMP หลัง loadScene) */
    public void hostSceneLoaded(String scene) {
        if (!scene.equals(pendingScene)) return;
        sceneLoadedCount++;
        checkAllSceneLoaded(scene);
    }

    /** Client บอกว่า navigate ไปซีนใหม่เสร็จแล้ว */
    public void clientSceneLoaded(String playerName, String scene) {
        if (!scene.equals(pendingScene)) return;
        clientReadyForScene.add(playerName);
        sceneLoadedCount++;
        checkAllSceneLoaded(scene);
    }

    private void checkAllSceneLoaded(String scene) {
        int total = lockedTotal > 0 ? lockedTotal : clients.size() + 1;
        if (sceneLoadedCount >= total) {
            pendingScene = "";
            startReadPhase(scene);
        }
    }

    // ════════════════════════════════════════════════
    //  Host พร้อม (in-process)
    // ════════════════════════════════════════════════
    public void hostSceneReady(String sceneName) {
        // ไม่เช็ค sceneName เพราะ host อาจอ่านเร็วกว่า client โหลดซีน
        if (inChoicePhase) return;
        readySet.add(hostName);
        checkAllReady();
    }

    private void checkAllReady() {
        int total = lockedTotal > 0 ? lockedTotal : clients.size() + 1;
        int ready = readySet.size();
        broadcast("READY_COUNT:" + ready + ":" + total);
        if (listener != null) listener.onReadyCount(ready, total);
        if (ready >= total) { stopTimer(); startCountdown(currentScene); }
    }

    // ════════════════════════════════════════════════
    //  Server Start/Stop
    // ════════════════════════════════════════════════
    public void start() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                running = true;
                if (listener != null) listener.onServerStarted(getLocalIP(), port);
                while (running) {
                    try {
                        Socket cs = serverSocket.accept();
                        if (locked || clients.size() >= MAX_PLAYERS - 1) {
                            try (PrintWriter pw = new PrintWriter(
                                    new OutputStreamWriter(cs.getOutputStream(), "UTF-8"), true)) {
                                pw.println("ERROR:ห้องเต็มหรือเริ่มไปแล้ว");
                            }
                            cs.close(); continue;
                        }
                        ClientHandler h = new ClientHandler(cs);
                        clients.add(h);
                        new Thread(h).start();
                    } catch (SocketException e) { if (!running) break; }
                }
            } catch (IOException e) {
                if (listener != null) listener.onServerError("Server error: " + e.getMessage());
            }
        }, "GameServer-Main").start();
    }

    public void stop() {
        running = false; stopTimer();
        // ✅ Bug 1: แจ้ง clients ว่า host ออกจากห้อง → clients จะกลับหน้าล็อบบี้
        broadcast("HOST_LEFT");
        for (ClientHandler c : clients) c.disconnect();
        clients.clear();
        try { if (serverSocket != null) serverSocket.close(); } catch (IOException ignored) {}
    }

    /** ถ้าชื่อซ้ำให้เพิ่มเลขต่อท้าย เช่น "ice" → "ice(2)" */
    private String resolveUniqueName(String name) {
        Set<String> taken = new java.util.HashSet<>();
        taken.add(hostName);
        for (ClientHandler ch : clients) {
            if (!ch.playerName.equals("Unknown")) taken.add(ch.playerName);
        }
        if (!taken.contains(name)) return name;
        int n = 2;
        while (taken.contains(name + "(" + n + ")")) n++;
        return name + "(" + n + ")";
    }

    public void broadcast(String msg) {
        for (ClientHandler c : clients) c.send(msg);
    }

    private void broadcastPlayerList() {
        StringBuilder sb = new StringBuilder(hostName);
        for (ClientHandler c : clients)
            if (!c.playerName.equals("Unknown")) sb.append(",").append(c.playerName);
        broadcast("PLAYER_LIST:" + sb);
    }

    private void checkAllFinished() {
        if (finishedSet.size() >= expectedPlayers) { // ✅ FIX 2: นับจากคนที่ส่งคะแนนจริง
            stopTimer();
            broadcast("LEADERBOARD:" + buildLeaderboard());
            if (listener != null) listener.onAllPlayersFinished(new HashMap<>(playerScores));
        }
    }

    private String buildLeaderboard() {
        List<Map.Entry<String,Integer>> s = new ArrayList<>(playerScores.entrySet());
        s.sort((a,b) -> b.getValue()-a.getValue());
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<s.size();i++) {
            if(i>0) sb.append("|");
            sb.append(s.get(i).getKey()).append(":").append(s.get(i).getValue());
        }
        return sb.toString();
    }

    public static String getLocalIP() {
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface ni = ifaces.nextElement();
                if (!ni.isUp() || ni.isLoopback()) continue;
                for (InetAddress a : Collections.list(ni.getInetAddresses()))
                    if (a instanceof Inet4Address && a.getHostAddress().startsWith("26."))
                        return a.getHostAddress();
            }
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) { return "127.0.0.1"; }
    }

    // ════════════════════════════════════════════════
    //  ClientHandler
    // ════════════════════════════════════════════════
    private class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        String playerName = "Unknown";

        ClientHandler(Socket s) { socket = s; }

        @Override public void run() {
            try {
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"),true);
                in  = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
                String line;
                while ((line=in.readLine())!=null) handle(line.trim());
            } catch (IOException ignored) {}
            finally { disconnect(); }
        }

        private void handle(String msg) {
            if (msg.startsWith("JOIN:")) {
                String requestedName = msg.substring(5).trim();
                // ✅ Bug 2: ถ้าชื่อซ้ำกับ host หรือ client อื่น ให้เพิ่มเลขต่อท้าย
                playerName = resolveUniqueName(requestedName);
                playerScores.put(playerName, 0);
                send("WELCOME:" + playerName); // ส่งชื่อที่ได้จริงๆ กลับไป
                broadcastPlayerList();
                if (listener != null) listener.onPlayerJoined(playerName, clients.size()+1);

            } else if (msg.startsWith("SCORE:")) {
                try {
                    int score = Integer.parseInt(msg.substring(6).trim());
                    playerScores.put(playerName, score);
                    finishedSet.add(playerName); // ✅ FIX 2
                    broadcast("SCORE_UPDATE:" + playerName + ":" + score);
                    if (listener != null) listener.onScoreReceived(playerName, score);
                    checkAllFinished();
                } catch (NumberFormatException ignored) {}

            } else if (msg.startsWith("SCENE_READY:")) {
                // ไม่เช็ค scene name — player อาจอ่านเร็วกว่า server update currentScene
                if (inChoicePhase) return;
                readySet.add(playerName);
                checkAllReady();

            } else if (msg.startsWith("SCENE_LOADED:")) {
                String scene = msg.substring(13).trim();
                clientSceneLoaded(playerName, scene);
            } else if (msg.startsWith("PLAYER_CHOICE:")) {
                // PLAYER_CHOICE:target:charName:score
                String[] p = msg.substring(14).split(":", 3);
                if (p.length >= 1) {
                    String t  = p[0];
                    String cn = p.length >= 2 ? p[1] : "";
                    int    sc = 0;
                    try { sc = p.length >= 3 ? Integer.parseInt(p[2]) : 0; } catch (NumberFormatException ignored) {}
                    receiveClientChoice(playerName, t, cn, sc);
                }
            } else if (msg.equals("PING")) { send("PONG"); }
        }

        void send(String m) { if (out!=null) out.println(m); }

        void disconnect() {
            clients.remove(this); playerScores.remove(playerName); readySet.remove(playerName);
            finishedSet.remove(playerName); // ลบออกจาก finished ด้วย
            // ✅ ลด expected count เสมอ เพื่อให้ ready/finish count ถูกต้อง
            if (lockedTotal > 1) lockedTotal--;
            if (expectedPlayers > 1) expectedPlayers--;
            // ถ้าออกระหว่าง choice phase → ลบ choice แล้วเช็คใหม่
            if (inChoicePhase) {
                playerChoices.remove(playerName);
                // แจ้ง host UI reset waiting state ก่อนเสมอ (กรณี host กดแล้วรอ client นี้อยู่)
                if (listener != null) listener.onPlayerLeftDuringChoice();
                recheckAfterDisconnect();
            }
            // ถ้าออกระหว่าง read phase → เช็ค ready count ใหม่
            if (!inChoicePhase) {
                readySet.remove(playerName);
                checkAllReady();
            }
            // เช็ค score ด้วย (ถ้าออกช่วงส่งคะแนน)
            checkAllFinished();
            try { if(socket!=null) socket.close(); } catch (IOException ignored) {}
            if (!playerName.equals("Unknown")) {
                broadcastPlayerList();
                broadcast("PLAYER_LEFT:" + playerName + ":" + (clients.size()+1));
                if (listener != null) listener.onPlayerLeft(playerName, clients.size()+1);
            }
        }
    }
}