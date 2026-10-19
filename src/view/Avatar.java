package view;

import javax.swing.*;
import java.awt.*;

/** Round picture with the first letter of a name. */
public class Avatar extends JComponent {
    private final int size;
    private final Color background;
    private final Color foreground;
    private String initial = "?";

    public Avatar(int size, Color background, Color foreground) {
        this.size = size;
        this.background = background;
        this.foreground = foreground;
        Dimension d = new Dimension(size, size);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
    }

    public void setInitial(String initial) {
        this.initial = initial;
        repaint();
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(background);
        g2.fillOval(0, 0, size - 1, size - 1);
        g2.setColor(foreground);
        g2.setFont(Theme.font(Font.BOLD, size / 2));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(initial, (size - fm.stringWidth(initial)) / 2, (size - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}
