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

    public static class RoomConfiguration {
        double width;
        double length;
        double xLowerBound;
        double yLowerBound;
    }

    private JTextField widthField;
    private JTextField lengthField;
    private JTextField xLowerBoundField;
    private JTextField yLowerBoundField;

    private ApplicationFrame parent;

    public ConfigureRoomDialog(ApplicationFrame parent) {
        super(parent, "Configure Room");
        this.parent = parent;
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

    private void formSubmitted(ActionEvent event) {
        RoomConfiguration config = new RoomConfiguration();
        try {
            config.width = Double.parseDouble(widthField.getText());
        } catch (NumberFormatException e) {
            widthField.requestFocus();
        }

        try {
            config.length = Double.parseDouble(lengthField.getText());
        } catch (NumberFormatException e) {
            lengthField.requestFocus();
        }

        try {
            config.xLowerBound = Double.parseDouble(xLowerBoundField.getText());
        } catch (NumberFormatException e) {
            xLowerBoundField.requestFocus();
        }

        try {
            config.yLowerBound = Double.parseDouble(yLowerBoundField.getText());
        } catch (NumberFormatException e) {
            yLowerBoundField.requestFocus();
        }

        dispose();
        parent.roomConfigured(config);
    }

    public RoomConfiguration prompt() {
        setLocationRelativeTo(getParent());
        setVisible(true);
        return null;
    }
    
}
