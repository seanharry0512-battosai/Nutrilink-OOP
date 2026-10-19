package model;

import java.io.Serializable;

/** Personal preferences of one user (saved with the account). */
public class UserSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean notifyUpdates = true;     // donation updates (approved, assigned, collected...)
    private boolean notifyExpiry = true;      // food that is about to expire
    private boolean confirmLogout = true;
    private String defaultReportRange = "This month";

    public boolean isNotifyUpdates() { return notifyUpdates; }
    public void setNotifyUpdates(boolean notifyUpdates) { this.notifyUpdates = notifyUpdates; }
    public boolean isNotifyExpiry() { return notifyExpiry; }
    public void setNotifyExpiry(boolean notifyExpiry) { this.notifyExpiry = notifyExpiry; }
    public boolean isConfirmLogout() { return confirmLogout; }
    public void setConfirmLogout(boolean confirmLogout) { this.confirmLogout = confirmLogout; }
    public String getDefaultReportRange() { return defaultReportRange; }
    public void setDefaultReportRange(String defaultReportRange) { this.defaultReportRange = defaultReportRange; }
}
