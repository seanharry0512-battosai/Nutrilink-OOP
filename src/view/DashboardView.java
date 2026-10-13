package view;

import model.Donation;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The logged-in screen: green sidebar + a content area that switches pages. */
public class DashboardView extends JPanel {
    public static final String HOME = "Dashboard";
    public static final String DONATIONS = "Donations";
    public static final String INVENTORY = "Inventory";
    public static final String COLLECTION = "Collection";
    public static final String DISTRIBUTION = "Distribution";
    public static final String BENEFICIARIES = "Beneficiaries";
    public static final String REPORTS = "Reports";
    public static final String ADD = "AddDonation";
    public static final String[] NAV_PAGES = {HOME, DONATIONS, INVENTORY, COLLECTION, DISTRIBUTION, BENEFICIARIES, REPORTS};

    private static final String[] DONATION_COLUMNS = {"ID", "Food", "Type", "Quantity", "Donor", "Pickup Location", "Deadline", "Status"};

    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private final JButton logoutButton = navButton("Logout");
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);

    private final JLabel welcomeLabel = Theme.label("Welcome!", 32, Font.BOLD, Color.BLACK);
    private final JLabel subtitleLabel = Theme.label("Here's an overview of today's activities.", 14, Font.PLAIN, Theme.MUTED);
    private final JButton bellButton = new JButton(new BellIcon());
    private final JLabel[] statValues = {new JLabel("0"), new JLabel("0"), new JLabel("0"), new JLabel("0")};
    private final JPanel[] statCards = new JPanel[4];
    private final JPanel recentList = new JPanel();

    private final JButton addDonationButton = Theme.button("+ Add Donation", Theme.ORANGE, Color.WHITE);
    private final JButton viewDonationsButton = quickButton("View Donations");
    private final JButton manageCollectionButton = quickButton("Manage Collection");
    private final JButton recordDistributionButton = quickButton("Record Distribution");
    private final JButton viewBeneficiariesButton = quickButton("View Beneficiaries");

    private final TablePage donationsPage = new TablePage("Donations", "All food donations recorded in the system.", DONATION_COLUMNS);
    private final TablePage inventoryPage = new TablePage("Inventory", "Food currently available or collected, not yet distributed.", DONATION_COLUMNS);
    private final TablePage collectionPage = new TablePage("Collection", "Approve donations and mark them as collected. Select a row first.", DONATION_COLUMNS);
    private final TablePage distributionPage = new TablePage("Distribution", "Record distribution of collected food. Select a row first.", DONATION_COLUMNS);
    private final TablePage beneficiariesPage = new TablePage("Beneficiaries", "Registered beneficiaries.", new String[]{"Name", "Email", "Contact Number"});
    private final JTextArea reportArea = new JTextArea();
    private final AddDonationView addDonationView = new AddDonationView();

    public DashboardView() {
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);

        content.setBackground(Color.WHITE);
        content.add(buildHome(), HOME);
        content.add(donationsPage, DONATIONS);
        content.add(inventoryPage, INVENTORY);
        content.add(collectionPage, COLLECTION);
        content.add(distributionPage, DISTRIBUTION);
        content.add(beneficiariesPage, BENEFICIARIES);
        content.add(buildReports(), REPORTS);
        content.add(addDonationView, ADD);
        add(content, BorderLayout.CENTER);
        showPage(HOME);
    }

    // ---------- building the screen ----------
    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(Theme.GREEN);
        side.setPreferredSize(new Dimension(230, 0));
        side.setBorder(new EmptyBorder(22, 0, 20, 0));

        JLabel logo = Theme.label("NutriLink", 26, Font.BOLD, Color.WHITE);
        logo.setBorder(new EmptyBorder(0, 24, 26, 0));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(logo);

        for (String page : NAV_PAGES) {
            JButton b = navButton(page);
            navButtons.put(page, b);
            side.add(b);
            side.add(Box.createVerticalStrut(4));
        }
        side.add(Box.createVerticalGlue());
        logoutButton.setForeground(new Color(0xFFD0D0));
        side.add(logoutButton);
        return side;
    }

    private static JButton navButton(String text) {
        JButton b = new JButton(text);
        b.setFont(Theme.font(Font.PLAIN, 16));
        b.setForeground(Color.WHITE);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 24, 10, 10));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static JButton quickButton(String text) {
        JButton b = Theme.button(text, Theme.LIGHT_GRAY, Color.BLACK);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        return b;
    }

    private JPanel buildHome() {
        JPanel home = new JPanel(new BorderLayout(0, 25));
        home.setBackground(Color.WHITE);
        home.setBorder(new EmptyBorder(22, 28, 22, 28));

        bellButton.setBorderPainted(false);
        bellButton.setContentAreaFilled(false);
        bellButton.setFocusPainted(false);
        bellButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        titles.add(welcomeLabel);
        titles.add(subtitleLabel);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(titles, BorderLayout.WEST);
        top.add(bellButton, BorderLayout.EAST);

        String[] captions = {"Total donations", "Collected", "Distributed", "Beneficiaries"};
        JPanel stats = new JPanel(new GridLayout(1, 4, 18, 0));
        stats.setOpaque(false);
        stats.setPreferredSize(new Dimension(0, 95));
        for (int i = 0; i < 4; i++) {
            statCards[i] = statCard(statValues[i], captions[i]);
            stats.add(statCards[i]);
        }

        JPanel north = new JPanel(new BorderLayout(0, 22));
        north.setOpaque(false);
        north.add(top, BorderLayout.NORTH);
        north.add(stats, BorderLayout.CENTER);

        // lower half: recent donations (left) + quick actions (right)
        recentList.setLayout(new BoxLayout(recentList, BoxLayout.Y_AXIS));
        recentList.setOpaque(false);
        JPanel left = new JPanel(new BorderLayout(0, 10));
        left.setOpaque(false);
        left.add(Theme.label("Recent Donations", 22, Font.BOLD, Color.BLACK), BorderLayout.NORTH);
        left.add(recentList, BorderLayout.CENTER);

        JPanel quick = new JPanel(new GridLayout(5, 1, 0, 8));
        quick.setOpaque(false);
        quick.add(addDonationButton);
        quick.add(viewDonationsButton);
        quick.add(manageCollectionButton);
        quick.add(recordDistributionButton);
        quick.add(viewBeneficiariesButton);
        JPanel quickWrap = new JPanel(new BorderLayout());
        quickWrap.setOpaque(false);
        quickWrap.add(quick, BorderLayout.NORTH);
        JPanel right = new JPanel(new BorderLayout(0, 10));
        right.setOpaque(false);
        right.add(Theme.label("Quick Actions", 22, Font.BOLD, Color.BLACK), BorderLayout.NORTH);
        right.add(quickWrap, BorderLayout.CENTER);

        JPanel lower = new JPanel(new GridBagLayout());
        lower.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH; g.weighty = 1; g.gridy = 0;
        g.gridx = 0; g.weightx = 0.62; g.insets = new Insets(0, 0, 0, 25);
        lower.add(left, g);
        g.gridx = 1; g.weightx = 0.38; g.insets = new Insets(0, 0, 0, 0);
        lower.add(right, g);

        home.add(north, BorderLayout.NORTH);
        home.add(lower, BorderLayout.CENTER);
        return home;
    }

    private JPanel statCard(JLabel value, String caption) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new LineBorder(new Color(0x444444), 1));
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        value.setFont(Theme.font(Font.BOLD, 32));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0;
        p.add(value, c);
        c.gridy = 1;
        p.add(Theme.label(caption, 13, Font.PLAIN, Theme.MUTED), c);
        return p;
    }

    private JPanel buildReports() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(22, 28, 22, 28));
        JPanel head = new JPanel(new GridLayout(2, 1));
        head.setOpaque(false);
        head.add(Theme.label("Reports", 30, Font.BOLD, Color.BLACK));
        head.add(Theme.label("Summary of donations, distributions and users.", 14, Font.PLAIN, Theme.MUTED));
        reportArea.setEditable(false);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        reportArea.setBorder(new EmptyBorder(14, 14, 14, 14));
        p.add(head, BorderLayout.NORTH);
        p.add(new JScrollPane(reportArea), BorderLayout.CENTER);
        return p;
    }

    // ---------- updating what is shown ----------
    public void showPage(String page) {
        cards.show(content, page);
        String active = page.equals(ADD) ? DONATIONS : page;
        for (Map.Entry<String, JButton> e : navButtons.entrySet()) {
            boolean selected = e.getKey().equals(active);
            e.getValue().setBackground(Theme.DARK_GREEN);
            e.getValue().setOpaque(selected);
            e.getValue().repaint();
        }
    }

    public void setWelcome(String firstName, String role) {
        welcomeLabel.setText("Welcome, " + firstName + "!");
        subtitleLabel.setText("Logged in as " + role + "  |  Here's an overview of today's activities.");
    }

    public void setStats(int total, int collected, int distributed, int beneficiaries) {
        statValues[0].setText(String.valueOf(total));
        statValues[1].setText(String.valueOf(collected));
        statValues[2].setText(String.valueOf(distributed));
        statValues[3].setText(String.valueOf(beneficiaries));
    }

    public void setRecent(List<Donation> recent) {
        recentList.removeAll();
        for (Donation d : recent) {
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setBackground(Color.WHITE);
            row.setBorder(BorderFactory.createCompoundBorder(new Theme.RoundedBorder(Theme.BORDER, 8), new EmptyBorder(6, 12, 6, 12)));
            row.setPreferredSize(new Dimension(0, 60));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

            JPanel names = new JPanel(new GridLayout(2, 1));
            names.setOpaque(false);
            names.add(Theme.label(d.getFoodName(), 16, Font.BOLD, Color.BLACK));
            names.add(Theme.label(d.getQuantityText(), 12, Font.PLAIN, Theme.MUTED));
            JPanel middle = new JPanel(new GridBagLayout());
            middle.setOpaque(false);
            middle.add(Theme.pill(d.getStatus()));

            row.add(names, BorderLayout.WEST);
            row.add(middle, BorderLayout.CENTER);
            row.add(Theme.label(d.getDateText(), 12, Font.PLAIN, Theme.MUTED), BorderLayout.EAST);
            recentList.add(row);
            recentList.add(Box.createVerticalStrut(8));
        }
        recentList.add(Box.createVerticalGlue());
        recentList.revalidate();
        recentList.repaint();
    }

    public void setTables(List<Donation> all, List<Donation> inventory, List<Donation> collection,
                          List<Donation> distribution, List<? extends User> beneficiaries) {
        donationsPage.setRows(toRows(all));
        inventoryPage.setRows(toRows(inventory));
        collectionPage.setRows(toRows(collection));
        distributionPage.setRows(toRows(distribution));
        List<Object[]> rows = new ArrayList<>();
        for (User u : beneficiaries) rows.add(new Object[]{u.getFullName(), u.getEmail(), u.getContactNumber()});
        beneficiariesPage.setRows(rows);
    }

    private List<Object[]> toRows(List<Donation> list) {
        List<Object[]> rows = new ArrayList<>();
        for (Donation d : list) {
            rows.add(new Object[]{d.getId(), d.getFoodName(), d.getDonationType(), d.getQuantityText(),
                    d.getDonorName(), d.getPickupLocation(), d.getDeadlineText(), d.getStatus()});
        }
        return rows;
    }

    public void setReport(String text) { reportArea.setText(text); reportArea.setCaretPosition(0); }

    // ---------- getters for the controller ----------
    public JButton getNavButton(String page) { return navButtons.get(page); }
    public JButton getLogoutButton() { return logoutButton; }
    public JButton getBellButton() { return bellButton; }
    public JPanel getStatCard(int index) { return statCards[index]; }
    public JButton getAddDonationButton() { return addDonationButton; }
    public JButton getViewDonationsButton() { return viewDonationsButton; }
    public JButton getManageCollectionButton() { return manageCollectionButton; }
    public JButton getRecordDistributionButton() { return recordDistributionButton; }
    public JButton getViewBeneficiariesButton() { return viewBeneficiariesButton; }
    public TablePage getCollectionPage() { return collectionPage; }
    public TablePage getDistributionPage() { return distributionPage; }
    public AddDonationView getAddDonationView() { return addDonationView; }

    /** Simple bell drawn with Java2D (no image file needed). */
    private static class BellIcon implements Icon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new Arc2D.Double(x + 6, y + 3, 18, 20, 0, 180, Arc2D.OPEN));
            g2.drawLine(x + 6, y + 13, x + 4, y + 23);
            g2.drawLine(x + 24, y + 13, x + 26, y + 23);
            g2.drawLine(x + 3, y + 23, x + 27, y + 23);
            g2.drawArc(x + 12, y + 24, 6, 5, 180, 180);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 30; }
        @Override public int getIconHeight() { return 32; }
    }
}
