package model;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * GameClient v3 — รับ message ใหม่ทั้งหมด
 *
 * FIX 1: รับ CHOICE_ALERT → แสดง banner
 * FIX 2: รับ COUNTDOWN:n → แสดง 3-2-1
 * FIX 3: รับ FORCE_NEXT:scene → ทุกคนไปพร้อมกัน
 * FIX 4: รับ RANDOM_CHOICE:scene:idx → สุ่มให้อัตโนมัติ
 * ส่ง CHOICE_MADE:scene เมื่อผู้เล่นกดเลือก
 */
public class GameClient {

    public interface ClientListener {
        void onConnected(String playerName);
        void onPlayerListReceived(List<String> names);
        void onGameStart(int readSeconds);
        void onPlayerJoined(String playerName, int total);
        void onPlayerLeft(String playerName, int total);
        void onScoreUpdate(String playerName, int score);
        void onLeaderboard(Map<String,Integer> scores);
        void onChatMessage(String sender, String message);
        void onDisconnected(String reason);
        void onError(String message);

        default void onPhaseRead(String scene, int sec, int total) {}
        default void onPhaseChoice(String scene, int sec, int total) {}
        default void onForceNext(String scene) {}
        default void onTimerSync(int t) {}
        default void onReadyCount(int ready, int total) {}
        /** FIX 1: แสดง banner "เลือกได้เลย!" */
        default void onChoiceAlert(String scene) {}
        /** FIX 2: แสดงตัวเลข 3-2-1 */
        default void onCountdown(int n) {}
        /** FIX 4: Server สุ่ม choice ให้ */
        default void onRandomChoice(String scene, int choiceIndex) {}
    }

    private Socket socket;
    private PrintWriter    out;
    private BufferedReader in;
    private ClientListener listener;
    private String playerName;
    private boolean connected = false;

    public GameClient(String name) { this.playerName = name; }
    public void setListener(ClientListener l) { this.listener = l; }

    public void connect(String ip, int port) {
        new Thread(() -> {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), 5000);
                out  = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"),true);
                in   = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
                connected = true;
                send("JOIN:" + playerName);
                String line;
                while ((line=in.readLine())!=null) handle(line.trim());
            } catch (SocketTimeoutException e) {
                if (listener!=null) listener.onError("หมดเวลาเชื่อมต่อ - ตรวจสอบ IP");
            } catch (ConnectException e) {
                if (listener!=null) listener.onError("ไม่พบห้อง - ตรวจสอบ IP:Port");
            } catch (IOException e) {
                if (connected && listener!=null) listener.onDisconnected("ตัดการเชื่อมต่อ");
            } finally { connected = false; }
        }, "GameClient-Recv").start();
    }

    private void handle(String msg) {
        if (msg.startsWith("WELCOME:")) {
            if (listener!=null) listener.onConnected(msg.substring(8));

        } else if (msg.startsWith("PLAYER_LIST:")) {
            String[] names = msg.substring(12).split(",");
            if (listener!=null) listener.onPlayerListReceived(new ArrayList<>(Arrays.asList(names)));

        } else if (msg.startsWith("START_GAME:")) {
            try { if (listener!=null) listener.onGameStart(Integer.parseInt(msg.substring(11).trim())); }
            catch (NumberFormatException e) { if (listener!=null) listener.onGameStart(50); }

        } else if (msg.startsWith("PHASE_READ:")) {
            String[] p = msg.substring(11).split(":");
            if (p.length>=3 && listener!=null) {
                try { listener.onPhaseRead(p[0], Integer.parseInt(p[1]), Integer.parseInt(p[2])); }
                catch (NumberFormatException ignored) {}
            }

        } else if (msg.startsWith("PHASE_CHOICE:")) {
            String[] p = msg.substring(13).split(":");
            if (p.length>=3 && listener!=null) {
                try { listener.onPhaseChoice(p[0], Integer.parseInt(p[1]), Integer.parseInt(p[2])); }
                catch (NumberFormatException ignored) {}
            }

        } else if (msg.startsWith("CHOICE_ALERT:")) {
            // FIX 1
            String scene = msg.substring(13).trim();
            if (listener!=null) listener.onChoiceAlert(scene);

        } else if (msg.startsWith("COUNTDOWN:")) {
            // FIX 2
            try {
                int n = Integer.parseInt(msg.substring(10).trim());
                if (listener!=null) listener.onCountdown(n);
            } catch (NumberFormatException ignored) {}

        } else if (msg.startsWith("FORCE_NEXT:")) {
            // FIX 3: รับชื่อ scene ที่ต้องไป
            String scene = msg.substring(11).trim();
            if (listener!=null) listener.onForceNext(scene);

        } else if (msg.startsWith("RANDOM_CHOICE:")) {
            // FIX 4: สุ่ม choice ให้
            String[] p = msg.substring(14).split(":");
            if (p.length>=2 && listener!=null) {
                try { listener.onRandomChoice(p[0], Integer.parseInt(p[1])); }
                catch (NumberFormatException ignored) {}
            }

        } else if (msg.startsWith("TIMER_SYNC:")) {
            try { if (listener!=null) listener.onTimerSync(Integer.parseInt(msg.substring(11).trim())); }
            catch (NumberFormatException ignored) {}

        } else if (msg.startsWith("READY_COUNT:")) {
            String[] p = msg.substring(12).split(":");
            try {
                if (p.length>=2 && listener!=null)
                    listener.onReadyCount(Integer.parseInt(p[0]), Integer.parseInt(p[1]));
            } catch (NumberFormatException ignored) {}

        } else if (msg.startsWith("PLAYER_JOINED:")) {
            String[] p = msg.substring(14).split(":");
            try { if (p.length>=2 && listener!=null) listener.onPlayerJoined(p[0],Integer.parseInt(p[1])); }
            catch (NumberFormatException ignored) {}

        } else if (msg.startsWith("PLAYER_LEFT:")) {
            String[] p = msg.substring(12).split(":");
            try { if (p.length>=2 && listener!=null) listener.onPlayerLeft(p[0],Integer.parseInt(p[1])); }
            catch (NumberFormatException ignored) {}

        } else if (msg.startsWith("SCORE_UPDATE:")) {
            String[] p = msg.substring(13).split(":");
            try { if (p.length>=2 && listener!=null) listener.onScoreUpdate(p[0],Integer.parseInt(p[1])); }
            catch (NumberFormatException ignored) {}

        } else if (msg.startsWith("LEADERBOARD:")) {
            Map<String,Integer> scores = new LinkedHashMap<>();
            for (String e : msg.substring(12).split("\\|")) {
                String[] kv = e.split(":");
                if (kv.length==2) try { scores.put(kv[0],Integer.parseInt(kv[1])); }
                    catch (NumberFormatException ignored) {}
            }
            if (listener!=null) listener.onLeaderboard(scores);

        } else if (msg.startsWith("ERROR:")) {
            if (listener!=null) listener.onError(msg.substring(6));
        }
    }

    public void sendScore(int score)         { send("SCORE:" + score); }
    public void notifySceneReady(String s)   { send("SCENE_READY:" + s); }
    /** FIX 4: แจ้ง Server ว่าเลือกแล้ว */
    public void notifyChoiceMade(String s)   { send("CHOICE_MADE:" + s); }
    @Deprecated
    public void notifyChoiceReady()          { send("SCENE_READY:"); }

    public void disconnect() {
        connected = false;
        try { if(socket!=null) socket.close(); } catch (IOException ignored) {}
    }

    private void send(String m) { if(out!=null) out.println(m); }
    public boolean isConnected()   { return connected; }
    public String  getPlayerName() { return playerName; }
}