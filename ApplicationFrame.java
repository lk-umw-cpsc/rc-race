import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * This class defines the application's main window.
 * 
 * The frame contains a single child component, an ApplicationCanvas.
 * 
 * It spawns a ConfigureRoomDialog when Settings -> Configure Room is chosen.
 */
public class ApplicationFrame extends JFrame {

    private final ApplicationCanvas canvas;
    
    public ApplicationFrame() {
        super("Goal Chase Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set up menu bar with Settings menu and "Configure Room" option
        JMenuBar menuBar = new JMenuBar();

        JMenu settingsMenu = new JMenu("Settings");

        JMenuItem roomConfigurationItem = new JMenuItem("Configure Room");
        // cause the dialog to appear when the option is chosen
        roomConfigurationItem.addActionListener(this::configureRoomChosen);

        // Add the "Configure Room" option to the Settings menu
        settingsMenu.add(roomConfigurationItem);

        // Add the Settings menu to the menu bar
        menuBar.add(settingsMenu);

        // Add the menu bar to the frame
        setJMenuBar(menuBar);

        // Add a canvas to the frame
        canvas = new ApplicationCanvas();
        add(canvas);

        // resize the frame to fit the menu bar and canvas component
        pack();
    }

    /**
     * Method called when the user chooses the 'Configure Room' option under the
     * Settings menu
     * @param e Event information from Swing (unused)
     */
    private void configureRoomChosen(ActionEvent e) {
        new ConfigureRoomDialog(this).prompt();
    }

    /**
     * Method called by a ConfigureRoomDialog, notifying the ApplicationFrame
     * that the user submitted their input
     * @param config Configuration information retreived by the dialog,
     * containing the user's input
     */
    public void roomConfigured(ConfigureRoomDialog.RoomConfiguration config) {
        canvas.setRoomDimensions(config.xLowerBound, config.yLowerBound,
                config.width, config.length);
    }

}
