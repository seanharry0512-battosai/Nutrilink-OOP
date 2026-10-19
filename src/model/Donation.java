package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * One food donation. Status flow: Pending -> Available -> Collected -> Distributed.
 * Collection flow (separate from status): Pending -> Ongoing -> Completed.
 */
public class Donation implements Serializable {
    private static final long serialVersionUID = 2L;

    public static final String PENDING = "Pending";
    public static final String AVAILABLE = "Available";
    public static final String COLLECTED = "Collected";
    public static final String DISTRIBUTED = "Distributed";

    // collection progress (shown on the Collection Management page)
    public static final String COL_PENDING = "Pending";
    public static final String COL_ONGOING = "Ongoing";
    public static final String COL_COMPLETED = "Completed";

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

    private final int id;
    private String foodName;
    private String donationType;
    private double quantity;
    private String unit;
    private String pickupLocation;
    private LocalDate deadline;
    private String condition;
    private String notes;
    private final int donorId;
    private String donorName;
    private LocalDate dateAdded;
    private String status = PENDING;

    // collection info
    private int assignedVolunteerId;
    private String assignedVolunteer = "";
    private LocalDate pickupDate;
    private String collectionStatus = COL_PENDING;
    private LocalDate collectedDate;

    // distribution info
    private double distributedQuantity;

    public Donation(int id, String foodName, String donationType, double quantity, String unit,
                    String pickupLocation, LocalDate deadline, String condition, String notes,
                    int donorId, String donorName, LocalDate dateAdded) {
        this.id = id;
        this.foodName = foodName;
        this.donationType = donationType;
        this.quantity = quantity;
        this.unit = unit;
        this.pickupLocation = pickupLocation;
        this.deadline = deadline;
        this.condition = condition;
        this.notes = notes;
        this.donorId = donorId;
        this.donorName = donorName;
        this.dateAdded = dateAdded;
    }

    public int getId() { return id; }
    public String getFoodName() { return foodName; }
    public String getDonationType() { return donationType; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getPickupLocation() { return pickupLocation; }
    public LocalDate getDeadline() { return deadline; }
    public String getCondition() { return condition; }
    public String getNotes() { return notes; }
    public int getDonorId() { return donorId; }
    public String getDonorName() { return donorName; }
    void setDonorName(String donorName) { this.donorName = donorName; }
    public LocalDate getDateAdded() { return dateAdded; }
    public String getStatus() { return status; }

    /** Keeps the collection progress, collected date and distributed amount in sync with the main status. */
    public void setStatus(String status) {
        this.status = status;
        if (status.equals(COLLECTED) || status.equals(DISTRIBUTED)) {
            collectionStatus = COL_COMPLETED;
            if (collectedDate == null) collectedDate = LocalDate.now();
        } else {
            collectedDate = null;                       // going back to Pending / Available
        }
        if (status.equals(DISTRIBUTED)) distributedQuantity = quantity;
    }

    // ---- deadline ----
    public boolean isExpired() { return deadline.isBefore(LocalDate.now()); }
    /** Deadline is today or within the next 2 days (and not yet over). */
    public boolean isUrgent() { return !isExpired() && !deadline.isAfter(LocalDate.now().plusDays(2)); }

    // ---- collection ----
    public int getAssignedVolunteerId() { return assignedVolunteerId; }
    public String getAssignedVolunteer() { return assignedVolunteer; }
    void setAssignedVolunteerName(String name) { if (hasVolunteer()) this.assignedVolunteer = name; }
    public boolean hasVolunteer() { return !assignedVolunteer.isEmpty(); }
    public String getAssignedVolunteerText() { return hasVolunteer() ? assignedVolunteer : "Not assigned"; }
    public LocalDate getPickupDate() { return pickupDate; }
    public String getPickupText() { return pickupDate == null ? "Not set" : pickupDate.format(FORMAT); }
    public String getCollectionStatus() { return collectionStatus; }
    public void setCollectionStatus(String collectionStatus) { this.collectionStatus = collectionStatus; }
    public LocalDate getCollectedDate() { return collectedDate; }
    void setCollectedDate(LocalDate date) { this.collectedDate = date; }

    public void assignVolunteer(User volunteer, LocalDate pickup) {
        this.assignedVolunteerId = volunteer.getId();
        this.assignedVolunteer = volunteer.getFullName();
        this.pickupDate = pickup;
        this.collectionStatus = COL_ONGOING;
    }

    public void clearVolunteer() {
        this.assignedVolunteerId = 0;
        this.assignedVolunteer = "";
        this.pickupDate = null;
    }

    // ---- distribution ----
    public double getDistributedQuantity() { return distributedQuantity; }
    public void addDistributed(double amount) { distributedQuantity += amount; }
    public double getRemainingQuantity() { return Math.max(0, quantity - distributedQuantity); }
    public String getRemainingText() { return formatNumber(getRemainingQuantity()) + " " + unit; }

    // ---- display text ----
    public String getQuantityText() { return formatNumber(quantity) + " " + unit; }
    public String getDateText() { return dateAdded.format(FORMAT); }
    public String getDeadlineText() { return deadline.format(FORMAT); }

    private static String formatNumber(double q) {
        if (q == Math.rint(q)) return String.valueOf((long) q);
        return BigDecimal.valueOf(q).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}
