package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** About Us page: what NutriLink is, how it works, who it is for and who made it. */
public class AboutView extends JPanel {
    // ================= EDIT THESE: your group's details =================
    private static final String[][] TEAM = {
            {"Member Name 1", "Role / what they worked on"},
            {"Member Name 2", "Role / what they worked on"},
            {"Member Name 3", "Role / what they worked on"}};
    private static final String SCHOOL = "Technological Institute of the Philippines - Quezon City";
    private static final String COURSE = "Advanced Object-Oriented Programming";
    private static final String TERM = "First Semester, S.Y. 2026-2027";
    private static final String VERSION = "Version 1.0";
    // =====================================================================

    private static final int TEXT_WIDTH = 640;   // width of paragraphs in pixels

    private static final String MISSION = "Every day, good food is thrown away while families in our communities go hungry. "
            + "NutriLink is a community food rescue and distribution system that makes it easier to share surplus food "
            + "before it goes to waste. Our work supports the United Nations Sustainable Development Goal 2: Zero Hunger.";

    private static final String ACCESS = "We designed NutriLink to be easy to use for everyone, including persons with disabilities: "
            + "readable fonts, clear buttons and labels, simple navigation, strong color contrast, and clear error messages.";

    public AboutView() {
        setLayout(new BorderLayout(0, 14));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(22, 28, 22, 28));
        add(Theme.label("About Us", 34, Font.BOLD, Color.BLACK), BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        int y = 0;
        body.add(heroCard(), Theme.row(y++, 0));
        body.add(section("Our Mission", paragraph(MISSION)), Theme.row(y++, 16));
        body.add(section("How NutriLink Works", stepsRow()), Theme.row(y++, 16));
        body.add(section("Who It Is For", rolesList()), Theme.row(y++, 16));
        body.add(section("Built for Everyone", paragraph(ACCESS)), Theme.row(y++, 16));
        body.add(section("Meet the Team", teamRow()), Theme.row(y++, 16));
        body.add(footer(), Theme.row(y++, 18));

        JPanel wrapper = new ScrollBody();
        wrapper.add(body, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    // ---------------- building blocks ----------------
    private static JPanel heroCard() {
        JPanel card = Theme.card(new GridBagLayout());
        card.add(Theme.label("NutriLink", 38, Font.BOLD, Theme.GREEN), Theme.row(0, 0));
        card.add(Theme.label("Connecting surplus food with the people who need it.", 18, Font.PLAIN, Color.BLACK), Theme.row(1, 4));
        card.add(Theme.label("Community Food Rescue and Distribution System", 14, Font.PLAIN, Theme.MUTED), Theme.row(2, 4));
        return card;
    }

    private static JPanel section(String title, JComponent content) {
        JPanel card = Theme.card(new GridBagLayout());
        card.add(Theme.label(title, 20, Font.BOLD, Color.BLACK), Theme.row(0, 0));
        card.add(content, Theme.row(1, 10));
        return card;
    }

    /** Wrapped, read-only text of a fixed width (the height is worked out from the real font). */
    private static JComponent text(String text, int width, int size, Color color) {
        JTextArea a = new JTextArea(text);
        a.setEditable(false);
        a.setFocusable(false);
        a.setOpaque(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBorder(null);
        a.setFont(Theme.font(Font.PLAIN, size));
        a.setForeground(color);
        a.setSize(width, Short.MAX_VALUE);                       // lets the text wrap at this width
        a.setPreferredSize(new Dimension(width, a.getPreferredSize().height));
        JPanel holder = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));   // keeps it from stretching
        holder.setOpaque(false);
        holder.add(a);
        return holder;
    }

    private static JComponent paragraph(String text) { return text(text, TEXT_WIDTH, 15, Theme.TEXT_GRAY); }

    private static JPanel stepsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.add(step(1, "Donate", "Food donors list surplus food with the quantity, pickup location and distribution deadline."));
        row.add(step(2, "Approve", "Coordinators review each donation and make sure it is ready to be shared."));
        row.add(step(3, "Collect", "Volunteers pick up the food, and every collection is tracked in the system."));
        row.add(step(4, "Distribute", "The food reaches registered beneficiaries, and every distribution is recorded."));
        return row;
    }

    private static JPanel step(int number, String title, String description) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(new Theme.RoundedBorder(Theme.BORDER, 10), new EmptyBorder(12, 12, 12, 12)));
        Avatar badge = new Avatar(34, Theme.GREEN, Color.WHITE);
        badge.setInitial(String.valueOf(number));
        JComponent text = text(description, 120, 13, Theme.TEXT_GRAY);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.anchor = GridBagConstraints.NORTHWEST; c.weightx = 1; c.fill = GridBagConstraints.NONE;
        c.gridy = 0; p.add(badge, c);
        c.gridy = 1; c.insets = new Insets(10, 0, 0, 0); p.add(Theme.label(title, 16, Font.BOLD, Color.BLACK), c);
        c.gridy = 2; c.insets = new Insets(4, 0, 0, 0); c.weighty = 1; p.add(text, c);
        return p;
    }

    private static JPanel rolesList() {
        JPanel list = new JPanel(new GridBagLayout());
        list.setOpaque(false);
        String[][] roles = {
                {"Food Donors", "Restaurants, bakeries, grocery stores and school canteens with food to share."},
                {"Coordinators", "Community leaders who manage donations, assign volunteers and track progress."},
                {"Volunteers", "People who collect donated food and bring it to those in need."},
                {"Beneficiaries", "Individuals and families who receive the food."}};
        for (int i = 0; i < roles.length; i++) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridy = i; c.anchor = GridBagConstraints.NORTHWEST; c.insets = new Insets(i == 0 ? 0 : 8, 0, 0, 0);
            c.gridx = 0;
            JLabel name = Theme.label(roles[i][0], 15, Font.BOLD, Theme.GREEN);
            name.setPreferredSize(new Dimension(125, name.getPreferredSize().height));
            list.add(name, c);
            c.gridx = 1; c.weightx = 1;
            list.add(text(roles[i][1], 500, 15, Theme.TEXT_GRAY), c);
        }
        return list;
    }

    private static JPanel teamRow() {
        JPanel row = new JPanel(new GridLayout(1, TEAM.length, 12, 0));
        row.setOpaque(false);
        for (String[] member : TEAM) {
            JPanel p = new JPanel(new GridBagLayout());
            p.setBackground(Color.WHITE);
            p.setBorder(BorderFactory.createCompoundBorder(new Theme.RoundedBorder(Theme.BORDER, 10), new EmptyBorder(16, 12, 16, 12)));
            Avatar avatar = new Avatar(64, Theme.GREEN, Color.WHITE);
            avatar.setInitial(member[0].isEmpty() ? "?" : member[0].substring(0, 1).toUpperCase());
            JLabel role = new JLabel("<html><body style='width:150px; text-align:center'>" + member[1] + "</body></html>");
            role.setFont(Theme.font(Font.PLAIN, 13));
            role.setForeground(Theme.MUTED);

            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0; p.add(avatar, c);
            c.gridy = 1; c.insets = new Insets(10, 0, 0, 0); p.add(Theme.label(member[0], 16, Font.BOLD, Color.BLACK), c);
            c.gridy = 2; c.insets = new Insets(4, 0, 0, 0); p.add(role, c);
            row.add(p);
        }
        return row;
    }

    private static JPanel footer() {
        JPanel p = new JPanel(new GridLayout(3, 1, 0, 4));
        p.setOpaque(false);
        for (String line : new String[]{SCHOOL, COURSE + "  |  " + TERM, "NutriLink " + VERSION}) {
            JLabel l = Theme.label(line, 13, Font.PLAIN, Theme.MUTED);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            p.add(l);
        }
        return p;
    }

    /** The scrolled content: always as wide as the window, so only up/down scrolling is needed. */
    private static class ScrollBody extends JPanel implements Scrollable {
        ScrollBody() {
            super(new BorderLayout());
            setBackground(Color.WHITE);
        }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) {
            return orientation == SwingConstants.VERTICAL ? Math.max(16, r.height - 32) : r.width;
        }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }
}
