package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Beneficiary extends User {
    private static final long serialVersionUID = 2L;

    public Beneficiary(String fullName, String email, String contactNumber, String password) {
        super(fullName, email, contactNumber, password);
    }

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
