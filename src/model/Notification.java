package model;

import java.io.Serializable;
import java.time.LocalDateTime;

/** One message for one user (shown on the Notifications page). */
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type { INFO, SUCCESS, WARNING }
    /** Decides which setting can switch the message off. SYSTEM messages are always delivered. */
    public enum Category { UPDATE, EXPIRY, SYSTEM }

    private final int id;
    private final int recipientId;
    private final Category category;
    private final Type type;
    private final String title;
    private final String message;
    private final LocalDateTime createdAt;
    private final int donationId;      // 0 = not about one donation
    private final String link;         // page name to open when clicked
    private boolean read;

    public Notification(int id, int recipientId, Category category, Type type, String title, String message,
                        LocalDateTime createdAt, int donationId, String link) {
        this.id = id;
        this.recipientId = recipientId;
        this.category = category;
        this.type = type;
        this.title = title;
        this.message = message;
        this.createdAt = createdAt;
        this.donationId = donationId;
        this.link = link;
    }

    public int getId() { return id; }
    public int getRecipientId() { return recipientId; }
    public Category getCategory() { return category; }
    public Type getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public int getDonationId() { return donationId; }
    public String getLink() { return link; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
