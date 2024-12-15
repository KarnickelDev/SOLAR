package karnickeldev.solar.physics;

public class Planet extends OrbitalObject {

    private final int atmosphere;

    public Planet(String name, float mass, int radius, int atmosphere, OrbitData orbitData) {
        super(name, mass, radius, orbitData);

        this.atmosphere = atmosphere;
    }

    public Planet(
        String name, float mass, int radius, int atmosphere,
        PhysicsObject centralBody, double a, double e, double omega, double t0) {

        this(name, mass, radius, atmosphere, new OrbitData(centralBody, a, e, omega, t0));
    }

    public int getAtmosphere() {
        return atmosphere;
    }
}
