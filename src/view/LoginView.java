package view;

import javax.swing.*;
import java.awt.*;

public class LoginView extends JPanel {
    private final JTextField emailField = new Theme.HintField("youremail@gmail.com");
    private final JPasswordField passwordField = new Theme.HintPassword("*********");
    private final JCheckBox rememberBox = new JCheckBox("Remember me");
    private final JButton loginButton = Theme.primary("Login");
    private final JLabel forgotLabel = Theme.link("Forgot Password?", Theme.LINK_BLUE);
    private final JLabel registerLabel = Theme.link("Register", Theme.GREEN);

    public LoginView() {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(380, 480));

        JLabel title = Theme.label("Welcome Back!", 32, Font.BOLD, Color.BLACK);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel subtitle = Theme.label("Sign in to continue to your account", 14, Font.BOLD, Theme.MUTED);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);

        rememberBox.setOpaque(false);
        rememberBox.setFont(Theme.font(Font.BOLD, 12));
        JPanel options = new JPanel(new BorderLayout());
        options.setOpaque(false);
        options.add(rememberBox, BorderLayout.WEST);
        options.add(forgotLabel, BorderLayout.EAST);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        bottom.setOpaque(false);
        bottom.add(Theme.label("Don't have an account?", 14, Font.BOLD, Theme.MUTED));
        bottom.add(registerLabel);

        int y = 0;
        form.add(Theme.logo(34), Theme.row(y++, 0));
        form.add(title, Theme.row(y++, 20));
        form.add(subtitle, Theme.row(y++, 2));
        form.add(Theme.fieldLabel("Email"), Theme.row(y++, 26));
        form.add(emailField, Theme.row(y++, 5));
        form.add(Theme.fieldLabel("Password"), Theme.row(y++, 14));
        form.add(passwordField, Theme.row(y++, 5));
        form.add(options, Theme.row(y++, 10));
        form.add(loginButton, Theme.row(y++, 18));
        form.add(bottom, Theme.row(y++, 16));
        add(form);
    }

    public String getEmail() { return emailField.getText().trim(); }
    public String getPassword() { return new String(passwordField.getPassword()); }
    public boolean isRememberSelected() { return rememberBox.isSelected(); }
    public void setEmail(String email) { emailField.setText(email); rememberBox.setSelected(!email.isEmpty()); }
    public void clearPassword() { passwordField.setText(""); }
    public JButton getLoginButton() { return loginButton; }
    public JPasswordField getPasswordField() { return passwordField; }
    public JLabel getForgotLabel() { return forgotLabel; }
    public JLabel getRegisterLabel() { return registerLabel; }
}
