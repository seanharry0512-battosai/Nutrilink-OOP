package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Simple login screen used by the controller and main frame. */
public class LoginView extends JPanel {
    private final JTextField emailField = new Theme.HintField("Email");
    private final JPasswordField passwordField = new Theme.HintPassword("Password");
    private final JCheckBox rememberCheck = new JCheckBox("Remember me");
    private final JButton loginButton = Theme.primary("Login");
    private final JLabel forgotLabel = Theme.link("Forgot password?", Theme.LINK_BLUE);
    private final JLabel registerLabel = Theme.link("Create an account", Theme.LINK_BLUE);

    public LoginView() {
        setLayout(new BorderLayout());
        setBackground(new Color(0xF5F7F5));

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(30, 0, 0, 0));

        JPanel formCard = new JPanel();
        formCard.setLayout(new GridBagLayout());
        formCard.setOpaque(false);
        formCard.setMaximumSize(new Dimension(420, 420));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.weightx = 1;
        c.insets = new Insets(0, 0, 14, 0);

        JLabel title = Theme.label("Welcome back", 34, Font.BOLD, Theme.DARK_GREEN);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = 0;
        formCard.add(title, c);

        JLabel subtitle = Theme.label("Sign in to continue to NutriLink", 14, Font.PLAIN, Theme.TEXT_GRAY);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = 1;
        formCard.add(subtitle, c);

        c.gridy = 2;
        formCard.add(emailField, c);
        c.gridy = 3;
        formCard.add(passwordField, c);

        JPanel actions = new JPanel(new BorderLayout(12, 0));
        actions.setOpaque(false);
        actions.add(rememberCheck, BorderLayout.WEST);
        actions.add(forgotLabel, BorderLayout.EAST);
        c.gridy = 4;
        formCard.add(actions, c);

        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        c.gridy = 5;
        formCard.add(loginButton, c);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.add(Theme.label("Don’t have an account?", 13, Font.PLAIN, Theme.TEXT_GRAY));
        footer.add(registerLabel);
        c.gridy = 6;
        formCard.add(footer, c);

        center.add(Box.createVerticalGlue());
        center.add(formCard);
        center.add(Box.createVerticalGlue());
        add(center, BorderLayout.CENTER);
    }

    public JTextField getEmailField() { return emailField; }
    public JPasswordField getPasswordField() { return passwordField; }
    public JButton getLoginButton() { return loginButton; }
    public JLabel getRegisterLabel() { return registerLabel; }
    public JLabel getForgotLabel() { return forgotLabel; }

    public String getEmail() { return emailField.getText().trim(); }
    public String getPassword() { return new String(passwordField.getPassword()); }
    public boolean isRememberSelected() { return rememberCheck.isSelected(); }

    public void setEmail(String email) { emailField.setText(email == null ? "" : email); }
    public void clearPassword() { passwordField.setText(""); }
    public void clear() {
        emailField.setText("");
        passwordField.setText("");
        rememberCheck.setSelected(false);
    }
}
