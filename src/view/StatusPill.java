package view;

import model.Donation;

import javax.swing.*;
import java.awt.*;

/** Outlined, rounded status label (Available, Collected, Ongoing, Ready...) like in the mockups. */
public class StatusPill extends JLabel {
    private static final Color GREEN_PILL = new Color(0x1E8449);
    private static final Color BLUE_PILL = new Color(0x2B7BBF);
    private static final Color PURPLE_PILL = new Color(0x7B3FBF);
    private static final Color ORANGE_PILL = new Color(0xE69500);

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

    public static Color colorFor(String status) {
        return switch (status) {
            case Donation.AVAILABLE, Donation.COL_COMPLETED, "Ready" -> GREEN_PILL;
            case Donation.COLLECTED, Donation.COL_ONGOING -> BLUE_PILL;
            case Donation.DISTRIBUTED -> PURPLE_PILL;
            default -> ORANGE_PILL;   // Pending
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