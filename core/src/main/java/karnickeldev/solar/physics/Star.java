package karnickeldev.solar.physics;

public class Star extends OrbitalObject {

    private final boolean canMove;

    public Star(String name, float mass, int radius, boolean canMove, OrbitData orbitData) {
        super(name, mass, radius, orbitData);
        this.canMove = canMove;
    }

    @Override
    public void update(double time) {
        if(canMove) {
            super.update(time);
        }
    }

    @Override
    public Vector2D getPosition() {
        if(canMove) {
            return super.getPosition();
        } else {
            return getOrbitData().getCentralBody().getPosition();
        }
    }

}
