package view;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

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

    // one color per meaning, used by the status pills, the chart and the notifications
    public static final Color STATUS_GREEN = new Color(0x1E8449);    // Available / Completed / Ready / donated
    public static final Color STATUS_BLUE = new Color(0x2B7BBF);     // Collected / Ongoing
    public static final Color STATUS_VIOLET = new Color(0x7B3FBF);   // Distributed
    public static final Color STATUS_AMBER = new Color(0xE69500);    // Pending / Urgent
    public static final Color STATUS_RED = new Color(0xD32F2F);      // Expired / danger

    public static final Color PANEL = new Color(0xF3F4F6);
    public static final Color TEXT_GRAY = new Color(0x6B6B6B);

    private static final String FAMILY = pickFamily();

    private Theme() { }

    /** Segoe UI on Windows, otherwise the default sans-serif font. */
    private static String pickFamily() {
        try {
            for (String name : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
                if (name.equals("Segoe UI")) return name;
            }
        } catch (Exception ignored) { }
        return Font.SANS_SERIF;
    }

    public static Font font(int style, int size) { return new Font(FAMILY, style, size); }

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
    public static RoundButton danger(String text) { return new RoundButton(text, STATUS_RED, Color.WHITE); }

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

    /** A vertical list of rows inside a scroll pane (used by every list page). */
    public static JScrollPane listScroll(JPanel rows) {
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setBackground(Color.WHITE);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(rows, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    /** White rounded card with padding. */
    public static JPanel card(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(new Color(0xC9CED6), 10), new EmptyBorder(14, 18, 16, 18)));
        return p;
    }

    // ---- images ----
    /** Looks for the picture on the classpath (/images/name) and then in resources/images/ next to the program. */
    public static BufferedImage loadImage(String name) {
        try {
            URL url = Theme.class.getResource("/images/" + name);
            if (url != null) return ImageIO.read(url);
            for (String dir : new String[]{"resources/images/", "../resources/images/"}) {
                File f = new File(dir + name);
                if (f.isFile()) return ImageIO.read(f);
            }
        } catch (IOException ignored) { }
        return null;
    }

    /** Uses resources/images/logo.png if it exists, otherwise a text logo. */
    public static JComponent logo(int size) {
        BufferedImage img = loadImage("logo.png");
        JLabel l;
        if (img != null) {
            l = new JLabel(new ImageIcon(img.getScaledInstance(-1, size * 2, Image.SCALE_SMOOTH)));
        } else {
            l = new JLabel("<html><span style='color:#15693A'>Nutri</span><span style='color:#1E8449'>Link</span></html>");
            l.setFont(font(Font.BOLD, size));
        }
        l.setHorizontalAlignment(SwingConstants.CENTER);
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
            Color fill = base;
            if (!isEnabled()) fill = Color.LIGHT_GRAY;
            else if (getModel().isPressed()) fill = base.darker();
            else if (getModel().isRollover()) fill = new Color((base.getRed() * 85 + 255 * 15) / 100,
                    (base.getGreen() * 85 + 255 * 15) / 100, (base.getBlue() * 85 + 255 * 15) / 100);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
