package view;

import javax.swing.*;
import java.awt.*;

/** The bell on the dashboard with a red badge showing how many notifications are unread. */
public class BellButton extends JButton {
    private int count;

    public BellButton() {
        super(new Icons.BellIcon(Color.BLACK));
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setToolTipText("Notifications");
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void setCount(int count) {
        this.count = count;
        repaint();
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (count <= 0) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        String text = count > 9 ? "9+" : String.valueOf(count);
        g2.setFont(Theme.font(Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        int w = Math.max(18, fm.stringWidth(text) + 10), h = 18;
        int x = getWidth() - w - 1, y = 1;
        g2.setColor(Theme.STATUS_RED);
        g2.fillRoundRect(x, y, w, h, h, h);
        g2.setColor(Color.WHITE);
        g2.drawString(text, x + (w - fm.stringWidth(text)) / 2, y + (h - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}
