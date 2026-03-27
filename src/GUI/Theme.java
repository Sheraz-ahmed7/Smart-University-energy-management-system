package GUI;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.AbstractBorder;

/** Central design system — all panels import from here. */
public class Theme {

    // ── Palette ────────────────────────────────────────────────
    public static final Color BG_DARK        = new Color(13, 20, 16);
    public static final Color BG_SIDEBAR     = new Color(18, 28, 21);
    public static final Color BG_CARD        = new Color(24, 38, 28);
    public static final Color BG_FIELD       = new Color(30, 46, 34);
    public static final Color ACCENT         = new Color(52, 199, 89);
    public static final Color ACCENT_DARK    = new Color(30, 120, 55);
    public static final Color ACCENT_DIM     = new Color(52, 199, 89, 40);
    public static final Color TEXT_PRIMARY   = new Color(238, 245, 238);
    public static final Color TEXT_SECONDARY = new Color(140, 168, 148);
    public static final Color TEXT_MUTED     = new Color(80, 110, 88);
    public static final Color BORDER         = new Color(44, 68, 50);
    public static final Color BORDER_FOCUS   = new Color(52, 199, 89);
    public static final Color RED            = new Color(255, 80, 80);
    public static final Color AMBER          = new Color(255, 178, 55);
    public static final Color BLUE           = new Color(64, 156, 255);
    public static final Color ROW_ALT        = new Color(28, 44, 33);
    public static final Color ROW_SEL        = new Color(52, 199, 89, 55);

    // ── Fonts ──────────────────────────────────────────────────
    public static final Font  FONT_TITLE     = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font  FONT_HEADING   = new Font("Segoe UI", Font.BOLD,  15);
    public static final Font  FONT_BODY      = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font  FONT_SMALL     = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font  FONT_BOLD      = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font  FONT_MONO      = new Font("Consolas",  Font.PLAIN, 12);
    public static final Font  FONT_NUM       = new Font("Segoe UI", Font.BOLD,  28);

    // ── Apply global Swing defaults ────────────────────────────
    public static void apply() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        UIManager.put("Panel.background",           BG_DARK);
        UIManager.put("OptionPane.background",       BG_CARD);
        UIManager.put("OptionPane.messageForeground",TEXT_PRIMARY);
        UIManager.put("Button.background",           BG_CARD);
        UIManager.put("Button.foreground",           TEXT_PRIMARY);
        UIManager.put("Label.foreground",            TEXT_PRIMARY);
        UIManager.put("TextField.background",        BG_FIELD);
        UIManager.put("TextField.foreground",        TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground",   ACCENT);
        UIManager.put("PasswordField.background",    BG_FIELD);
        UIManager.put("PasswordField.foreground",    TEXT_PRIMARY);
        UIManager.put("PasswordField.caretForeground",ACCENT);
        UIManager.put("ComboBox.background",         BG_FIELD);
        UIManager.put("ComboBox.foreground",         TEXT_PRIMARY);
        UIManager.put("Table.background",            BG_CARD);
        UIManager.put("Table.foreground",            TEXT_PRIMARY);
        UIManager.put("Table.selectionBackground",   ROW_SEL);
        UIManager.put("Table.selectionForeground",   TEXT_PRIMARY);
        UIManager.put("Table.gridColor",             BORDER);
        UIManager.put("TableHeader.background",      BG_SIDEBAR);
        UIManager.put("TableHeader.foreground",      TEXT_SECONDARY);
        UIManager.put("ScrollPane.background",       BG_DARK);
        UIManager.put("ScrollBar.thumb",             BORDER);
        UIManager.put("ScrollBar.track",             BG_DARK);
        UIManager.put("TabbedPane.background",       BG_DARK);
        UIManager.put("TabbedPane.foreground",       TEXT_SECONDARY);
        UIManager.put("TabbedPane.selected",         BG_CARD);
        UIManager.put("TabbedPane.selectedForeground",ACCENT);
        UIManager.put("SplitPane.background",        BG_DARK);
        UIManager.put("TextArea.background",         BG_FIELD);
        UIManager.put("TextArea.foreground",         TEXT_PRIMARY);
        UIManager.put("TextArea.caretForeground",    ACCENT);
    }

    // ── Factory: styled JButton ────────────────────────────────
    public static JButton button(String text, Color bg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isRollover() && isEnabled() ? bg.brighter() : bg;
                if (!isEnabled()) c = new Color(50, 70, 54);
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(isEnabled() ? Color.WHITE : TEXT_MUTED);
                g2.setFont(FONT_BOLD);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()  - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        b.setFont(FONT_BOLD);
        b.setPreferredSize(new Dimension(130, 36));
        b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Small icon button (e.g. close / minimize) */
    public static JButton iconButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(BORDER);
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
                }
                g2.setColor(getModel().isRollover() ? TEXT_PRIMARY : TEXT_MUTED);
                g2.setFont(FONT_BODY);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(26,22));
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ── Factory: styled JTextField ────────────────────────────
    public static JTextField field(String placeholder) {
        JTextField f = new JTextField();
        styleField(f, placeholder);
        return f;
    }

    public static JPasswordField passField(String placeholder) {
        JPasswordField f = new JPasswordField();
        styleField(f, placeholder);
        f.setEchoChar((char)0);
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (new String(f.getPassword()).equals(placeholder)) {
                    f.setText(""); f.setEchoChar('●');
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (new String(f.getPassword()).isEmpty()) {
                    f.setEchoChar((char)0); f.setText(placeholder);
                    f.setForeground(TEXT_SECONDARY);
                }
            }
        });
        return f;
    }

    private static void styleField(JTextField f, String placeholder) {
        f.setFont(FONT_BODY); f.setForeground(TEXT_SECONDARY);
        f.setBackground(BG_FIELD); f.setCaretColor(ACCENT);
        f.setBorder(new RoundBorder(8, BORDER, BG_FIELD));
        f.setPreferredSize(new Dimension(200, 38));
        f.setText(placeholder);
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (f.getText().equals(placeholder)) { f.setText(""); f.setForeground(TEXT_PRIMARY); }
                f.setBorder(new RoundBorder(8, BORDER_FOCUS, BG_FIELD));
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (f.getText().isEmpty()) { f.setText(placeholder); f.setForeground(TEXT_SECONDARY); }
                f.setBorder(new RoundBorder(8, BORDER, BG_FIELD));
            }
        });
    }

    // ── Factory: dark card panel ───────────────────────────────
    public static JPanel card(String title) {
        JPanel p = new JPanel(new BorderLayout(0, 12)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(0.8f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(14,16,14,16));
        if (title != null && !title.isEmpty()) {
            JLabel lbl = new JLabel(title);
            lbl.setFont(FONT_HEADING); lbl.setForeground(TEXT_PRIMARY);
            p.add(lbl, BorderLayout.NORTH);
        }
        return p;
    }

    // ── Factory: section label ─────────────────────────────────
    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font); l.setForeground(color);
        return l;
    }

    // ── KPI card ───────────────────────────────────────────────
    public static JPanel kpiCard(String title, String value, String unit, Color accent) {
        JPanel p = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD); g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(accent); g2.setStroke(new BasicStroke(2f));
                g2.drawLine(16,getHeight()-3,getWidth()-16,getHeight()-3);
                g2.setColor(BORDER); g2.setStroke(new BasicStroke(0.8f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx=0; c.gridy=0; c.fill=GridBagConstraints.HORIZONTAL; c.weightx=1;
        c.insets=new Insets(12,14,2,14);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(FONT_SMALL); titleLbl.setForeground(TEXT_SECONDARY);
        p.add(titleLbl,c);

        c.gridy=1; c.insets=new Insets(0,14,2,14);
        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI",Font.BOLD,26)); valLbl.setForeground(accent);
        p.add(valLbl,c);

        c.gridy=2; c.insets=new Insets(0,14,14,14);
        JLabel unitLbl = new JLabel(unit);
        unitLbl.setFont(FONT_SMALL); unitLbl.setForeground(TEXT_MUTED);
        p.add(unitLbl,c);

        return p;
    }

    // ── Rounded border ─────────────────────────────────────────
    public static class RoundBorder extends AbstractBorder {
        private final int r; private final Color border, fill;
        public RoundBorder(int r, Color border, Color fill) {
            this.r=r; this.border=border; this.fill=fill;
        }
        @Override public void paintBorder(Component c,Graphics g,int x,int y,int w,int h){
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill); g2.fillRoundRect(x,y,w-1,h-1,r*2,r*2);
            g2.setColor(border); g2.setStroke(new BasicStroke(1.3f));
            g2.drawRoundRect(x,y,w-1,h-1,r*2,r*2); g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c){return new Insets(8,12,8,12);}
        @Override public boolean isBorderOpaque(){return false;}
    }

    // ── Dark scroll pane ───────────────────────────────────────
    public static JScrollPane scroll(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBackground(BG_DARK); sp.getViewport().setBackground(BG_DARK);
        sp.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        sp.getVerticalScrollBar().setUnitIncrement(12);
        return sp;
    }

    // ── Dark JTable ────────────────────────────────────────────
    public static void styleTable(JTable t) {
        t.setBackground(BG_CARD); t.setForeground(TEXT_PRIMARY);
        t.setGridColor(BORDER); t.setRowHeight(32);
        t.setFont(FONT_BODY); t.setSelectionBackground(ROW_SEL);
        t.setSelectionForeground(TEXT_PRIMARY); t.setShowGrid(true);
        t.setIntercellSpacing(new Dimension(0,1));
        t.getTableHeader().setBackground(BG_SIDEBAR);
        t.getTableHeader().setForeground(TEXT_SECONDARY);
        t.getTableHeader().setFont(FONT_BOLD);
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDER));
    }

    // ── Dark JComboBox styling ─────────────────────────────────
    public static void styleCombo(JComboBox<?> c) {
        c.setBackground(BG_FIELD); c.setForeground(TEXT_PRIMARY);
        c.setFont(FONT_BODY); c.setBorder(new RoundBorder(8,BORDER,BG_FIELD));
        c.setPreferredSize(new Dimension(200,36));
    }

    // ── Dark bg panel ──────────────────────────────────────────
    public static JPanel darkPanel(LayoutManager lm) {
        JPanel p = new JPanel(lm) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BG_DARK); g.fillRect(0,0,getWidth(),getHeight());
            }
        };
        p.setOpaque(true); return p;
    }
}