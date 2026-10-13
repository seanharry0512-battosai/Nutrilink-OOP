package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Keeps all data in memory (no database). */
public class DataStore {
    private final ArrayList<User> users = new ArrayList<>();
    private final ArrayList<Donation> donations = new ArrayList<>();
    private int nextDonationId = 1;
    private User currentUser;
    private String rememberedEmail = "";

    public DataStore() { seed(); }

    /** Demo data so the system is not empty. All demo passwords: 123456 */
    private void seed() {
        users.add(new Donor("Jessica Santos", "jessica@gmail.com", "09171234567", "123456"));
        users.add(new Coordinator("Carlo Reyes", "coordinator@gmail.com", "09181234567", "123456"));
        users.add(new Volunteer("Mika Cruz", "volunteer@gmail.com", "09191234567", "123456"));
        users.add(new Beneficiary("Juan Dela Cruz", "beneficiary@gmail.com", "09201234567", "123456"));

        createDonation("Chicken Meals", "Cooked Food", 20, "packs", "Quezon City", LocalDate.of(2026, 10, 20),
                "Fresh", "", "Jessica Santos", LocalDate.of(2026, 10, 2)).setStatus(Donation.AVAILABLE);
        createDonation("Bread", "Bakery / Bread", 15, "packs", "Quezon City", LocalDate.of(2026, 10, 20),
                "Fresh", "", "Jessica Santos", LocalDate.of(2026, 10, 1)).setStatus(Donation.COLLECTED);
        createDonation("Vegetables", "Vegetables", 10, "kg", "Quezon City", LocalDate.of(2026, 10, 20),
                "Fresh", "", "Jessica Santos", LocalDate.of(2026, 9, 3)).setStatus(Donation.DISTRIBUTED);
        createDonation("Rice", "Rice / Grains", 25, "kg", "Quezon City", LocalDate.of(2026, 10, 20),
                "Safe for Consumption", "", "Jessica Santos", LocalDate.of(2026, 10, 3));
    }

    // ---- users ----
    public List<User> getUsers() { return users; }

    public void addUser(User user) { users.add(user); }

    public User findUserByEmail(String email) {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email.trim())) return u;
        }
        return null;
    }

    /** Returns the user if the email and password match, otherwise null. */
    public User authenticate(String email, String password) {
        User u = findUserByEmail(email);
        return (u != null && u.checkPassword(password)) ? u : null;
    }

    /** Generic method with a bound (Topic 3.4): e.g. getUsersByType(Beneficiary.class). */
    public <T extends User> List<T> getUsersByType(Class<T> type) {
        List<T> result = new ArrayList<>();
        for (User u : users) {
            if (type.isInstance(u)) result.add(type.cast(u));
        }
        return result;
    }

    // ---- donations ----
    public List<Donation> getDonations() { return donations; }

    public Donation createDonation(String foodName, String type, double qty, String unit, String pickup,
                                   LocalDate deadline, String condition, String notes,
                                   String donorName, LocalDate dateAdded) {
        Donation d = new Donation(nextDonationId++, foodName, type, qty, unit, pickup, deadline,
                condition, notes, donorName, dateAdded);
        donations.add(d);
        return d;
    }

    public Donation findDonation(int id) {
        for (Donation d : donations) {
            if (d.getId() == id) return d;
        }
        return null;
    }

    public List<Donation> getDonationsByStatus(String... statuses) {
        List<Donation> result = new ArrayList<>();
        for (Donation d : donations) {
            for (String s : statuses) {
                if (d.getStatus().equals(s)) { result.add(d); break; }
            }
        }
        return result;
    }

    public int countByStatus(String... statuses) { return getDonationsByStatus(statuses).size(); }

    // ---- session ----
    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User currentUser) { this.currentUser = currentUser; }
    public String getRememberedEmail() { return rememberedEmail; }
    public void setRememberedEmail(String rememberedEmail) { this.rememberedEmail = rememberedEmail; }
}
