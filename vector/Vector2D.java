package vector;

public class Vector2D {

    public double x;
    public double y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double distanceFromSquared(Vector2D v) {
        double dx = x - v.x;
        double dy = y - v.y;
        return dx * dx + dy * dy;
    }

    public double distanceFrom(Vector2D v) {
        double dx = x - v.x;
        double dy = y - v.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public Vector2D plus(Vector2D v) {
        return new Vector2D(x + v.x, y + v.y);
    }

    public Vector2D minus(Vector2D v) {
        return new Vector2D(x - v.x, y - v.y);
    }

    public double dotProduct(Vector2D v) {
        return x * v.x + y * v.y;
    }

    public Vector2D scale(double s) {
        return new Vector2D(x * s, y * s);
    }

    public Vector2D divide(double d) {
        return new Vector2D(x / d, y / d);
    }

}
