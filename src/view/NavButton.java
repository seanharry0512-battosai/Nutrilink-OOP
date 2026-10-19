package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Sidebar item: icon + text, highlighted when active, with an optional red number badge. */
public class NavButton extends JButton {
    private boolean active;
    private int badge;

    public NavButton(String text, Icon icon) {
        super(text, icon);
        setFont(Theme.font(Font.PLAIN, 15));
        setForeground(Color.WHITE);
        setIconTextGap(18);
        setHorizontalAlignment(SwingConstants.LEFT);
        setContentAreaFilled(false);
        setOpaque(false);
        setFocusPainted(false);
        setBorder(new EmptyBorder(8, 20, 8, 10));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        setPreferredSize(new Dimension(250, 44));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void setActive(boolean active) {
        this.active = active;
        repaint();
    }

    public void setBadge(int badge) {
        this.badge = badge;
        repaint();
    }

    @Override protected void paintComponent(Graphics g) {
        if (active) {
            g.setColor(Theme.DARK_GREEN);
            g.fillRect(0, 0, getWidth(), getHeight());
        } else if (getModel().isRollover()) {
            g.setColor(new Color(0x23925A));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        super.paintComponent(g);
        if (badge > 0) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            String text = badge > 9 ? "9+" : String.valueOf(badge);
            g2.setFont(Theme.font(Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            int w = Math.max(22, fm.stringWidth(text) + 12), h = 22;
            int x = getWidth() - w - 18, y = (getHeight() - h) / 2;
            g2.setColor(Theme.STATUS_RED);
            g2.fillRoundRect(x, y, w, h, h, h);
            g2.setColor(Color.WHITE);
            g2.drawString(text, x + (w - fm.stringWidth(text)) / 2, y + (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}
