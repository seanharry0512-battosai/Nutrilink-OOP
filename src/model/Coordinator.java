package model;

import java.util.ArrayList;
import java.util.List;

public class Coordinator extends User {
    private static final long serialVersionUID = 2L;

    // sign-up details (empty in accounts saved before these existed)
    private String organizationName;
    private String position;
    private String assignedArea;

    public Coordinator(String fullName, String email, String contactNumber, String password) {
        super(fullName, email, contactNumber, password);
    }

    public String getOrganizationName() { return organizationName == null ? "" : organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    public String getPosition() { return position == null ? "" : position; }
    public void setPosition(String position) { this.position = position; }
    public String getAssignedArea() { return assignedArea == null ? "" : assignedArea; }
    public void setAssignedArea(String assignedArea) { this.assignedArea = assignedArea; }

    @Override public String getRole() { return "Coordinator"; }
    @Override public boolean canAddDonation() { return true; }
    @Override public boolean canApproveDonation() { return true; }
    @Override public boolean canManageCollection() { return true; }
    @Override public boolean canViewDonations() { return true; }
    @Override public boolean canViewInventory() { return true; }
    @Override public boolean canViewBeneficiaries() { return true; }
    @Override public boolean canViewReports() { return true; }
    @Override public boolean canManageSystem() { return true; }

    @Override public List<String[]> getActivitySummary(DataStore store) {
        int waiting = store.countByStatus(Donation.PENDING);
        int ongoing = store.getDonationsByCollectionStatus(Donation.COL_ONGOING).size();
        List<String[]> list = new ArrayList<>();
        list.add(new String[]{"Awaiting approval", String.valueOf(waiting)});
        list.add(new String[]{"Ongoing pickups", String.valueOf(ongoing)});
        list.add(new String[]{"Distribution records", String.valueOf(store.getDistributions().size())});
        return list;
    }
}
