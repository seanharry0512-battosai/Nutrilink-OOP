package controller;

import model.*;
import view.*;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Connects the views to the model (Topic 4.5 Controller). Listeners are inner classes (Topic 4.6). */
public class AppController {
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

    private final DataStore store;
    private final MainFrame frame;
    private final LoginView loginView;
    private final RegisterView registerView;
    private final DashboardView dashboard;
    private final AddDonationView addView;
    private final DonationsView donationsView;
    private final DonationDetailsView detailsView;
    private final CollectionView collectionView;
    private final DistributionView distributionView;
    private final ReportsView reportsView;
    private final NotificationsView notificationsView;
    private final ProfileView profileView;
    private final SettingsView settingsView;
    private Report currentReport;

    public AppController(DataStore store, MainFrame frame) {
        this.store = store;
        this.frame = frame;
        this.loginView = frame.getLoginView();
        this.registerView = frame.getRegisterView();
        this.dashboard = frame.getDashboardView();
        this.addView = dashboard.getAddDonationView();
        this.donationsView = dashboard.getDonationsView();
        this.detailsView = dashboard.getDetailsView();
        this.collectionView = dashboard.getCollectionView();
        this.distributionView = dashboard.getDistributionView();
        this.reportsView = dashboard.getReportsView();
        this.notificationsView = dashboard.getNotificationsView();
        this.profileView = dashboard.getProfileView();
        this.settingsView = dashboard.getSettingsView();
        wireLogin();
        wireRegister();
        wireDashboard();
        wireAddDonation();
        wireDonations();
        wireDetails();
        wireCollection();
        wireDistribution();
        wireReports();
        wireNotifications();
        wireProfile();
        wireSettings();
    }

    public void start() {
        loginView.setEmail(store.getRememberedEmail());
        frame.showScreen(MainFrame.LOGIN);
        frame.setVisible(true);
    }

    // ---------------- wiring ----------------
    private void wireLogin() {
        loginView.getLoginButton().addActionListener(new LoginAction());
        loginView.getPasswordField().addActionListener(new LoginAction()); // Enter key
        loginView.getRegisterLabel().addMouseListener(new LinkClick(() -> frame.showScreen(MainFrame.REGISTER)));
        loginView.getForgotLabel().addMouseListener(new LinkClick(this::forgotPassword));
    }

    private void wireRegister() {
        registerView.getCreateButton().addActionListener(new RegisterAction());
        registerView.getLoginLabel().addMouseListener(new LinkClick(() -> frame.showScreen(MainFrame.LOGIN)));
    }

    private void wireDashboard() {
        for (String page : DashboardView.NAV_PAGES) {
            dashboard.getNavButton(page).addActionListener(new NavAction(page));
        }
        dashboard.getNavButton(DashboardView.NOTIFICATIONS).addActionListener(new NavAction(DashboardView.NOTIFICATIONS));
        dashboard.getNavButton(DashboardView.SETTINGS).addActionListener(new NavAction(DashboardView.SETTINGS));
        dashboard.getProfileChip().addMouseListener(new LinkClick(() -> goTo(DashboardView.PROFILE)));
        dashboard.getBellButton().addActionListener(new NavAction(DashboardView.NOTIFICATIONS));

        dashboard.getAddDonationButton().addActionListener(new NavAction(DashboardView.ADD));
        dashboard.getViewDonationsButton().addActionListener(new NavAction(DashboardView.DONATIONS));
        dashboard.getViewInventoryButton().addActionListener(new NavAction(DashboardView.INVENTORY));
        dashboard.getManageCollectionButton().addActionListener(new NavAction(DashboardView.COLLECTION));
        dashboard.getRecordDistributionButton().addActionListener(new NavAction(DashboardView.DISTRIBUTION));
        dashboard.getViewBeneficiariesButton().addActionListener(new NavAction(DashboardView.BENEFICIARIES));

        // clicking a stat card opens the first related page this user is allowed to see
        dashboard.getStatCard(0).addMouseListener(new LinkClick(() -> goToFirstAllowed(DashboardView.DONATIONS, DashboardView.INVENTORY)));
        dashboard.getStatCard(1).addMouseListener(new LinkClick(() -> goToFirstAllowed(DashboardView.COLLECTION, DashboardView.DONATIONS, DashboardView.INVENTORY)));
        dashboard.getStatCard(2).addMouseListener(new LinkClick(() -> goToFirstAllowed(DashboardView.REPORTS, DashboardView.DISTRIBUTION, DashboardView.INVENTORY)));
        dashboard.getStatCard(3).addMouseListener(new LinkClick(() -> goToFirstAllowed(DashboardView.BENEFICIARIES)));

        dashboard.setOnRecentClick(this::openDetails);
        dashboard.getLogoutButton().addActionListener(new LogoutAction());
    }

    private void wireAddDonation() {
        addView.getSubmitButton().addActionListener(new AddDonationAction());
        addView.getCancelButton().addActionListener(e -> { addView.clear(); goTo(DashboardView.HOME); });
    }

    /** Donations list page: Add Donation button, row click / menu -> details, Approve. */
    private void wireDonations() {
        donationsView.getAddButton().addActionListener(new NavAction(DashboardView.ADD));
        donationsView.setOnView(this::openDetails);
        donationsView.setOnApprove(this::approveDonation);
    }

    /** Donation details page: Back link, Assign Volunteer, Update Status. */
    private void wireDetails() {
        detailsView.getBackLabel().addMouseListener(new LinkClick(() -> goTo(DashboardView.DONATIONS)));
        detailsView.getAssignButton().addActionListener(e -> assignVolunteer(detailsView.getCurrentDonation()));
        detailsView.getUpdateStatusButton().addActionListener(e -> chooseCollectionStatus(detailsView.getCurrentDonation()));
    }

    /** Collection page: View, Assign Volunteer, Mark Completed. */
    private void wireCollection() {
        collectionView.setOnView(this::openDetails);
        collectionView.setOnAssign(this::assignVolunteer);
        collectionView.setOnComplete(d -> changeCollectionStatus(d, Donation.COL_COMPLETED));
    }

    private void wireDistribution() {
        distributionView.getRecordButton().addActionListener(new RecordDistributionAction());
    }

    private void wireReports() {
        reportsView.getGenerateButton().addActionListener(e -> generateReport());
        reportsView.getExportButton().addActionListener(e -> exportReport());
        reportsView.getPresetBox().addActionListener(e -> {
            String preset = (String) reportsView.getPresetBox().getSelectedItem();
            if (preset == null || preset.equals(ReportsView.CUSTOM)) return;      // the dates are chosen by hand
            LocalDate[] range = Report.presetRange(preset, LocalDate.now());
            reportsView.setRange(preset, range[0], range[1]);
            generateReport();
        });
    }

    private void wireNotifications() {
        notificationsView.setOnClick(this::openNotification);
        notificationsView.getMarkAllButton().addActionListener(e -> {
            store.markAllRead(store.getCurrentUser());
            refresh();
        });
        notificationsView.getClearButton().addActionListener(e -> {
            int answer = JOptionPane.showConfirmDialog(frame, "Delete all your notifications?", "Clear notifications",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (answer != JOptionPane.YES_OPTION) return;
            store.clearNotifications(store.getCurrentUser());
            refresh();
        });
    }

    private void wireProfile() {
        profileView.getSaveButton().addActionListener(e -> saveProfile());
        profileView.getPasswordButton().addActionListener(e -> changePassword());
    }

    private void wireSettings() {
        settingsView.setOnChange(() -> {
            User user = store.getCurrentUser();
            if (user == null) return;
            UserSettings s = user.getSettings();
            s.setNotifyUpdates(settingsView.isNotifyUpdates());
            s.setNotifyExpiry(settingsView.isNotifyExpiry());
            s.setConfirmLogout(settingsView.isConfirmLogout());
            s.setDefaultReportRange(settingsView.getReportRange());
            store.save();
        });
        settingsView.getResetButton().addActionListener(e -> {
            User user = store.getCurrentUser();
            if (user == null || !user.canManageSystem()) return;
            int answer = JOptionPane.showConfirmDialog(frame,
                    "This deletes ALL donations, accounts and notifications\nand restores the demo data. Continue?",
                    "Reset demo data", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (answer != JOptionPane.YES_OPTION) return;
            store.resetToDemoData();
            currentReport = null;
            loginView.setEmail("");
            frame.showScreen(MainFrame.LOGIN);
            info("Demo data restored", "Everything was reset. All demo accounts use the password 123456.");
        });
    }

    // ---------------- helpers ----------------
    private void info(String title, String message) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(frame, message, "NutriLink", JOptionPane.ERROR_MESSAGE);
    }

    private void forgotPassword() {
        String email = JOptionPane.showInputDialog(frame, "Enter your registered email:", "Forgot Password", JOptionPane.QUESTION_MESSAGE);
        if (email == null) return;
        if (store.findUserByEmail(email) == null) error("No account found for that email.");
        else info("Forgot Password", "A password reset link has been sent to " + email.trim() + " (simulated).");
    }

    /** The pages this user may open (what the sidebar shows, plus the pages reached from buttons). */
    private static Set<String> allowedPages(User u) {
        Set<String> pages = new LinkedHashSet<>();
        pages.add(DashboardView.HOME);
        if (u.canViewDonations()) { pages.add(DashboardView.DONATIONS); pages.add(DashboardView.DETAILS); }
        if (u.canAddDonation()) pages.add(DashboardView.ADD);
        if (u.canViewInventory()) pages.add(DashboardView.INVENTORY);
        if (u.canManageCollection()) { pages.add(DashboardView.COLLECTION); pages.add(DashboardView.DISTRIBUTION); }
        if (u.canViewBeneficiaries()) pages.add(DashboardView.BENEFICIARIES);
        if (u.canViewReports()) pages.add(DashboardView.REPORTS);
        pages.add(DashboardView.NOTIFICATIONS);
        pages.add(DashboardView.SETTINGS);
        pages.add(DashboardView.PROFILE);
        return pages;
    }

    private boolean isAllowed(String page) {
        User user = store.getCurrentUser();
        return user != null && allowedPages(user).contains(page);
    }

    private void goTo(String page) {
        User user = store.getCurrentUser();
        if (user == null) return;
        if (!allowedPages(user).contains(page)) {
            if (page.equals(DashboardView.ADD)) info("Not allowed", "Only food donors and coordinators can add donations.");
            return;
        }
        if (page.equals(DashboardView.PROFILE)) loadProfile();
        refresh();
        dashboard.showPage(page);
    }

    private void goToFirstAllowed(String... pages) {
        for (String p : pages) {
            if (isAllowed(p)) { goTo(p); return; }
        }
    }

    private void openDashboard() {
        User user = store.getCurrentUser();
        store.generateExpiryAlerts();
        dashboard.setUser(user);
        dashboard.setVisiblePages(allowedPages(user));
        settingsView.load(user.getSettings(), user.canManageSystem());
        String preset = user.getSettings().getDefaultReportRange();
        LocalDate[] range = Report.presetRange(preset, LocalDate.now());
        reportsView.setRange(preset, range[0], range[1]);
        detailsView.showDonation(null);
        goTo(DashboardView.HOME);
        frame.showScreen(MainFrame.APP);
    }

    private void openDetails(Donation d) {
        User user = store.getCurrentUser();
        if (d == null || user == null || !user.canViewDonations()) return;
        refresh();
        detailsView.showDonation(d);
        detailsView.setManagerActions(user.canManageCollection());
        dashboard.showPage(DashboardView.DETAILS);
    }

    private void openNotification(Notification n) {
        User user = store.getCurrentUser();
        store.markRead(n);
        Donation d = n.getDonationId() > 0 ? store.findDonation(n.getDonationId()) : null;
        if (d != null && user.canViewDonations() && (user.seesAllDonations() || d.getDonorId() == user.getId())) {
            openDetails(d);
        } else if (n.getLink() != null && isAllowed(n.getLink())) {
            goTo(n.getLink());
        } else {
            refresh();
        }
    }

    /** Only coordinators and volunteers may change collection / distribution data. */
    private boolean requireManager() {
        if (store.getCurrentUser().canManageCollection()) return true;
        info("Not allowed", "Only coordinators and volunteers can update donation status.");
        return false;
    }

    private void approveDonation(Donation d) {
        if (!store.getCurrentUser().canApproveDonation()) {
            info("Not allowed", "Only coordinators can approve donations.");
            return;
        }
        String problem = store.approveDonation(d);
        if (problem != null) { error(problem); return; }
        refresh();
    }

    private void assignVolunteer(Donation d) {
        if (d == null || !requireManager()) return;
        List<Volunteer> volunteers = store.getUsersByType(Volunteer.class);
        if (volunteers.isEmpty()) { error("There are no registered volunteers yet."); return; }

        AssignVolunteerDialog.Result r = AssignVolunteerDialog.show(frame, d, volunteers);
        if (r == null) return;
        String problem = store.assignVolunteer(d, r.volunteer, r.pickupDate);
        if (problem != null) { error(problem); return; }
        refresh();
        info("Volunteer assigned", r.volunteer.getFullName() + " will pick up " + d.getFoodName() + " on " + d.getPickupText() + ".");
    }

    private void chooseCollectionStatus(Donation d) {
        if (d == null || !requireManager()) return;
        Object choice = JOptionPane.showInputDialog(frame,
                "Collection status for " + d.getFoodName() + ":", "Update Status",
                JOptionPane.QUESTION_MESSAGE, null,
                new String[]{Donation.COL_PENDING, Donation.COL_ONGOING, Donation.COL_COMPLETED},
                d.getCollectionStatus());
        if (choice == null) return;
        changeCollectionStatus(d, (String) choice);
    }

    private void changeCollectionStatus(Donation d, String newStatus) {
        if (d == null || !requireManager()) return;
        String problem = store.updateCollectionStatus(d, newStatus);
        if (problem != null) { error(problem); return; }
        refresh();
    }

    // ---------------- profile ----------------
    private void loadProfile() {
        User user = store.getCurrentUser();
        profileView.setUser(user, user.getActivitySummary(store));
    }

    private void saveProfile() {
        User user = store.getCurrentUser();
        String oldEmail = user.getEmail();
        String problem = store.updateProfile(user, profileView.getNameText(), profileView.getEmailText(), profileView.getContactText());
        if (problem != null) { error(problem); return; }
        if (store.getRememberedEmail().equalsIgnoreCase(oldEmail)) store.setRememberedEmail(user.getEmail());
        dashboard.setUser(user);
        loadProfile();
        refresh();
        info("Profile updated", "Your details were saved.");
    }

    private void changePassword() {
        User user = store.getCurrentUser();
        String problem = store.changePassword(user, profileView.getCurrentPassword(),
                profileView.getNewPassword(), profileView.getConfirmPassword());
        if (problem != null) { error(problem); return; }
        profileView.clearPasswordFields();
        info("Password changed", "Your password was updated.");
    }

    // ---------------- reports ----------------
    private void generateReport() {
        User user = store.getCurrentUser();
        if (user == null || !user.canViewReports()) return;
        LocalDate from = reportsView.getFrom();
        LocalDate to = reportsView.getTo();
        String problem = Report.validateRange(from, to);
        if (problem != null) { reportsView.showError(problem); return; }
        currentReport = Report.generate(store.getDonationsVisibleTo(user), store.getDistributionsVisibleTo(user), from, to);
        reportsView.showReport(currentReport);
    }

    private void exportReport() {
        User user = store.getCurrentUser();
        generateReport();                                   // make sure the file matches what the dates say
        if (currentReport == null) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Report");
        FileNameExtensionFilter csv = new FileNameExtensionFilter("CSV spreadsheet (*.csv)", "csv");
        FileNameExtensionFilter html = new FileNameExtensionFilter("Web page (*.html)", "html");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(csv);
        chooser.addChoosableFileFilter(html);
        chooser.setFileFilter(csv);
        chooser.setSelectedFile(new File("nutrilink-report-" + currentReport.getFrom() + "_to_" + currentReport.getTo() + ".csv"));
        if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        String lower = file.getName().toLowerCase();
        boolean asHtml = lower.endsWith(".html") || lower.endsWith(".htm")
                || (!lower.endsWith(".csv") && chooser.getFileFilter() == html);
        if (!lower.endsWith(".csv") && !lower.endsWith(".html") && !lower.endsWith(".htm")) {
            file = new File(file.getParentFile(), file.getName() + (asHtml ? ".html" : ".csv"));
        }
        if (file.exists()) {
            int answer = JOptionPane.showConfirmDialog(frame, file.getName() + " already exists. Replace it?",
                    "Export Report", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (answer != JOptionPane.YES_OPTION) return;
        }
        String by = user.getFullName() + " (" + user.getRole() + ")";
        String text = asHtml ? ReportExporter.toHtml(currentReport, by) : ReportExporter.toCsv(currentReport, by);
        try {
            Files.write(file.toPath(), text.getBytes(StandardCharsets.UTF_8));
            info("Report exported", "Saved to:\n" + file.getAbsolutePath());
        } catch (IOException ex) {
            error("Could not save the file: " + ex.getMessage());
        }
    }

    // ---------------- refresh ----------------
    /** Re-reads the model and updates every screen so nothing is static. */
    private void refresh() {
        User user = store.getCurrentUser();
        if (user == null) return;
        store.generateExpiryAlerts();

        List<Donation> mine = store.getDonationsVisibleTo(user);
        int collected = 0, distributed = 0;
        for (Donation d : mine) {
            if (d.getStatus().equals(Donation.COLLECTED) || d.getStatus().equals(Donation.DISTRIBUTED)) collected++;
            if (d.getStatus().equals(Donation.DISTRIBUTED)) distributed++;
        }
        List<Beneficiary> beneficiaries = store.getUsersByType(Beneficiary.class);
        dashboard.setStats(mine.size(), collected, distributed, beneficiaries.size());

        List<Donation> recent = new ArrayList<>(mine);
        recent.sort((a, b) -> {
            int c = b.getDateAdded().compareTo(a.getDateAdded());
            return (c != 0) ? c : Integer.compare(b.getId(), a.getId());
        });
        dashboard.setRecent(recent.subList(0, Math.min(4, recent.size())));

        // food that was collected and still has something left
        List<Donation> inStock = new ArrayList<>();
        for (Donation d : store.getDonationsByStatus(Donation.COLLECTED)) {
            if (d.getRemainingQuantity() > 0) inStock.add(d);
        }
        dashboard.getInventoryView().setItems(inStock);
        dashboard.getBeneficiariesView().setRows(beneficiaryRows(beneficiaries));

        donationsView.setPermissions(user.canAddDonation(), user.canApproveDonation());
        donationsView.setDonations(mine);
        collectionView.setData(
                store.getDonationsByCollectionStatus(Donation.COL_PENDING),
                store.getDonationsByCollectionStatus(Donation.COL_ONGOING),
                store.getDonationsByCollectionStatus(Donation.COL_COMPLETED));
        distributionView.setAvailableFood(store.getDonationsReadyToDistribute());
        distributionView.setBeneficiaries(beneficiaries);
        detailsView.setManagerActions(user.canManageCollection());
        if (detailsView.getCurrentDonation() != null) detailsView.showDonation(detailsView.getCurrentDonation());

        notificationsView.setNotifications(store.getNotificationsFor(user));
        dashboard.setUnread(store.countUnread(user));
        generateReport();
    }

    private List<String[]> beneficiaryRows(List<Beneficiary> beneficiaries) {
        List<String[]> rows = new ArrayList<>();
        for (Beneficiary b : beneficiaries) {
            int times = 0;
            LocalDate last = null;
            for (Distribution x : store.getDistributions()) {
                if (x.getBeneficiaryId() != b.getId()) continue;
                times++;
                if (last == null || x.getDate().isAfter(last)) last = x.getDate();
            }
            rows.add(new String[]{b.getFullName(), b.getEmail(), b.getContactNumber(),
                    String.valueOf(times), last == null ? "-" : last.format(DAY)});
        }
        return rows;
    }

    // ---------------- inner classes: listeners ----------------
    private class LoginAction implements ActionListener {
        @Override public void actionPerformed(ActionEvent e) {
            String email = loginView.getEmail();
            String password = loginView.getPassword();
            if (email.isEmpty() || password.isEmpty()) { error("Please enter your email and password."); return; }
            User user = store.authenticate(email, password);
            if (user == null) { error("Invalid email or password."); return; }
            store.setCurrentUser(user);
            store.setRememberedEmail(loginView.isRememberSelected() ? email : "");
            loginView.clearPassword();
            openDashboard();
        }
    }

    private class RegisterAction implements ActionListener {
        @Override public void actionPerformed(ActionEvent e) {
            String name = registerView.getFullName();
            String email = registerView.getEmail();
            String contact = registerView.getContact();
            String password = registerView.getPassword();

            if (name.isEmpty() || email.isEmpty() || contact.isEmpty() || password.isEmpty()) {
                error("Please fill in all fields."); return;
            }
            if (!DataStore.EMAIL.matcher(email).matches()) { error("Please enter a valid email address."); return; }
            if (!DataStore.CONTACT.matcher(contact).matches()) { error("Contact number must be 11 digits (e.g. 09171234567)."); return; }
            if (password.length() < 6) { error("Password must be at least 6 characters."); return; }
            if (!password.equals(registerView.getConfirm())) { error("Passwords do not match."); return; }
            if (store.findUserByEmail(email) != null) { error("An account with that email already exists."); return; }

            User user;
            switch (registerView.getSelectedRole()) {
                case "Coordinator": user = new Coordinator(name, email, contact, password); break;
                case "Volunteer": user = new Volunteer(name, email, contact, password); break;
                case "Beneficiary": user = new Beneficiary(name, email, contact, password); break;
                default: user = new Donor(name, email, contact, password);
            }
            store.addUser(user);
            info("Account created", "Welcome to NutriLink, " + user.getFirstName() + "! You can now log in.");
            loginView.setEmail(email);
            registerView.clear();
            frame.showScreen(MainFrame.LOGIN);
        }
    }

    private class NavAction implements ActionListener {
        private final String page;
        NavAction(String page) { this.page = page; }
        @Override public void actionPerformed(ActionEvent e) { goTo(page); }
    }

    private class LogoutAction implements ActionListener {
        @Override public void actionPerformed(ActionEvent e) {
            User user = store.getCurrentUser();
            if (user != null && user.getSettings().isConfirmLogout()) {
                int answer = JOptionPane.showConfirmDialog(frame, "Do you want to log out?", "Logout",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (answer != JOptionPane.YES_OPTION) return;
            }
            store.setCurrentUser(null);
            currentReport = null;
            loginView.setEmail(store.getRememberedEmail());
            frame.showScreen(MainFrame.LOGIN);
        }
    }

    private class AddDonationAction implements ActionListener {
        @Override public void actionPerformed(ActionEvent e) {
            String food = addView.getFoodName().trim();
            String pickup = addView.getPickupLocation().trim();
            if (food.isEmpty()) { error("Please enter the food name."); return; }
            if (food.length() > 60) { error("The food name is too long (60 characters at most)."); return; }
            double qty;
            try {
                qty = Double.parseDouble(addView.getQuantityText().trim());
            } catch (NumberFormatException ex) {
                error("Quantity must be a number."); return;
            }
            if (Double.isNaN(qty) || Double.isInfinite(qty) || qty <= 0) { error("Quantity must be greater than zero."); return; }
            if (qty > 1_000_000) { error("That quantity is too large."); return; }
            if (pickup.isEmpty()) { error("Please enter the pickup location."); return; }
            if (addView.getDeadline().isBefore(LocalDate.now())) { error("Distribution deadline cannot be in the past."); return; }

            store.createDonation(store.getCurrentUser(), food, addView.getDonationType(), qty, addView.getUnit(), pickup,
                    addView.getDeadline(), addView.getCondition(), addView.getNotes());
            addView.clear();
            info("Donation submitted", "Thank you! Your donation was added and is now Pending approval.");
            goTo(DashboardView.DONATIONS);
        }
    }

    /** "Record Distribution" button on the Distribution page. */
    private class RecordDistributionAction implements ActionListener {
        @Override public void actionPerformed(ActionEvent e) {
            if (!requireManager()) return;
            Donation d = distributionView.getSelectedDonation();
            if (d == null) { info("Select food", "Please select a food item from the Available Food list."); return; }
            Beneficiary beneficiary = distributionView.getBeneficiary();
            if (beneficiary == null) { error("There are no beneficiaries to choose from."); return; }

            double qty;
            try {
                qty = Double.parseDouble(distributionView.getQuantityText());
            } catch (NumberFormatException ex) {
                error("Quantity must be a number."); return;
            }

            String food = d.getFoodName();
            String unit = d.getUnit();
            String problem = store.recordDistribution(d, beneficiary, qty, distributionView.getDate());
            if (problem != null) { error(problem); return; }

            distributionView.clearForm();
            refresh();
            String shown = (qty == Math.rint(qty)) ? String.valueOf((long) qty) : String.valueOf(qty);
            info("Distribution recorded", shown + " " + unit + " of " + food + " given to " + beneficiary.getFullName() + ".");
        }
    }

    /** Runs an action when a label/panel is clicked (links and stat cards). */
    private class LinkClick extends MouseAdapter {
        private final Runnable action;
        LinkClick(Runnable action) { this.action = action; }
        @Override public void mouseClicked(MouseEvent e) { action.run(); }
    }
}
