import controller.AppController;
import model.DataStore;
import view.MainFrame;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DataStore store = DataStore.load();                          // saved data, or demo data the first time
            Runtime.getRuntime().addShutdownHook(new Thread(store::save)); // save once more when the window closes
            MainFrame frame = new MainFrame();
            new AppController(store, frame).start();
        });
    }
}
