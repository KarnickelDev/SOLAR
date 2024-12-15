package karnickeldev.solar.physics;

public interface PhysicsObject {

    void update(double time);

    Vector2D getPosition();

    String getName();

    float getMass();
}
