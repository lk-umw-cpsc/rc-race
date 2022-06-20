import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class ConfigureRoomDialog extends JDialog {

    /**
     * Structure defining room configuration captured by a
     * ConfigureRoomDialog
     */
    public static class RoomConfiguration {
        double width;
        double length;
        double xLowerBound;
        double yLowerBound;
    }
    private final ApplicationFrame parent;

    private final JTextField widthField;
    private final JTextField lengthField;
    private final JTextField xLowerBoundField;
    private final JTextField yLowerBoundField;


    public ConfigureRoomDialog(ApplicationFrame parent) {
        super(parent, "Configure Room");
        this.parent = parent;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // prevent parent from being interacted with while dialog is visible
        setModal(true);

        Box rowContainer = Box.createVerticalBox();
        rowContainer.setBorder(new EmptyBorder(8, 16, 8, 16));

        Box row;

        row = Box.createHorizontalBox();
            row.add(new JLabel("Room width:"));
            row.add(Box.createHorizontalGlue());
        rowContainer.add(row);

        row = Box.createHorizontalBox();
            widthField = new JTextField(16);
            widthField.addActionListener(this::formSubmitted);
            widthField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 
                    widthField.getPreferredSize().height));
            row.add(widthField);
        rowContainer.add(row);

        row = Box.createHorizontalBox();
            row.add(new JLabel("Room length:"));
            row.add(Box.createHorizontalGlue());
        rowContainer.add(row);

        row = Box.createHorizontalBox();
            lengthField = new JTextField(16);
            lengthField.addActionListener(this::formSubmitted);
            lengthField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 
                    lengthField.getPreferredSize().height));
            row.add(lengthField);
        rowContainer.add(row);

        row = Box.createHorizontalBox();
            row.add(new JLabel("X Lower Bound:"));
            row.add(Box.createHorizontalGlue());
        rowContainer.add(row);

        row = Box.createHorizontalBox();
            xLowerBoundField = new JTextField(16);
            xLowerBoundField.addActionListener(this::formSubmitted);
            xLowerBoundField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 
                    xLowerBoundField.getPreferredSize().height));
            row.add(xLowerBoundField);
        rowContainer.add(row);

        row = Box.createHorizontalBox();
            row.add(new JLabel("Y Lower Bound:"));
            row.add(Box.createHorizontalGlue());
        rowContainer.add(row);

        row = Box.createHorizontalBox();
            yLowerBoundField = new JTextField(16);
            yLowerBoundField.addActionListener(this::formSubmitted);
            yLowerBoundField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 
                    yLowerBoundField.getPreferredSize().height));
            row.add(yLowerBoundField);
        rowContainer.add(row);

        row = Box.createHorizontalBox();
            JButton button = new JButton("Update");
            button.addActionListener(this::formSubmitted);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                    button.getPreferredSize().height));
            row.add(button);
        add(rowContainer);
        
        pack();
    }

    /**
     * Method called when the user hits the "Update" button or
     * pressed the Enter/Return key within the form's text fields.
     * 
     * If valid double values are entered into each input field,
     * the parent ApplicationFrame's roomConfigured method is called,
     * updating the frame's canvas with new room dimension information.
     * @param event Event info from Swing (unused)
     */
    private void formSubmitted(ActionEvent event) {
        RoomConfiguration config = new RoomConfiguration();
        try {
            config.width = Double.parseDouble(widthField.getText());
        } catch (NumberFormatException e) {
            widthField.requestFocus();
            return;
        }

        try {
            config.length = Double.parseDouble(lengthField.getText());
        } catch (NumberFormatException e) {
            lengthField.requestFocus();
            return;
        }

        try {
            config.xLowerBound = Double.parseDouble(xLowerBoundField.getText());
        } catch (NumberFormatException e) {
            xLowerBoundField.requestFocus();
            return;
        }

        try {
            config.yLowerBound = Double.parseDouble(yLowerBoundField.getText());
        } catch (NumberFormatException e) {
            yLowerBoundField.requestFocus();
            return;
        }

        // destroy the frame so that Swing will free up its resources
        dispose();
        // notify parent of successful form submission
        parent.roomConfigured(config);
    }

    /**
     * Centers this dialog in front of its parent, then makes this
     * dialog visible.
     */
    public void prompt() {
        setLocationRelativeTo(getParent());
        setVisible(true);
    }
    
}
