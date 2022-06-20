import javax.swing.SwingUtilities;

/**
 * Program entry point
 */
public class Main {

    public static void main(String[] args) {
        // initialize the GUI elements on the Swing event thread (required)
        SwingUtilities.invokeLater(Main::initiateSwingComponents);
    }

    private static void initiateSwingComponents() {
        // create a new ApplicationFrame (our main window) and make it visible
        new ApplicationFrame().setVisible(true);
    }

}