package model;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

/** Base class of every NutriLink account (Topic 2.4 Inheritance). */
public abstract class User implements Serializable {
    private static final long serialVersionUID = 2L;
    private static final SecureRandom RANDOM = new SecureRandom();

    private int id;
    private String fullName;
    private String email;
    private String contactNumber;
    private final String salt;
    private String passwordHash;
    private final UserSettings settings = new UserSettings();
    private byte[] avatarImage;   // profile photo as PNG bytes; null = show the initial

    public User(String fullName, String email, String contactNumber, String password) {
        this.fullName = fullName;
        this.email = email;
        this.contactNumber = contactNumber;
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        this.salt = Base64.getEncoder().encodeToString(bytes);
        this.passwordHash = hash(password);
    }

    /** Set once by DataStore when the account is added. */
    void setId(int id) { this.id = id; }
    public int getId() { return id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public UserSettings getSettings() { return settings; }
    public byte[] getAvatarImage() { return avatarImage; }
    public void setAvatarImage(byte[] avatarImage) { this.avatarImage = avatarImage; }

    // ---- password: only a salted hash is stored (Topic 1.7 Encapsulation) ----
    private String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(md.digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean checkPassword(String attempt) {
        return attempt != null && MessageDigest.isEqual(
                hash(attempt).getBytes(StandardCharsets.UTF_8), passwordHash.getBytes(StandardCharsets.UTF_8));
    }

    /** Changes the password if the current one is correct. */
    public boolean changePassword(String current, String next) {
        if (!checkPassword(current)) return false;
        passwordHash = hash(next);
        return true;
    }

    public String getFirstName() {
        String name = fullName.trim();
        return name.isEmpty() ? "" : name.split("\\s+")[0];
    }

    public String getInitial() {
        String name = fullName.trim();
        return name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
    }

    // ---- Polymorphism (Topic 2.7): each subclass says what it may do ----
    public abstract String getRole();
    public boolean canAddDonation() { return false; }
    public boolean canApproveDonation() { return false; }
    public boolean canManageCollection() { return false; }    // collection + distribution
    public boolean canViewDonations() { return false; }
    public boolean canViewInventory() { return false; }
    public boolean canViewBeneficiaries() { return false; }
    public boolean canViewReports() { return false; }
    public boolean canManageSystem() { return false; }
    /** Donors only see their own donations; everyone else sees all of them. */
    public boolean seesAllDonations() { return true; }

    /** Small "activity" numbers for the Profile page: {label, value}. */
    public List<String[]> getActivitySummary(DataStore store) { return List.of(); }

    @Override
    public String toString() { return fullName + " (" + getRole() + ")"; }
}
