package model;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * GameClient — รับ 2-phase timer จาก Server
 *
 * ข้อความที่รับ:
 *   PHASE_READ:scene:sec:total    → Phase 1 อ่าน
 *   TIMER_SYNC:t                  → update timer ทุก 1 วิ
 *   READY_COUNT:n:total           → update ready count
 *   PHASE_CHOICE:scene:sec:total  → Phase 2 เลือก (unlock)
 *   FORCE_NEXT:scene              → Phase 2 หมด → ไปซีนถัดไป
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

        /** Phase 1: Server เริ่ม timer อ่าน */
        default void onPhaseRead(String scene, int sec, int total) {}
        /** countdown ก่อน choice */
        default void onCountdown(int n) {}
        /** Phase 2: Server unlock + เริ่ม timer */
        default void onPhaseChoice(String scene, int sec, int total) {}
        /** Phase 2 หมด: ข้ามซีน (targetScene = ซีนที่ต้องไป) */
        default void onForceNext(String scene, String targetScene) {}
        /** Server ส่งผล choice → client ไปซีนเดียวกัน */
        default void onChoiceResult(String targetScene, String charName, int score) {}
        /** Timer sync ทุก 1 วิ */
        default void onTimerSync(int t) {}
        /** Ready count */
        default void onReadyCount(int ready, int total) {}
    }

    private Socket socket;
    private PrintWriter out;
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
            // PHASE_READ:scene:sec:total
            String[] p = msg.substring(11).split(":");
            if (p.length>=3 && listener!=null) {
                try { listener.onPhaseRead(p[0], Integer.parseInt(p[1]), Integer.parseInt(p[2])); }
                catch (NumberFormatException ignored) {}
            }

        } else if (msg.startsWith("PHASE_CHOICE:")) {
            // PHASE_CHOICE:scene:sec:total
            String[] p = msg.substring(13).split(":");
            if (p.length>=3 && listener!=null) {
                try { listener.onPhaseChoice(p[0], Integer.parseInt(p[1]), Integer.parseInt(p[2])); }
                catch (NumberFormatException ignored) {}
            }

        } else if (msg.startsWith("COUNTDOWN:")) {
            try { if (listener!=null) listener.onCountdown(Integer.parseInt(msg.substring(10).trim())); }
            catch (NumberFormatException ignored) {}

        } else if (msg.startsWith("CHOICE_RESULT:")) {
            // CHOICE_RESULT:targetScene:charName:score
            String rest = msg.substring(14);
            String[] p = rest.split(":", 3);
            String target = p.length > 0 ? p[0] : "";
            String charN  = p.length > 1 ? p[1] : "";
            int    sc     = 0;
            try { if (p.length > 2) sc = Integer.parseInt(p[2]); } catch (NumberFormatException ignored) {}
            if (listener != null) listener.onChoiceResult(target, charN, sc);

        } else if (msg.startsWith("FORCE_NEXT:")) {
            // FORCE_NEXT:scene:targetScene
            String rest = msg.substring(11);
            int idx = rest.indexOf(":");
            String scene  = idx>0 ? rest.substring(0,idx) : rest;
            String target = idx>0 ? rest.substring(idx+1) : "";
            if (listener!=null) listener.onForceNext(scene, target);

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
                if (kv.length==2) try { scores.put(kv[0],Integer.parseInt(kv[1])); } catch (NumberFormatException ignored) {}
            }
            if (listener!=null) listener.onLeaderboard(scores);

        } else if (msg.startsWith("ERROR:")) {
            if (listener!=null) listener.onError(msg.substring(6));
        }
    }

    public void sendScore(int score)               { send("SCORE:" + score); }
    /** แจ้ง Server ว่า navigate ไปซีนใหม่เสร็จแล้ว */
    public void notifySceneLoaded(String scene)    { send("SCENE_LOADED:" + scene); }
    /** ส่ง choice ของตัวเองไปให้ Server รวบรวม (รอให้ครบทุกคนก่อน broadcast) */
    public void sendPlayerChoice(String target, String charName, int score) {
        send("PLAYER_CHOICE:" + target + ":" + (charName != null ? charName : "") + ":" + score);
    }
    /** บอก Server ว่าอ่านถึง choice แล้ว */
    public void notifySceneReady(String scene)     { send("SCENE_READY:" + scene); }
    @Deprecated
    public void notifyChoiceReady()                { send("SCENE_READY:"); }

    public void disconnect() {
        connected = false;
        try { if(socket!=null) socket.close(); } catch (IOException ignored) {}
    }

    private void send(String m) { if(out!=null) out.println(m); }
    public boolean isConnected()  { return connected; }
    public String  getPlayerName(){ return playerName; }
}