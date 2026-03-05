package core; 

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import UI_Screens.*; 
import model.StoryData;
import model.GameConstants;
import model.SoundManager; 

public class Main {
    public static JFrame mainFrame;
    public static CardLayout cardLayout = new CardLayout();
    public static JPanel mainContainer = new JPanel(cardLayout);
    public static SoundManager soundManager = new SoundManager();
    
    public static float brightnessAlpha = 0.0f;
    public static Color overlayColor = Color.BLACK;
    private static JPanel brightnessOverlay;

    // สำหรับเก็บหน้าต่างแชท
    public static Map<String, ChatScenePanel> chatPanels = new HashMap<>();

    public static final java.util.Set<String> CHAT_SCENES = new java.util.HashSet<>(
        java.util.Arrays.asList(
            "SCENE_4", "SCENE_7", "SCENE_8", "SCENE_9",
            "SCENE_13_CHAT", "SCENE_14", "SCENE_15", "SCENE_16"
        )
    );

    public static void main(String[] args) {
        setUIFont(new Font("Tahoma", Font.PLAIN, 18));

        SwingUtilities.invokeLater(() -> {
            try {
                mainFrame = new JFrame("HeartToHeart");
                mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                mainFrame.setResizable(false); 

                setupScreens(); 
                
                mainFrame.add(mainContainer);
                mainFrame.setSize(1920, 1080); 
                mainFrame.setLocationRelativeTo(null); 
                
                initBrightnessSystem();
                soundManager.setVolume(0.5f);
                
                mainFrame.setVisible(true);
                cardLayout.show(mainContainer, "MENU");

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void setUIFont(Font font) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, font);
            }
        }
    }

    private static void setupScreens() {
        Font titleFont   = new Font("Tahoma", Font.BOLD, 80); 
        Font menuFont    = new Font("Tahoma", Font.BOLD, 24);  
        Font subTitleFont = new Font("Tahoma", Font.BOLD, 40);

        try {
            mainContainer.add(new MainMenu(titleFont, menuFont), "MENU");
            mainContainer.add(new SettingPage(subTitleFont, menuFont), "SETTING");
            mainContainer.add(new CreditPage(subTitleFont, menuFont), "CREDIT");
            mainContainer.add(new PlayPage(subTitleFont), "PLAY_PAGE");

            model.GameState.reset();
            PlaySceneMain gameplayScene = new PlaySceneMain(StoryData.SCENE_1, null, "SCENE_1");
            mainContainer.add(gameplayScene, "PLAY_SCENE");

            // ✅ หน้าจอ Multiplayer
            mainContainer.add(new MultiplayerLobby(), "MULTIPLAYER");

            // ✅ เตรียมหน้าจอ Chat (จากฝั่งเพื่อน)
            String chatBG = GameConstants.SCENE_PATH + "dating_chat.png";

            ChatScenePanel c4 = new ChatScenePanel(StoryData.SCENE_4, chatBG, () -> {
                for (Component comp : mainContainer.getComponents()) {
                    if (comp instanceof PlaySceneMain) {
                        ((PlaySceneMain) comp).loadNewScene(StoryData.SCENE_5, "SCENE_5");
                        break;
                    }
                }
                cardLayout.show(mainContainer, "PLAY_SCENE");
            }, true);

            Runnable goToHub = () -> {
                for (Component comp : mainContainer.getComponents()) {
                    if (comp instanceof PlaySceneMain) {
                        ((PlaySceneMain) comp).loadNewScene(StoryData.SCENE_6, "SCENE_6");
                        break;
                    }
                }
                cardLayout.show(mainContainer, "PLAY_SCENE");
            };

            ChatScenePanel c7 = new ChatScenePanel(StoryData.SCENE_7, chatBG, goToHub, true);
            ChatScenePanel c8 = new ChatScenePanel(StoryData.SCENE_8, chatBG, goToHub, true);
            ChatScenePanel c9 = new ChatScenePanel(StoryData.SCENE_9, chatBG, goToHub, true);

            Runnable goToScene17 = () -> {
                for (Component comp : mainContainer.getComponents()) {
                    if (comp instanceof PlaySceneMain) {
                        ((PlaySceneMain) comp).loadNewScene(StoryData.SCENE_17, "SCENE_17");
                        break;
                    }
                }
                cardLayout.show(mainContainer, "PLAY_SCENE");
            };

            ChatScenePanel c13 = new ChatScenePanel(StoryData.SCENE_13_CHAT, 
                chatBG, () -> {
                    for (Component comp : mainContainer.getComponents()) {
                        if (comp instanceof PlaySceneMain) {
                            Object[][] choiceOnly = new Object[][] {
                                StoryData.SCENE_13_CHAT[StoryData.SCENE_13_CHAT.length - 1]
                            };
                            ((PlaySceneMain) comp).loadNewScene(choiceOnly, "SCENE_13_CHAT");
                            break;
                        }
                    }
                    cardLayout.show(mainContainer, "PLAY_SCENE");
                }, true);

            chatPanels.put("CHAT_13_CHAT", c13);
            mainContainer.add(c13, "CHAT_13_CHAT");

            ChatScenePanel c14 = new ChatScenePanel(StoryData.SCENE_14, chatBG, goToScene17, true);
            ChatScenePanel c15 = new ChatScenePanel(StoryData.SCENE_15, chatBG, goToScene17, true);
            ChatScenePanel c16 = new ChatScenePanel(StoryData.SCENE_16, chatBG, goToScene17, true);

            chatPanels.put("CHAT_4",  c4);  chatPanels.put("CHAT_7",  c7);
            chatPanels.put("CHAT_8",  c8);  chatPanels.put("CHAT_9",  c9);
            chatPanels.put("CHAT_13", c13); chatPanels.put("CHAT_14", c14);
            chatPanels.put("CHAT_15", c15); chatPanels.put("CHAT_16", c16);

            mainContainer.add(c4,  "CHAT_4");  mainContainer.add(c7,  "CHAT_7");
            mainContainer.add(c8,  "CHAT_8");  mainContainer.add(c9,  "CHAT_9");
            mainContainer.add(c13, "CHAT_13"); mainContainer.add(c14, "CHAT_14");
            mainContainer.add(c15, "CHAT_15"); mainContainer.add(c16, "CHAT_16");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initBrightnessSystem() {
        brightnessOverlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, brightnessAlpha));
                g2d.setColor(overlayColor);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        brightnessOverlay.setOpaque(false);
        mainFrame.setGlassPane(brightnessOverlay);
        brightnessOverlay.setVisible(true);
    }

    public static void repaintBrightness() {
        if (brightnessOverlay != null) brightnessOverlay.repaint();
    }
}