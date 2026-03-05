package model;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GameServer {

    public static final int MAX_PLAYERS     = 3;
    public static final int READ_SECONDS    = 50;
    public static final int COUNTDOWN_SECS  = 3;
    public static final int CHOICE_SECONDS  = 10;

    public static int randomPort() {
        return 40000 + new Random().nextInt(9999);
    }

    public interface ServerListener {
        void onPlayerJoined(String playerName, int total);
        void onPlayerLeft(String playerName, int total);
        void onScoreReceived(String playerName, int score);
        void onAllPlayersFinished(Map<String, Integer> finalScores);
        default void onPlayerLeftDuringChoice() {}
        void onServerError(String message);
        void onServerStarted(String ip, int port);
        default void onPhaseRead(String scene, int sec, int total) {}
        default void onCountdown(int n) {}
        default void onPhaseChoice(String scene, int sec, int total) {}
        default void onForceNext(String scene, String targetScene) {}
        default void onChoiceResult(String targetScene, String charName, int score) {}
        default void onTimerSync(int t) {}
        default void onReadyCount(int ready, int total) {}
    }

    private ServerSocket serverSocket;
    private final List<ClientHandler>  clients       = new CopyOnWriteArrayList<>();
    private final Map<String,Integer>  playerScores  = new ConcurrentHashMap<>();
    private final Set<String>          readySet      = ConcurrentHashMap.newKeySet();
    private final Set<String>          finishedSet   = ConcurrentHashMap.newKeySet();
    private final Set<String>          clientReadyForScene = ConcurrentHashMap.newKeySet();
    private String  pendingScene     = "";
    private int     sceneLoadedCount = 0;
    private final Map<String,String[]> playerChoices = new ConcurrentHashMap<>();
    private String  hostChoiceTarget = null;

    private ServerListener listener;
    private boolean running       = false;
    private boolean locked        = false;
    private int     port;
    private int     expectedPlayers = 2;
    private int     lockedTotal     = 0;
    private String  hostName        = "Host";
    private String  currentScene    = "";
    private boolean inChoicePhase   = false;

    private java.util.Timer serverTimer = null;
    private final AtomicInteger timerSec = new AtomicInteger(0);
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
    public void setHostName(String name) { this.hostName = name; }
    public int  getPlayerCount()         { return clients.size(); }
    public boolean isRunning()           { return running; }

    public void registerNextScene(String scene, String next) {
        if (next != null) sceneNextMap.put(scene, next);
    }

    public void receiveHostScore(String name, int score) {
        playerScores.put(name, score);
        finishedSet.add(name);
        broadcast("SCORE_UPDATE:" + name + ":" + score);
        if (listener != null) listener.onScoreReceived(name, score);
        checkAllFinished();
    }

    // ═══ Phase 1 READ ═══
    public void startReadPhase(String sceneName) {
        stopTimer();
        readySet.clear();
        playerChoices.clear();
        hostChoiceTarget = null;
        currentScene  = sceneName;
        inChoicePhase = false;
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

    // ═══ Countdown ═══
    private void startCountdown(String sceneName) {
        stopTimer();
        final int[] count = {COUNTDOWN_SECS};
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

    // ═══ Phase 2 CHOICE ═══
    private void startChoicePhase(String sceneName) {
        stopTimer();
        readySet.clear();
        inChoicePhase = true;
        int total = lockedTotal > 0 ? lockedTotal : clients.size() + 1;
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

    public void broadcastChoiceResult(String targetScene, String charName, int score) {
        if (!inChoicePhase) return;
        hostChoiceTarget = targetScene;
        playerChoices.put(hostName, new String[]{targetScene, charName != null ? charName : "", String.valueOf(score)});
        checkAllChosen();
    }

    public void receiveClientChoice(String playerName, String target, String charName, int score) {
        if (!inChoicePhase) return;
        playerChoices.put(playerName, new String[]{target, charName != null ? charName : "", String.valueOf(score)});
        checkAllChosen();
    }

    // ✅ FIX: checkAllChosen + safety timeout 5 วิ ถ้า host กดแล้วแต่ client ช้า
    private void checkAllChosen() {
        int total = lockedTotal > 0 ? lockedTotal : clients.size() + 1;
        if (hostChoiceTarget == null) return;
        if (playerChoices.size() >= total) {
            doFinalBroadcastChoice(hostChoiceTarget);
            return;
        }
        // Safety: รอ client อีกสูงสุด 5 วิ แล้ว broadcast เลย
        final String targetSnap = hostChoiceTarget;
        new java.util.Timer("ChoiceWaitTimeout", true).schedule(new java.util.TimerTask() {
            @Override public void run() {
                if (inChoicePhase && targetSnap.equals(hostChoiceTarget)) {
                    doFinalBroadcastChoice(targetSnap);
                }
            }
        }, 5000);
    }

    private void recheckAfterDisconnect() {
        if (!inChoicePhase) return;
        int total = lockedTotal > 0 ? lockedTotal : clients.size() + 1;
        if (hostChoiceTarget != null && !hostChoiceTarget.isEmpty()) {
            if (total <= 1 || playerChoices.size() >= total) {
                doFinalBroadcastChoice(hostChoiceTarget);
            }
        }
    }

    public void forceChoiceTimeout(String fallbackTarget) {
        if (!inChoicePhase) return;
        String target = (hostChoiceTarget != null && !hostChoiceTarget.isEmpty())
            ? hostChoiceTarget : fallbackTarget;
        doFinalBroadcastChoice(target);
    }

    private void doFinalBroadcastChoice(String targetScene) {
        inChoicePhase = false;
        playerChoices.clear();
        hostChoiceTarget = null;
        clientReadyForScene.clear();
        pendingScene     = targetScene;
        sceneLoadedCount = 0;

        broadcast("CHOICE_RESULT:" + targetScene + "::");
        if (listener != null) listener.onChoiceResult(targetScene, "", 0);
    }

    public boolean hasPendingScene(String scene) {
        return scene != null && scene.equals(pendingScene) && !pendingScene.isEmpty();
    }

    public void hostSceneLoaded(String scene) {
        if (!scene.equals(pendingScene)) return;
        sceneLoadedCount++;
        checkAllSceneLoaded(scene);
    }

    public void clientSceneLoaded(String playerName, String scene) {
        if (!scene.equals(pendingScene)) return;
        clientReadyForScene.add(playerName);
        sceneLoadedCount++;
        checkAllSceneLoaded(scene);
    }

    // ✅ FIX: checkAllSceneLoaded + safety timeout 3 วิ ป้องกันค้าง
    private void checkAllSceneLoaded(String scene) {
        int total = lockedTotal > 0 ? lockedTotal : clients.size() + 1;
        if (sceneLoadedCount >= total) {
            pendingScene = "";
            startReadPhase(scene);
            return;
        }
        // Safety: ถ้า 3 วิยังไม่ครบ → startReadPhase เลย
        final String snap = scene;
        new java.util.Timer("SceneLoadTimeout", true).schedule(new java.util.TimerTask() {
            @Override public void run() {
                if (snap.equals(pendingScene)) {
                    pendingScene = "";
                    startReadPhase(snap);
                }
            }
        }, 3000);
    }

    public void hostSceneReady(String sceneName) {
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

    // ═══ Server Start/Stop ═══
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
        broadcast("HOST_LEFT");
        for (ClientHandler c : clients) c.disconnect();
        clients.clear();
        try { if (serverSocket != null) serverSocket.close(); } catch (IOException ignored) {}
    }

    private String resolveUniqueName(String name) {
        Set<String> taken = new java.util.HashSet<>();
        taken.add(hostName);
        for (ClientHandler ch : clients)
            if (!ch.playerName.equals("Unknown")) taken.add(ch.playerName);
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
        if (finishedSet.size() >= expectedPlayers) {
            stopTimer();
            broadcast("LEADERBOARD:" + buildLeaderboard());
            if (listener != null) listener.onAllPlayersFinished(new HashMap<>(playerScores));
        }
    }

    private String buildLeaderboard() {
        List<Map.Entry<String,Integer>> s = new ArrayList<>(playerScores.entrySet());
        s.sort((a,b) -> b.getValue()-a.getValue());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.size(); i++) {
            if (i > 0) sb.append("|");
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

    // ═══ ClientHandler ═══
    private class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        String playerName = "Unknown";

        ClientHandler(Socket s) { socket = s; }

        @Override public void run() {
            try {
                // ✅ FIX: socket timeout 15 วิ — ถ้าไม่มีข้อมูลถือว่า disconnect
                socket.setSoTimeout(15000);
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"),true);
                in  = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
                String line;
                while ((line=in.readLine())!=null) handle(line.trim());
            } catch (java.net.SocketTimeoutException e) {
                // ✅ FIX: ไม่มี PING นาน 15 วิ → ถือว่าหลุด
            } catch (IOException ignored) {}
            finally { disconnect(); }
        }

        private void handle(String msg) {
            if (msg.startsWith("JOIN:")) {
                String requestedName = msg.substring(5).trim();
                playerName = resolveUniqueName(requestedName);
                playerScores.put(playerName, 0);
                send("WELCOME:" + playerName);
                broadcastPlayerList();
                if (listener != null) listener.onPlayerJoined(playerName, clients.size()+1);

            } else if (msg.startsWith("SCORE:")) {
                try {
                    int score = Integer.parseInt(msg.substring(6).trim());
                    playerScores.put(playerName, score);
                    finishedSet.add(playerName);
                    broadcast("SCORE_UPDATE:" + playerName + ":" + score);
                    if (listener != null) listener.onScoreReceived(playerName, score);
                    checkAllFinished();
                } catch (NumberFormatException ignored) {}

            } else if (msg.startsWith("SCENE_READY:")) {
                if (inChoicePhase) return;
                readySet.add(playerName);
                checkAllReady();

            } else if (msg.startsWith("SCENE_LOADED:")) {
                String scene = msg.substring(13).trim();
                clientSceneLoaded(playerName, scene);

            } else if (msg.startsWith("PLAYER_CHOICE:")) {
                String[] p = msg.substring(14).split(":", 3);
                if (p.length >= 1) {
                    String t  = p[0];
                    String cn = p.length >= 2 ? p[1] : "";
                    int    sc = 0;
                    try { sc = p.length >= 3 ? Integer.parseInt(p[2]) : 0; } catch (NumberFormatException ignored) {}
                    receiveClientChoice(playerName, t, cn, sc);
                }
            } else if (msg.equals("PING")) {
                // ✅ FIX: ตอบ PONG ให้ client keepalive
                send("PONG");
            }
        }

        void send(String m) { if (out!=null) out.println(m); }

        void disconnect() {
            clients.remove(this); playerScores.remove(playerName); readySet.remove(playerName);
            finishedSet.remove(playerName);
            if (lockedTotal > 1) lockedTotal--;
            if (expectedPlayers > 1) expectedPlayers--;
            if (inChoicePhase) {
                playerChoices.remove(playerName);
                if (listener != null) listener.onPlayerLeftDuringChoice();
                recheckAfterDisconnect();
            }
            if (!inChoicePhase) {
                readySet.remove(playerName);
                checkAllReady();
            }
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