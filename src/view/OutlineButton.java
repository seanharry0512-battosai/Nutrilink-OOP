package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Pill button with a border and a fill color (View, Assign Volunteer...). */
public class OutlineButton extends JButton {
    private final Color border;
    private final Color fill;

    public OutlineButton(String text, Color border, Color fg, Color fill) {
        super(text);
        this.border = border;
        this.fill = fill;
        setForeground(fg);
        setFont(Theme.font(Font.BOLD, 14));
        setContentAreaFilled(false);
        setOpaque(false);
        setFocusPainted(false);
        setBorder(new EmptyBorder(9, 22, 9, 22));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(!isEnabled() ? new Color(0xF0F0F0) : getModel().isPressed() ? fill.darker() : fill);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
        g2.setColor(isEnabled() ? border : new Color(0xCCCCCC));
        g2.setStroke(new BasicStroke(1.6f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
        g2.dispose();
        super.paintComponent(g);
    }
}