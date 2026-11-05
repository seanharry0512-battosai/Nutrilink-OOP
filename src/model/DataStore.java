package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Keeps all of NutriLink's data. The data is stored in an in-memory H2 SQL database (see Database): nothing is
 * written to disk, so when the program is closed everything is gone and the next run starts again with the demo
 * accounts and donations.
 *
 * The objects (users, donations...) are kept here for the screens to use, and every change is written to the
 * database tables by save(). Searches and counts are answered by SQL queries.
 */
public class DataStore implements Serializable {
    private static final long serialVersionUID = 2L;
    private static final int MAX_NOTIFICATIONS = 300;

    public static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    public static final Pattern CONTACT = Pattern.compile("\\d{11}");

    private final ArrayList<User> users = new ArrayList<>();
    private final ArrayList<Donation> donations = new ArrayList<>();
    private final ArrayList<Distribution> distributions = new ArrayList<>();
    private final ArrayList<Notification> notifications = new ArrayList<>();
    private final HashSet<String> alertKeys = new HashSet<>();
    private int nextUserId = 1;
    private int nextDonationId = 1;
    private int nextDistributionId = 1;
    private int nextNotificationId = 1;
    private String rememberedEmail = "";

    private transient User currentUser;     // the session is not saved
    private transient Database db;          // the in-memory SQL database

    private DataStore() { }

    // =====================================================================
    //  loading and saving
    // =====================================================================
    /** Starts the in-memory database and fills it with the demo accounts and donations. Nothing is read from disk. */
    public static DataStore load() {
        DataStore s = new DataStore();
        s.db = new Database();
        s.seedDemoData();
        return s;
    }

    /** Writes every change into the database tables. Called after each change. */
    public void save() {
        db.sync(users, donations, distributions, notifications);
    }

    /** Throws everything away and starts again with the demo accounts and donations. */
    public void resetToDemoData() {
        users.clear();
        donations.clear();
        distributions.clear();
        notifications.clear();
        alertKeys.clear();
        nextUserId = nextDonationId = nextDistributionId = nextNotificationId = 1;
        rememberedEmail = "";
        currentUser = null;
        db.clearAll();
        seedDemoData();
    }

    // =====================================================================
    //  demo data (all demo passwords: 123456; dates are relative to today)
    // =====================================================================
    private void seedDemoData() {
        LocalDate today = LocalDate.now();
        Donor jessica = new Donor("Jessica Santos", "jessica@gmail.com", "09171234567", "123456");
        Coordinator carlo = new Coordinator("Carlo Reyes", "coordinator@gmail.com", "09181234567", "123456");
        Volunteer mika = new Volunteer("Mika Cruz", "volunteer@gmail.com", "09191234567", "123456");
        Beneficiary juan = new Beneficiary("Juan Dela Cruz", "beneficiary@gmail.com", "09201234567", "123456");
        Beneficiary maria = new Beneficiary("Maria Lopez", "maria@gmail.com", "09211234567", "123456");
        for (User u : new User[]{jessica, carlo, mika, juan, maria}) addUser(u);

        Donation d;
        d = seedDonation(jessica, "Vegetables", "Vegetables", 10, "kg", 46, -40, "Fresh");
        seedCollected(d, 45);
        seedDistribution(d, juan, 10, 44);

        d = seedDonation(jessica, "Vegetables", "Vegetables", 8, "kg", 52, -45, "Fresh");
        seedCollected(d, 51);
        seedDistribution(d, juan, 8, 50);

        d = seedDonation(jessica, "Fruits", "Fruits", 12, "kg", 40, -33, "Fresh");
        seedCollected(d, 39);
        seedDistribution(d, maria, 12, 37);

        d = seedDonation(jessica, "Canned Goods", "Canned Goods", 40, "pcs", 25, 60, "Safe for Consumption");
        seedCollected(d, 24);
        seedDistribution(d, juan, 25, 22);
        seedDistribution(d, maria, 15, 21);

        d = seedDonation(jessica, "Pastries", "Bakery / Bread", 30, "pcs", 21, -15, "Fresh");
        seedCollected(d, 20);
        seedDistribution(d, maria, 30, 19);

        Donation rice = seedDonation(jessica, "Rice", "Rice / Grains", 25, "kg", 15, 90, "Safe for Consumption");

        d = seedDonation(jessica, "Chicken Meals", "Cooked Food", 20, "packs", 16, 2, "Fresh");
        d.setStatus(Donation.AVAILABLE);

        d = seedDonation(jessica, "Bread", "Bakery / Bread", 15, "packs", 17, 2, "Fresh");
        seedCollected(d, 15);

        d = seedDonation(jessica, "Adobo Meals", "Cooked Food", 30, "packs", 6, 3, "Fresh");
        seedCollected(d, 5);
        seedDistribution(d, maria, 18, 3);

        Donation drinks = seedDonation(jessica, "Bottled Water", "Beverages", 24, "liters", 9, 5, "Safe for Consumption");
        drinks.setStatus(Donation.AVAILABLE);
        drinks.assignVolunteer(mika, today.plusDays(1));

        LocalDateTime now = LocalDateTime.now();
        push(carlo, Notification.Category.UPDATE, Notification.Type.INFO, "New donation waiting",
                "Rice (25 kg) from Jessica Santos is waiting for your approval.", rice.getId(), "Donations", now.minusHours(5));
        push(mika, Notification.Category.UPDATE, Notification.Type.INFO, "New pickup assigned",
                "Bottled Water (24 liters) is assigned to you for pickup on " + drinks.getPickupText() + ".",
                drinks.getId(), "Collection", now.minusHours(2));
        push(jessica, Notification.Category.UPDATE, Notification.Type.SUCCESS, "Food given out",
                "18 packs of Adobo Meals were given to Maria Lopez.", 0, "Donations", now.minusDays(3));
        push(juan, Notification.Category.UPDATE, Notification.Type.SUCCESS, "You received food",
                "You received 15 pcs of Canned Goods.", 0, "Dashboard", now.minusDays(21));
        save();
    }

    private Donation seedDonation(User donor, String food, String type, double qty, String unit,
                                  int addedDaysAgo, int deadlineInDays, String condition) {
        LocalDate today = LocalDate.now();
        Donation d = new Donation(nextDonationId++, food, type, qty, unit, "Quezon City",
                today.plusDays(deadlineInDays), condition, "", donor.getId(), donor.getFullName(),
                today.minusDays(addedDaysAgo));
        donations.add(d);
        return d;
    }

    private void seedCollected(Donation d, int collectedDaysAgo) {
        d.setStatus(Donation.COLLECTED);
        d.setCollectedDate(LocalDate.now().minusDays(collectedDaysAgo));
    }

    private void seedDistribution(Donation d, Beneficiary b, double qty, int daysAgo) {
        distributions.add(new Distribution(nextDistributionId++, d.getId(), d.getFoodName(), b.getId(),
                b.getFullName(), qty, d.getUnit(), LocalDate.now().minusDays(daysAgo)));
        d.addDistributed(qty);
        if (d.getRemainingQuantity() <= 1e-9) d.setStatus(Donation.DISTRIBUTED);
    }

    // =====================================================================
    //  users
    // =====================================================================
    public List<User> getUsers() { return users; }

    public void addUser(User user) {
        user.setId(nextUserId++);
        users.add(user);
        save();
    }

    public User findUserByEmail(String email) {
        if (email == null) return null;
        Integer id = db.findUserIdByEmail(email);
        return id == null ? null : findUserById(id);
    }

    public User findUserById(int id) {
        for (User u : users) {
            if (u.getId() == id) return u;
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

    /** Changes name / email / contact number. Returns an error message, or null if OK. */
    public String updateProfile(User u, String name, String email, String contact) {
        name = name == null ? "" : name.trim();
        email = email == null ? "" : email.trim();
        contact = contact == null ? "" : contact.trim();
        if (name.isEmpty() || email.isEmpty() || contact.isEmpty()) return "Please fill in all fields.";
        if (!EMAIL.matcher(email).matches()) return "Please enter a valid email address.";
        if (!CONTACT.matcher(contact).matches()) return "Contact number must be 11 digits (e.g. 09171234567).";
        User other = findUserByEmail(email);
        if (other != null && other != u) return "Another account already uses that email.";

        u.setFullName(name);
        u.setEmail(email);
        u.setContactNumber(contact);
        for (Donation d : donations) {                       // keep the names shown on old records up to date
            if (d.getDonorId() == u.getId()) d.setDonorName(name);
            if (d.getAssignedVolunteerId() == u.getId()) d.setAssignedVolunteerName(name);
        }
        for (Distribution x : distributions) {
            if (x.getBeneficiaryId() == u.getId()) x.setBeneficiaryName(name);
        }
        save();
        return null;
    }

    /** Returns an error message, or null if the password was changed. */
    public String changePassword(User u, String current, String next, String confirm) {
        if (current.isEmpty() || next.isEmpty() || confirm.isEmpty()) return "Please fill in all password fields.";
        if (!u.checkPassword(current)) return "Your current password is not correct.";
        String weak = PasswordRules.problem(next);
        if (weak != null) return weak;
        if (!next.equals(confirm)) return "The new passwords do not match.";
        if (next.equals(current)) return "The new password must be different from the current one.";
        u.changePassword(current, next);
        save();
        return null;
    }

    // =====================================================================
    //  donations
    // =====================================================================
    public List<Donation> getDonations() { return donations; }

    /** Donors see only their own donations; everyone else sees all of them. */
    public List<Donation> getDonationsVisibleTo(User u) {
        if (u.seesAllDonations()) return new ArrayList<>(donations);
        return donationsFromIds(db.donationIdsByDonor(u.getId()));
    }

    public Donation createDonation(User donor, String foodName, String type, double qty, String unit, String pickup,
                                   LocalDate deadline, String condition, String notes) {
        Donation d = new Donation(nextDonationId++, foodName, type, qty, unit, pickup, deadline,
                condition, notes, donor.getId(), donor.getFullName(), LocalDate.now());
        donations.add(d);
        for (User u : users) {
            if (u.canApproveDonation() && u != donor) {
                push(u, Notification.Category.UPDATE, Notification.Type.INFO, "New donation waiting",
                        foodName + " (" + d.getQuantityText() + ") from " + donor.getFullName()
                                + " is waiting for your approval.", d.getId(), "Donations");
            }
        }
        push(donor, Notification.Category.UPDATE, Notification.Type.INFO, "Donation submitted",
                foodName + " was submitted and is waiting for approval.", d.getId(), "Donations");
        save();
        return d;
    }

    public Donation findDonation(int id) {
        for (Donation d : donations) {
            if (d.getId() == id) return d;
        }
        return null;
    }

    public List<Donation> getDonationsByStatus(String... statuses) {
        return donationsFromIds(db.donationIdsByStatus(statuses));
    }

    public int countByStatus(String... statuses) { return db.countDonationsByStatus(statuses); }

    /** Turns ids returned by a SQL query into the matching donation objects (same order as the ids). */
    private List<Donation> donationsFromIds(List<Integer> ids) {
        Map<Integer, Donation> byId = new HashMap<>();
        for (Donation d : donations) byId.put(d.getId(), d);
        List<Donation> result = new ArrayList<>();
        for (int id : ids) {
            Donation d = byId.get(id);
            if (d != null) result.add(d);
        }
        return result;
    }

    /** Approves a Pending donation. Returns an error message, or null if OK. */
    public String approveDonation(Donation d) {
        if (d == null) return "No donation selected.";
        if (!d.getStatus().equals(Donation.PENDING)) return "This donation is " + d.getStatus() + " and can't be approved.";
        d.setStatus(Donation.AVAILABLE);
        User donor = findUserById(d.getDonorId());
        if (donor != null) {
            push(donor, Notification.Category.UPDATE, Notification.Type.SUCCESS, "Donation approved",
                    "Your donation " + d.getFoodName() + " was approved and is ready for pickup.", d.getId(), "Donations");
        }
        for (User u : users) {
            if (u.canManageCollection() && !u.canApproveDonation()) {
                push(u, Notification.Category.UPDATE, Notification.Type.INFO, "New food ready for pickup",
                        d.getFoodName() + " (" + d.getQuantityText() + ") was approved.", d.getId(), "Collection");
            }
        }
        save();
        return null;
    }

    // =====================================================================
    //  collection
    // =====================================================================
    /**
     * Donations for one tab of the Collection page.
     * Pending tab = approved (Available) donations that still need a volunteer.
     */
    public List<Donation> getDonationsByCollectionStatus(String collectionStatus) {
        return donationsFromIds(db.donationIdsByCollectionStatus(collectionStatus));
    }

    /** Assigns a volunteer and moves the collection to Ongoing. Returns an error message, or null if OK. */
    public String assignVolunteer(Donation d, User volunteer, LocalDate pickupDate) {
        if (d == null) return "No donation selected.";
        if (volunteer == null) return "Please choose a volunteer.";
        if (d.getStatus().equals(Donation.PENDING)) return "This donation is still waiting for approval.";
        if (d.getStatus().equals(Donation.COLLECTED) || d.getStatus().equals(Donation.DISTRIBUTED)) {
            return "This donation was already collected.";
        }
        if (d.isExpired()) return "The deadline of this donation (" + d.getDeadlineText() + ") has already passed.";
        if (pickupDate.isBefore(LocalDate.now())) return "Pickup date cannot be in the past.";
        if (pickupDate.isAfter(d.getDeadline())) return "Pickup date cannot be after the deadline (" + d.getDeadlineText() + ").";
        d.assignVolunteer(volunteer, pickupDate);
        push(volunteer, Notification.Category.UPDATE, Notification.Type.INFO, "New pickup assigned",
                d.getFoodName() + " (" + d.getQuantityText() + ") is assigned to you for pickup on "
                        + d.getPickupText() + ".", d.getId(), "Collection");
        User donor = findUserById(d.getDonorId());
        if (donor != null && donor != volunteer) {
            push(donor, Notification.Category.UPDATE, Notification.Type.INFO, "Volunteer assigned",
                    volunteer.getFullName() + " will pick up " + d.getFoodName() + " on " + d.getPickupText() + ".",
                    d.getId(), "Donations");
        }
        save();
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
            User donor = findUserById(d.getDonorId());
            if (donor != null) {
                push(donor, Notification.Category.UPDATE, Notification.Type.SUCCESS, "Donation collected",
                        d.getFoodName() + " was collected by " + d.getAssignedVolunteer() + ".", d.getId(), "Donations");
            }
            for (User u : users) {
                if (u.canApproveDonation()) {
                    push(u, Notification.Category.UPDATE, Notification.Type.SUCCESS, "Pickup completed",
                            d.getFoodName() + " is now in the inventory.", d.getId(), "Inventory");
                }
            }
        } else {
            d.setStatus(Donation.AVAILABLE);          // going back from Completed
            d.setCollectionStatus(newStatus);
            if (newStatus.equals(Donation.COL_PENDING)) d.clearVolunteer();
        }
        save();
        return null;
    }

    // =====================================================================
    //  distribution
    // =====================================================================
    public List<Distribution> getDistributions() { return distributions; }

    public List<Distribution> getDistributionsVisibleTo(User u) {
        if (u.seesAllDonations()) return new ArrayList<>(distributions);
        Map<Integer, Distribution> byId = new HashMap<>();
        for (Distribution x : distributions) byId.put(x.getId(), x);
        List<Distribution> result = new ArrayList<>();
        for (int id : db.distributionIdsForDonor(u.getId())) {
            Distribution x = byId.get(id);
            if (x != null) result.add(x);
        }
        return result;
    }

    /** Collected donations that still have food left to give out (expired food is left out). */
    public List<Donation> getDonationsReadyToDistribute() {
        return donationsFromIds(db.donationIdsReadyToDistribute(LocalDate.now()));
    }

    /** Records food given to a beneficiary. Returns an error message, or null if OK. */
    public String recordDistribution(Donation d, User beneficiary, double qty, LocalDate date) {
        if (d == null) return "Please select a food item from the list.";
        if (!d.getStatus().equals(Donation.COLLECTED)) return "Only collected food can be distributed.";
        if (d.isExpired()) return d.getFoodName() + " passed its deadline (" + d.getDeadlineText() + ") and can't be distributed.";
        if (beneficiary == null) return "Please select a beneficiary.";
        if (Double.isNaN(qty) || Double.isInfinite(qty) || qty <= 0) return "Quantity must be greater than zero.";
        if (qty > d.getRemainingQuantity() + 1e-9) {
            return "Only " + d.getRemainingText() + " of " + d.getFoodName() + " is left.";
        }
        if (date.isAfter(LocalDate.now())) return "The distribution date cannot be in the future.";
        if (d.getCollectedDate() != null && date.isBefore(d.getCollectedDate())) {
            return "The distribution date cannot be before the food was collected (" + d.getCollectedDate() + ").";
        }

        d.addDistributed(qty);
        Distribution record = new Distribution(nextDistributionId++, d.getId(), d.getFoodName(),
                beneficiary.getId(), beneficiary.getFullName(), qty, d.getUnit(), date);
        distributions.add(record);
        if (d.getRemainingQuantity() <= 1e-9) d.setStatus(Donation.DISTRIBUTED);

        User donor = findUserById(d.getDonorId());
        if (donor != null) {
            push(donor, Notification.Category.UPDATE, Notification.Type.SUCCESS, "Your food reached someone",
                    record.getQuantityText() + " of " + d.getFoodName() + " was given to " + beneficiary.getFullName() + ".",
                    d.getId(), "Donations");
        }
        push(beneficiary, Notification.Category.UPDATE, Notification.Type.SUCCESS, "You received food",
                "You received " + record.getQuantityText() + " of " + d.getFoodName() + ".", 0, "Dashboard");
        save();
        return null;
    }

    // =====================================================================
    //  notifications
    // =====================================================================
    /** Newest first. */
    public List<Notification> getNotificationsFor(User u) {
        Map<Integer, Notification> byId = new HashMap<>();
        for (Notification n : notifications) byId.put(n.getId(), n);
        List<Notification> result = new ArrayList<>();
        for (int id : db.notificationIdsFor(u.getId())) {
            Notification n = byId.get(id);
            if (n != null) result.add(n);
        }
        return result;
    }

    public int countUnread(User u) { return db.countUnreadNotifications(u.getId()); }

    public void markRead(Notification n) {
        if (n != null && !n.isRead()) {
            n.setRead(true);
            save();
        }
    }

    public void markAllRead(User u) {
        for (Notification n : notifications) {
            if (n.getRecipientId() == u.getId()) n.setRead(true);
        }
        save();
    }

    public void clearNotifications(User u) {
        notifications.removeIf(n -> n.getRecipientId() == u.getId());
        save();
    }

    /**
     * Warns about food that is about to expire or already expired. Each warning is created only once,
     * so it is safe to call this every time someone logs in.
     */
    public void generateExpiryAlerts() {
        boolean any = false;
        for (Donation d : donations) {
            boolean live = d.getStatus().equals(Donation.AVAILABLE) || d.getStatus().equals(Donation.COLLECTED);
            if (!live || d.getRemainingQuantity() <= 0) continue;
            boolean expired = d.isExpired();
            if (!expired && !d.isUrgent()) continue;
            if (!alertKeys.add(d.getId() + (expired ? ":expired" : ":soon"))) continue;

            String title = expired ? "Food has expired" : "Food expiring soon";
            String text = d.getFoodName() + " (" + d.getRemainingText() + " left) "
                    + (expired ? "passed its deadline on " : "must be given out by ") + d.getDeadlineText() + ".";
            for (User u : users) {
                if (u.canManageCollection() || u.getId() == d.getDonorId()) {
                    push(u, Notification.Category.EXPIRY, Notification.Type.WARNING, title, text, d.getId(), "Donations");
                }
            }
            any = true;
        }
        if (any) save();
    }

    private void push(User to, Notification.Category category, Notification.Type type, String title, String message,
                      int donationId, String link) {
        push(to, category, type, title, message, donationId, link, LocalDateTime.now());
    }

    private void push(User to, Notification.Category category, Notification.Type type, String title, String message,
                      int donationId, String link, LocalDateTime when) {
        if (category == Notification.Category.UPDATE && !to.getSettings().isNotifyUpdates()) return;
        if (category == Notification.Category.EXPIRY && !to.getSettings().isNotifyExpiry()) return;
        notifications.add(new Notification(nextNotificationId++, to.getId(), category, type, title, message,
                when, donationId, link));
        while (notifications.size() > MAX_NOTIFICATIONS) notifications.remove(0);
    }

    // =====================================================================
    //  session
    // =====================================================================
    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User currentUser) { this.currentUser = currentUser; }
    public String getRememberedEmail() { return rememberedEmail; }

    public void setRememberedEmail(String rememberedEmail) {
        this.rememberedEmail = rememberedEmail;
        save();
    }
}
