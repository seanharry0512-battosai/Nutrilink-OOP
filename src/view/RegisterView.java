package view;

import model.Beneficiary;
import model.Donor;
import model.PasswordRules;
import model.Volunteer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/** Registration form: everyone fills in the basics, then only the fields for the chosen role appear. */
public class RegisterView extends JPanel {
    private static final String PASSWORD_HINT = "8+ characters with upper and lower case letters and a number.";

    // ---- basics (every role) ----
    private final JTextField fullNameField = new Theme.HintField("Full name");
    private final JTextField emailField = new Theme.HintField("Email");
    private final JTextField contactField = new Theme.HintField("Contact number");
    private final JPasswordField passwordField = new Theme.HintPassword("Password");
    private final JLabel strengthLabel = Theme.label(PASSWORD_HINT, 12, Font.PLAIN, Theme.TEXT_GRAY);
    private final JPasswordField confirmField = new Theme.HintPassword("Confirm password");
    private final JComboBox<String> roleBox = new JComboBox<>(new String[]{"Donor", "Coordinator", "Volunteer", "Beneficiary"});

    // ---- donor ----
    private final JComboBox<String> donorTypeBox = new JComboBox<>(Donor.TYPES);
    private final JTextField donorOrgField = new Theme.HintField("Business or organization name");
    private final JPanel donorOrgRow = labeled("Business or organization name", donorOrgField);
    private final JTextField donorPickupField = new Theme.HintField("Pickup address or donation location");

    // ---- beneficiary ----
    private final JComboBox<String> beneficiaryTypeBox = new JComboBox<>(Beneficiary.TYPES);
    private final JTextField householdField = new Theme.HintField("Number of people in your household");
    private final JPanel householdRow = labeled("Household size", householdField);
    private final JTextField beneficiaryOrgField = new Theme.HintField("Organization name");
    private final JPanel beneficiaryOrgRow = labeled("Organization name", beneficiaryOrgField);
    private final JTextField beneficiaryLocationField = new Theme.HintField("Delivery or collection address");

    // ---- coordinator ----
    private final JTextField coordOrgField = new Theme.HintField("Organization or partner name");
    private final JTextField coordPositionField = new Theme.HintField("Position or designation");
    private final JTextField coordAreaField = new Theme.HintField("Assigned area or service location");

    // ---- volunteer ----
    private final JTextField volunteerAreaField = new Theme.HintField("Service area or home address");
    private final JComboBox<String> volunteerActivityBox = new JComboBox<>(Volunteer.ACTIVITIES);
    private final JTextField volunteerAvailabilityField = new Theme.HintField("e.g. Weekends, 8 AM - 12 NN");

    private final Map<String, JPanel> roleGroups = new LinkedHashMap<>();
    private final JPanel roleArea = new JPanel(new GridBagLayout());
    private final JButton createButton = Theme.primary("Create account");
    private final JLabel loginLabel = Theme.link("Back to login", Theme.LINK_BLUE);

    public RegisterView() {
        setLayout(new BorderLayout());
        setBackground(new Color(0xF5F7F5));

        for (JComboBox<String> box : java.util.List.of(roleBox, donorTypeBox, beneficiaryTypeBox, volunteerActivityBox)) {
            styleCombo(box);
        }

        // the fields that belong to each role
        roleGroups.put("Donor", group("Donor details",
                labeled("Donor type", donorTypeBox), donorOrgRow, labeled("Pickup location", donorPickupField)));
        roleGroups.put("Beneficiary", group("Beneficiary details",
                labeled("Beneficiary type", beneficiaryTypeBox), householdRow, beneficiaryOrgRow,
                labeled("Delivery location", beneficiaryLocationField)));
        roleGroups.put("Coordinator", group("Coordinator details",
                labeled("Organization", coordOrgField), labeled("Position", coordPositionField),
                labeled("Assigned area", coordAreaField)));
        roleGroups.put("Volunteer", group("Volunteer details",
                labeled("Service area", volunteerAreaField), labeled("Preferred activity", volunteerActivityBox),
                labeled("Availability", volunteerAvailabilityField)));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        for (JPanel group : roleGroups.values()) roleArea.add(group, g);     // hidden ones take no space
        roleArea.setOpaque(false);

        // the form
        JPanel formCard = new JPanel(new GridBagLayout()) {
            @Override public Dimension getPreferredSize() { return new Dimension(440, super.getPreferredSize().height); }
        };
        formCard.setOpaque(false);
        formCard.setBorder(new EmptyBorder(28, 0, 28, 0));

        JLabel title = Theme.label("Create your account", 32, Font.BOLD, Theme.DARK_GREEN);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel subtitle = Theme.label("Join NutriLink to donate, collect, and distribute food.", 14, Font.PLAIN, Theme.TEXT_GRAY);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.add(Theme.label("Already registered?", 13, Font.PLAIN, Theme.TEXT_GRAY));
        footer.add(loginLabel);

        int y = 0;
        y = addRow(formCard, title, y, 12);
        y = addRow(formCard, subtitle, y, 12);
        y = addRow(formCard, fullNameField, y, 12);
        y = addRow(formCard, emailField, y, 12);
        y = addRow(formCard, contactField, y, 12);
        y = addRow(formCard, passwordField, y, 4);
        y = addRow(formCard, strengthLabel, y, 12);
        y = addRow(formCard, confirmField, y, 12);
        y = addRow(formCard, labeled("Register as", roleBox), y, 14);
        y = addRow(formCard, roleArea, y, 14);
        y = addRow(formCard, createButton, y, 12);
        addRow(formCard, footer, y, 0);

        // centered, and it scrolls when the window is too short for the role's fields
        JPanel centered = new JPanel(new GridBagLayout());
        centered.setOpaque(false);
        centered.add(formCard);
        JScrollPane scroll = new JScrollPane(centered, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // behavior
        roleBox.addActionListener(e -> updateRoleFields());
        donorTypeBox.addActionListener(e -> updateRoleFields());
        beneficiaryTypeBox.addActionListener(e -> updateRoleFields());
        passwordField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateStrength(); }
            @Override public void removeUpdate(DocumentEvent e) { updateStrength(); }
            @Override public void changedUpdate(DocumentEvent e) { updateStrength(); }
        });
        updateRoleFields();
    }

    // ---------------- layout helpers ----------------
    private static int addRow(JPanel p, Component c, int y, int bottom) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = y; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, bottom, 0);
        p.add(c, g);
        return y + 1;
    }

    private static void styleCombo(JComboBox<String> box) {
        box.setFont(Theme.font(Font.PLAIN, 14));
        box.setBorder(BorderFactory.createCompoundBorder(new Theme.RoundedBorder(Theme.BORDER, 10), new EmptyBorder(6, 10, 6, 10)));
        box.setPreferredSize(new Dimension(200, 40));
    }

    /** A small label above an input. */
    private static JPanel labeled(String label, JComponent input) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(Theme.fieldLabel(label), BorderLayout.NORTH);
        p.add(input, BorderLayout.CENTER);
        return p;
    }

    private static JPanel group(String title, JComponent... rows) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        p.add(Theme.label(title, 16, Font.BOLD, Theme.DARK_GREEN), c);
        for (JComponent row : rows) {
            c.gridy++;
            c.insets = new Insets(10, 0, 0, 0);
            p.add(row, c);
        }
        return p;
    }

    // ---------------- behavior ----------------
    /** Shows only the fields of the chosen role (and of the chosen type, for donors and beneficiaries). */
    private void updateRoleFields() {
        String role = getSelectedRole();
        for (Map.Entry<String, JPanel> e : roleGroups.entrySet()) e.getValue().setVisible(e.getKey().equals(role));
        donorOrgRow.setVisible(Donor.needsOrganization(getDonorType()));
        String type = getBeneficiaryType();
        householdRow.setVisible(Beneficiary.needsHousehold(type));
        beneficiaryOrgRow.setVisible(Beneficiary.needsOrganization(type));
        roleArea.revalidate();
        roleArea.repaint();
    }

    private void updateStrength() {
        String pw = getPassword();
        if (pw.isEmpty()) {
            strengthLabel.setText(PASSWORD_HINT);
            strengthLabel.setForeground(Theme.TEXT_GRAY);
            return;
        }
        String strength = PasswordRules.strength(pw);
        String missing = PasswordRules.hint(pw);
        strengthLabel.setText("Strength: " + strength + (missing == null ? "" : " - add " + missing));
        switch (strength) {
            case "Weak": strengthLabel.setForeground(new Color(0xC62828)); break;
            case "Fair": strengthLabel.setForeground(new Color(0xB36B00)); break;
            default: strengthLabel.setForeground(new Color(0x1E7A45));
        }
    }

    // ---------------- API for the controller ----------------
    public JButton getCreateButton() { return createButton; }
    public JLabel getLoginLabel() { return loginLabel; }

    public String getFullName() { return fullNameField.getText().trim(); }
    public String getEmail() { return emailField.getText().trim(); }
    public String getContact() { return contactField.getText().trim(); }
    public String getPassword() { return new String(passwordField.getPassword()); }
    public String getConfirm() { return new String(confirmField.getPassword()); }
    public String getSelectedRole() { return (String) roleBox.getSelectedItem(); }

    public String getDonorType() { return (String) donorTypeBox.getSelectedItem(); }
    public String getDonorOrganization() { return donorOrgField.getText().trim(); }
    public String getDonorPickup() { return donorPickupField.getText().trim(); }

    public String getBeneficiaryType() { return (String) beneficiaryTypeBox.getSelectedItem(); }
    public String getHouseholdSize() { return householdField.getText().trim(); }
    public String getBeneficiaryOrganization() { return beneficiaryOrgField.getText().trim(); }
    public String getBeneficiaryLocation() { return beneficiaryLocationField.getText().trim(); }

    public String getCoordinatorOrganization() { return coordOrgField.getText().trim(); }
    public String getCoordinatorPosition() { return coordPositionField.getText().trim(); }
    public String getCoordinatorArea() { return coordAreaField.getText().trim(); }

    public String getVolunteerArea() { return volunteerAreaField.getText().trim(); }
    public String getVolunteerActivity() { return (String) volunteerActivityBox.getSelectedItem(); }
    public String getVolunteerAvailability() { return volunteerAvailabilityField.getText().trim(); }

    public void clear() {
        for (JTextField f : new JTextField[]{fullNameField, emailField, contactField, donorOrgField, donorPickupField,
                householdField, beneficiaryOrgField, beneficiaryLocationField, coordOrgField, coordPositionField,
                coordAreaField, volunteerAreaField, volunteerAvailabilityField}) {
            f.setText("");
        }
        passwordField.setText("");
        confirmField.setText("");
        donorTypeBox.setSelectedIndex(0);
        beneficiaryTypeBox.setSelectedIndex(0);
        volunteerActivityBox.setSelectedIndex(0);
        roleBox.setSelectedIndex(0);
        updateRoleFields();
    }
}
