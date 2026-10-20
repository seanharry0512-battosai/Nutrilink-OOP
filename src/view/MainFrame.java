package view;

import javax.swing.*;
import java.awt.*;

/** The one window. It switches between Login, Register and the Dashboard. */
public class MainFrame extends JFrame {
    public static final String LOGIN = "login";
    public static final String REGISTER = "register";
    public static final String APP = "app";

    private final PageTransition root = new PageTransition();   // fades between Login, Register and the app
    private final LoginView loginView = new LoginView();
    private final RegisterView registerView = new RegisterView();
    private final DashboardView dashboardView = new DashboardView();

    public MainFrame() {
        super("NutriLink");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 730);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);
        root.add(loginView, LOGIN);
        root.add(registerView, REGISTER);
        root.add(dashboardView, APP);
        setContentPane(root);
    }

    public void showScreen(String name) { root.showPage(name); }
    public LoginView getLoginView() { return loginView; }
    public RegisterView getRegisterView() { return registerView; }
    public DashboardView getDashboardView() { return dashboardView; }
}
