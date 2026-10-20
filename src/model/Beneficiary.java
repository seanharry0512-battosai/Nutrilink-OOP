package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Beneficiary extends User {
    private static final long serialVersionUID = 2L;

    public static final String[] TYPES = {"Individual", "Family", "Organization"};

    public static boolean needsOrganization(String type) { return "Organization".equals(type); }
    /** Individuals and families tell us their household size; organizations do not. */
    public static boolean needsHousehold(String type) { return !needsOrganization(type); }

    // sign-up details (empty in accounts saved before these existed)
    private String beneficiaryType;
    private int householdSize;
    private String organizationName;
    private String deliveryLocation;

    public Beneficiary(String fullName, String email, String contactNumber, String password) {
        super(fullName, email, contactNumber, password);
    }

    public String getBeneficiaryType() { return beneficiaryType == null ? "" : beneficiaryType; }
    public void setBeneficiaryType(String beneficiaryType) { this.beneficiaryType = beneficiaryType; }
    public int getHouseholdSize() { return householdSize; }
    public void setHouseholdSize(int householdSize) { this.householdSize = householdSize; }
    public String getOrganizationName() { return organizationName == null ? "" : organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    public String getDeliveryLocation() { return deliveryLocation == null ? "" : deliveryLocation; }
    public void setDeliveryLocation(String deliveryLocation) { this.deliveryLocation = deliveryLocation; }

    @Override public String getRole() { return "Beneficiary"; }
    @Override public boolean canViewInventory() { return true; }

    @Override public List<String[]> getActivitySummary(DataStore store) {
        int times = 0;
        Set<Integer> foods = new HashSet<>();
        for (Distribution d : store.getDistributions()) {
            if (d.getBeneficiaryId() != getId()) continue;
            times++;
            foods.add(d.getDonationId());
        }
        List<String[]> list = new ArrayList<>();
        list.add(new String[]{"Times food received", String.valueOf(times)});
        list.add(new String[]{"Different donations", String.valueOf(foods.size())});
        return list;
    }
}
