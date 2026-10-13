package view;

import javax.swing.*;
import java.awt.*;

/** The one window. It switches between Login, Register and the Dashboard. */
public class MainFrame extends JFrame {
    public static final String LOGIN = "login";
    public static final String REGISTER = "register";
    public static final String APP = "app";

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final LoginView loginView = new LoginView();
    private final RegisterView registerView = new RegisterView();
    private final DashboardView dashboardView = new DashboardView();

    public MainFrame() {
        super("NutriLink");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 700);
        setMinimumSize(new Dimension(1000, 660));
        setLocationRelativeTo(null);
        root.add(loginView, LOGIN);
        root.add(registerView, REGISTER);
        root.add(dashboardView, APP);
        setContentPane(root);
    }

    public void showScreen(String name) { cards.show(root, name); }
    public LoginView getLoginView() { return loginView; }
    public RegisterView getRegisterView() { return registerView; }
    public DashboardView getDashboardView() { return dashboardView; }
}
