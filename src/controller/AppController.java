package controller;

import model.*;
import view.*;

import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Connects the views to the model (Topic 4.5 Controller). Listeners are inner classes (Topic 4.6). */
public class AppController {
    private final DataStore store;
    private final MainFrame frame;
    private final LoginView loginView;
    private final RegisterView registerView;
    private final DashboardView dashboard;
    private final AddDonationView addView;

    public AppController(DataStore store, MainFrame frame) {
        this.store = store;
        this.frame = frame;
        this.loginView = frame.getLoginView();
        this.registerView = frame.getRegisterView();
        this.dashboard = frame.getDashboardView();
        this.addView = dashboard.getAddDonationView();
        wireLogin();
        wireRegister();
        wireDashboard();
        wireAddDonation();
        wireStatusButtons();
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
        dashboard.getAddDonationButton().addActionListener(new NavAction(DashboardView.ADD));
        dashboard.getViewDonationsButton().addActionListener(new NavAction(DashboardView.DONATIONS));
        dashboard.getManageCollectionButton().addActionListener(new NavAction(DashboardView.COLLECTION));
        dashboard.getRecordDistributionButton().addActionListener(new NavAction(DashboardView.DISTRIBUTION));
        dashboard.getViewBeneficiariesButton().addActionListener(new NavAction(DashboardView.BENEFICIARIES));

        // clicking a stat card opens the related page
        dashboard.getStatCard(0).addMouseListener(new LinkClick(() -> goTo(DashboardView.DONATIONS)));
        dashboard.getStatCard(1).addMouseListener(new LinkClick(() -> goTo(DashboardView.DISTRIBUTION)));
        dashboard.getStatCard(2).addMouseListener(new LinkClick(() -> goTo(DashboardView.REPORTS)));
        dashboard.getStatCard(3).addMouseListener(new LinkClick(() -> goTo(DashboardView.BENEFICIARIES)));

        dashboard.getBellButton().addActionListener(e -> {
            int pending = store.countByStatus(Donation.PENDING);
            info("Notifications", pending == 0 ? "You have no pending donations."
                    : "You have " + pending + " pending donation(s) waiting for approval.");
        });
        dashboard.getLogoutButton().addActionListener(new LogoutAction());
    }

    private void wireAddDonation() {
        addView.getSubmitButton().addActionListener(new AddDonationAction());
        addView.getCancelButton().addActionListener(e -> { addView.clear(); goTo(DashboardView.HOME); });
    }

    private void wireStatusButtons() {
        TablePage collection = dashboard.getCollectionPage();
        collection.addAction("Approve", Theme.GREEN)
                .addActionListener(new StatusAction(collection, Donation.AVAILABLE, Donation.PENDING));
        collection.addAction("Mark as Collected", Theme.ORANGE)
                .addActionListener(new StatusAction(collection, Donation.COLLECTED, Donation.PENDING, Donation.AVAILABLE));

        TablePage distribution = dashboard.getDistributionPage();
        distribution.addAction("Mark as Distributed", Theme.GREEN)
                .addActionListener(new StatusAction(distribution, Donation.DISTRIBUTED, Donation.COLLECTED));
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

    private void goTo(String page) {
        User user = store.getCurrentUser();
        if (user == null) return;
        if (page.equals(DashboardView.ADD) && !user.canAddDonation()) {
            info("Not allowed", "Only food donors and coordinators can add donations.");
            return;
        }
        refresh();
        dashboard.showPage(page);
    }

    private void openDashboard() {
        User user = store.getCurrentUser();
        dashboard.setWelcome(user.getFirstName(), user.getRole());
        goTo(DashboardView.HOME);
        frame.showScreen(MainFrame.APP);
    }

    /** Re-reads the model and updates every screen so nothing is static. */
    private void refresh() {
        List<Donation> all = store.getDonations();
        int collected = store.countByStatus(Donation.COLLECTED, Donation.DISTRIBUTED);
        List<Beneficiary> beneficiaries = store.getUsersByType(Beneficiary.class);
        dashboard.setStats(all.size(), collected, store.countByStatus(Donation.DISTRIBUTED), beneficiaries.size());

        List<Donation> recent = new ArrayList<>(all);
        recent.sort((a, b) -> {
            int c = b.getDateAdded().compareTo(a.getDateAdded());
            return (c != 0) ? c : Integer.compare(b.getId(), a.getId());
        });
        dashboard.setRecent(recent.subList(0, Math.min(4, recent.size())));

        dashboard.setTables(all,
                store.getDonationsByStatus(Donation.AVAILABLE, Donation.COLLECTED),
                store.getDonationsByStatus(Donation.PENDING, Donation.AVAILABLE),
                store.getDonationsByStatus(Donation.COLLECTED, Donation.DISTRIBUTED),
                beneficiaries);
        dashboard.setReport(buildReport());
    }

    private String buildReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("NUTRILINK SUMMARY REPORT\n");
        sb.append("Generated: ").append(LocalDate.now()).append("\n\n");
        sb.append("DONATIONS\n");
        sb.append(String.format("  %-20s %d%n", "Total donations:", store.getDonations().size()));
        sb.append(String.format("  %-20s %d%n", "Pending:", store.countByStatus(Donation.PENDING)));
        sb.append(String.format("  %-20s %d%n", "Available:", store.countByStatus(Donation.AVAILABLE)));
        sb.append(String.format("  %-20s %d%n", "Collected:", store.countByStatus(Donation.COLLECTED)));
        sb.append(String.format("  %-20s %d%n%n", "Distributed:", store.countByStatus(Donation.DISTRIBUTED)));
        sb.append("REGISTERED USERS\n");
        sb.append(String.format("  %-20s %d%n", "Food donors:", store.getUsersByType(Donor.class).size()));
        sb.append(String.format("  %-20s %d%n", "Coordinators:", store.getUsersByType(Coordinator.class).size()));
        sb.append(String.format("  %-20s %d%n", "Volunteers:", store.getUsersByType(Volunteer.class).size()));
        sb.append(String.format("  %-20s %d%n", "Beneficiaries:", store.getUsersByType(Beneficiary.class).size()));
        return sb.toString();
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
            if (!email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) { error("Please enter a valid email address."); return; }
            if (!contact.matches("\\d{11}")) { error("Contact number must be 11 digits (e.g. 09171234567)."); return; }
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
            store.setCurrentUser(null);
            loginView.setEmail(store.getRememberedEmail());
            frame.showScreen(MainFrame.LOGIN);
        }
    }

    private class AddDonationAction implements ActionListener {
        @Override public void actionPerformed(ActionEvent e) {
            String food = addView.getFoodName().trim();
            String pickup = addView.getPickupLocation().trim();
            if (food.isEmpty()) { error("Please enter the food name."); return; }
            double qty;
            try {
                qty = Double.parseDouble(addView.getQuantityText().trim());
            } catch (NumberFormatException ex) {
                error("Quantity must be a number."); return;
            }
            if (qty <= 0) { error("Quantity must be greater than zero."); return; }
            if (pickup.isEmpty()) { error("Please enter the pickup location."); return; }
            if (addView.getDeadline().isBefore(LocalDate.now())) { error("Distribution deadline cannot be in the past."); return; }

            store.createDonation(food, addView.getDonationType(), qty, addView.getUnit(), pickup,
                    addView.getDeadline(), addView.getCondition(), addView.getNotes(),
                    store.getCurrentUser().getFullName(), LocalDate.now());
            addView.clear();
            info("Donation submitted", "Thank you! Your donation was added and is now Pending approval.");
            goTo(DashboardView.DONATIONS);
        }
    }

    private class StatusAction implements ActionListener {
        private final TablePage page;
        private final String newStatus;
        private final List<String> allowedFrom;

        StatusAction(TablePage page, String newStatus, String... allowedFrom) {
            this.page = page;
            this.newStatus = newStatus;
            this.allowedFrom = List.of(allowedFrom);
        }

        @Override public void actionPerformed(ActionEvent e) {
            if (!store.getCurrentUser().canManageCollection()) {
                info("Not allowed", "Only coordinators and volunteers can update donation status.");
                return;
            }
            int id = page.getSelectedId();
            if (id < 0) { info("Select a donation", "Please select a donation from the table first."); return; }
            Donation d = store.findDonation(id);
            if (d == null || !allowedFrom.contains(d.getStatus())) {
                error("This donation is " + (d == null ? "missing" : d.getStatus()) + " and can't be changed to " + newStatus + ".");
                return;
            }
            d.setStatus(newStatus);
            refresh();
        }
    }

    /** Runs an action when a label/panel is clicked (links and stat cards). */
    private class LinkClick extends MouseAdapter {
        private final Runnable action;
        LinkClick(Runnable action) { this.action = action; }
        @Override public void mouseClicked(MouseEvent e) { action.run(); }
    }
}
