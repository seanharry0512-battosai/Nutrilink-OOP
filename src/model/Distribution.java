package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** One record of food given to a beneficiary. */
public class Distribution implements Serializable {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private final int id;
    private final int donationId;
    private final String foodName;
    private final String beneficiaryName;
    private final double quantity;
    private final String unit;
    private final LocalDate date;

    public Distribution(int id, int donationId, String foodName, String beneficiaryName,
                        double quantity, String unit, LocalDate date) {
        this.id = id;
        this.donationId = donationId;
        this.foodName = foodName;
        this.beneficiaryName = beneficiaryName;
        this.quantity = quantity;
        this.unit = unit;
        this.date = date;
    }

    public int getId() { return id; }
    public int getDonationId() { return donationId; }
    public String getFoodName() { return foodName; }
    public String getBeneficiaryName() { return beneficiaryName; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public LocalDate getDate() { return date; }
    public String getDateText() { return date.format(FORMAT); }

    public String getQuantityText() {
        String unitText = (unit == null || unit.isBlank()) ? "" : unit.trim();
        String q = formatNumber(quantity);
        return unitText.isEmpty() ? q : q + " " + unitText;
    }

    private static String formatNumber(double q) {
        if (q == Math.rint(q)) return String.valueOf((long) q);
        return BigDecimal.valueOf(q).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}