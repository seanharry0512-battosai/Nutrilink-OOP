package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/** A compact outlined button styled for list and action rows. */
public class OutlineButton extends JButton {
    private final Color borderColor;
    private final Color textColor;
    private final Color hoverFill;

    public OutlineButton(String text, Color borderColor, Color textColor, Color hoverFill) {
        super(text);
        this.borderColor = borderColor;
        this.textColor = textColor;
        this.hoverFill = hoverFill;
        setForeground(textColor);
        setFont(Theme.font(Font.BOLD, 13));
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fill = Color.WHITE;
        if (getModel().isPressed()) fill = hoverFill.darker();
        else if (getModel().isRollover()) fill = hoverFill;
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

        g2.setColor(borderColor);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        g2.dispose();

        super.paintComponent(g);
    }
}
