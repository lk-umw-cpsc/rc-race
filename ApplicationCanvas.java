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
import vector.Vector2D;

public class ApplicationCanvas extends JPanel implements RigidBodyUpdateListener, MouseListener {
    
    private static final boolean TEST_MODE = false;

    // the width and height of the canvas, in pixels
    private static final int CANVAS_WIDTH_HEIGHT = 600;

    // the default room lower X and Y limit
    private static final double ROOM_X_LOWER_LIMIT = -1.0;
    private static final double ROOM_Y_LOWER_LIMIT = -1.0;

    // the default room length and width
    private static final double ROOM_LENGTH = 2.0;
    private static final double ROOM_WIDTH = 2.0;

    // the maximum distance before we consider the player having reached the pickup
    private static final double PICKUP_RADIUS = 0.2;

    private double roomXLowerBound = ROOM_X_LOWER_LIMIT;
    private double roomYLowerBound = ROOM_Y_LOWER_LIMIT;
    private double roomWidth = ROOM_WIDTH;
    private double roomLength = ROOM_LENGTH;

    private Vector2D playerLocation;
    private Vector2D pickupLocation;

    private final Random rng;

    public ApplicationCanvas() {
        // set size of the canvas
        setPreferredSize(new Dimension(CANVAS_WIDTH_HEIGHT, CANVAS_WIDTH_HEIGHT));
        // start player and pick up at specific coordinates (for testing purposes)
        playerLocation = new Vector2D(0.75, 0.75);
        pickupLocation = new Vector2D(-0.75, -0.75);
        // instantiate the Random object for random number generation
        rng = new Random();

        if (TEST_MODE) {
            addMouseListener(this);
        } else {
            // begin listening for updates from Motive
            CommandStreamManager streamManager = new CommandStreamManager();
            streamManager.addRigidBodyUpdateListener(this);
            new Thread(streamManager).start();
        }
    }

    // colors for the dots drawn to the screen
    private static final Color BACKGROUND_COLOR = new Color(51, 51, 51);
    private static final Color PLAYER_DOT_COLOR = new Color(227, 0, 170);
    private static final Color PICKUP_DOT_COLOR = new Color(154, 189, 0);

    // radius of the dots drawn to screen, in pixels
    private static final int PLAYER_DOT_RADIUS = 15;
    private static final int PICKUP_DOT_RADIUS = 15;

    @Override
    public void paint(Graphics g) {
        // turn on shape anti-aliasing (reduces jagged pixels)
        setRenderingHints(g);
        final int width = getWidth();
        final int height = getHeight();

        // draw over the previous frame with the background color
        g.setColor(BACKGROUND_COLOR);
        g.fillRect(0, 0, width, height);
        
        // draw each dot
        drawVectorPoint(g, pickupLocation, PICKUP_DOT_RADIUS, PICKUP_DOT_COLOR);
        drawVectorPoint(g, playerLocation, PLAYER_DOT_RADIUS, PLAYER_DOT_COLOR);
    }

    /**
     * Tweaks rendering hints for the scene
     * @param graphics The Graphics object to set rendering hints on
     */
    private static void setRenderingHints(Graphics graphics) {
        Graphics2D g = (Graphics2D)graphics;
        // Enable anti-aliasing for shapes
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    /**
     * Draws a dot to the screen, translating room coordinates to screen coordinates
     * @param g The Graphics object to draw to
     * @param v The 3D vector to draw
     * @param radius The radius of the point to draw
     * @param color The color to draw the point in
     */
    private void drawVectorPoint(Graphics g, Vector2D v, int radius, Color color) {
        int x = coordinate3dToScreenCoordinateX(v.x);
        int y = coordinate3dToScreenCoordinateY(v.y);
        x -= radius + 1;
        y -= radius + 1;
        final int diameter = (radius * 2) + 1;
        g.setColor(color);
        g.fillOval(x, y, diameter, diameter);
    }

    /**
     * Converts a 3d coordinate's X coordinate to a screen coordinate
     * @param x the X coordinate to translate
     * @return a translated X coordinate
     */
    private int coordinate3dToScreenCoordinateX(double x) {
        final int canvasWidth = getWidth();
        return (int) ((x - roomXLowerBound) / roomWidth * canvasWidth);
    }

    /**
     * Converts a 3d coordinate's Y coordinate to a screen coordinate
     * @param y the Y coordinate to translate
     * @return a translated Y coordinate
     */
    private int coordinate3dToScreenCoordinateY(double y) {
        final int canvasHeight = getHeight();
        return (int) -((y + roomYLowerBound) / roomLength * canvasHeight);
    }

    /**
     * Updates the room dimensions used by the application and then
     * redraws the scene.
     * @param xLowerBound the left-most X coordinate that should correlate to the left of the screen
     * @param yLowerBound the bottom-most Y coordinate that should correlate to the bottom of the screen
     * @param width the width of the room
     * @param length the height of the room
     */
    public void setRoomDimensions(double xLowerBound, double yLowerBound, double width, double length) {
        roomXLowerBound = xLowerBound;
        roomYLowerBound = yLowerBound;
        roomWidth = width;
        roomLength = length;
        repaint();
    }

    /**
     * Method called by motive when the RC vehicle's location is updated
     */
    @Override
    public void update(int id, float x, float y, float z) {
        // Update the player location, and the z coordinate of the pickup
        playerLocation.x = x;
        playerLocation.y = y;

        // Move the pick up if the player is near it
        // (use a loop so that the pickup doesn't spawn under the player)
        while (playerLocation.distanceFrom(pickupLocation) < PICKUP_RADIUS) {
            pickupLocation.x = rng.nextDouble() * roomWidth + roomXLowerBound;
            pickupLocation.y = rng.nextDouble() * roomLength + roomYLowerBound;
        }
        // Redraw the canvas with the updated scene information
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        
    }

    /**
     * Used for testing purposes
     * @param e Event information from Swing
     */
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
