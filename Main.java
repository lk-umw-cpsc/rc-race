import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::initiateSwingComponents);
    }

    private static void initiateSwingComponents() {
        new ApplicationFrame().setVisible(true);
    }

}