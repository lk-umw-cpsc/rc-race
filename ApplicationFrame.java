import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class ApplicationFrame extends JFrame {

    private final ApplicationCanvas canvas;
    
    public ApplicationFrame() {
        super("Goal Chase Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        JMenu settingsMenu = new JMenu("Settings");
        JMenuItem roomConfigurationItem = new JMenuItem("Configure Room");
        roomConfigurationItem.addActionListener(this::configureRoomChosen);
        settingsMenu.add(roomConfigurationItem);
        menuBar.add(settingsMenu);

        setJMenuBar(menuBar);
        canvas = new ApplicationCanvas();
        add(canvas);

        pack();
    }

    private void configureRoomChosen(ActionEvent e) {
        new ConfigureRoomDialog(this).prompt();
    }

    public void roomConfigured(ConfigureRoomDialog.RoomConfiguration config) {
        canvas.setRoomDimensions(config.xLowerBound, config.yLowerBound,
                config.width, config.length);
    }

}
