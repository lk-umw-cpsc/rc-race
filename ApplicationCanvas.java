import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Random;

import javax.swing.JPanel;

import motive.CommandStreamManager;
import motive.RigidBodyUpdateListener;
import vector.Vector3D;

public class ApplicationCanvas extends JPanel implements RigidBodyUpdateListener, MouseListener {
    
    private static final int CANVAS_WIDTH_HEIGHT = 600;

    private static final double ROOM_X_LOWER_LIMIT = -6.0;
    private static final double ROOM_Y_LOWER_LIMIT = -6.0;

    private static final double ROOM_LENGTH = 12.0;
    private static final double ROOM_WIDTH = 12.0;

    private static final double PICKUP_RADIUS = 0.5;

    private double roomXLowerBound = ROOM_X_LOWER_LIMIT;
    private double roomYLowerBound = ROOM_Y_LOWER_LIMIT;
    private double roomWidth = ROOM_WIDTH;
    private double roomLength = ROOM_LENGTH;

    private Vector3D playerLocation;
    private Vector3D pickupLocation;

    private final Random rng;

    public ApplicationCanvas() {
        setPreferredSize(new Dimension(CANVAS_WIDTH_HEIGHT, CANVAS_WIDTH_HEIGHT));
        playerLocation = new Vector3D(2.0, 5, 0);
        pickupLocation = new Vector3D(0, -5, 0);
        rng = new Random();
        addMouseListener(this);
        // CommandStreamManager streamManager = new CommandStreamManager();
        // streamManager.addRigidBodyUpdateListener(this);
    }

    private static final Color BACKGROUND_COLOR = new Color(51, 51, 51);
    private static final Color PLAYER_DOT_COLOR = new Color(227, 0, 170);
    private static final Color PICKUP_DOT_COLOR = new Color(154, 189, 0);

    private static final int PLAYER_DOT_RADIUS = 15;
    private static final int PICKUP_DOT_RADIUS = 15;

    @Override
    public void paint(Graphics g) {
        setRenderingHints(g);
        final int width = getWidth();
        final int height = getHeight();

        g.setColor(BACKGROUND_COLOR);
        g.fillRect(0, 0, width, height);
        
        drawVectorPoint(g, pickupLocation, PICKUP_DOT_RADIUS, PICKUP_DOT_COLOR);
        drawVectorPoint(g, playerLocation, PLAYER_DOT_RADIUS, PLAYER_DOT_COLOR);
    }

    public void setRoomDimensions(double xLowerBound, double yLowerBound, double width, double length) {
        roomXLowerBound = xLowerBound;
        roomYLowerBound = yLowerBound;
        roomWidth = width;
        roomLength = length;
        repaint();
    }

    private static void setRenderingHints(Graphics graphics) {
        Graphics2D g = (Graphics2D)graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    private void drawVectorPoint(Graphics g, Vector3D v, int radius, Color color) {
        int x = coordinate3dToScreenCoordinateX(v.x);
        int y = coordinate3dToScreenCoordinateY(v.y);
        x -= radius + 1;
        y -= radius + 1;
        final int diameter = (radius * 2) + 1;
        g.setColor(color);
        g.fillOval(x, y, diameter, diameter);
    }

    private int coordinate3dToScreenCoordinateX(double x) {
        final int canvasWidth = getWidth();
        return (int) ((x - roomXLowerBound) / roomWidth * canvasWidth);
    }

    private int coordinate3dToScreenCoordinateY(double y) {
        final int canvasHeight = getHeight();
        return (int) -((y + roomYLowerBound) / roomLength * canvasHeight);
    }

    @Override
    public void update(int id, float x, float y, float z) {
        playerLocation.x = x;
        playerLocation.y = y;
        playerLocation.z = z;
        pickupLocation.z = z;

        while (playerLocation.distanceFrom(pickupLocation) < PICKUP_RADIUS) {
            pickupLocation.x = rng.nextDouble() * roomWidth + roomXLowerBound;
            pickupLocation.y = rng.nextDouble() * roomLength + roomYLowerBound;
        }
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int screenX = e.getX();
        int screenY = -e.getY();
        float unitX = screenX / (float) getWidth();
        float unitY = screenY / (float) getHeight();

        update(0, (float)(unitX * roomWidth + roomXLowerBound), 
                (float)(unitY * roomLength - roomYLowerBound), 0.0f);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        
    }

    @Override
    public void mouseExited(MouseEvent e) {
        
    }

}
