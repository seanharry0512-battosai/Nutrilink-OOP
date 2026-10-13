package model;

public class Beneficiary extends User {
    public Beneficiary(String fullName, String email, String contactNumber, String password) {
        super(fullName, email, contactNumber, password);
    }
    @Override public String getRole() { return "Beneficiary"; }
}
