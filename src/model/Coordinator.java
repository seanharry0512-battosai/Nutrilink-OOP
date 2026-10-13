package model;

public class Coordinator extends User {
    public Coordinator(String fullName, String email, String contactNumber, String password) {
        super(fullName, email, contactNumber, password);
    }
    @Override public String getRole() { return "Coordinator"; }
    @Override public boolean canAddDonation() { return true; }
    @Override public boolean canManageCollection() { return true; }
}
