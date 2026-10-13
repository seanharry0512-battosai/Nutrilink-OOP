package model;

public class Volunteer extends User {
    public Volunteer(String fullName, String email, String contactNumber, String password) {
        super(fullName, email, contactNumber, password);
    }
    @Override public String getRole() { return "Volunteer"; }
    @Override public boolean canManageCollection() { return true; }
}
