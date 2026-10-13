package model;

import java.io.Serializable;

/** Base class of every NutriLink account (Topic 2.4 Inheritance). */
public abstract class User implements Serializable {
    private String fullName;
    private String email;
    private String contactNumber;
    private String password;

    public User(String fullName, String email, String contactNumber, String password) {
        this.fullName = fullName;
        this.email = email;
        this.contactNumber = contactNumber;
        this.password = password;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    /** The password stays private (Topic 1.7 Encapsulation); it can only be checked. */
    public boolean checkPassword(String attempt) { return password.equals(attempt); }

    public String getFirstName() { return fullName.trim().split("\\s+")[0]; }

    // Polymorphism (Topic 2.7): each subclass overrides these.
    public abstract String getRole();
    public boolean canAddDonation() { return false; }
    public boolean canManageCollection() { return false; }

    @Override
    public String toString() { return fullName + " (" + getRole() + ")"; }
}
