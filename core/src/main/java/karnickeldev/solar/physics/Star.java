package karnickeldev.solar.physics;

public class Star implements PhysicsObject {

    private final String name;
    private final float mass;
    private final Vector2D position;

    public Star(String name, float mass, Vector2D position) {
        this.name = name;
        this.mass = mass;
        this.position = position;
    }

    @Override
    public void update(double time) {

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
