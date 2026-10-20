package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** The green menu on the left: pages, notifications, settings, the logged-in user and logout. */
public class Sidebar extends JPanel {
    private static final String LOGOUT = "Logout";

    private final Map<String, NavButton> buttons = new LinkedHashMap<>();   // every page button
    private final NavButton logoutButton = new NavButton(LOGOUT, new Icons.NavIcon(LOGOUT, new Color(0xFF6B60)));
    private final Avatar avatar = new Avatar(38, Color.WHITE, Theme.GREEN);
    private final JLabel nameLabel = Theme.label("", 14, Font.BOLD, Color.WHITE);
    private final JLabel roleLabel = Theme.label("", 12, Font.PLAIN, new Color(0xCFE8D8));
    private final ProfileChip profileChip = new ProfileChip();
    private final AboutItem aboutItem = new AboutItem();

    public Sidebar() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Theme.GREEN);
        setPreferredSize(new Dimension(250, 0));
        // thin light strip on the left edge, like the mockup
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 14, 0, 0, new Color(0x8DB89E)),
                new EmptyBorder(18, 0, 14, 0)));

        JLabel logo = new JLabel("NutriLink", new Icons.LeafIcon(), SwingConstants.LEFT);
        logo.setFont(Theme.font(Font.BOLD, 26));
        logo.setForeground(Color.WHITE);
        logo.setIconTextGap(8);
        logo.setBorder(new EmptyBorder(0, 20, 20, 0));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(logo);

        for (String page : DashboardView.NAV_PAGES) addButton(page);
        add(Box.createVerticalGlue());
        addButton(DashboardView.NOTIFICATIONS);
        addButton(DashboardView.SETTINGS);
        add(aboutItem);
        add(profileChip);
        add(logoutButton);
    }

    private void addButton(String page) {
        NavButton b = new NavButton(page, new Icons.NavIcon(page, Color.WHITE));
        buttons.put(page, b);
        add(b);
    }

    // ---------------- API for DashboardView / controller ----------------
    public NavButton getButton(String page) { return buttons.get(page); }
    public NavButton getLogoutButton() { return logoutButton; }
    public JComponent getProfileChip() { return profileChip; }
    public JComponent getAboutItem() { return aboutItem; }

    public void setUser(String name, String role) {
        nameLabel.setText(name);
        roleLabel.setText(role);
        avatar.setInitial(name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase());
    }

    /** Shows the user's profile photo in the sidebar (null goes back to the letter). */
    public void setAvatarImage(byte[] imageBytes) { avatar.setImageBytes(imageBytes); }

    /** Shows only the given pages (a donor, for example, does not see Inventory). */
    public void setVisiblePages(Set<String> pages) {
        for (Map.Entry<String, NavButton> e : buttons.entrySet()) e.getValue().setVisible(pages.contains(e.getKey()));
        revalidate();
        repaint();
    }

    public void setActive(String page) {
        for (Map.Entry<String, NavButton> e : buttons.entrySet()) e.getValue().setActive(e.getKey().equals(page));
        profileChip.setActive(DashboardView.PROFILE.equals(page));
        aboutItem.setActive(DashboardView.ABOUT.equals(page));
    }

    public void setUnread(int count) { buttons.get(DashboardView.NOTIFICATIONS).setBadge(count); }

    /** "About Us" menu row: an info icon and text, drawn like the other menu items. Clicking it opens the About page. */
    private class AboutItem extends JComponent {
        private boolean active;
        private boolean hover;

        AboutItem() {
            setPreferredSize(new Dimension(250, 44));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("About NutriLink");
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        void setActive(boolean active) {
            this.active = active;
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (active || hover) {
                g2.setColor(active ? Theme.DARK_GREEN : new Color(0x23925A));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            int cy = getHeight() / 2;
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(20, cy - 11, 22, 22);              // circle
            g2.fillOval(30, cy - 7, 3, 3);                 // dot of the "i"
            g2.drawLine(31, cy - 2, 31, cy + 6);           // stem of the "i"
            g2.setFont(Theme.font(Font.PLAIN, 15));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString("About Us", 56, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Avatar + name + role. Clicking it opens the Profile page. */
    private class ProfileChip extends JPanel {
        private boolean active;
        private boolean hover;

        ProfileChip() {
            super(new BorderLayout(12, 0));
            setOpaque(false);
            setBorder(new EmptyBorder(9, 20, 9, 12));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
            setPreferredSize(new Dimension(250, 58));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Open my profile");

            JPanel text = new JPanel(new GridLayout(2, 1));
            text.setOpaque(false);
            text.add(nameLabel);
            text.add(roleLabel);
            add(avatar, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        void setActive(boolean active) {
            this.active = active;
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            if (active || hover) {
                g.setColor(active ? Theme.DARK_GREEN : new Color(0x23925A));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            super.paintComponent(g);
        }
    }
}
