package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * One food donation. Status flow: Pending -> Available -> Collected -> Distributed.
 * Collection flow (separate from status): Pending -> Ongoing -> Completed.
 */
public class Donation implements Serializable {
    public static final String PENDING = "Pending";
    public static final String AVAILABLE = "Available";
    public static final String COLLECTED = "Collected";
    public static final String DISTRIBUTED = "Distributed";

    // collection progress (shown on the Collection Management page)
    public static final String COL_PENDING = "Pending";
    public static final String COL_ONGOING = "Ongoing";
    public static final String COL_COMPLETED = "Completed";

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private final int id;
    private String foodName;
    private String donationType;
    private double quantity;
    private String unit;
    private String pickupLocation;
    private LocalDate deadline;
    private String condition;
    private String notes;
    private String donorName;
    private LocalDate dateAdded;
    private String status = PENDING;

    // collection info
    private String assignedVolunteer = "";
    private LocalDate pickupDate;
    private String collectionStatus = COL_PENDING;

    // distribution info
    private double distributedQuantity;

    public Donation(int id, String foodName, String donationType, double quantity, String unit,
                    String pickupLocation, LocalDate deadline, String condition, String notes,
                    String donorName, LocalDate dateAdded) {
        this.id = id;
        this.foodName = foodName;
        this.donationType = donationType;
        this.quantity = quantity;
        this.unit = unit;
        this.pickupLocation = pickupLocation;
        this.deadline = deadline;
        this.condition = condition;
        this.notes = notes;
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
    public String getDonorName() { return donorName; }
    public LocalDate getDateAdded() { return dateAdded; }
    public String getStatus() { return status; }

    /** Keeps the collection progress and distributed amount in sync with the main status. */
    public void setStatus(String status) {
        this.status = status;
        if (status.equals(COLLECTED) || status.equals(DISTRIBUTED)) collectionStatus = COL_COMPLETED;
        if (status.equals(DISTRIBUTED)) distributedQuantity = quantity;
    }

    // ---- collection ----
    public String getAssignedVolunteer() { return assignedVolunteer; }
    public boolean hasVolunteer() { return !assignedVolunteer.isEmpty(); }
    public String getAssignedVolunteerText() { return hasVolunteer() ? assignedVolunteer : "Not assigned"; }
    public LocalDate getPickupDate() { return pickupDate; }
    public String getPickupText() { return pickupDate == null ? "Not set" : pickupDate.format(FORMAT); }
    public String getCollectionStatus() { return collectionStatus; }
    public void setCollectionStatus(String collectionStatus) { this.collectionStatus = collectionStatus; }

    public void assignVolunteer(String name, LocalDate pickup) {
        this.assignedVolunteer = name;
        this.pickupDate = pickup;
        this.collectionStatus = COL_ONGOING;
    }

    public void clearVolunteer() {
        this.assignedVolunteer = "";
        this.pickupDate = null;
    }

    // ---- distribution ----
    public double getDistributedQuantity() { return distributedQuantity; }
    public void addDistributed(double amount) { distributedQuantity += amount; }
    public double getRemainingQuantity() { return Math.max(0, quantity - distributedQuantity); }
    public String getRemainingText() { return formatNumber(getRemainingQuantity()) + " " + unit; }

    // ---- display text ----
    public String getQuantityText() {
        String q = (quantity == Math.rint(quantity)) ? String.valueOf((long) quantity) : String.valueOf(quantity);
        return q + " " + unit;
    }
    public String getDateText() { return dateAdded.format(FORMAT); }
    public String getDeadlineText() { return deadline.format(FORMAT); }

    private static String formatNumber(double q) {
        if (q == Math.rint(q)) return String.valueOf((long) q);
        return BigDecimal.valueOf(q).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}