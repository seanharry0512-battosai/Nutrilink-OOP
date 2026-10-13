package view;

import model.Donation;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.io.File;

/** Shared colors, fonts and small styled components so every screen looks the same. */
public final class Theme {
    public static final Color GREEN = new Color(0x1E8449);
    public static final Color DARK_GREEN = new Color(0x15693A);
    public static final Color ORANGE = new Color(0xFFA726);
    public static final Color LIGHT_GRAY = new Color(0xD9D9D9);
    public static final Color BORDER = new Color(0x8A97A6);
    public static final Color PURPLE = new Color(0x7C3AED);
    public static final Color MUTED = new Color(0x8C8C8C);
    public static final Color LINK_BLUE = new Color(0x0B4FA8);

    private Theme() { }

    public static Font font(int style, int size) { return new Font("Segoe UI", style, size); }

    public static JLabel label(String text, int size, int style, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font(style, size));
        l.setForeground(color);
        return l;
    }

    public static JLabel fieldLabel(String text) { return label(text, 14, Font.BOLD, Color.BLACK); }

    public static JLabel link(String text, Color color) {
        JLabel l = label("<html><u>" + text + "</u></html>", 13, Font.BOLD, color);
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return l;
    }

    public static RoundButton button(String text, Color bg, Color fg) { return new RoundButton(text, bg, fg); }
    public static RoundButton primary(String text) { return new RoundButton(text, GREEN, Color.WHITE); }

    public static void styleField(JComponent c) {
        c.setFont(font(Font.PLAIN, 14));
        c.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(BORDER, 10), new EmptyBorder(6, 10, 6, 10)));
        c.setPreferredSize(new Dimension(200, 40));
    }

    public static GridBagConstraints row(int y, int top) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = y; c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(top, 0, 0, 0);
        return c;
    }

    /** Uses resources/images/logo.png if it exists, otherwise a text logo. */
    public static JComponent logo(int size) {
        File f = new File("resources/images/logo.png");
        JLabel l;
        if (f.exists()) {
            Image img = new ImageIcon(f.getPath()).getImage().getScaledInstance(-1, size * 2, Image.SCALE_SMOOTH);
            l = new JLabel(new ImageIcon(img));
        } else {
            l = new JLabel("<html><span style='color:#15693A'>Nutri</span><span style='color:#1E8449'>Link</span></html>");
            l.setFont(font(Font.BOLD, size));
        }
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    // ---- status colors ----
    public static Color statusFg(String status) {
        switch (status) {
            case Donation.AVAILABLE: return new Color(0xD93025);
            case Donation.COLLECTED: return new Color(0xA13DE0);
            case Donation.DISTRIBUTED: return new Color(0x0B3FB8);
            default: return new Color(0xE69500);
        }
    }

    public static Color statusBg(String status) {
        switch (status) {
            case Donation.AVAILABLE: return new Color(0xFFB3B3);
            case Donation.COLLECTED: return new Color(0xEBC8F7);
            case Donation.DISTRIBUTED: return new Color(0xB3A6F2);
            default: return new Color(0xFFD9A0);
        }
    }

    public static JLabel pill(String status) {
        JLabel l = new JLabel(status, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(statusBg(getText()));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setOpaque(false);
        l.setFont(font(Font.BOLD, 14));
        l.setForeground(statusFg(status));
        l.setPreferredSize(new Dimension(130, 32));
        return l;
    }

    static void paintHint(JTextComponent c, Graphics g, String hint) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(MUTED);
        g2.setFont(c.getFont());
        Insets in = c.getInsets();
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(hint, in.left, (c.getHeight() - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }

    // ---- custom components ----
    public static class HintField extends JTextField {
        private final String hint;
        public HintField(String hint) { this.hint = hint; styleField(this); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) paintHint(this, g, hint);
        }
    }

    public static class HintPassword extends JPasswordField {
        private final String hint;
        public HintPassword(String hint) { this.hint = hint; styleField(this); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getPassword().length == 0) paintHint(this, g, hint);
        }
    }

    public static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;
        public RoundedBorder(Color color, int radius) { this.color = color; this.radius = radius; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, w - 1, h - 1, radius * 2, radius * 2);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(radius / 2, radius / 2, radius / 2, radius / 2); }
    }

    public static class RoundButton extends JButton {
        private final Color base;
        public RoundButton(String text, Color bg, Color fg) {
            super(text);
            this.base = bg;
            setForeground(fg);
            setFont(font(Font.BOLD, 14));
            setContentAreaFilled(false);
            setOpaque(false);
            setFocusPainted(false);
            setBorder(new EmptyBorder(9, 18, 9, 18));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(!isEnabled() ? Color.LIGHT_GRAY : getModel().isPressed() ? base.darker() : base);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
