package karnickeldev.solar.physics;

public class GhostObject implements PhysicsObject {

    private static int OBJ_COUNT;

    private final String name;
    private final float mass;
    private final Vector2D position;

    public GhostObject(Vector2D position) {
        this("" + (OBJ_COUNT++), 0, position);
    }

    public GhostObject(float mass, Vector2D position) {
        this("" + (OBJ_COUNT++), mass, position);
    }

    public GhostObject(String name, float mass, Vector2D position) {
        this.name = name;
        this.mass = mass;
        this.position = new Vector2D(position);
    }

    @Override
    public void update(double time) {
        // object does not move
    }

    @Override
    public Vector2D getPosition() {
        return position;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public float getMass() {
        return mass;
    }
}
