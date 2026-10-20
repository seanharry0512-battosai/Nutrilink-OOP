package model;

import java.util.ArrayList;
import java.util.List;

public class Donor extends User {
    private static final long serialVersionUID = 2L;

    public static final String[] TYPES = {"Individual", "Business", "Organization"};

    /** Businesses and organizations give a name; individuals do not. */
    public static boolean needsOrganization(String type) { return !"Individual".equals(type); }

    // sign-up details (empty in accounts saved before these existed)
    private String donorType;
    private String organizationName;
    private String pickupLocation;

    public Donor(String fullName, String email, String contactNumber, String password) {
        super(fullName, email, contactNumber, password);
    }

    public String getDonorType() { return donorType == null ? "" : donorType; }
    public void setDonorType(String donorType) { this.donorType = donorType; }
    public String getOrganizationName() { return organizationName == null ? "" : organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    public String getPickupLocation() { return pickupLocation == null ? "" : pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    @Override public String getRole() { return "Food Donor"; }
    @Override public boolean canAddDonation() { return true; }
    @Override public boolean canViewDonations() { return true; }
    @Override public boolean canViewReports() { return true; }
    @Override public boolean seesAllDonations() { return false; }

    @Override public List<String[]> getActivitySummary(DataStore store) {
        int made = 0, collected = 0, distributed = 0;
        for (Donation d : store.getDonations()) {
            if (d.getDonorId() != getId()) continue;
            made++;
            if (d.getStatus().equals(Donation.COLLECTED) || d.getStatus().equals(Donation.DISTRIBUTED)) collected++;
            if (d.getStatus().equals(Donation.DISTRIBUTED)) distributed++;
        }
        List<String[]> list = new ArrayList<>();
        list.add(new String[]{"Donations made", String.valueOf(made)});
        list.add(new String[]{"Collected", String.valueOf(collected)});
        list.add(new String[]{"Fully distributed", String.valueOf(distributed)});
        return list;
    }
}
