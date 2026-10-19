package view;

import model.Donation;

import javax.swing.*;
import java.awt.*;

/** Outlined, rounded status label (Available, Collected, Ongoing, Ready...) like in the mockups. */
public class StatusPill extends JLabel {

    public StatusPill(String status) {
        super(status, SwingConstants.CENTER);
        setOpaque(false);
        setFont(Theme.font(Font.BOLD, 13));
        setPreferredSize(new Dimension(112, 30));
        setStatus(status);
    }

    public final void setStatus(String status) {
        setText(status);
        setForeground(colorFor(status));
        repaint();
    }

    /** The one place that decides which color means what. */
    public static Color colorFor(String status) {
        return switch (status) {
            case Donation.AVAILABLE, Donation.COL_COMPLETED, "Ready" -> Theme.STATUS_GREEN;
            case Donation.COLLECTED, Donation.COL_ONGOING -> Theme.STATUS_BLUE;
            case Donation.DISTRIBUTED -> Theme.STATUS_VIOLET;
            case "Expired" -> Theme.STATUS_RED;
            default -> Theme.STATUS_AMBER;   // Pending, Urgent
        };
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getForeground());
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, getHeight(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }
}
