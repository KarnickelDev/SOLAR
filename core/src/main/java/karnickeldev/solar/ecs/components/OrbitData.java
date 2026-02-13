package karnickeldev.solar.ecs.components;

import karnickeldev.solar.ecs.EntityReference;

// unused for now
public class OrbitData {

    private final float semiMajorAxis;
    private final float eccentricity;
    private final float omega;        // Argument of periapsis (radians)
    private final long t0;           // Time of periapsis passage

    private final EntityReference centralBody;

    public OrbitData(OrbitData source) {
        this(source.semiMajorAxis, source.eccentricity, source.omega, source.t0, source.centralBody);
    }

    public OrbitData(float semiMajorAxis, float eccentricity, float omega, long t0, EntityReference centralBody) {
        this.semiMajorAxis = semiMajorAxis;
        this.eccentricity = eccentricity;
        this.omega = omega;
        this.t0 = t0;
        this.centralBody = centralBody;
    }

    public float getSemiMajorAxis() {
        return semiMajorAxis;
    }

    public float getEccentricity() {
        return eccentricity;
    }

    public float getOmega() {
        return omega;
    }

    public long getT0() {
        return t0;
    }

    public EntityReference getCentralBody() {
        return centralBody;
    }
}
