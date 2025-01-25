package karnickeldev.solar.physics;

public class OrbitData{

    private double a; // Semi-major axis
    private double e; // Eccentricity
    private double omega; // Argument of periapsis (radians)
    private double M; // Mass of central Body
    private double t0; // Time of periapsis passage

    private PhysicsObject centralBody;

    public OrbitData(PhysicsObject centralBody, double semiMajorAxis, double eccentricity, double omega, double t0) {
        this.centralBody = centralBody;
        this.a = semiMajorAxis;
        this.e = eccentricity;
        this.omega = omega;
        this.M = centralBody.getMass();
        this.t0 = t0;
    }

    public PhysicsObject getCentralBody() {
        return centralBody;
    }

    public double semiMajorAxis() {
        return a;
    }

    public double eccentricity() {
        return e;
    }

    public double omega() {
        return omega;
    }

    public double M() {
        return M;
    }

    public double t0() {
        return t0;
    }
}
