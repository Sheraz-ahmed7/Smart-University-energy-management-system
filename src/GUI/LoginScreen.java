package GUI;

import Models.User;
import controllers.LoginController;
import database.Queries;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoginScreen extends JFrame {

    // ── Colors ────────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(13, 20, 16);
    private static final Color BG_CARD      = new Color(24, 38, 28);
    private static final Color BG_FIELD     = new Color(30, 46, 34);
    private static final Color ACCENT       = new Color(52, 199, 89);
    private static final Color ACCENT_DARK  = new Color(28, 105, 50);
    private static final Color TEXT_WHITE   = new Color(238, 245, 238);
    private static final Color TEXT_GRAY    = new Color(140, 168, 148);
    private static final Color TEXT_MUTED   = new Color(80, 110, 88);
    private static final Color BORDER_NORM  = new Color(44, 68, 50);
    private static final Color BORDER_FOCUS = new Color(52, 199, 89);
    private static final Color RED          = new Color(255, 80, 80);
    private static final Color LINK_COLOR   = new Color(80, 180, 120);

    private DarkTextField  usernameField;
    private DarkPassField  passwordField;
    private JButton        loginButton;
    private JLabel         statusLabel;
    private final LoginController ctrl = new LoginController();

    public LoginScreen() {
        setUndecorated(true);
        setSize(440, 590);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setShape(new RoundRectangle2D.Double(0, 0, 440, 590, 20, 20));
        setBackground(BG_DARK);
        buildUI();
        makeDraggable();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BG_DARK);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        root.setOpaque(true);
        root.setBorder(BorderFactory.createLineBorder(BORDER_NORM, 1));
        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── Top bar ───────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 18, 0, 14));
        bar.setPreferredSize(new Dimension(440, 38));

        JLabel app = lbl("⚡ SEUS  ·  Energy Management",
                new Font("Segoe UI", Font.PLAIN, 11), TEXT_MUTED);

        JButton close = winBtn("✕", RED);
        close.addActionListener(e -> System.exit(0));
        JButton min = winBtn("−", TEXT_MUTED);
        min.addActionListener(e -> setState(ICONIFIED));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        btns.setOpaque(false);
        btns.add(min); btns.add(close);
        bar.add(app, BorderLayout.WEST);
        bar.add(btns, BorderLayout.EAST);
        return bar;
    }

    private JButton winBtn(String text, Color hover) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) { g2.setColor(hover.darker()); g2.fillRoundRect(0,0,getWidth(),getHeight(),5,5); }
                g2.setColor(getModel().isRollover() ? Color.WHITE : TEXT_MUTED);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text,(getWidth()-fm.stringWidth(text))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(26, 22));
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ── Center card ───────────────────────────────────────────
    private JPanel buildCenter() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);

        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD); g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16);
                GradientPaint gp = new GradientPaint(0,0,ACCENT,getWidth(),0,new Color(52,199,89,0));
                g2.setPaint(gp); g2.fillRoundRect(0,0,getWidth(),4,4,4);
                g2.setColor(BORDER_NORM); g2.setStroke(new BasicStroke(0.8f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,16,16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(360, 480));
        card.setBorder(BorderFactory.createEmptyBorder(26, 32, 22, 32));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1; c.gridx = 0;

        // Logo
        c.gridy = 0; c.insets = new Insets(0,0,6,0); card.add(buildLogo(), c);

        // Title
        c.gridy = 1; c.insets = new Insets(0,0,4,0);
        JLabel title = lbl("Welcome Back", new Font("Segoe UI", Font.BOLD, 24), TEXT_WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER); card.add(title, c);

        // Subtitle
        c.gridy = 2; c.insets = new Insets(0,0,22,0);
        JLabel sub = lbl("Sign in to your energy dashboard", new Font("Segoe UI", Font.PLAIN, 13), TEXT_GRAY);
        sub.setHorizontalAlignment(SwingConstants.CENTER); card.add(sub, c);

        // Username
        c.gridy = 3; c.insets = new Insets(0,2,5,0);
        card.add(lbl("Username", new Font("Segoe UI", Font.BOLD, 12), TEXT_GRAY), c);
        c.gridy = 4; c.insets = new Insets(0,0,13,0);
        usernameField = new DarkTextField("Enter your username");
        card.add(usernameField, c);

        // Password
        c.gridy = 5; c.insets = new Insets(0,2,5,0);
        card.add(lbl("Password", new Font("Segoe UI", Font.BOLD, 12), TEXT_GRAY), c);
        c.gridy = 6; c.insets = new Insets(0,0,8,0);
        passwordField = new DarkPassField("Enter your password");
        card.add(passwordField, c);

        // Status
        c.gridy = 7; c.insets = new Insets(0,2,10,0);
        statusLabel = lbl(" ", new Font("Segoe UI", Font.PLAIN, 12), RED);
        card.add(statusLabel, c);

        // Sign In button
        c.gridy = 8; c.insets = new Insets(0,0,0,0);
        loginButton = buildLoginBtn();
        card.add(loginButton, c);

        // Register link
        c.gridy = 9; c.insets = new Insets(14,0,0,0);
        card.add(buildRegisterRow(), c);

        // Footer
        c.gridy = 10; c.insets = new Insets(14,0,0,0);
        JLabel footer = lbl("Smart Energy Optimization System  v2.0",
                new Font("Segoe UI", Font.PLAIN, 10), TEXT_MUTED);
        footer.setHorizontalAlignment(SwingConstants.CENTER); card.add(footer, c);

        // Enter key on both fields
        usernameField.addActionListener(e -> performLogin());
        passwordField.addActionListener(e -> performLogin());

        outer.add(card);
        return outer;
    }

    private JPanel buildLogo() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx=getWidth()/2, cy=getHeight()/2, r=26;
                g2.setColor(new Color(52,199,89,25)); g2.fillOval(cx-r-8,cy-r-8,(r+8)*2,(r+8)*2);
                g2.setColor(new Color(52,199,89,50)); g2.fillOval(cx-r,cy-r,r*2,r*2);
                g2.setColor(ACCENT); g2.setStroke(new BasicStroke(1.5f)); g2.drawOval(cx-r,cy-r,r*2,r*2);
                g2.setFont(new Font("Segoe UI Emoji",Font.PLAIN,24));
                FontMetrics fm=g2.getFontMetrics(); String s="⚡";
                g2.drawString(s,cx-fm.stringWidth(s)/2,cy+fm.getAscent()/2-1);
                g2.dispose();
            }
        };
        p.setOpaque(false); p.setPreferredSize(new Dimension(360,70)); return p;
    }

    private JButton buildLoginBtn() {
        JButton b = new JButton("Sign In") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg=isEnabled()?(getModel().isRollover()?ACCENT:ACCENT_DARK):new Color(40,60,44);
                g2.setColor(bg); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                if(isEnabled()){g2.setColor(new Color(255,255,255,20));g2.fillRoundRect(0,0,getWidth(),getHeight()/2,10,10);}
                g2.setFont(new Font("Segoe UI",Font.BOLD,14)); g2.setColor(Color.WHITE);
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(296, 44));
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> performLogin());
        return b;
    }

    private JPanel buildRegisterRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        row.setOpaque(false);
        row.add(lbl("Don't have an account?", new Font("Segoe UI", Font.PLAIN, 12), TEXT_GRAY));
        JButton link = new JButton("Register") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(new Font("Segoe UI",Font.BOLD,12));
                Color c=getModel().isRollover()?ACCENT:LINK_COLOR; g2.setColor(c);
                FontMetrics fm=g2.getFontMetrics();
                int x=(getWidth()-fm.stringWidth(getText()))/2, y=(getHeight()+fm.getAscent()-fm.getDescent())/2;
                g2.drawString(getText(),x,y);
                if(getModel().isRollover()) g2.drawLine(x,y+2,x+fm.stringWidth(getText()),y+2);
                g2.dispose();
            }
        };
        link.setPreferredSize(new Dimension(70, 22));
        link.setContentAreaFilled(false); link.setBorderPainted(false); link.setFocusPainted(false);
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.addActionListener(e -> openRegisterDialog());
        row.add(link);
        return row;
    }

    // ── Login logic ───────────────────────────────────────────
    private void performLogin() {
        String user = usernameField.getRealText();
        String pass = passwordField.getRealPassword();
        if (user.isEmpty()) { showStatus("Please enter username"); return; }
        if (pass.isEmpty()) { showStatus("Please enter password"); return; }

        loginButton.setEnabled(false); loginButton.setText("Signing in…"); statusLabel.setText(" ");

        new SwingWorker<User, Void>() {
            @Override protected User doInBackground() { return ctrl.authenticate(user, pass); }
            @Override protected void done() {
                loginButton.setEnabled(true); loginButton.setText("Sign In");
                try {
                    User u = get();
                    if (u != null) { dispose(); new Dashboard(u).setVisible(true); }
                    else { showStatus("Invalid username or password"); passwordField.clear(); }
                } catch (Exception ex) { showStatus("Connection error: " + ex.getMessage()); }
            }
        }.execute();
    }

    private void showStatus(String msg) {
        statusLabel.setText("⚠  " + msg);
        Point o = getLocation(); Timer t = new Timer(28, null); int[] i = {0};
        int[] off = {-7,7,-5,5,-3,3,-1,1,0};
        t.addActionListener(e -> {
            if(i[0]<off.length){setLocation(o.x+off[i[0]],o.y);i[0]++;}
            else{setLocation(o);((Timer)e.getSource()).stop();}
        });
        t.start();
    }

    private void makeDraggable() {
        Point[] origin = {null};
        addMouseListener(new MouseAdapter(){public void mousePressed(MouseEvent e){origin[0]=e.getPoint();}});
        addMouseMotionListener(new MouseMotionAdapter(){
            public void mouseDragged(MouseEvent e){
                if(origin[0]!=null){Point l=getLocation();setLocation(l.x+e.getX()-origin[0].x,l.y+e.getY()-origin[0].y);}
            }
        });
    }

    private JLabel lbl(String t, Font f, Color c) { JLabel l=new JLabel(t);l.setFont(f);l.setForeground(c);return l; }

    // ══════════════════════════════════════════════════════════
    //  DARK TEXT FIELD — custom painted, works with any LAF
    //  The key: extends JPanel (not JTextField), embeds a real
    //  JTextField inside but HIDES the default white background.
    // ══════════════════════════════════════════════════════════
    static class DarkTextField extends JPanel {
        protected final JTextField input;
        private final String placeholder;
        private boolean focused = false;

        DarkTextField(String placeholder) {
            this.placeholder = placeholder;
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(296, 42));
            setOpaque(false); // panel is transparent — we paint manually

            input = new JTextField();
            styleInput(input);
            add(input, BorderLayout.CENTER);

            input.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) { focused = true; repaint(); }
                public void focusLost(FocusEvent e)   { focused = false; repaint(); }
            });
        }

        protected void styleInput(JTextField f) {
            f.setOpaque(false);               // field itself transparent
            f.setBackground(new Color(0,0,0,0));
            f.setForeground(TEXT_GRAY);
            f.setCaretColor(ACCENT);
            f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            f.setBorder(new EmptyBorder(0, 12, 0, 12)); // only inner padding
            f.setText(placeholder);
            f.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    if (f.getText().equals(placeholder)) { f.setText(""); f.setForeground(TEXT_WHITE); }
                }
                public void focusLost(FocusEvent e) {
                    if (f.getText().isEmpty()) { f.setText(placeholder); f.setForeground(TEXT_GRAY); }
                }
            });
        }

        /** Returns actual typed text, empty string if still showing placeholder */
        public String getRealText() {
            String t = input.getText();
            return t.equals(placeholder) ? "" : t.trim();
        }

        public void addActionListener(ActionListener l) { input.addActionListener(l); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Background fill
            g2.setColor(BG_FIELD);
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
            // Border
            g2.setColor(focused ? BORDER_FOCUS : BORDER_NORM);
            g2.setStroke(new BasicStroke(focused ? 1.8f : 1.2f));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
            g2.dispose();
        }
    }

    // ── Password variant ──────────────────────────────────────
    static class DarkPassField extends DarkTextField {
        private final JPasswordField passInput;
        private final String ph;

        DarkPassField(String placeholder) {
            super(placeholder);
            this.ph = placeholder;
            // Remove the plain text field, add password field
            remove(input);
            passInput = new JPasswordField();
            styleInput(passInput);
            passInput.setEchoChar((char) 0); // show placeholder as plain text

            passInput.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    if (new String(passInput.getPassword()).equals(ph)) {
                        passInput.setText(""); passInput.setForeground(TEXT_WHITE); passInput.setEchoChar('●');
                    }
                }
                public void focusLost(FocusEvent e) {
                    if (new String(passInput.getPassword()).isEmpty()) {
                        passInput.setEchoChar((char)0); passInput.setText(ph); passInput.setForeground(TEXT_GRAY);
                    }
                }
            });
            passInput.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) { repaint(); }
                public void focusLost(FocusEvent e)   { repaint(); }
            });

            add(passInput, BorderLayout.CENTER);
            revalidate();
        }

        public String getRealPassword() {
            String p = new String(passInput.getPassword());
            return p.equals(ph) ? "" : p.trim();
        }

        public void clear() {
            passInput.setEchoChar((char) 0);
            passInput.setText(ph);
            passInput.setForeground(TEXT_GRAY);
        }

        @Override public void addActionListener(ActionListener l) { passInput.addActionListener(l); }
    }

    // ══════════════════════════════════════════════════════════
    //  REGISTRATION DIALOG
    // ══════════════════════════════════════════════════════════
    private void openRegisterDialog() {
        JDialog dlg = new JDialog(this, "Create Account", true);
        dlg.setUndecorated(true);
        dlg.setSize(380, 490);
        dlg.setLocationRelativeTo(this);
        dlg.setBackground(BG_DARK);
        dlg.setShape(new RoundRectangle2D.Double(0,0,380,490,16,16));

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD); g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16);
                GradientPaint gp=new GradientPaint(0,0,ACCENT,getWidth(),0,new Color(52,199,89,0));
                g2.setPaint(gp); g2.fillRoundRect(0,0,getWidth(),4,4,4);
                g2.setColor(BORDER_NORM); g2.setStroke(new BasicStroke(0.8f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,16,16);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(24,28,22,28));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill=GridBagConstraints.HORIZONTAL; c.weightx=1; c.gridx=0;

        c.gridy=0; c.insets=new Insets(0,0,4,0);
        JLabel ico=lbl("🔐",new Font("Segoe UI Emoji",Font.PLAIN,28),TEXT_GRAY);
        ico.setHorizontalAlignment(SwingConstants.CENTER); form.add(ico,c);

        c.gridy=1; c.insets=new Insets(0,0,20,0);
        JLabel title=lbl("Create Account",new Font("Segoe UI",Font.BOLD,20),TEXT_WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER); form.add(title,c);

        c.gridy=2; c.insets=new Insets(0,0,5,0);
        form.add(lbl("Username",new Font("Segoe UI",Font.BOLD,12),TEXT_GRAY),c);
        c.gridy=3; c.insets=new Insets(0,0,12,0);
        DarkTextField regUser = new DarkTextField("Choose a username");
        form.add(regUser,c);

        c.gridy=4; c.insets=new Insets(0,0,5,0);
        form.add(lbl("Password  (min 6 chars)",new Font("Segoe UI",Font.BOLD,12),TEXT_GRAY),c);
        c.gridy=5; c.insets=new Insets(0,0,12,0);
        DarkPassField regPass = new DarkPassField("Create a password");
        form.add(regPass,c);

        c.gridy=6; c.insets=new Insets(0,0,5,0);
        form.add(lbl("Confirm Password",new Font("Segoe UI",Font.BOLD,12),TEXT_GRAY),c);
        c.gridy=7; c.insets=new Insets(0,0,14,0);
        DarkPassField regConf = new DarkPassField("Repeat your password");
        form.add(regConf,c);

        c.gridy=8; c.insets=new Insets(0,0,10,0);
        JLabel regStatus=lbl(" ",new Font("Segoe UI",Font.PLAIN,11),RED);
        form.add(regStatus,c);

        JPanel btnRow=new JPanel(new GridLayout(1,2,10,0)); btnRow.setOpaque(false);
        JButton cancel=smallBtn("Cancel",new Color(55,75,60));
        JButton create=smallBtn("Create Account",ACCENT_DARK);
        cancel.addActionListener(e->dlg.dispose());
        create.addActionListener(e->{
            String uname=regUser.getRealText();
            String pass1=regPass.getRealPassword();
            String pass2=regConf.getRealPassword();
            if(uname.isEmpty()){regStatus.setText("⚠  Username required");return;}
            if(uname.length()<3){regStatus.setText("⚠  Username min 3 chars");return;}
            if(pass1.isEmpty()){regStatus.setText("⚠  Password required");return;}
            if(pass1.length()<6){regStatus.setText("⚠  Password min 6 chars");return;}
            if(!pass1.equals(pass2)){regStatus.setText("⚠  Passwords do not match");return;}
            if(Queries.getUserByUsername(uname)!=null){regStatus.setText("⚠  Username already taken");return;}
            User newUser=new User(uname,LoginController.sha256(pass1),"staff",null);
            if(Queries.addUser(newUser)){
                dlg.dispose();
                JOptionPane.showMessageDialog(LoginScreen.this,
                    "<html><b>Account created!</b><br>You can now sign in.<br>Ask admin to assign your department.</html>",
                    "Success",JOptionPane.INFORMATION_MESSAGE);
                usernameField.input.setText(uname);
                usernameField.input.setForeground(TEXT_WHITE);
                passwordField.clear();
                passwordField.passInput.requestFocus();
            } else { regStatus.setText("⚠  Registration failed. Try again."); }
        });
        btnRow.add(cancel); btnRow.add(create);
        c.gridy=9; c.insets=new Insets(0,0,0,0); form.add(btnRow,c);

        root.add(form,BorderLayout.CENTER);
        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    private JButton smallBtn(String text, Color bg) {
        JButton b=new JButton(text){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()?bg.brighter():bg);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setFont(new Font("Segoe UI",Font.BOLD,12)); g2.setColor(Color.WHITE);
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(140,38));
        b.setContentAreaFilled(false);b.setBorderPainted(false);b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}