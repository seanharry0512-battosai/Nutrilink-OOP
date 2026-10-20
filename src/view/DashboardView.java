package view;

import model.Donation;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/** The logged-in screen: green sidebar + a content area that switches pages. */
public class DashboardView extends JPanel {
    public static final String HOME = "Dashboard";
    public static final String DONATIONS = "Donations";
    public static final String INVENTORY = "Inventory";
    public static final String COLLECTION = "Collection";
    public static final String DISTRIBUTION = "Distribution";
    public static final String BENEFICIARIES = "Beneficiaries";
    public static final String REPORTS = "Reports";
    public static final String NOTIFICATIONS = "Notifications";
    public static final String SETTINGS = "Settings";
    public static final String PROFILE = "Profile";
    public static final String ABOUT = "About";
    public static final String ADD = "AddDonation";
    public static final String DETAILS = "DonationDetails";
    /** The pages in the main part of the sidebar. */
    public static final String[] NAV_PAGES = {HOME, DONATIONS, INVENTORY, COLLECTION, DISTRIBUTION, BENEFICIARIES, REPORTS};

    private final Sidebar sidebar = new Sidebar();
    private final PageTransition content = new PageTransition();   // switches pages with a fade

    // ---- home page ----
    private final JLabel welcomeLabel = Theme.label("Welcome!", 32, Font.BOLD, Color.BLACK);
    private final JLabel subtitleLabel = Theme.label("Here's an overview of today's activities.", 14, Font.PLAIN, Theme.MUTED);
    private final BellButton bellButton = new BellButton();
    private final JLabel[] statValues = {new JLabel("0"), new JLabel("0"), new JLabel("0"), new JLabel("0")};
    private final JLabel[] statCaptions = new JLabel[4];
    private final JPanel[] statCards = new JPanel[4];
    private final JPanel recentList = new JPanel();
    private Consumer<Donation> onRecentClick;

    private final JButton addDonationButton = Theme.button("+ Add Donation", Theme.ORANGE, Color.WHITE);
    private final JButton viewDonationsButton = quickButton("View Donations");
    private final JButton viewInventoryButton = quickButton("View Inventory");
    private final JButton manageCollectionButton = quickButton("Manage Collection");
    private final JButton recordDistributionButton = quickButton("Record Distribution");
    private final JButton viewBeneficiariesButton = quickButton("View Beneficiaries");
    private final List<JPanel> quickRows = new ArrayList<>();      // same order as the buttons above

    // ---- other pages ----
    private final DonationsView donationsView = new DonationsView();
    private final DonationDetailsView detailsView = new DonationDetailsView();
    private final CollectionView collectionView = new CollectionView();
    private final DistributionView distributionView = new DistributionView();
    private final InventoryView inventoryView = new InventoryView();
    private final BeneficiariesView beneficiariesView = new BeneficiariesView();
    private final ReportsView reportsView = new ReportsView();
    private final AddDonationView addDonationView = new AddDonationView();
    private final NotificationsView notificationsView = new NotificationsView();
    private final SettingsView settingsView = new SettingsView();
    private final ProfileView profileView = new ProfileView();
    private final AboutView aboutView = new AboutView();

    public DashboardView() {
        setLayout(new BorderLayout());
        add(sidebar, BorderLayout.WEST);

        content.setBackground(Color.WHITE);
        content.add(buildHome(), HOME);
        content.add(donationsView, DONATIONS);
        content.add(detailsView, DETAILS);
        content.add(inventoryView, INVENTORY);
        content.add(collectionView, COLLECTION);
        content.add(distributionView, DISTRIBUTION);
        content.add(beneficiariesView, BENEFICIARIES);
        content.add(reportsView, REPORTS);
        content.add(addDonationView, ADD);
        content.add(notificationsView, NOTIFICATIONS);
        content.add(settingsView, SETTINGS);
        content.add(profileView, PROFILE);
        content.add(aboutView, ABOUT);
        add(content, BorderLayout.CENTER);
        // the sidebar's "About Us" row opens the About page by itself (no controller code needed)
        sidebar.getAboutItem().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showPage(ABOUT); }
        });
        showPage(HOME);
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

        bellButton.setPreferredSize(new Dimension(44, 44));
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
            statCaptions[i] = Theme.label(captions[i], 13, Font.PLAIN, Theme.MUTED);
            statCards[i] = statCard(statValues[i], statCaptions[i]);
            stats.add(statCards[i]);
        }

        JPanel north = new JPanel(new BorderLayout(0, 22));
        north.setOpaque(false);
        north.add(top, BorderLayout.NORTH);
        north.add(stats, BorderLayout.CENTER);

        // lower half: recent donations (left) + quick actions (right)
        recentList.setLayout(new BoxLayout(recentList, BoxLayout.Y_AXIS));
        recentList.setOpaque(false);
        JPanel recentWrap = new JPanel(new BorderLayout());
        recentWrap.setOpaque(false);
        recentWrap.add(recentList, BorderLayout.NORTH);
        JPanel left = new JPanel(new BorderLayout(0, 10));
        left.setOpaque(false);
        left.add(Theme.label("Recent Donations", 22, Font.BOLD, Color.BLACK), BorderLayout.NORTH);
        left.add(recentWrap, BorderLayout.CENTER);

        JPanel quick = new JPanel();
        quick.setLayout(new BoxLayout(quick, BoxLayout.Y_AXIS));
        quick.setOpaque(false);
        for (JButton b : new JButton[]{addDonationButton, viewDonationsButton, viewInventoryButton,
                manageCollectionButton, recordDistributionButton, viewBeneficiariesButton}) {
            JPanel wrap = new JPanel(new BorderLayout());
            wrap.setOpaque(false);
            wrap.setBorder(new EmptyBorder(0, 0, 8, 0));
            wrap.add(b, BorderLayout.CENTER);
            wrap.setPreferredSize(new Dimension(100, 48));
            wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
            quick.add(wrap);
            quickRows.add(wrap);
        }
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

    private JPanel statCard(JLabel value, JLabel caption) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new Theme.RoundedBorder(new Color(0x444444), 8));
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        value.setFont(Theme.font(Font.BOLD, 32));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0;
        p.add(value, c);
        c.gridy = 1;
        p.add(caption, c);
        return p;
    }

    // ---------- updating what is shown ----------
    public void showPage(String page) {
        content.showPage(page);
        // Add Donation and the details page belong to the Donations menu item
        sidebar.setActive((page.equals(ADD) || page.equals(DETAILS)) ? DONATIONS : page);
    }

    /** Fills the welcome text, the sidebar user and the quick actions this user is allowed to use. */
    public void setUser(User u) {
        welcomeLabel.setText("Welcome, " + u.getFirstName() + "!");
        subtitleLabel.setText("Logged in as " + u.getRole() + "  |  Here's an overview of "
                + (u.seesAllDonations() ? "today's activities." : "your donations."));
        statCaptions[0].setText(u.seesAllDonations() ? "Total donations" : "My donations");
        sidebar.setUser(u.getFullName(), u.getRole());
        boolean[] visible = {u.canAddDonation(), u.canViewDonations(), u.canViewInventory(),
                u.canManageCollection(), u.canManageCollection(), u.canViewBeneficiaries()};
        for (int i = 0; i < visible.length; i++) quickRows.get(i).setVisible(visible[i]);
    }

    public void setVisiblePages(Set<String> pages) { sidebar.setVisiblePages(pages); }

    public void setStats(int total, int collected, int distributed, int beneficiaries) {
        statValues[0].setText(String.valueOf(total));
        statValues[1].setText(String.valueOf(collected));
        statValues[2].setText(String.valueOf(distributed));
        statValues[3].setText(String.valueOf(beneficiaries));
    }

    public void setUnread(int count) {
        sidebar.setUnread(count);
        bellButton.setCount(count);
    }

    public void setOnRecentClick(Consumer<Donation> onRecentClick) { this.onRecentClick = onRecentClick; }

    public void setRecent(List<Donation> recent) {
        recentList.removeAll();
        if (recent.isEmpty()) {
            JLabel empty = Theme.label("No donations yet.", 15, Font.PLAIN, Theme.MUTED);
            empty.setBorder(new EmptyBorder(10, 4, 0, 0));
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            recentList.add(empty);
        }
        for (Donation d : recent) {
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setBackground(Color.WHITE);
            row.setBorder(BorderFactory.createCompoundBorder(new Theme.RoundedBorder(Theme.BORDER, 8), new EmptyBorder(6, 12, 6, 12)));
            row.setPreferredSize(new Dimension(0, 60));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

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
            MouseAdapter click = new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { if (onRecentClick != null) onRecentClick.accept(d); }
            };
            row.addMouseListener(click);
            recentList.add(row);
            recentList.add(Box.createVerticalStrut(8));
        }
        recentList.revalidate();
        recentList.repaint();
    }

    // ---------- getters for the controller ----------
    public JButton getNavButton(String page) { return sidebar.getButton(page); }
    public JButton getLogoutButton() { return sidebar.getLogoutButton(); }
    public JComponent getProfileChip() { return sidebar.getProfileChip(); }
    public JButton getBellButton() { return bellButton; }
    public JPanel getStatCard(int index) { return statCards[index]; }
    public JButton getAddDonationButton() { return addDonationButton; }
    public JButton getViewDonationsButton() { return viewDonationsButton; }
    public JButton getViewInventoryButton() { return viewInventoryButton; }
    public JButton getManageCollectionButton() { return manageCollectionButton; }
    public JButton getRecordDistributionButton() { return recordDistributionButton; }
    public JButton getViewBeneficiariesButton() { return viewBeneficiariesButton; }
    public AddDonationView getAddDonationView() { return addDonationView; }
    public DonationsView getDonationsView() { return donationsView; }
    public DonationDetailsView getDetailsView() { return detailsView; }
    public CollectionView getCollectionView() { return collectionView; }
    public DistributionView getDistributionView() { return distributionView; }
    public InventoryView getInventoryView() { return inventoryView; }
    public BeneficiariesView getBeneficiariesView() { return beneficiariesView; }
    public ReportsView getReportsView() { return reportsView; }
    public NotificationsView getNotificationsView() { return notificationsView; }
    public SettingsView getSettingsView() { return settingsView; }
    public ProfileView getProfileView() { return profileView; }
}
