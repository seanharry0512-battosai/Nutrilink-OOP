import controller.AppController;
import model.DataStore;
import view.MainFrame;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DataStore store = new DataStore();
            MainFrame frame = new MainFrame();
            new AppController(store, frame).start();
        });
    }
}