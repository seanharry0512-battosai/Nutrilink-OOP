package model;

import java.util.ArrayList;
import java.util.List;

public class Volunteer extends User {
    private static final long serialVersionUID = 2L;

    public Volunteer(String fullName, String email, String contactNumber, String password) {
        super(fullName, email, contactNumber, password);
    }

    @Override public String getRole() { return "Volunteer"; }
    @Override public boolean canManageCollection() { return true; }
    @Override public boolean canViewDonations() { return true; }
    @Override public boolean canViewInventory() { return true; }

    @Override public List<String[]> getActivitySummary(DataStore store) {
        int assigned = 0, done = 0;
        for (Donation d : store.getDonations()) {
            if (d.getAssignedVolunteerId() != getId()) continue;
            assigned++;
            if (d.getCollectionStatus().equals(Donation.COL_COMPLETED)) done++;
        }
        List<String[]> list = new ArrayList<>();
        list.add(new String[]{"Pickups assigned", String.valueOf(assigned)});
        list.add(new String[]{"Pickups completed", String.valueOf(done)});
        return list;
    }
}
