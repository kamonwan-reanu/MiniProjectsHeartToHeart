package model;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * GameClient — รับ protocol ใหม่จาก GameServer
 *
 * ข้อความที่รับใหม่:
 *   TIMER_START:sec:total  → reset timer UI
 *   TIMER_SYNC:sec         → update countdown display
 *   READY_COUNT:n:total    → update ready label
 *   UNLOCK_CHOICE          → ปลดล็อคปุ่มเลือก
 *   PLAYER_LIST:a,b,c      → update lobby list
 */
public class GameClient {

    public interface ClientListener {
        void onConnected(String playerName);
        void onPlayerListReceived(List<String> names);
        void onGameStart(int readSeconds);
        void onPlayerJoined(String playerName, int totalPlayers);
        void onPlayerLeft(String playerName, int totalPlayers);
        void onScoreUpdate(String playerName, int score);
        void onLeaderboard(Map<String, Integer> scores);
        void onChatMessage(String sender, String message);
        void onDisconnected(String reason);
        void onError(String message);

        /** Server sync: เวลาที่เหลือ (ทุก 1 วิ) */
        default void onTimerSync(int secondsLeft) {}
        /** Server sync: ready count */
        default void onReadyCount(int ready, int total) {}
        /** Server บอกให้ unlock choice */
        default void onUnlockChoice() {}
    }

    private Socket         socket;
    private PrintWriter    out;
    private BufferedReader in;
    private ClientListener listener;
    private String         playerName;
    private boolean        connected = false;

    public GameClient(String playerName) { this.playerName = playerName; }
    public void setListener(ClientListener l) { this.listener = l; }

    public void connect(String ip, int port) {
        new Thread(() -> {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), 5000);
                out  = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
                in   = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
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

        } else if (msg.startsWith("PLAYER_LIST:")) {
            // ✅ Server ส่ง list ผู้เล่นทั้งหมด (hostName อยู่ index 0)
            String[] names = msg.substring(12).split(",");
            List<String> list = new ArrayList<>(Arrays.asList(names));
            if (listener != null) listener.onPlayerListReceived(list);

        } else if (msg.startsWith("START_GAME:")) {
            try {
                int sec = Integer.parseInt(msg.substring(11).trim());
                if (listener != null) listener.onGameStart(sec);
            } catch (NumberFormatException e) {
                if (listener != null) listener.onGameStart(40);
            }

        } else if (msg.startsWith("TIMER_START:")) {
            // ✅ Server เริ่มนาฬิกา — client reset display
            String[] p = msg.substring(12).split(":");
            try {
                int sec   = Integer.parseInt(p[0]);
                // total ไม่ต้องใช้ที่นี่ — TIMER_SYNC จะ update ต่อเนื่อง
                if (listener != null) listener.onTimerSync(sec);
            } catch (NumberFormatException ignored) {}

        } else if (msg.startsWith("TIMER_SYNC:")) {
            // ✅ Server บอกเวลาที่เหลือ (ทุก 1 วิ)
            try {
                int t = Integer.parseInt(msg.substring(11).trim());
                if (listener != null) listener.onTimerSync(t);
            } catch (NumberFormatException ignored) {}

        } else if (msg.startsWith("READY_COUNT:")) {
            // ✅ Server บอก ready count — ทุกคนเห็นเหมือนกัน
            String[] p = msg.substring(12).split(":");
            try {
                int ready = Integer.parseInt(p[0]);
                int total = Integer.parseInt(p[1]);
                if (listener != null) listener.onReadyCount(ready, total);
            } catch (NumberFormatException ignored) {}

        } else if (msg.equals("UNLOCK_CHOICE")) {
            // ✅ Server unlock ทุกคนพร้อมกัน
            if (listener != null) listener.onUnlockChoice();

        } else if (msg.startsWith("PLAYER_JOINED:")) {
            String[] p = msg.substring(14).split(":");
            try {
                if (p.length >= 2 && listener != null)
                    listener.onPlayerJoined(p[0], Integer.parseInt(p[1]));
            } catch (NumberFormatException ignored) {}

        } else if (msg.startsWith("PLAYER_LEFT:")) {
            String[] p = msg.substring(12).split(":");
            try {
                if (p.length >= 2 && listener != null)
                    listener.onPlayerLeft(p[0], Integer.parseInt(p[1]));
            } catch (NumberFormatException ignored) {}

        } else if (msg.startsWith("SCORE_UPDATE:")) {
            String[] p = msg.substring(13).split(":");
            try {
                if (p.length >= 2 && listener != null)
                    listener.onScoreUpdate(p[0], Integer.parseInt(p[1]));
            } catch (NumberFormatException ignored) {}

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
                listener.onChatMessage(rest.substring(0, idx), rest.substring(idx + 1));

        } else if (msg.startsWith("ERROR:")) {
            if (listener != null) listener.onError(msg.substring(6));
        }
    }

    // ── Send methods ──────────────────────────────────────
    public void sendScore(int score)        { send("SCORE:" + score); }
    /** บอก Server ว่าผู้เล่นนี้อ่านถึง choice แล้ว */
    public void notifyChoiceReady()         { send("CHOICE_READY:"); }
    public void sendChat(String m)          { send("CHAT:" + m); }

    public void disconnect() {
        connected = false;
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    }

    private void send(String msg) { if (out != null) out.println(msg); }

    public boolean isConnected()  { return connected; }
    public String  getPlayerName(){ return playerName; }
}   