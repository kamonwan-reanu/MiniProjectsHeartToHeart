package UI_Components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class PauseMenuUI extends JPanel {
    private static PauseMenuUI instance;
    private Container parentContainer;
    private boolean isVisible = false;
    
    // ปุ่มต่างๆในเมนู
    private JButton resumeButton, saveButton, mainMenuButton, exitButton;
    private JPanel menuPanel;
    
    private PauseMenuUI() {
        setLayout(null);
        setOpaque(false);
        setVisible(false);
        setupMenuPanel();
        setupButtons();
    }
    
    public static PauseMenuUI getInstance() {
        if (instance == null) {
            instance = new PauseMenuUI();
        }
        return instance;
    }
    
    private void setupMenuPanel() {
        menuPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                
                // วาดพื้นหลังกล่องเมนู
                RoundRectangle2D background = new RoundRectangle2D.Float(0, 0, w, h, 20, 20);
                g2d.setColor(new Color(15, 20, 35, 240));
                g2d.fill(background);
                
                // วาดขอบสีทอง
                g2d.setColor(new Color(212, 175, 55, 180));
                g2d.setStroke(new BasicStroke(3.0f));
                g2d.draw(background);
                
                // วาดหัวข้อ PAUSE
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Tahoma", Font.BOLD, 28));
                FontMetrics fm = g2d.getFontMetrics();
                String title = "PAUSE";
                int titleX = (w - fm.stringWidth(title)) / 2;
                g2d.drawString(title, titleX, 50);
                
                g2d.dispose();
            }
        };
        menuPanel.setOpaque(false);
        add(menuPanel);
    }
    
    private void setupButtons() {
        Font buttonFont = new Font("Tahoma", Font.BOLD, 18);
        Color buttonBg = new Color(255, 105, 180);
        Color buttonHover = new Color(255, 140, 200);
        
        // ปุ่ม RESUME
        resumeButton = createStyledButton("RESUME", buttonFont, buttonBg, buttonHover);
        resumeButton.addActionListener(e -> hideMenu());
        
        // ปุ่ม SAVE GAME
        saveButton = createStyledButton("SAVE GAME", buttonFont, buttonBg, buttonHover);
        saveButton.addActionListener(e -> {
            SaveSystemUI.getInstance().triggerSave();
        });
        
        // ปุ่ม MAIN MENU
        mainMenuButton = createStyledButton("MAIN MENU", buttonFont, buttonBg, buttonHover);
        mainMenuButton.addActionListener(e -> {
            hideMenu();
            core.Main.cardLayout.show(core.Main.mainContainer, "MENU");
        });
        
        // ปุ่ม EXIT
        exitButton = createStyledButton("EXIT", buttonFont, new Color(220, 53, 69), new Color(240, 73, 89));
        exitButton.addActionListener(e -> System.exit(0));
        
        menuPanel.add(resumeButton);
        menuPanel.add(saveButton);
        menuPanel.add(mainMenuButton);
        menuPanel.add(exitButton);
    }
    
    private JButton createStyledButton(String text, Font font, Color normalColor, Color hoverColor) {
        JButton button = new JButton(text) {
            private Color currentColor = normalColor;
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                
                // วาดพื้นหลังปุ่ม
                RoundRectangle2D buttonBg = new RoundRectangle2D.Float(2, 2, w-4, h-4, 8, 8);
                g2d.setColor(currentColor);
                g2d.fill(buttonBg);
                
                // วาดขอบสีขาว
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.draw(buttonBg);
                
                // วาดข้อความ
                g2d.setColor(Color.WHITE);
                g2d.setFont(font);
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (w - fm.stringWidth(text)) / 2;
                int textY = (h + fm.getAscent()) / 2;
                g2d.drawString(text, textX, textY);
                
                g2d.dispose();
            }
            
            @Override
            public void setBackground(Color bg) {
                currentColor = bg;
                repaint();
            }
        };
        
        button.setFont(font);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(normalColor);
            }
        });
        
        return button;
    }
    
    public void showMenu(Container parent) {
        if (parent == null) return;
        
        this.parentContainer = parent;
        
        // ตั้งขนาดให้เต็มหน้าจอ
        setBounds(0, 0, parent.getWidth(), parent.getHeight());
        
        // ตั้งขนาดและตำแหน่งกล่องเมนูตรงกลาง
        int menuWidth = 350;
        int menuHeight = 400;
        int menuX = (parent.getWidth() - menuWidth) / 2;
        int menuY = (parent.getHeight() - menuHeight) / 2;
        menuPanel.setBounds(menuX, menuY, menuWidth, menuHeight);
        
        // จัดตำแหน่งปุ่มต่างๆ
        int buttonWidth = 250;
        int buttonHeight = 50;
        int buttonX = (menuWidth - buttonWidth) / 2;
        int startY = 100;
        int spacing = 70;
        
        resumeButton.setBounds(buttonX, startY, buttonWidth, buttonHeight);
        saveButton.setBounds(buttonX, startY + spacing, buttonWidth, buttonHeight);
        mainMenuButton.setBounds(buttonX, startY + spacing * 2, buttonWidth, buttonHeight);
        exitButton.setBounds(buttonX, startY + spacing * 3, buttonWidth, buttonHeight);
        
        // ตรวจสอบว่ามีอยู่ใน parent แล้วหรือไม่
        boolean alreadyAdded = false;
        for (Component comp : parent.getComponents()) {
            if (comp instanceof PauseMenuUI) {
                alreadyAdded = true;
                break;
            }
        }
        
        if (!alreadyAdded) {
            parent.add(this);
        }
        
        // ตั้ง Z-order ให้อยู่หน้าสุด
        parent.setComponentZOrder(this, 0);
        
        setVisible(true);
        isVisible = true;
        parent.revalidate();
        parent.repaint();
    }
    
    public void hideMenu() {
        if (parentContainer != null && isVisible) {
            setVisible(false);
            isVisible = false;
            parentContainer.revalidate();
            parentContainer.repaint();
            
            // คืน focus ให้กับ PlaySceneMain
            parentContainer.requestFocusInWindow();
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (isVisible) {
            // วาดพื้นหลังโปร่งแสงสีดำ
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.dispose();
        }
    }
    
    public boolean isMenuVisible() {
        return isVisible;
    }
}
