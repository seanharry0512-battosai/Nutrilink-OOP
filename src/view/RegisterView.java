package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Basic registration form matching the controller's expected methods. */
public class RegisterView extends JPanel {
    private final JTextField fullNameField = new Theme.HintField("Full name");
    private final JTextField emailField = new Theme.HintField("Email");
    private final JTextField contactField = new Theme.HintField("Contact number");
    private final JPasswordField passwordField = new Theme.HintPassword("Password");
    private final JPasswordField confirmField = new Theme.HintPassword("Confirm password");
    private final JComboBox<String> roleBox = new JComboBox<>(new String[]{"Donor", "Coordinator", "Volunteer", "Beneficiary"});
    private final JButton createButton = Theme.primary("Create account");
    private final JLabel loginLabel = Theme.link("Back to login", Theme.LINK_BLUE);

    public RegisterView() {
        setLayout(new BorderLayout());
        setBackground(new Color(0xF5F7F5));

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setOpaque(false);
        formCard.setBorder(new EmptyBorder(28, 0, 0, 0));
        formCard.setMaximumSize(new Dimension(440, 560));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(0, 0, 12, 0);

        JLabel title = Theme.label("Create your account", 32, Font.BOLD, Theme.DARK_GREEN);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = 0;
        formCard.add(title, c);

        JLabel subtitle = Theme.label("Join NutriLink to donate, collect, and distribute food.", 14, Font.PLAIN, Theme.TEXT_GRAY);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = 1;
        formCard.add(subtitle, c);

        c.gridy = 2;
        formCard.add(fullNameField, c);
        c.gridy = 3;
        formCard.add(emailField, c);
        c.gridy = 4;
        formCard.add(contactField, c);
        c.gridy = 5;
        formCard.add(passwordField, c);
        c.gridy = 6;
        formCard.add(confirmField, c);

        roleBox.setFont(Theme.font(Font.PLAIN, 14));
        roleBox.setBorder(BorderFactory.createCompoundBorder(
                new Theme.RoundedBorder(Theme.BORDER, 10),
                new EmptyBorder(6, 10, 6, 10)
        ));
        c.gridy = 7;
        formCard.add(roleBox, c);

        c.gridy = 8;
        formCard.add(createButton, c);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.add(Theme.label("Already registered?", 13, Font.PLAIN, Theme.TEXT_GRAY));
        footer.add(loginLabel);
        c.gridy = 9;
        formCard.add(footer, c);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.add(Box.createVerticalGlue());
        center.add(formCard);
        center.add(Box.createVerticalGlue());
        add(center, BorderLayout.CENTER);
    }

    public JButton getCreateButton() { return createButton; }
    public JLabel getLoginLabel() { return loginLabel; }

    public String getFullName() { return fullNameField.getText().trim(); }
    public String getEmail() { return emailField.getText().trim(); }
    public String getContact() { return contactField.getText().trim(); }
    public String getPassword() { return new String(passwordField.getPassword()); }
    public String getConfirm() { return new String(confirmField.getPassword()); }
    public String getSelectedRole() { return (String) roleBox.getSelectedItem(); }

    public void clear() {
        fullNameField.setText("");
        emailField.setText("");
        contactField.setText("");
        passwordField.setText("");
        confirmField.setText("");
        roleBox.setSelectedIndex(0);
    }
}
