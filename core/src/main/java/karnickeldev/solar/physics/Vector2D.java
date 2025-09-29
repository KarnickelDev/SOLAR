package karnickeldev.solar.physics;

public final class Vector2D {

    private double x, y;

    public Vector2D() {
        this(0, 0);
    }

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D(Vector2D toCopy) {
        this.x = toCopy.getX();
        this.y = toCopy.getY();
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Vector2D set(Vector2D value) {
        this.x = value.getX();
        this.y = value.getY();
        return this;
    }

    public Vector2D set(double x, double y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector2D add(double x, double y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vector2D add(Vector2D other) {
        this.x += other.x;
        this.y += other.y;
        return this;
    }

    public Vector2D subtract(double x, double y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public Vector2D subtract(Vector2D other) {
        this.x -= other.x;
        this.y -= other.y;
        return this;
    }

    public Vector2D scale(double a) {
        x *= a;
        y *= a;
        return this;
    }

    public double len() {
        return Math.sqrt((x * x) + (y * y));
    }

    public double len2() {
        return (x * x) + (y * y);
    }

    public Vector2D nor() {
        double len = len();
        x /= len;
        y /= len;
        return this;
    }

    public Vector2D lerp(Vector2D target, float alpha) {
        x += (target.x - x) * alpha;
        y += (target.y - y) * alpha;
        return this;
    }

    public Vector2D zero() {
        x = 0;
        y = 0;
        return this;
    }

    public boolean isZero() {
        return x == 0 && y == 0;
    }

    public Vector2D copy() {
        return new Vector2D(this);
    }

}
