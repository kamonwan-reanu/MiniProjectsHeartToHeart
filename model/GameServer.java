package model;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class GameServer {

    public static final int DEFAULT_PORT = 45621;
    public static final int MAX_PLAYERS  = 4;

    public interface ServerListener {
        void onPlayerJoined(String playerName, int totalClients);
        void onPlayerLeft(String playerName, int totalClients);
        void onScoreReceived(String playerName, int score);
        void onAllPlayersFinished(Map<String, Integer> finalScores);
        void onServerError(String message);
        void onServerStarted(String ip, int port);
    }

    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final Map<String, Integer> playerScores  = new ConcurrentHashMap<>();

    // ✨ เก็บลำดับชื่อผู้เล่น (index 0 = Host เสมอ)
    private final List<String> playerNameOrder = new CopyOnWriteArrayList<>();

    private ServerListener listener;
    private boolean running = false;
    private boolean locked  = false;
    private int port;
    private int expectedPlayers = 2;

    // ✨ ชื่อ Host (set จาก MultiplayerLobby ก่อน start server)
    private String hostPlayerName = "";

    public GameServer(int port) { this.port = port; }

    public void setListener(ServerListener l)  { this.listener = l; }
    public void setExpectedPlayers(int n)      { this.expectedPlayers = n; }
    public void lockRoom()                     { locked = true; }

    // ✨ MultiplayerLobby เรียก setHostName() ก่อน start() เพื่อให้ Host ติด index 0
    public void setHostName(String name) {
        this.hostPlayerName = name;
        if (!playerNameOrder.contains(name)) {
            playerNameOrder.add(0, name); // Host อยู่ index 0 เสมอ
        }
    }

    // Host ส่งคะแนนตัวเองเข้าระบบโดยตรง
    public void receiveHostScore(String name, int score) {
        playerScores.put(name, score);
        broadcast("SCORE_UPDATE:" + name + ":" + score);
        if (listener != null) listener.onScoreReceived(name, score);
        checkAllFinished();
    }

    public void start() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                running = true;
                String ip = getLocalIP();
                if (listener != null) listener.onServerStarted(ip, port);

                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        if (locked || clients.size() >= MAX_PLAYERS) {
                            PrintWriter out = new PrintWriter(
                                new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true);
                            out.println("ERROR:ห้องนี้เริ่มเกมไปแล้ว หรือเต็มแล้ว");
                            clientSocket.close();
                            continue;
                        }
                        ClientHandler h = new ClientHandler(clientSocket);
                        clients.add(h);
                        new Thread(h).start();
                    } catch (SocketException e) {
                        if (!running) break;
                    }
                }
            } catch (IOException e) {
                if (listener != null) listener.onServerError("ไม่สามารถเปิดเซิร์ฟเวอร์ได้: " + e.getMessage());
            }
        }, "GameServer-Main").start();
    }

    public void stop() {
        running = false;
        for (ClientHandler c : clients) c.disconnect();
        clients.clear();
        playerNameOrder.clear();
        try { if (serverSocket != null) serverSocket.close(); } catch (IOException ignored) {}
    }

    public void broadcast(String message) {
        for (ClientHandler c : clients) c.send(message);
    }

    // ✨ สร้าง PLAYER_LIST string (index 0 = Host เสมอ)
    private String buildPlayerList() {
        StringBuilder sb = new StringBuilder("PLAYER_LIST:");
        for (int i = 0; i < playerNameOrder.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(playerNameOrder.get(i));
        }
        return sb.toString();
    }

    private void checkAllFinished() {
        if (playerScores.size() >= expectedPlayers) {
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
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!ni.isUp() || ni.isLoopback()) continue;
                for (InetAddress addr : Collections.list(ni.getInetAddresses())) {
                    if (addr instanceof Inet4Address) {
                        String ip = addr.getHostAddress();
                        if (ip.startsWith("26.")) return ip; // Radmin VPN
                    }
                }
            }
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) { return "127.0.0.1"; }
    }

    public int getPlayerCount() { return clients.size(); }
    public boolean isRunning()  { return running; }

    // -------------------------------------------------------
    private class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String playerName = "Unknown";

        ClientHandler(Socket s) { this.socket = s; }

        @Override public void run() {
            try {
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"), true);
                in  = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
                String line;
                while ((line = in.readLine()) != null) handle(line.trim());
            } catch (IOException ignored) {
            } finally { disconnect(); }
        }

        private void handle(String msg) {
            if (msg.startsWith("JOIN:")) {
                playerName = msg.substring(5).trim();
                playerScores.put(playerName, 0);

                // ✨ เพิ่มชื่อใน playerNameOrder (Host ถูกเพิ่มไว้แล้วตั้งแต่ setHostName)
                if (!playerNameOrder.contains(playerName)) {
                    playerNameOrder.add(playerName);
                }

                // แจ้งทุกคนว่ามีคนใหม่เข้ามา
                broadcast("PLAYER_JOINED:" + playerName + ":" + clients.size());

                // ✨ ส่ง WELCOME และ PLAYER_LIST ให้ client ใหม่ทราบว่าใครอยู่ในห้องแล้วบ้าง
                send("WELCOME:" + playerName);
                send(buildPlayerList());

                // ✨ broadcast PLAYER_LIST อัปเดตให้ทุกคนในห้องด้วย
                broadcast(buildPlayerList());

                if (listener != null) listener.onPlayerJoined(playerName, clients.size());

            } else if (msg.startsWith("SCORE:")) {
                try {
                    int score = Integer.parseInt(msg.substring(6).trim());
                    playerScores.put(playerName, score);
                    broadcast("SCORE_UPDATE:" + playerName + ":" + score);
                    if (listener != null) listener.onScoreReceived(playerName, score);
                    checkAllFinished();
                } catch (NumberFormatException ignored) {}

            } else if (msg.startsWith("CHOICE_READY:")) {
                broadcast("CHOICE_READY:" + playerName);

            } else if (msg.equals("PING")) {
                send("PONG");
            }
        }

        public void send(String m) { if (out != null) out.println(m); }

        public void disconnect() {
            clients.remove(this);
            playerScores.remove(playerName);
            playerNameOrder.remove(playerName); // ✨ เอาออกจาก order ด้วย
            try { if (socket != null) socket.close(); } catch (IOException ignored) {}
            if (!playerName.equals("Unknown")) {
                broadcast("PLAYER_LEFT:" + playerName + ":" + clients.size());
                // ✨ broadcast list ที่อัปเดตแล้วให้ทุกคน
                broadcast(buildPlayerList());
                if (listener != null) listener.onPlayerLeft(playerName, clients.size());
            }
        }
    }
}