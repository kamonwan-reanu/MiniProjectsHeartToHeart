package model;

import java.io.*;
import java.net.*;
import java.util.*;

public class GameClient {

    public interface ClientListener {
        void onConnected(String playerName);
        void onGameStart(int readSeconds);
        void onPlayerJoined(String playerName, int totalPlayers);
        void onPlayerLeft(String playerName, int totalPlayers);
        void onScoreUpdate(String playerName, int score);
        void onLeaderboard(Map<String, Integer> scores);
        void onChatMessage(String sender, String message);
        void onDisconnected(String reason);
        void onError(String message);

        // ✨ รับรายชื่อผู้เล่นทั้งหมดในห้อง (index 0 = Host เสมอ)
        void onPlayerListReceived(List<String> playerNames);
    }

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ClientListener listener;
    private String playerName;
    private boolean connected = false;

    public GameClient(String playerName) { this.playerName = playerName; }
    public void setListener(ClientListener l) { this.listener = l; }

    public void connect(String ip, int port) {
        new Thread(() -> {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), 5000);
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"), true);
                in  = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
                connected = true;
                send("JOIN:" + playerName);

                String line;
                while ((line = in.readLine()) != null) handle(line.trim());

            } catch (SocketTimeoutException e) {
                if (listener != null) listener.onError("หมดเวลาเชื่อมต่อ - ตรวจสอบ IP และ Radmin VPN");
            } catch (ConnectException e) {
                if (listener != null) listener.onError("ไม่พบห้อง - ตรวจสอบ IP:Port และ Radmin VPN");
            } catch (IOException e) {
                if (connected && listener != null) listener.onDisconnected("ตัดการเชื่อมต่อ");
            } finally {
                connected = false;
            }
        }, "GameClient-Recv").start();
    }

    private void handle(String msg) {
        if (msg.startsWith("WELCOME:")) {
            if (listener != null) listener.onConnected(msg.substring(8));

        // ✨ รับรายชื่อผู้เล่นทั้งหมด — index 0 คือ Host เสมอ
        } else if (msg.startsWith("PLAYER_LIST:")) {
            String csv = msg.substring("PLAYER_LIST:".length());
            List<String> names = new ArrayList<>();
            for (String n : csv.split(",")) {
                String t = n.trim();
                if (!t.isEmpty()) names.add(t);
            }
            if (listener != null) listener.onPlayerListReceived(names);

        } else if (msg.startsWith("START_GAME:")) {
            try {
                int seconds = Integer.parseInt(msg.substring(11).trim());
                if (listener != null) listener.onGameStart(seconds);
            } catch (NumberFormatException ignored) {
                if (listener != null) listener.onGameStart(30);
            }

        } else if (msg.startsWith("PLAYER_JOINED:")) {
            String[] p = msg.substring(14).split(":");
            if (p.length >= 2 && listener != null)
                listener.onPlayerJoined(p[0], Integer.parseInt(p[1]));

        } else if (msg.startsWith("PLAYER_LEFT:")) {
            String[] p = msg.substring(12).split(":");
            if (p.length >= 2 && listener != null)
                listener.onPlayerLeft(p[0], Integer.parseInt(p[1]));

        } else if (msg.startsWith("SCORE_UPDATE:")) {
            String[] p = msg.substring(13).split(":");
            if (p.length >= 2 && listener != null) {
                try { listener.onScoreUpdate(p[0], Integer.parseInt(p[1])); }
                catch (NumberFormatException ignored) {}
            }

        } else if (msg.startsWith("LEADERBOARD:")) {
            Map<String, Integer> scores = new LinkedHashMap<>();
            for (String entry : msg.substring(12).split("\\|")) {
                String[] kv = entry.split(":");
                if (kv.length == 2) {
                    try { scores.put(kv[0], Integer.parseInt(kv[1])); }
                    catch (NumberFormatException ignored) {}
                }
            }
            if (listener != null) listener.onLeaderboard(scores);

        } else if (msg.startsWith("CHAT:")) {
            String rest = msg.substring(5);
            int idx = rest.indexOf(":");
            if (idx > 0 && listener != null)
                listener.onChatMessage(rest.substring(0,idx), rest.substring(idx+1));

        } else if (msg.startsWith("ERROR:")) {
            if (listener != null) listener.onError(msg.substring(6));
        }
    }

    public void sendScore(int score)     { send("SCORE:" + score); }
    public void notifyChoiceReady()      { send("CHOICE_READY:"); }
    public void sendChat(String msg)     { send("CHAT:" + msg); }

    public void disconnect() {
        connected = false;
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    }

    private void send(String msg) { if (out != null) out.println(msg); }

    public boolean isConnected()  { return connected; }
    public String getPlayerName() { return playerName; }
}