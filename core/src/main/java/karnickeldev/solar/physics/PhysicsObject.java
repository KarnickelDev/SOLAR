package karnickeldev.solar.physics;

public interface PhysicsObject {

    void update(double time);

    Vector2D getPosition();

    String getName();

    float getMass();

    default double getX() {
        return getPosition().getX();
    }

    default double getY() {
        return getPosition().getY();
    }

    default boolean equals(PhysicsObject other) {
        return other != null && this.getMass() == other.getMass() && this.getName().equals(other.getName());
    }
}
