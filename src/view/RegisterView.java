package view;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class RegisterView extends JPanel {
    private static final String[] ROLES = {"Food Donor", "Coordinator", "Volunteer", "Beneficiary"};

    private final JToggleButton[] roleButtons = new JToggleButton[ROLES.length];
    private final JTextField nameField = new Theme.HintField("Juan Dela Cruz");
    private final JTextField emailField = new Theme.HintField("juan@gmail.com");
    private final JTextField contactField = new Theme.HintField("09XXXXXXXXX");
    private final JPasswordField passwordField = new Theme.HintPassword("Min. 6 characters");
    private final JPasswordField confirmField = new Theme.HintPassword("Re-enter password");
    private final JToggleButton showButton = new JToggleButton("Show");
    private final JButton createButton = Theme.primary("Create Account");
    private final JLabel loginLabel = Theme.link("Login", Theme.GREEN);

    public RegisterView() {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(580, 560));

        // role cards
        JPanel roles = new JPanel(new GridLayout(1, ROLES.length, 10, 0));
        roles.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < ROLES.length; i++) {
            JToggleButton b = new JToggleButton("<html><center>" + ROLES[i] + "</center></html>");
            b.setFont(Theme.font(Font.BOLD, 13));
            b.setPreferredSize(new Dimension(100, 70));
            b.setFocusPainted(false);
            b.setContentAreaFilled(false);
            b.setOpaque(true);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addItemListener(e -> styleRoleButtons());
            group.add(b);
            roles.add(b);
            roleButtons[i] = b;
        }
        roleButtons[0].setSelected(true);
        styleRoleButtons();

        // password + show/hide
        showButton.setFont(Theme.font(Font.BOLD, 12));
        showButton.setFocusPainted(false);
        showButton.setMargin(new Insets(0, 4, 0, 4));
        showButton.setPreferredSize(new Dimension(76, 40));
        final char defaultEcho = passwordField.getEchoChar();
        showButton.addActionListener(e -> {
            char echo = showButton.isSelected() ? (char) 0 : defaultEcho;
            passwordField.setEchoChar(echo);
            confirmField.setEchoChar(echo);
            showButton.setText(showButton.isSelected() ? "Hide" : "Show");
        });
        JPanel passwordPanel = new JPanel(new BorderLayout(6, 0));
        passwordPanel.setOpaque(false);
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(showButton, BorderLayout.EAST);

        JLabel title = Theme.label("Create Your Account", 30, Font.BOLD, Color.BLACK);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel subtitle = Theme.label("Join our community and help fight food waste", 14, Font.BOLD, Theme.MUTED);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        bottom.setOpaque(false);
        bottom.add(Theme.label("Already have an account?", 14, Font.BOLD, Theme.MUTED));
        bottom.add(loginLabel);

        place(form, Theme.logo(30), 0, 0, 2, 0);
        place(form, title, 0, 1, 2, 14);
        place(form, subtitle, 0, 2, 2, 0);
        place(form, Theme.fieldLabel("I am a:"), 0, 3, 2, 16);
        place(form, roles, 0, 4, 2, 6);
        place(form, Theme.fieldLabel("Full Name"), 0, 5, 1, 14);
        place(form, Theme.fieldLabel("Email"), 1, 5, 1, 14);
        place(form, nameField, 0, 6, 1, 4);
        place(form, emailField, 1, 6, 1, 4);
        place(form, Theme.fieldLabel("Contact Number"), 0, 7, 1, 10);
        place(form, Theme.fieldLabel("Password"), 1, 7, 1, 10);
        place(form, contactField, 0, 8, 1, 4);
        place(form, passwordPanel, 1, 8, 1, 4);
        place(form, Theme.fieldLabel("Confirm Password"), 0, 9, 2, 10);
        place(form, confirmField, 0, 10, 2, 4);
        place(form, createButton, 0, 11, 2, 20);
        place(form, bottom, 0, 12, 2, 12);
        add(form);
    }

    private void place(JPanel p, JComponent c, int x, int y, int w, int top) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x; g.gridy = y; g.gridwidth = w; g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(top, x == 1 ? 12 : 0, 0, 0);
        p.add(c, g);
    }

    private void styleRoleButtons() {
        for (JToggleButton b : roleButtons) {
            if (b == null) continue;
            b.setBorder(b.isSelected() ? new LineBorder(Theme.PURPLE, 3) : new LineBorder(Color.GRAY, 1));
            b.setBackground(b.isSelected() ? new Color(0xF3EEFF) : Color.WHITE);
        }
    }

    public String getSelectedRole() {
        for (int i = 0; i < ROLES.length; i++) {
            if (roleButtons[i].isSelected()) return ROLES[i];
        }
        return ROLES[0];
    }

    public String getFullName() { return nameField.getText().trim(); }
    public String getEmail() { return emailField.getText().trim(); }
    public String getContact() { return contactField.getText().trim(); }
    public String getPassword() { return new String(passwordField.getPassword()); }
    public String getConfirm() { return new String(confirmField.getPassword()); }
    public JButton getCreateButton() { return createButton; }
    public JLabel getLoginLabel() { return loginLabel; }

    public void clear() {
        nameField.setText(""); emailField.setText(""); contactField.setText("");
        passwordField.setText(""); confirmField.setText("");
        roleButtons[0].setSelected(true);
        if (showButton.isSelected()) showButton.doClick();
    }
}
