package view;

import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/** Profile page: who you are, a little activity summary, edit your details and change your password. */
public class ProfileView extends JPanel {
    private final Avatar avatar = new Avatar(96, Theme.GREEN, Color.WHITE);
    private final JLabel nameLabel = Theme.label("", 24, Font.BOLD, Color.BLACK);
    private final JLabel emailLabel = Theme.label("", 14, Font.PLAIN, Theme.MUTED);
    private final JLabel roleLabel = new JLabel("", SwingConstants.CENTER) {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0xDFF3E6));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    };
    private final JPanel activity = new JPanel();

    private final JTextField nameField = new Theme.HintField("Full name");
    private final JTextField emailField = new Theme.HintField("Email");
    private final JTextField contactField = new Theme.HintField("09XXXXXXXXX");
    private final JButton saveButton = Theme.primary("Save Changes");

    private final JPasswordField currentField = new Theme.HintPassword("Current password");
    private final JPasswordField newField = new Theme.HintPassword("Min. 6 characters");
    private final JPasswordField confirmField = new Theme.HintPassword("Re-enter new password");
    private final JButton passwordButton = Theme.primary("Update Password");

    public ProfileView() {
        setLayout(new BorderLayout(0, 14));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(22, 28, 22, 28));
        add(Theme.label("My Profile", 34, Font.BOLD, Color.BLACK), BorderLayout.NORTH);

        // ---- left: summary card ----
        roleLabel.setFont(Theme.font(Font.BOLD, 13));
        roleLabel.setForeground(Theme.GREEN);
        roleLabel.setOpaque(false);
        roleLabel.setPreferredSize(new Dimension(130, 28));
        activity.setLayout(new BoxLayout(activity, BoxLayout.Y_AXIS));
        activity.setOpaque(false);

        JPanel summary = Theme.card(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.weightx = 1; c.fill = GridBagConstraints.NONE; c.anchor = GridBagConstraints.CENTER;
        c.gridy = 0; c.insets = new Insets(8, 0, 0, 0); summary.add(avatar, c);
        c.gridy = 1; c.insets = new Insets(12, 0, 0, 0); summary.add(nameLabel, c);
        c.gridy = 2; c.insets = new Insets(4, 0, 0, 0); summary.add(emailLabel, c);
        c.gridy = 3; c.insets = new Insets(10, 0, 0, 0); summary.add(roleLabel, c);
        c.gridy = 4; c.insets = new Insets(22, 0, 0, 0); c.fill = GridBagConstraints.HORIZONTAL;
        summary.add(Theme.label("My Activity", 16, Font.BOLD, Color.BLACK), c);
        c.gridy = 5; c.insets = new Insets(6, 0, 0, 0); c.weighty = 1; c.anchor = GridBagConstraints.NORTH;
        summary.add(activity, c);

        // ---- right: two forms ----
        JPanel info = Theme.card(new GridBagLayout());
        int y = 0;
        info.add(Theme.label("Personal Information", 20, Font.BOLD, Color.BLACK), Theme.row(y++, 0));
        info.add(Theme.fieldLabel("Full Name"), Theme.row(y++, 14));
        info.add(nameField, Theme.row(y++, 4));
        info.add(Theme.fieldLabel("Email"), Theme.row(y++, 10));
        info.add(emailField, Theme.row(y++, 4));
        info.add(Theme.fieldLabel("Contact Number"), Theme.row(y++, 10));
        info.add(contactField, Theme.row(y++, 4));
        info.add(rightAligned(saveButton), Theme.row(y++, 14));

        JPanel password = Theme.card(new GridBagLayout());
        y = 0;
        password.add(Theme.label("Change Password", 20, Font.BOLD, Color.BLACK), Theme.row(y++, 0));
        password.add(Theme.fieldLabel("Current Password"), Theme.row(y++, 14));
        password.add(currentField, Theme.row(y++, 4));
        password.add(Theme.fieldLabel("New Password"), Theme.row(y++, 10));
        password.add(newField, Theme.row(y++, 4));
        password.add(Theme.fieldLabel("Confirm New Password"), Theme.row(y++, 10));
        password.add(confirmField, Theme.row(y++, 4));
        password.add(rightAligned(passwordButton), Theme.row(y++, 14));

        JPanel forms = new JPanel(new GridBagLayout());
        forms.setOpaque(false);
        forms.add(info, Theme.row(0, 0));
        forms.add(password, Theme.row(1, 16));

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0; g.anchor = GridBagConstraints.NORTH;
        g.gridx = 0; g.weightx = 0.34; g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(0, 0, 0, 20);
        body.add(summary, g);
        g.gridx = 1; g.weightx = 0.66; g.insets = new Insets(0, 0, 0, 0);
        body.add(forms, g);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(body, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private static JPanel rightAligned(JButton b) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        p.setOpaque(false);
        p.add(b);
        return p;
    }

    // ---------------- API for the controller ----------------
    public void setUser(User u, List<String[]> activityRows) {
        avatar.setInitial(u.getInitial());
        nameLabel.setText(u.getFullName());
        emailLabel.setText(u.getEmail());
        roleLabel.setText(u.getRole());
        nameField.setText(u.getFullName());
        emailField.setText(u.getEmail());
        contactField.setText(u.getContactNumber());
        clearPasswordFields();

        activity.removeAll();
        if (activityRows.isEmpty()) {
            activity.add(Theme.label("Nothing to show yet.", 13, Font.PLAIN, Theme.MUTED));
        }
        for (String[] r : activityRows) {
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xEEEEEE)));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            JLabel left = Theme.label(r[0], 14, Font.PLAIN, Theme.TEXT_GRAY);
            left.setBorder(new EmptyBorder(7, 0, 7, 0));
            row.add(left, BorderLayout.WEST);
            row.add(Theme.label(r[1], 15, Font.BOLD, Color.BLACK), BorderLayout.EAST);
            activity.add(row);
        }
        activity.revalidate();
        activity.repaint();
    }

    public void clearPasswordFields() {
        currentField.setText("");
        newField.setText("");
        confirmField.setText("");
    }

    public String getNameText() { return nameField.getText(); }
    public String getEmailText() { return emailField.getText(); }
    public String getContactText() { return contactField.getText(); }
    public String getCurrentPassword() { return new String(currentField.getPassword()); }
    public String getNewPassword() { return new String(newField.getPassword()); }
    public String getConfirmPassword() { return new String(confirmField.getPassword()); }
    public JButton getSaveButton() { return saveButton; }
    public JButton getPasswordButton() { return passwordButton; }
}
