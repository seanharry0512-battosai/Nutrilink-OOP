package view;

import model.Report;
import model.UserSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Settings page. Every switch is applied (and saved) as soon as it is changed. */
public class SettingsView extends JPanel {
    private final ToggleSwitch updatesSwitch = new ToggleSwitch();
    private final ToggleSwitch expirySwitch = new ToggleSwitch();
    private final ToggleSwitch logoutSwitch = new ToggleSwitch();
    private final JComboBox<String> rangeBox = new JComboBox<>(Report.PRESETS);
    private final JButton resetButton = Theme.danger("Reset demo data");
    private final JPanel dataCard;
    private Runnable onChange;
    private boolean loading;

    public SettingsView() {
        setLayout(new BorderLayout(0, 14));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(22, 28, 22, 28));

        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        titles.add(Theme.label("Settings", 34, Font.BOLD, Color.BLACK));
        titles.add(Theme.label("Changes are saved automatically.", 14, Font.PLAIN, Theme.MUTED));
        add(titles, BorderLayout.NORTH);

        rangeBox.setFont(Theme.font(Font.BOLD, 14));
        rangeBox.setBackground(Color.WHITE);
        rangeBox.setPreferredSize(new Dimension(160, 38));
        resetButton.setPreferredSize(new Dimension(170, 40));

        JPanel notifications = section("Notifications",
                option("Donation updates", "Approved, assigned, collected and distributed donations.", updatesSwitch),
                option("Expiry alerts", "Warn me when food is about to expire or has expired.", expirySwitch));
        JPanel reports = section("Reports",
                option("Default date range", "The range the Reports page opens with.", rangeBox));
        JPanel general = section("General",
                option("Confirm before logging out", "Ask \"Are you sure?\" when you press Logout.", logoutSwitch));
        dataCard = section("Data",
                option("Reset demo data", "Deletes every donation, account and message and restores the demo data.", resetButton));

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        body.add(notifications, Theme.row(0, 0));
        body.add(reports, Theme.row(1, 14));
        body.add(general, Theme.row(2, 14));
        body.add(dataCard, Theme.row(3, 14));
        body.add(Theme.label("NutriLink  |  Food donation management", 12, Font.PLAIN, Theme.MUTED), Theme.row(4, 18));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(body, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        Runnable fire = () -> { if (!loading && onChange != null) onChange.run(); };
        updatesSwitch.setOnChange(fire);
        expirySwitch.setOnChange(fire);
        logoutSwitch.setOnChange(fire);
        rangeBox.addActionListener(e -> fire.run());
    }

    private static JPanel section(String title, JComponent... rows) {
        JPanel card = Theme.card(new GridBagLayout());
        card.add(Theme.label(title, 20, Font.BOLD, Color.BLACK), Theme.row(0, 0));
        for (int i = 0; i < rows.length; i++) card.add(rows[i], Theme.row(i + 1, 12));
        return card;
    }

    private static JPanel option(String title, String description, JComponent control) {
        JPanel row = new JPanel(new BorderLayout(20, 0));
        row.setOpaque(false);
        JPanel text = new JPanel(new GridLayout(2, 1, 0, 1));
        text.setOpaque(false);
        text.add(Theme.label(title, 15, Font.BOLD, Color.BLACK));
        text.add(Theme.label(description, 13, Font.PLAIN, Theme.MUTED));
        JPanel controlWrap = new JPanel(new GridBagLayout());
        controlWrap.setOpaque(false);
        controlWrap.add(control);
        row.add(text, BorderLayout.CENTER);
        row.add(controlWrap, BorderLayout.EAST);
        return row;
    }

    // ---------------- API for the controller ----------------
    public void setOnChange(Runnable onChange) { this.onChange = onChange; }
    public JButton getResetButton() { return resetButton; }
    public boolean isNotifyUpdates() { return updatesSwitch.isOn(); }
    public boolean isNotifyExpiry() { return expirySwitch.isOn(); }
    public boolean isConfirmLogout() { return logoutSwitch.isOn(); }
    public String getReportRange() { return (String) rangeBox.getSelectedItem(); }

    public void load(UserSettings s, boolean canResetData) {
        loading = true;
        updatesSwitch.setOn(s.isNotifyUpdates());
        expirySwitch.setOn(s.isNotifyExpiry());
        logoutSwitch.setOn(s.isConfirmLogout());
        rangeBox.setSelectedItem(s.getDefaultReportRange());
        dataCard.setVisible(canResetData);
        loading = false;
    }
}
