package model;

public class Donor extends User {
    public Donor(String fullName, String email, String contactNumber, String password) {
        super(fullName, email, contactNumber, password);
    }
    @Override public String getRole() { return "Food Donor"; }
    @Override public boolean canAddDonation() { return true; }
}
