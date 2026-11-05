package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * NutriLink's SQL database: an H2 database that lives in memory only.
 * It is created when the program starts and disappears when the program closes, so no file is ever written.
 * It is used through JDBC (needs the H2 .jar in the lib folder).
 *
 * DataStore keeps the objects the screens work with and calls sync(...) after every change, so the tables
 * always match. The queries below (counts, filters, lookups) are real SQL that return the matching ids.
 */
public final class Database {
    private static final AtomicInteger COUNTER = new AtomicInteger();

    private final Connection connection;

    public Database() {
        try {
            // a different name each time, so two databases in one run never mix
            connection = DriverManager.getConnection("jdbc:h2:mem:nutrilink" + COUNTER.incrementAndGet() + ";DB_CLOSE_DELAY=-1", "sa", "");
            createTables();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not start the in-memory database. "
                    + "Is the H2 .jar (h2-....jar) in the lib folder and listed under Referenced Libraries? " + e.getMessage(), e);
        }
    }

    // =====================================================================
    //  tables
    // =====================================================================
    private void createTables() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE users ("
                    + "id INT PRIMARY KEY, "
                    + "user_role VARCHAR NOT NULL, "
                    + "full_name VARCHAR NOT NULL, "
                    + "email VARCHAR_IGNORECASE NOT NULL UNIQUE, "
                    + "contact_number VARCHAR NOT NULL, "
                    + "account_type VARCHAR, "           // donor type / beneficiary type
                    + "organization VARCHAR, "
                    + "location VARCHAR, "               // pickup, delivery, assigned or service area
                    + "household_size INT, "
                    + "job_position VARCHAR, "
                    + "activity VARCHAR, "
                    + "availability VARCHAR, "
                    + "notify_updates BOOLEAN NOT NULL, "
                    + "notify_expiry BOOLEAN NOT NULL, "
                    + "confirm_logout BOOLEAN NOT NULL, "
                    + "default_report_range VARCHAR)");
            st.execute("CREATE TABLE donations ("
                    + "id INT PRIMARY KEY, "
                    + "donor_id INT NOT NULL REFERENCES users(id), "
                    + "donor_name VARCHAR NOT NULL, "
                    + "food_name VARCHAR NOT NULL, "
                    + "donation_type VARCHAR NOT NULL, "
                    + "quantity DOUBLE PRECISION NOT NULL, "
                    + "unit VARCHAR NOT NULL, "
                    + "pickup_location VARCHAR NOT NULL, "
                    + "deadline DATE NOT NULL, "
                    + "food_condition VARCHAR, "
                    + "notes VARCHAR, "
                    + "date_added DATE NOT NULL, "
                    + "status VARCHAR NOT NULL, "
                    + "collection_status VARCHAR NOT NULL, "
                    + "assigned_volunteer_id INT REFERENCES users(id), "
                    + "assigned_volunteer VARCHAR, "
                    + "pickup_date DATE, "
                    + "collected_date DATE, "
                    + "distributed_quantity DOUBLE PRECISION NOT NULL)");
            st.execute("CREATE TABLE distributions ("
                    + "id INT PRIMARY KEY, "
                    + "donation_id INT NOT NULL REFERENCES donations(id), "
                    + "food_name VARCHAR NOT NULL, "
                    + "beneficiary_id INT NOT NULL REFERENCES users(id), "
                    + "beneficiary_name VARCHAR NOT NULL, "
                    + "quantity DOUBLE PRECISION NOT NULL, "
                    + "unit VARCHAR, "
                    + "distribution_date DATE NOT NULL)");
            st.execute("CREATE TABLE notifications ("
                    + "id INT PRIMARY KEY, "
                    + "recipient_id INT NOT NULL REFERENCES users(id), "
                    + "category VARCHAR NOT NULL, "
                    + "notif_type VARCHAR NOT NULL, "
                    + "title VARCHAR NOT NULL, "
                    + "message VARCHAR NOT NULL, "
                    + "created_at TIMESTAMP NOT NULL, "
                    + "donation_id INT REFERENCES donations(id), "
                    + "link VARCHAR, "
                    + "is_read BOOLEAN NOT NULL)");
            st.execute("CREATE INDEX idx_donations_status ON donations(status)");
            st.execute("CREATE INDEX idx_donations_donor ON donations(donor_id)");
            st.execute("CREATE INDEX idx_notifications_recipient ON notifications(recipient_id)");
        }
    }

    // =====================================================================
    //  writing: make the tables match the objects
    // =====================================================================
    /** Writes the current objects into the tables (one transaction: all of it, or none of it). */
    public synchronized void sync(Collection<User> users, Collection<Donation> donations,
                                  Collection<Distribution> distributions, Collection<Notification> notifications) {
        try {
            connection.setAutoCommit(false);
            try {
                writeUsers(users);
                writeDonations(donations);
                writeDistributions(distributions);
                writeNotifications(notifications);
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not update the database: " + e.getMessage(), e);
        }
    }

    /** Empties every table (children first, because of the foreign keys). */
    public synchronized void clearAll() {
        try (Statement st = connection.createStatement()) {
            st.execute("DELETE FROM notifications");
            st.execute("DELETE FROM distributions");
            st.execute("DELETE FROM donations");
            st.execute("DELETE FROM users");
        } catch (SQLException e) {
            throw new IllegalStateException("Could not clear the database: " + e.getMessage(), e);
        }
    }

    private void writeUsers(Collection<User> users) throws SQLException {
        String sql = "MERGE INTO users (id, user_role, full_name, email, contact_number, account_type, organization, "
                + "location, household_size, job_position, activity, availability, notify_updates, notify_expiry, "
                + "confirm_logout, default_report_range) KEY (id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (User u : users) {
                String accountType = null, organization = null, location = null, position = null, activity = null, availability = null;
                Integer household = null;
                if (u instanceof Donor) {
                    Donor d = (Donor) u;
                    accountType = d.getDonorType(); organization = d.getOrganizationName(); location = d.getPickupLocation();
                } else if (u instanceof Beneficiary) {
                    Beneficiary b = (Beneficiary) u;
                    accountType = b.getBeneficiaryType(); organization = b.getOrganizationName(); location = b.getDeliveryLocation();
                    household = b.getHouseholdSize() > 0 ? b.getHouseholdSize() : null;
                } else if (u instanceof Coordinator) {
                    Coordinator c = (Coordinator) u;
                    organization = c.getOrganizationName(); position = c.getPosition(); location = c.getAssignedArea();
                } else if (u instanceof Volunteer) {
                    Volunteer v = (Volunteer) u;
                    location = v.getServiceArea(); activity = v.getActivityPreference(); availability = v.getAvailability();
                }
                UserSettings s = u.getSettings();
                int i = 1;
                ps.setInt(i++, u.getId());
                ps.setString(i++, u.getRole());
                ps.setString(i++, u.getFullName());
                ps.setString(i++, u.getEmail().trim());
                ps.setString(i++, u.getContactNumber());
                ps.setString(i++, blankToNull(accountType));
                ps.setString(i++, blankToNull(organization));
                ps.setString(i++, blankToNull(location));
                if (household == null) ps.setNull(i++, Types.INTEGER); else ps.setInt(i++, household);
                ps.setString(i++, blankToNull(position));
                ps.setString(i++, blankToNull(activity));
                ps.setString(i++, blankToNull(availability));
                ps.setBoolean(i++, s.isNotifyUpdates());
                ps.setBoolean(i++, s.isNotifyExpiry());
                ps.setBoolean(i++, s.isConfirmLogout());
                ps.setString(i, s.getDefaultReportRange());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void writeDonations(Collection<Donation> donations) throws SQLException {
        String sql = "MERGE INTO donations (id, donor_id, donor_name, food_name, donation_type, quantity, unit, "
                + "pickup_location, deadline, food_condition, notes, date_added, status, collection_status, "
                + "assigned_volunteer_id, assigned_volunteer, pickup_date, collected_date, distributed_quantity) "
                + "KEY (id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Donation d : donations) {
                int i = 1;
                ps.setInt(i++, d.getId());
                ps.setInt(i++, d.getDonorId());
                ps.setString(i++, d.getDonorName());
                ps.setString(i++, d.getFoodName());
                ps.setString(i++, d.getDonationType());
                ps.setDouble(i++, d.getQuantity());
                ps.setString(i++, d.getUnit());
                ps.setString(i++, d.getPickupLocation());
                setDate(ps, i++, d.getDeadline());
                ps.setString(i++, d.getCondition());
                ps.setString(i++, d.getNotes());
                setDate(ps, i++, d.getDateAdded());
                ps.setString(i++, d.getStatus());
                ps.setString(i++, d.getCollectionStatus());
                if (d.hasVolunteer()) ps.setInt(i++, d.getAssignedVolunteerId()); else ps.setNull(i++, Types.INTEGER);
                ps.setString(i++, d.hasVolunteer() ? d.getAssignedVolunteer() : null);
                setDate(ps, i++, d.getPickupDate());
                setDate(ps, i++, d.getCollectedDate());
                ps.setDouble(i, d.getDistributedQuantity());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void writeDistributions(Collection<Distribution> distributions) throws SQLException {
        String sql = "MERGE INTO distributions (id, donation_id, food_name, beneficiary_id, beneficiary_name, quantity, unit, "
                + "distribution_date) KEY (id) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Distribution x : distributions) {
                int i = 1;
                ps.setInt(i++, x.getId());
                ps.setInt(i++, x.getDonationId());
                ps.setString(i++, x.getFoodName());
                ps.setInt(i++, x.getBeneficiaryId());
                ps.setString(i++, x.getBeneficiaryName());
                ps.setDouble(i++, x.getQuantity());
                ps.setString(i++, x.getUnit());
                setDate(ps, i, x.getDate());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** Notifications can be removed (Clear all, or the 300 message limit), so this table is rewritten. */
    private void writeNotifications(Collection<Notification> notifications) throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("DELETE FROM notifications");
        }
        String sql = "INSERT INTO notifications (id, recipient_id, category, notif_type, title, message, created_at, "
                + "donation_id, link, is_read) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Notification n : notifications) {
                int i = 1;
                ps.setInt(i++, n.getId());
                ps.setInt(i++, n.getRecipientId());
                ps.setString(i++, n.getCategory().name());
                ps.setString(i++, n.getType().name());
                ps.setString(i++, n.getTitle());
                ps.setString(i++, n.getMessage());
                ps.setTimestamp(i++, Timestamp.valueOf(n.getCreatedAt()));
                if (n.getDonationId() > 0) ps.setInt(i++, n.getDonationId()); else ps.setNull(i++, Types.INTEGER);
                ps.setString(i++, n.getLink());
                ps.setBoolean(i, n.isRead());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** Details a user did not give are stored as NULL instead of empty text. */
    private static String blankToNull(String text) { return (text == null || text.isBlank()) ? null : text; }

    private static void setDate(PreparedStatement ps, int index, LocalDate date) throws SQLException {
        if (date == null) ps.setNull(index, Types.DATE); else ps.setObject(index, date);
    }

    // =====================================================================
    //  reading: SQL queries that return ids (DataStore turns the ids into objects)
    // =====================================================================
    /** The id of the user with this email (any capital letters), or null. */
    public synchronized Integer findUserIdByEmail(String email) {
        List<Integer> ids = ids("SELECT id FROM users WHERE email = ?", email.trim());
        return ids.isEmpty() ? null : ids.get(0);
    }

    public synchronized int countDonationsByStatus(String... statuses) {
        if (statuses.length == 0) return 0;
        return count("SELECT COUNT(*) FROM donations WHERE status IN (" + marks(statuses.length) + ")", (Object[]) statuses);
    }

    public synchronized List<Integer> donationIdsByStatus(String... statuses) {
        if (statuses.length == 0) return new ArrayList<>();
        return ids("SELECT id FROM donations WHERE status IN (" + marks(statuses.length) + ") ORDER BY id", (Object[]) statuses);
    }

    public synchronized List<Integer> donationIdsByDonor(int donorId) {
        return ids("SELECT id FROM donations WHERE donor_id = ? ORDER BY id", donorId);
    }

    /** Pending tab = approved donations that still need a volunteer; the other tabs match the collection status. */
    public synchronized List<Integer> donationIdsByCollectionStatus(String collectionStatus) {
        if (collectionStatus.equals(Donation.COL_PENDING)) {
            return ids("SELECT id FROM donations WHERE status = ? AND collection_status = ? ORDER BY id",
                    Donation.AVAILABLE, Donation.COL_PENDING);
        }
        return ids("SELECT id FROM donations WHERE collection_status = ? ORDER BY id", collectionStatus);
    }

    /** Collected donations with food left that have not expired. */
    public synchronized List<Integer> donationIdsReadyToDistribute(LocalDate today) {
        return ids("SELECT id FROM donations WHERE status = ? AND quantity - distributed_quantity > 0 AND deadline >= ? ORDER BY id",
                Donation.COLLECTED, today);
    }

    public synchronized List<Integer> distributionIdsForDonor(int donorId) {
        return ids("SELECT x.id FROM distributions x JOIN donations d ON d.id = x.donation_id WHERE d.donor_id = ? ORDER BY x.id", donorId);
    }

    public synchronized int countUnreadNotifications(int userId) {
        return count("SELECT COUNT(*) FROM notifications WHERE recipient_id = ? AND is_read = FALSE", userId);
    }

    /** Newest first. */
    public synchronized List<Integer> notificationIdsFor(int userId) {
        return ids("SELECT id FROM notifications WHERE recipient_id = ? ORDER BY created_at DESC, id DESC", userId);
    }

    /** Row counts, handy for checking the database (for example in a test). */
    public synchronized int countRows(String table) {
        switch (table) {
            case "users": case "donations": case "distributions": case "notifications":
                return count("SELECT COUNT(*) FROM " + table);
            default:
                throw new IllegalArgumentException("Unknown table: " + table);
        }
    }

    // ---- small JDBC helpers ----
    private static String marks(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(i == 0 ? "?" : ",?");
        return sb.toString();
    }

    private List<Integer> ids(String sql, Object... params) {
        List<Integer> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database query failed: " + e.getMessage(), e);
        }
        return result;
    }

    private int count(String sql, Object... params) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database query failed: " + e.getMessage(), e);
        }
    }

    private static void bind(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
    }
}
