package karnickeldev.solar.physics;

public class Planetoid extends OrbitalObject {

    private final int atmosphereHeight;     // in km

    public Planetoid(String name, float mass, int radius, int atmosphereHeight, OrbitData orbitData) {
        super(name, mass, radius, orbitData);

        this.atmosphereHeight = atmosphereHeight;
    }

    public Planetoid(
        String name, float mass, int radius, int atmosphereHeight,
        PhysicsObject centralBody, double a, double e, double omega, double t0) {

        this(name, mass, radius, atmosphereHeight, new OrbitData(centralBody, a, e, omega, t0));
    }

    public int getAtmosphereHeight() {
        return atmosphereHeight;
    }
}
