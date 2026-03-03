package model;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GameServer {

    public static final int DEFAULT_PORT      = 45621;
    public static final int MAX_PLAYERS       = 3;
    public static final int READ_SECONDS      = 50;
    public static final int CHOICE_SECONDS    = 10;   // ✅ แก้จาก 8 → 10
    public static final int ALERT_BEFORE_SECS = 3;

    public interface ServerListener {
        void onPlayerJoined(String playerName, int total);
        void onPlayerLeft(String playerName, int total);
        void onScoreReceived(String playerName, int score);
        void onAllPlayersFinished(Map<String, Integer> finalScores);
        void onServerError(String message);
        void onServerStarted(String ip, int port);

        default void onPhaseRead(String scene, int sec, int total) {}
        default void onPhaseChoice(String scene, int sec, int total) {}
        default void onForceNext(String scene) {}
        default void onTimerSync(int t) {}
        default void onReadyCount(int ready, int total) {}
        default void onChoiceAlert(String scene) {}
        default void onCountdown(int n) {}
        default void onRandomChoice(String scene, int choiceIndex) {}
    }

    private ServerSocket  serverSocket;
    private final List<ClientHandler> clients      = new CopyOnWriteArrayList<>();
    private final Map<String,Integer> playerScores = new ConcurrentHashMap<>();
    private final Set<String>         readySet     = ConcurrentHashMap.newKeySet();
    private final Set<String>         choiceSet    = ConcurrentHashMap.newKeySet();
    private int currentChoiceCount = 3;

    private ServerListener listener;
    private boolean running  = false;
    private boolean locked   = false;
    private int     port;
    private int     expectedPlayers = 2;
    private String  hostName        = "Host";
    private String  currentScene    = "";
    private boolean inChoicePhase   = false;

    private java.util.Timer  serverTimer = null;
    private final AtomicInteger timerSec = new AtomicInteger(0);

    public GameServer(int port)               { this.port = port; }
    public void setListener(ServerListener l) { this.listener = l; }
    public void setExpectedPlayers(int n)     { this.expectedPlayers = Math.min(n, MAX_PLAYERS); }
    public void lockRoom()                    { locked = true; }
    public void setHostName(String name)      { this.hostName = name; }
    public int  getPlayerCount()              { return clients.size(); }
    public boolean isRunning()                { return running; }
    public void setChoiceCount(int n)         { this.currentChoiceCount = Math.max(1, n); }

    public void receiveHostScore(String name, int score) {
        playerScores.put(name, score);
        broadcast("SCORE_UPDATE:" + name + ":" + score);
        if (listener != null) listener.onScoreReceived(name, score);
        checkAllFinished();
    }

    public void startReadPhase(String sceneName) {
        stopTimer();
        readySet.clear(); choiceSet.clear();
        currentScene  = sceneName; inChoicePhase = false;
        int total = clients.size() + 1, sec = READ_SECONDS;
        timerSec.set(sec);
        broadcast("PHASE_READ:" + sceneName + ":" + sec + ":" + total);
        if (listener != null) listener.onPhaseRead(sceneName, sec, total);

        serverTimer = new java.util.Timer("ReadTimer", true);
        serverTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                int t = timerSec.decrementAndGet();
                broadcast("TIMER_SYNC:" + t);
                if (listener != null) listener.onTimerSync(t);
                if (t <= 0) { stopTimer(); triggerAlertThenChoice(sceneName); }
            }
        }, 1000, 1000);
    }

    private void triggerAlertThenChoice(String sceneName) {
        broadcast("CHOICE_ALERT:" + sceneName);
        if (listener != null) listener.onChoiceAlert(sceneName);
        serverTimer = new java.util.Timer("AlertDelay", true);
        serverTimer.schedule(new TimerTask() {
            @Override public void run() { stopTimer(); startChoicePhase(sceneName); }
        }, 1200);
    }

    private void startChoicePhase(String sceneName) {
        stopTimer();
        readySet.clear(); choiceSet.clear();
        inChoicePhase = true;
        int total = clients.size() + 1, sec = CHOICE_SECONDS;
        timerSec.set(sec);
        broadcast("PHASE_CHOICE:" + sceneName + ":" + sec + ":" + total);
        if (listener != null) listener.onPhaseChoice(sceneName, sec, total);

        serverTimer = new java.util.Timer("ChoiceTimer", true);
        serverTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                int t = timerSec.decrementAndGet();
                broadcast("TIMER_SYNC:" + t);
                if (listener != null) listener.onTimerSync(t);

                if (t > 0 && t <= ALERT_BEFORE_SECS) {
                    broadcast("COUNTDOWN:" + t);
                    if (listener != null) listener.onCountdown(t);
                }

                if (t <= 0) {
                    stopTimer();
                    // ✅ Step 1: ส่ง RANDOM_CHOICE ให้ทุกคนที่ยังไม่เลือก
                    randomChoiceForLatecomers(sceneName);
                    // ✅ Step 2: หน่วง 1200ms ให้ client ประมวล random + animate ก่อน
                    //    แล้วค่อยส่ง FORCE_NEXT เป็น safety net
                    //    client ที่ random แล้วจะ ignore FORCE_NEXT (hasChosen=true)
                    java.util.Timer delay = new java.util.Timer("ForceDelay", true);
                    delay.schedule(new TimerTask() {
                        @Override public void run() {
                            broadcast("FORCE_NEXT:" + sceneName);
                            if (listener != null) listener.onForceNext(sceneName);
                        }
                    }, 1200);
                }
            }
        }, 1000, 1000);
    }

    // ✅ สุ่ม choice ให้ทุกคนที่ยังไม่เลือก รวมถึง host
    private void randomChoiceForLatecomers(String sceneName) {
        Random rng = new Random();
        for (ClientHandler c : clients) {
            if (!choiceSet.contains(c.playerName)) {
                int idx = rng.nextInt(Math.max(1, currentChoiceCount));
                c.send("RANDOM_CHOICE:" + sceneName + ":" + idx);
            }
        }
        if (!choiceSet.contains(hostName)) {
            int idx = rng.nextInt(Math.max(1, currentChoiceCount));
            if (listener != null) listener.onRandomChoice(sceneName, idx);
        }
    }

    public void hostMadeChoice(String sceneName) { choiceSet.add(hostName); }

    public void hostSceneReady(String sceneName) {
        if (!sceneName.equals(currentScene) || inChoicePhase) return;
        readySet.add(hostName);
        int total = clients.size() + 1, ready = readySet.size();
        broadcast("READY_COUNT:" + ready + ":" + total);
        if (listener != null) listener.onReadyCount(ready, total);
        if (ready >= total) { stopTimer(); triggerAlertThenChoice(sceneName); }
    }

    private void stopTimer() {
        if (serverTimer != null) { serverTimer.cancel(); serverTimer = null; }
    }

    public void start() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                running = true;
                // ✅ FIX 5: port จริง
                int actualPort = serverSocket.getLocalPort();
                this.port = actualPort;
                if (listener != null) listener.onServerStarted(getLocalIP(), actualPort);
                while (running) {
                    try {
                        Socket cs = serverSocket.accept();
                        if (locked || clients.size() >= MAX_PLAYERS - 1) {
                            try (PrintWriter pw = new PrintWriter(
                                    new OutputStreamWriter(cs.getOutputStream(),"UTF-8"),true)) {
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
        for (ClientHandler c : clients) c.disconnect();
        clients.clear();
        try { if (serverSocket != null) serverSocket.close(); } catch (IOException ignored) {}
    }

    public void broadcast(String msg) { for (ClientHandler c : clients) c.send(msg); }

    private void broadcastPlayerList() {
        StringBuilder sb = new StringBuilder(hostName);
        for (ClientHandler c : clients)
            if (!c.playerName.equals("Unknown")) sb.append(",").append(c.playerName);
        broadcast("PLAYER_LIST:" + sb);
    }

    private void checkAllFinished() {
        if (playerScores.size() >= expectedPlayers) {
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

    private class ClientHandler implements Runnable {
        Socket socket; PrintWriter out; BufferedReader in;
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
                playerName = msg.substring(5).trim();
                playerScores.put(playerName, 0);
                send("WELCOME:" + playerName);
                broadcastPlayerList();
                if (listener != null) listener.onPlayerJoined(playerName, clients.size()+1);
            } else if (msg.startsWith("SCORE:")) {
                try {
                    int score = Integer.parseInt(msg.substring(6).trim());
                    playerScores.put(playerName, score);
                    broadcast("SCORE_UPDATE:" + playerName + ":" + score);
                    if (listener != null) listener.onScoreReceived(playerName, score);
                    checkAllFinished();
                } catch (NumberFormatException ignored) {}
            } else if (msg.startsWith("SCENE_READY:")) {
                String scene = msg.substring(12).trim();
                if (!scene.equals(currentScene) || inChoicePhase) return;
                readySet.add(playerName);
                int total = clients.size()+1, ready = readySet.size();
                broadcast("READY_COUNT:" + ready + ":" + total);
                if (listener != null) listener.onReadyCount(ready, total);
                if (ready >= total) { stopTimer(); triggerAlertThenChoice(scene); }
            } else if (msg.startsWith("CHOICE_MADE:")) {
                choiceSet.add(playerName);
            } else if (msg.equals("PING")) { send("PONG"); }
        }

        void send(String m) { if (out!=null) out.println(m); }

        void disconnect() {
            clients.remove(this); playerScores.remove(playerName);
            readySet.remove(playerName); choiceSet.remove(playerName);
            try { if(socket!=null) socket.close(); } catch (IOException ignored) {}
            if (!playerName.equals("Unknown")) {
                broadcastPlayerList();
                broadcast("PLAYER_LEFT:" + playerName + ":" + (clients.size()+1));
                if (listener != null) listener.onPlayerLeft(playerName, clients.size()+1);
            }
        }
    }
}