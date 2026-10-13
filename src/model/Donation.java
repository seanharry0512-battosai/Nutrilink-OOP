package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** One food donation. Status flow: Pending -> Available -> Collected -> Distributed. */
public class Donation implements Serializable {
    public static final String PENDING = "Pending";
    public static final String AVAILABLE = "Available";
    public static final String COLLECTED = "Collected";
    public static final String DISTRIBUTED = "Distributed";

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
    public void setStatus(String status) { this.status = status; }

    public String getQuantityText() {
        String q = (quantity == Math.rint(quantity)) ? String.valueOf((long) quantity) : String.valueOf(quantity);
        return q + " " + unit;
    }
    public String getDateText() { return dateAdded.format(FORMAT); }
    public String getDeadlineText() { return deadline.format(FORMAT); }
}
