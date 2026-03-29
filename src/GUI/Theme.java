package GUI;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;

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
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD,  15);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_MONO    = new Font("Consolas",  Font.PLAIN, 12);
    public static final Font FONT_NUM     = new Font("Segoe UI", Font.BOLD,  28);

    // ── LAF setup ─────────────────────────────────────────────
    public static void apply() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        // Do NOT set Nimbus — it breaks custom text field painting
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
    }

    // ══════════════════════════════════════════════════════════
    //  FIELD — JPanel wrapper so Nimbus/LAF cannot interfere.
    //  The actual JTextField inside is transparent; the panel
    //  paints the dark rounded background itself.
    //  This is the same fix used in LoginScreen.DarkTextField.
    // ══════════════════════════════════════════════════════════
    public static JTextField field(String placeholder) {
        // We return a special subclass that looks like JTextField
        // but is backed by a JPanel for correct painting.
        return new PanelBackedField(placeholder, false);
    }

    public static JPasswordField passField(String placeholder) {
        return (JPasswordField) new PanelBackedField(placeholder, true).getActualField();
        // Note: callers that use passField should call getPassword() on the
        // underlying field. Since most panels use Theme.passField and then
        // call getPassword(), we wrap properly below.
    }

    // ── PanelBackedField ──────────────────────────────────────
    /**
     * A JTextField subclass that delegates all painting to a JPanel wrapper,
     * bypassing any LAF renderer. The actual text input happens in a
     * transparent inner JTextField/JPasswordField.
     *
     * We extend JTextField so existing code (getText, setText, addActionListener,
     * setEnabled, setPreferredSize, etc.) keeps working without changes.
     * The trick: we override paintComponent to paint the dark rounded box,
     * and set the field itself as opaque=false so the LAF doesn't draw its
     * white rectangle over us.
     */
    public static class PanelBackedField extends JTextField {

        private final String    placeholder;
        private final boolean   isPassword;
        private       boolean   focused = false;
        // Inner field: null if plain, non-null if password
        private final JPasswordField passInner;

        public PanelBackedField(String placeholder, boolean isPassword) {
            this.placeholder = placeholder;
            this.isPassword  = isPassword;

            setOpaque(false);           // we paint ourselves
            setBackground(BG_FIELD);
            setForeground(TEXT_SECONDARY);
            setCaretColor(ACCENT);
            setFont(FONT_BODY);
            setBorder(new EmptyBorder(6, 12, 6, 12));
            setText(placeholder);

            if (isPassword) {
                passInner = new JPasswordField();
                passInner.setEchoChar((char) 0);   // plain text until focus
                passInner.setOpaque(false);
                passInner.setBackground(new Color(0,0,0,0));
                passInner.setForeground(TEXT_SECONDARY);
                passInner.setCaretColor(ACCENT);
                passInner.setFont(FONT_BODY);
                passInner.setBorder(new EmptyBorder(0,0,0,0));
                passInner.setText(placeholder);
                passInner.addFocusListener(new FocusAdapter() {
                    public void focusGained(FocusEvent e) {
                        focused = true;
                        if (new String(passInner.getPassword()).equals(placeholder)) {
                            passInner.setText("");
                            passInner.setForeground(TEXT_PRIMARY);
                            passInner.setEchoChar('●');
                        }
                        repaint();
                    }
                    public void focusLost(FocusEvent e) {
                        focused = false;
                        if (new String(passInner.getPassword()).isEmpty()) {
                            passInner.setEchoChar((char)0);
                            passInner.setText(placeholder);
                            passInner.setForeground(TEXT_SECONDARY);
                        }
                        repaint();
                    }
                });
            } else {
                passInner = null;
                addFocusListener(new FocusAdapter() {
                    public void focusGained(FocusEvent e) {
                        focused = true;
                        if (getText().equals(placeholder)) {
                            setText("");
                            setForeground(TEXT_PRIMARY);
                        }
                        repaint();
                    }
                    public void focusLost(FocusEvent e) {
                        focused = false;
                        if (getText().isEmpty()) {
                            setText(placeholder);
                            setForeground(TEXT_SECONDARY);
                        }
                        repaint();
                    }
                });
            }
        }

        /** Returns the underlying field (for passField callers needing getPassword()) */
        public JTextField getActualField() {
            return passInner != null ? passInner : this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Rounded dark background
            g2.setColor(BG_FIELD);
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
            // Border color changes on focus
            g2.setColor(focused ? BORDER_FOCUS : BORDER);
            g2.setStroke(new BasicStroke(focused ? 1.8f : 1.2f));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
            g2.dispose();
            // Now let the text/caret render on top
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            // Suppress default border — we draw it in paintComponent
        }
    }

    // ── JButton factory ────────────────────────────────────────
    public static JButton button(String text, Color bg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = !isEnabled() ? new Color(50,70,54)
                        : getModel().isRollover() ? bg.brighter() : bg;
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
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton iconButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(BORDER); g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
                }
                g2.setColor(getModel().isRollover() ? TEXT_PRIMARY : TEXT_MUTED);
                g2.setFont(FONT_BODY);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth()  - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(26, 22));
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ── Card panel ─────────────────────────────────────────────
    public static JPanel card(String title) {
        JPanel p = new JPanel(new BorderLayout(0, 12)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD); g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(BORDER); g2.setStroke(new BasicStroke(0.8f));
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

    // ── Label ──────────────────────────────────────────────────
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
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
        c.gridx=0; c.fill=GridBagConstraints.HORIZONTAL; c.weightx=1;

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(FONT_SMALL); titleLbl.setForeground(TEXT_SECONDARY);
        c.gridy=0; c.insets=new Insets(12,14,2,14); p.add(titleLbl,c);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI",Font.BOLD,26)); valLbl.setForeground(accent);
        c.gridy=1; c.insets=new Insets(0,14,2,14); p.add(valLbl,c);

        JLabel unitLbl = new JLabel(unit);
        unitLbl.setFont(FONT_SMALL); unitLbl.setForeground(TEXT_MUTED);
        c.gridy=2; c.insets=new Insets(0,14,14,14); p.add(unitLbl,c);

        return p;
    }

    // ── Rounded border (for non-field use) ─────────────────────
    public static class RoundBorder extends AbstractBorder {
        private final int r; private final Color border, fill;
        public RoundBorder(int r, Color border, Color fill) { this.r=r; this.border=border; this.fill=fill; }
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

    // ── ScrollPane ─────────────────────────────────────────────
    public static JScrollPane scroll(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBackground(BG_DARK); sp.getViewport().setBackground(BG_DARK);
        sp.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        sp.getVerticalScrollBar().setUnitIncrement(12);
        return sp;
    }

    // ── JTable ─────────────────────────────────────────────────
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

    // ── JComboBox ──────────────────────────────────────────────
    public static void styleCombo(JComboBox<?> c) {
        c.setBackground(BG_FIELD); c.setForeground(TEXT_PRIMARY);
        c.setFont(FONT_BODY);
        c.setPreferredSize(new Dimension(200, 36));
    }

    // ── Dark JPanel ────────────────────────────────────────────
    public static JPanel darkPanel(LayoutManager lm) {
        JPanel p = new JPanel(lm) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BG_DARK); g.fillRect(0,0,getWidth(),getHeight());
            }
        };
        p.setOpaque(true); return p;
    }
}