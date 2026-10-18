package view;

import model.Donation;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
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
    public static final String DETAILS = "DonationDetails";
    public static final String[] NAV_PAGES = {HOME, DONATIONS, INVENTORY, COLLECTION, DISTRIBUTION, BENEFICIARIES, REPORTS};

    private static final String LOGOUT = "Logout";
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private final JButton logoutButton = navButton(LOGOUT, new NavIcon(LOGOUT, new Color(0xFF3B30)));
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

    // pages that follow the mockups
    private final DonationsView donationsView = new DonationsView();
    private final DonationDetailsView detailsView = new DonationDetailsView();
    private final CollectionView collectionView = new CollectionView();
    private final DistributionView distributionView = new DistributionView();
    private final InventoryView inventoryView = new InventoryView();

    // pages that still use the simple table
    private final TablePage beneficiariesPage = new TablePage("Beneficiaries", "Registered beneficiaries.", new String[]{"Name", "Email", "Contact Number"});
    private final JTextArea reportArea = new JTextArea();
    private final AddDonationView addDonationView = new AddDonationView();

    public DashboardView() {
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);

        content.setBackground(Color.WHITE);
        content.add(buildHome(), HOME);
        content.add(donationsView, DONATIONS);
        content.add(detailsView, DETAILS);
        content.add(inventoryView, INVENTORY);
        content.add(collectionView, COLLECTION);
        content.add(distributionView, DISTRIBUTION);
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
        side.setPreferredSize(new Dimension(250, 0));
        // thin light strip on the left edge, like the mockup
        side.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 14, 0, 0, new Color(0x8DB89E)),
                new EmptyBorder(22, 0, 20, 0)));

        JLabel logo = new JLabel("NutriLink", new LeafIcon(), SwingConstants.LEFT);
        logo.setFont(Theme.font(Font.BOLD, 26));
        logo.setForeground(Color.WHITE);
        logo.setIconTextGap(8);
        logo.setBorder(new EmptyBorder(0, 20, 26, 0));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(logo);

        for (String page : NAV_PAGES) {
            JButton b = navButton(page, new NavIcon(page, Color.WHITE));
            navButtons.put(page, b);
            side.add(b);
            side.add(Box.createVerticalStrut(4));
        }
        side.add(Box.createVerticalGlue());
        side.add(logoutButton);
        return side;
    }

    private static JButton navButton(String text, Icon icon) {
        JButton b = new JButton(text, icon);
        b.setFont(Theme.font(Font.PLAIN, 16));
        b.setForeground(Color.WHITE);
        b.setIconTextGap(18);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 20, 10, 10));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
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
        // Add Donation and the details page belong to the Donations menu item
        String active = (page.equals(ADD) || page.equals(DETAILS)) ? DONATIONS : page;
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
            middle.add(new StatusPill(d.getStatus()));

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

    /**
     * Fills the Inventory and Beneficiaries pages. Donations, Collection and Distribution have
     * their own views (filled by the controller), so those parameters are not used here.
     */
    public void setTables(List<Donation> all, List<Donation> inventory, List<Donation> collection,
                          List<Donation> distribution, List<? extends User> beneficiaries) {
        inventoryView.setItems(inventory);
        List<Object[]> rows = new ArrayList<>();
        for (User u : beneficiaries) rows.add(new Object[]{u.getFullName(), u.getEmail(), u.getContactNumber()});
        beneficiariesPage.setRows(rows);
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
    public AddDonationView getAddDonationView() { return addDonationView; }
    public DonationsView getDonationsView() { return donationsView; }
    public DonationDetailsView getDetailsView() { return detailsView; }
    public CollectionView getCollectionView() { return collectionView; }
    public DistributionView getDistributionView() { return distributionView; }

    // ---------- small drawn icons (no image files needed) ----------

    /** Simple bell drawn with Java2D. */
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

    /** White leaf for the NutriLink logo. */
    private static class LeafIcon implements Icon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            Path2D leaf = new Path2D.Double();
            leaf.moveTo(3, 26);
            leaf.curveTo(0, 12, 10, 2, 25, 2);
            leaf.curveTo(26, 16, 16, 27, 3, 26);
            g2.setColor(Color.WHITE);
            g2.fill(leaf);
            g2.setColor(Theme.GREEN);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(6, 23, 17, 12);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 28; }
        @Override public int getIconHeight() { return 28; }
    }

    /** Outline icon for each sidebar item. Replace later with PNGs if you like. */
    private static class NavIcon implements Icon {
        private final String type;
        private final Color color;

        NavIcon(String type, Color color) {
            this.type = type;
            this.color = color;
        }

        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(color);
            g2.translate(x, y);
            switch (type) {
                case HOME -> {
                    Path2D roof = new Path2D.Double();
                    roof.moveTo(3, 13); roof.lineTo(14, 3); roof.lineTo(25, 13);
                    g2.draw(roof);
                    g2.drawRect(6, 13, 16, 12);
                    g2.drawRect(11, 18, 6, 7);
                }
                case DONATIONS -> {
                    g2.drawRect(4, 14, 20, 10);
                    g2.drawLine(2, 14, 26, 14);
                    Path2D heart = new Path2D.Double();
                    heart.moveTo(14, 11);
                    heart.curveTo(8, 8, 9, 2, 14, 5);
                    heart.curveTo(19, 2, 20, 8, 14, 11);
                    g2.draw(heart);
                }
                case INVENTORY -> {
                    g2.drawRect(3, 3, 22, 22);
                    g2.drawLine(3, 12, 25, 12);
                    g2.drawLine(3, 19, 25, 19);
                    g2.drawLine(14, 3, 14, 12);
                }
                case COLLECTION -> {
                    Path2D cart = new Path2D.Double();
                    cart.moveTo(2, 5); cart.lineTo(6, 5); cart.lineTo(9, 18); cart.lineTo(23, 18);
                    cart.lineTo(25, 8); cart.lineTo(7, 8);
                    g2.draw(cart);
                    g2.draw(new Ellipse2D.Double(9, 21, 4, 4));
                    g2.draw(new Ellipse2D.Double(19, 21, 4, 4));
                }
                case DISTRIBUTION -> {
                    g2.draw(new Arc2D.Double(7, 3, 14, 14, 0, 180, Arc2D.OPEN));
                    g2.drawLine(7, 10, 14, 25);
                    g2.drawLine(21, 10, 14, 25);
                    g2.draw(new Ellipse2D.Double(11.5, 7.5, 5, 5));
                }
                case BENEFICIARIES -> {
                    g2.draw(new Ellipse2D.Double(11, 3, 6, 6));
                    g2.draw(new Arc2D.Double(7, 12, 14, 14, 0, 180, Arc2D.OPEN));
                    g2.draw(new Ellipse2D.Double(2, 9, 5, 5));
                    g2.draw(new Arc2D.Double(0, 17, 10, 8, 0, 180, Arc2D.OPEN));
                    g2.draw(new Ellipse2D.Double(21, 9, 5, 5));
                    g2.draw(new Arc2D.Double(18, 17, 10, 8, 0, 180, Arc2D.OPEN));
                }
                case REPORTS -> {
                    g2.drawRoundRect(5, 2, 18, 24, 4, 4);
                    g2.drawLine(8, 19, 12, 14);
                    g2.drawLine(12, 14, 15, 18);
                    g2.drawLine(15, 18, 20, 11);
                }
                case LOGOUT -> {
                    Path2D door = new Path2D.Double();
                    door.moveTo(12, 4); door.lineTo(3, 4); door.lineTo(3, 24); door.lineTo(12, 24);
                    g2.draw(door);
                    g2.drawLine(9, 14, 25, 14);
                    g2.drawLine(20, 9, 25, 14);
                    g2.drawLine(20, 19, 25, 14);
                }
                default -> { }
            }
            g2.dispose();
        }

        @Override public int getIconWidth() { return 28; }
        @Override public int getIconHeight() { return 28; }
    }
}