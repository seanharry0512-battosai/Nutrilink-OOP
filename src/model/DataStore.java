package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Keeps all data in memory (no database). */
public class DataStore {
    private final ArrayList<User> users = new ArrayList<>();
    private final ArrayList<Donation> donations = new ArrayList<>();
    private final ArrayList<Distribution> distributions = new ArrayList<>();
    private int nextDonationId = 1;
    private int nextDistributionId = 1;
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

    // ---- collection ----

    /**
     * Donations for one tab of the Collection page.
     * Pending tab = approved (Available) donations that still need a volunteer.
     */
    public List<Donation> getDonationsByCollectionStatus(String collectionStatus) {
        List<Donation> result = new ArrayList<>();
        for (Donation d : donations) {
            boolean match;
            if (collectionStatus.equals(Donation.COL_PENDING)) {
                match = d.getStatus().equals(Donation.AVAILABLE)
                        && d.getCollectionStatus().equals(Donation.COL_PENDING);
            } else {
                match = d.getCollectionStatus().equals(collectionStatus);
            }
            if (match) result.add(d);
        }
        return result;
    }

    /** Assigns a volunteer and moves the collection to Ongoing. Returns an error message, or null if OK. */
    public String assignVolunteer(Donation d, String volunteerName, LocalDate pickupDate) {
        if (d == null) return "No donation selected.";
        if (volunteerName == null || volunteerName.isBlank()) return "Please choose a volunteer.";
        if (d.getStatus().equals(Donation.PENDING)) return "This donation is still waiting for approval.";
        if (d.getStatus().equals(Donation.COLLECTED) || d.getStatus().equals(Donation.DISTRIBUTED)) {
            return "This donation was already collected.";
        }
        if (pickupDate.isBefore(LocalDate.now())) return "Pickup date cannot be in the past.";
        if (pickupDate.isAfter(d.getDeadline())) return "Pickup date cannot be after the deadline (" + d.getDeadlineText() + ").";
        d.assignVolunteer(volunteerName, pickupDate);
        return null;
    }

    /** Changes the collection status (Pending / Ongoing / Completed). Returns an error message, or null if OK. */
    public String updateCollectionStatus(Donation d, String newStatus) {
        if (d == null) return "No donation selected.";
        if (d.getStatus().equals(Donation.PENDING)) return "This donation is still waiting for approval.";
        if (d.getStatus().equals(Donation.DISTRIBUTED) || d.getDistributedQuantity() > 0) {
            return "Food from this donation was already distributed, so its status can't be changed.";
        }
        if (d.getCollectionStatus().equals(newStatus)) return "Collection status is already " + newStatus + ".";
        if (!newStatus.equals(Donation.COL_PENDING) && !d.hasVolunteer()) return "Assign a volunteer first.";

        if (newStatus.equals(Donation.COL_COMPLETED)) {
            d.setStatus(Donation.COLLECTED);          // also sets collection status to Completed
        } else {
            d.setStatus(Donation.AVAILABLE);          // going back from Completed
            d.setCollectionStatus(newStatus);
            if (newStatus.equals(Donation.COL_PENDING)) d.clearVolunteer();
        }
        return null;
    }

    // ---- distribution ----
    public List<Distribution> getDistributions() { return distributions; }

    /** Collected donations that still have food left to give out. */
    public List<Donation> getDonationsReadyToDistribute() {
        List<Donation> result = new ArrayList<>();
        for (Donation d : donations) {
            if (d.getStatus().equals(Donation.COLLECTED) && d.getRemainingQuantity() > 0) result.add(d);
        }
        return result;
    }

    /** Records food given to a beneficiary. Returns an error message, or null if OK. */
    public String recordDistribution(Donation d, String beneficiaryName, double qty, LocalDate date) {
        if (d == null) return "Please select a food item from the list.";
        if (!d.getStatus().equals(Donation.COLLECTED)) return "Only collected food can be distributed.";
        if (beneficiaryName == null || beneficiaryName.isBlank()) return "Please select a beneficiary.";
        if (qty <= 0) return "Quantity must be greater than zero.";
        if (qty > d.getRemainingQuantity() + 1e-9) {
            return "Only " + d.getRemainingText() + " of " + d.getFoodName() + " is left.";
        }
        d.addDistributed(qty);
        distributions.add(new Distribution(nextDistributionId++, d.getId(), d.getFoodName(),
                beneficiaryName, qty, d.getUnit(), date));
        if (d.getRemainingQuantity() <= 1e-9) d.setStatus(Donation.DISTRIBUTED);
        return null;
    }

    // ---- session ----
    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User currentUser) { this.currentUser = currentUser; }
    public String getRememberedEmail() { return rememberedEmail; }
    public void setRememberedEmail(String rememberedEmail) { this.rememberedEmail = rememberedEmail; }
}