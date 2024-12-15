package karnickeldev.solar.physics;

public class OrbitalObject implements PhysicsObject {

    private final OrbitData orbitData;

    private String name;
    private final float mass;
    private final int radius;

    private final Vector2D position;

    public OrbitalObject(String name, float mass, int radius, PhysicsObject parent,
                         double semiMajorAxis, double eccentricity, double periapsisArgument, double timeOfPeriapsis) {

        orbitData = new OrbitData(parent, semiMajorAxis, eccentricity, periapsisArgument, timeOfPeriapsis);

        this.name = name;
        this.mass = mass;
        this.radius = radius;

        position = calculatePosition(timeOfPeriapsis);
    }

    public OrbitalObject(String name, float mass, int radius, OrbitData orbitData) {
        this.name = name;
        this.mass = mass;
        this.radius = radius;

        this.orbitData = orbitData;

        position = calculatePosition(orbitData.t0());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getMass() {
        return mass;
    }

    public int getRadius() {
        return radius;
    }

    public OrbitData getOrbitData() {
        return orbitData;
    }

    public PhysicsObject getCentralBody() {
        return orbitData.getCentralBody();
    }

    public double getSemiMajorAxis() {
        return orbitData.semiMajorAxis();
    }

    public double getEccentricity() {
        return orbitData.eccentricity();
    }

    public Vector2D getPosition() {
        return position;
    }


    public void update(double time) {
        position.set(calculatePosition(time).add(getCentralBody().getPosition()));
    }

    public Vector2D calculatePosition(double t) {
        // Step 1: Compute mean anomaly
        double trueAnomaly = getTrueAnomaly(t);

        // Step 4: Compute orbital radius
        double r = getSemiMajorAxis() *
            (1 - getEccentricity() * getEccentricity()) / (1 + getEccentricity() * Math.cos(trueAnomaly));

        // Step 5: Position in orbital plane
        double x = r * Math.cos(trueAnomaly);
        double y = r * Math.sin(trueAnomaly);

        // Step 6: Rotate by argument of periapsis
        double xRot = x * Math.cos(orbitData.omega()) - y * Math.sin(orbitData.omega());
        double yRot = x * Math.sin(orbitData.omega()) + y * Math.cos(orbitData.omega());

        return new Vector2D(xRot, yRot);
    }

    private double getTrueAnomaly(double t) {
        double n = Math.sqrt(Units.G_KM_TON*orbitData.M() / Math.pow(getSemiMajorAxis(), 3)); // Mean motion
        double M = n * (t - orbitData.t0());

        // Step 2: Solve Kepler's Equation for eccentric anomaly
        double E = M; // Initial guess
        for (int i = 0; i < 10; i++) { // Newton's method
            double deltaE = (M - (E - getEccentricity() * Math.sin(E))) / (1 - getEccentricity() * Math.cos(E));
            E += deltaE;
            if (Math.abs(deltaE) < 1e-6) break; // Convergence
        }

        // Step 3: Compute true anomaly
        return 2 * Math.atan2(
            Math.sqrt(1 + getEccentricity()) * Math.sin(E / 2),
            Math.sqrt(1 - getEccentricity()) * Math.cos(E / 2)
        );
    }

}
