package view;

import model.Notification;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/** Notifications page: All / Unread tabs, a card for each message, mark all as read, clear all. */
public class NotificationsView extends JPanel {
    private final JLabel subtitle = Theme.label("", 14, Font.PLAIN, Theme.MUTED);
    private final OutlineButton markAllButton = new OutlineButton("Mark all as read", Theme.GREEN, Theme.GREEN, Color.WHITE);
    private final OutlineButton clearButton = new OutlineButton("Clear all", new Color(0x9CA3AF), Theme.TEXT_GRAY, Color.WHITE);
    private final JLabel[] tabs = {Theme.label("All", 16, Font.BOLD, Theme.TEXT_GRAY), Theme.label("Unread", 16, Font.BOLD, Theme.TEXT_GRAY)};
    private final JPanel list = new JPanel();

    private List<Notification> all = new ArrayList<>();
    private boolean unreadOnly;
    private Consumer<Notification> onClick;

    public NotificationsView() {
        setLayout(new BorderLayout(0, 14));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(22, 28, 22, 28));

        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        titles.add(Theme.label("Notifications", 34, Font.BOLD, Color.BLACK));
        titles.add(subtitle);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(markAllButton);
        buttons.add(clearButton);
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(titles, BorderLayout.WEST);
        titleRow.add(buttons, BorderLayout.EAST);

        JPanel tabRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabRow.setOpaque(false);
        for (int i = 0; i < tabs.length; i++) {
            final boolean unread = (i == 1);
            tabs[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tabs[i].addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { unreadOnly = unread; rebuild(); }
            });
            if (i > 0) tabRow.add(Box.createHorizontalStrut(30));
            tabRow.add(tabs[i]);
        }

        JPanel north = new JPanel(new GridBagLayout());
        north.setOpaque(false);
        north.add(titleRow, Theme.row(0, 0));
        north.add(tabRow, Theme.row(1, 14));
        add(north, BorderLayout.NORTH);
        add(Theme.listScroll(list), BorderLayout.CENTER);
        rebuild();
    }

    // ---------------- API for the controller ----------------
    public JButton getMarkAllButton() { return markAllButton; }
    public JButton getClearButton() { return clearButton; }
    public void setOnClick(Consumer<Notification> onClick) { this.onClick = onClick; }

    public void setNotifications(List<Notification> notifications) {
        this.all = new ArrayList<>(notifications);
        rebuild();
    }

    // ---------------- internals ----------------
    private void rebuild() {
        int unread = 0;
        for (Notification n : all) if (!n.isRead()) unread++;
        subtitle.setText(unread == 0 ? "You're all caught up." : unread + " unread notification" + (unread == 1 ? "" : "s"));
        markAllButton.setEnabled(unread > 0);
        clearButton.setEnabled(!all.isEmpty());
        tabs[0].setText("All (" + all.size() + ")");
        tabs[1].setText("Unread (" + unread + ")");
        for (int i = 0; i < tabs.length; i++) {
            boolean selected = (i == 1) == unreadOnly;
            tabs[i].setForeground(selected ? Theme.GREEN : Theme.TEXT_GRAY);
            tabs[i].setBorder(selected ? BorderFactory.createMatteBorder(0, 0, 3, 0, Theme.GREEN) : new EmptyBorder(0, 0, 3, 0));
        }

        list.removeAll();
        int shown = 0;
        for (Notification n : all) {
            if (unreadOnly && n.isRead()) continue;
            list.add(buildCard(n));
            list.add(Box.createVerticalStrut(8));
            shown++;
        }
        if (shown == 0) {
            JLabel empty = Theme.label(unreadOnly ? "No unread notifications." : "No notifications yet.", 16, Font.PLAIN, Theme.MUTED);
            empty.setBorder(new EmptyBorder(24, 6, 0, 0));
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            list.add(empty);
        }
        list.revalidate();
        list.repaint();
    }

    private JPanel buildCard(Notification n) {
        Color accent = switch (n.getType()) {
            case SUCCESS -> Theme.STATUS_GREEN;
            case WARNING -> Theme.STATUS_AMBER;
            default -> Theme.STATUS_BLUE;
        };
        JPanel card = new JPanel(new BorderLayout(14, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(n.isRead() ? Color.WHITE : new Color(0xF2F9F5));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(new Color(0xC9CED6));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 8, getHeight() - 1, 8, 8);
                g2.fillRect(4, 0, 4, getHeight() - 1);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10, 22, 10, 16));
        card.setPreferredSize(new Dimension(100, 74));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 2));
        text.setOpaque(false);
        text.add(Theme.label(n.getTitle(), 16, n.isRead() ? Font.PLAIN : Font.BOLD, Color.BLACK));
        text.add(Theme.label(n.getMessage(), 14, Font.PLAIN, Theme.TEXT_GRAY));

        JPanel when = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        when.setOpaque(false);
        when.add(Theme.label(timeAgo(n.getCreatedAt()), 12, Font.PLAIN, Theme.MUTED));
        if (!n.isRead()) when.add(new Dot(Theme.STATUS_RED));

        card.add(text, BorderLayout.CENTER);
        card.add(when, BorderLayout.EAST);
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (onClick != null) onClick.accept(n); }
        });
        return card;
    }

    static String timeAgo(LocalDateTime t) {
        Duration d = Duration.between(t, LocalDateTime.now());
        long minutes = Math.max(0, d.toMinutes());
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " min ago";
        if (minutes < 60 * 24) return (minutes / 60) + " h ago";
        if (minutes < 60 * 48) return "Yesterday";
        return t.format(DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH));
    }

    private static class Dot extends JComponent {
        private final Color color;
        Dot(Color color) {
            this.color = color;
            setPreferredSize(new Dimension(10, 16));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(0, 4, 9, 9);
            g2.dispose();
        }
    }
}
