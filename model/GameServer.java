package model;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GameServer — Server เป็น Master ของ Timer และ Ready Count
 *
 * โปรโตคอล:
 *   Client → Server:  JOIN:name | SCORE:n | CHOICE_READY: | PING
 *   Server → Client:  WELCOME:name | PLAYER_LIST:a,b,c | START_GAME:sec
 *                     TIMER_START:sec:total | TIMER_SYNC:sec
 *                     READY_COUNT:n:total | UNLOCK_CHOICE
 *                     SCORE_UPDATE:name:n | LEADERBOARD:... | PLAYER_LEFT:name:n
 */
public class GameServer {

    public static final int DEFAULT_PORT = 45621;
    public static final int MAX_PLAYERS  = 3;

    public interface ServerListener {
        void onPlayerJoined(String playerName, int totalClients);
        void onPlayerLeft(String playerName, int totalClients);
        void onScoreReceived(String playerName, int score);
        void onAllPlayersFinished(Map<String, Integer> finalScores);
        void onServerError(String message);
        void onServerStarted(String ip, int port);
        /** Server บอก Host ว่า ready count เปลี่ยน */
        default void onReadyCountChanged(int ready, int total) {}
        /** Server บอก Host ให้ unlock choice */
        default void onUnlockChoice() {}
    }

    private ServerSocket   serverSocket;
    private final List<ClientHandler>  clients      = new CopyOnWriteArrayList<>();
    private final Map<String, Integer> playerScores = new ConcurrentHashMap<>();
    private final Set<String>          readySet     = ConcurrentHashMap.newKeySet();

    private ServerListener listener;
    private boolean running = false;
    private boolean locked  = false;
    private int     port;
    private int     expectedPlayers = 2;
    private String  hostName        = "Host";

    // Server-side countdown (ใช้ Timer thread ของ Server เอง)
    private java.util.Timer serverTimer = null;
    private final AtomicInteger timerSeconds = new AtomicInteger(0);

    public GameServer(int port)             { this.port = port; }
    public void setListener(ServerListener l) { this.listener = l; }
    public void setExpectedPlayers(int n)   { this.expectedPlayers = Math.min(n, MAX_PLAYERS); }
    public void lockRoom()                  { locked = true; }
    public void setHostName(String name)    { this.hostName = name; }
    public int  getPlayerCount()            { return clients.size(); }
    public boolean isRunning()              { return running; }

    /** Host เรียกเมื่อถึง choice */
    public void hostReady() {
        readySet.add(hostName);
        int total = clients.size() + 1;
        int ready = readySet.size();
        broadcast("READY_COUNT:" + ready + ":" + total);
        if (listener != null) listener.onReadyCountChanged(ready, total);
        if (ready >= total) { stopServerTimer(); unlockAll(); }
    }

    // ============================================================
    //  Host ส่งคะแนนตัวเอง
    // ============================================================
    public void receiveHostScore(String name, int score) {
        playerScores.put(name, score);
        broadcast("SCORE_UPDATE:" + name + ":" + score);
        if (listener != null) listener.onScoreReceived(name, score);
        checkAllFinished();
    }

    // ============================================================
    //  Server-side countdown — เรียกหลัง startGameAsHost
    //  ทุก 1 วิ broadcast TIMER_SYNC:t ให้ทุกคนเห็นเหมือนกัน
    // ============================================================
    public void startServerTimer(int totalSeconds) {
        stopServerTimer();
        readySet.clear();

        int total = clients.size() + 1; // รวม Host
        timerSeconds.set(totalSeconds);

        // บอกทุกคน reset นาฬิกา
        broadcast("TIMER_START:" + totalSeconds + ":" + total);

        serverTimer = new java.util.Timer("ServerCountdown", true);
        serverTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                int t = timerSeconds.decrementAndGet();
                // Sync ทุก 1 วิ
                broadcast("TIMER_SYNC:" + t);

                if (t <= 0) {
                    stopServerTimer();
                    unlockAll();
                }
            }
        }, 1000, 1000);
    }

    private void stopServerTimer() {
        if (serverTimer != null) {
            serverTimer.cancel();
            serverTimer = null;
        }
    }

    /** Broadcast UNLOCK_CHOICE ให้ทุกคน + แจ้ง Host ด้วย */
    private void unlockAll() {
        broadcast("UNLOCK_CHOICE");
        readySet.clear();
        if (listener != null) listener.onUnlockChoice();
    }

    // ============================================================
    //  เริ่ม Server
    // ============================================================
    public void start() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                running = true;
                String ip = getLocalIP();
                if (listener != null) listener.onServerStarted(ip, port);

                while (running) {
                    try {
                        Socket cs = serverSocket.accept();
                        if (locked || clients.size() >= MAX_PLAYERS - 1) {
                            PrintWriter pw = new PrintWriter(
                                new OutputStreamWriter(cs.getOutputStream(), "UTF-8"), true);
                            pw.println("ERROR:ห้องนี้เริ่มเกมไปแล้ว หรือเต็มแล้ว");
                            cs.close();
                            continue;
                        }
                        ClientHandler h = new ClientHandler(cs);
                        clients.add(h);
                        new Thread(h).start();
                    } catch (SocketException e) {
                        if (!running) break;
                    }
                }
            } catch (IOException e) {
                if (listener != null)
                    listener.onServerError("ไม่สามารถเปิดเซิร์ฟเวอร์ได้: " + e.getMessage());
            }
        }, "GameServer-Main").start();
    }

    public void stop() {
        running = false;
        stopServerTimer();
        for (ClientHandler c : clients) c.disconnect();
        clients.clear();
        try { if (serverSocket != null) serverSocket.close(); } catch (IOException ignored) {}
    }

    public void broadcast(String message) {
        for (ClientHandler c : clients) c.send(message);
    }

    // ============================================================
    //  Broadcast Player List (hostName อยู่ index 0 เสมอ)
    // ============================================================
    private void broadcastPlayerList() {
        StringBuilder sb = new StringBuilder(hostName);
        for (ClientHandler c : clients) {
            if (!c.playerName.equals("Unknown"))
                sb.append(",").append(c.playerName);
        }
        broadcast("PLAYER_LIST:" + sb);
    }

    // ============================================================
    //  Check All Finished
    // ============================================================
    private void checkAllFinished() {
        if (playerScores.size() >= expectedPlayers) {
            stopServerTimer();
            String lb = buildLeaderboard();
            broadcast("LEADERBOARD:" + lb);
            if (listener != null) listener.onAllPlayersFinished(new HashMap<>(playerScores));
        }
    }

    private String buildLeaderboard() {
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(playerScores.entrySet());
        sorted.sort((a, b) -> b.getValue() - a.getValue());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sorted.size(); i++) {
            if (i > 0) sb.append("|");
            sb.append(sorted.get(i).getKey()).append(":").append(sorted.get(i).getValue());
        }
        return sb.toString();
    }

    public static String getLocalIP() {
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface ni = ifaces.nextElement();
                if (!ni.isUp() || ni.isLoopback()) continue;
                for (InetAddress addr : Collections.list(ni.getInetAddresses())) {
                    if (addr instanceof Inet4Address) {
                        String ip = addr.getHostAddress();
                        if (ip.startsWith("26.")) return ip;
                    }
                }
            }
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) { return "127.0.0.1"; }
    }

    // ============================================================
    //  ClientHandler
    // ============================================================
    private class ClientHandler implements Runnable {
        private Socket         socket;
        private PrintWriter    out;
        private BufferedReader in;
        String playerName = "Unknown";

        ClientHandler(Socket s) { this.socket = s; }

        @Override public void run() {
            try {
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
                in  = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                String line;
                while ((line = in.readLine()) != null) handle(line.trim());
            } catch (IOException ignored) {
            } finally { disconnect(); }
        }

        private void handle(String msg) {
            if (msg.startsWith("JOIN:")) {
                playerName = msg.substring(5).trim();
                playerScores.put(playerName, 0);
                send("WELCOME:" + playerName);
                broadcastPlayerList();
                if (listener != null)
                    listener.onPlayerJoined(playerName, clients.size() + 1);

            } else if (msg.startsWith("SCORE:")) {
                try {
                    int score = Integer.parseInt(msg.substring(6).trim());
                    playerScores.put(playerName, score);
                    broadcast("SCORE_UPDATE:" + playerName + ":" + score);
                    if (listener != null) listener.onScoreReceived(playerName, score);
                    checkAllFinished();
                } catch (NumberFormatException ignored) {}

            } else if (msg.startsWith("CHOICE_READY:")) {
                // ✅ Server นับ ready — broadcast ให้ทุกคนพร้อมกัน
                readySet.add(playerName);
                int total = clients.size() + 1; // รวม Host
                int ready = readySet.size();

                // broadcast READY_COUNT ให้ทุก client
                broadcast("READY_COUNT:" + ready + ":" + total);
                // แจ้ง Host ผ่าน listener
                if (listener != null) listener.onReadyCountChanged(ready, total);

                // ถ้าครบทุกคนก่อนหมดเวลา → unlock ทันที
                if (ready >= total) {
                    stopServerTimer();
                    unlockAll();
                }

            } else if (msg.equals("PING")) {
                send("PONG");
            }
        }

        void send(String m) { if (out != null) out.println(m); }

        void disconnect() {
            clients.remove(this);
            playerScores.remove(playerName);
            readySet.remove(playerName);
            try { if (socket != null) socket.close(); } catch (IOException ignored) {}
            if (!playerName.equals("Unknown")) {
                broadcastPlayerList();
                int total = clients.size() + 1;
                broadcast("PLAYER_LEFT:" + playerName + ":" + total);
                if (listener != null) listener.onPlayerLeft(playerName, total);
            }
        }
    }
}