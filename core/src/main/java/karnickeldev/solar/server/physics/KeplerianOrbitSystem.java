package karnickeldev.solar.server.physics;

import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.ecs.components.OrbitDataComponent;
import karnickeldev.solar.physics.Units;

public class KeplerianOrbitSystem {

    private final EntityManager em;
    private final OrbitDataComponent orbitData;

    public KeplerianOrbitSystem(EntityManager entityManager) {
        this.em = entityManager;
        this.orbitData = entityManager.orbitData;
    }

    public void updateHCS(double timeDays) {
        for (int entity = 0; entity < em.getAll(); entity++) {
            if(!em.isValid(entity) || !orbitData.has(entity)) continue;

            float a = Units.toSU(orbitData.getSemiMajorAxis(entity), Units.Length.AU);
            float e = orbitData.getEccentricity(entity);
            float omega = orbitData.getOmega(entity);
            float t0 = orbitData.getT0(entity);
            double mu = getGravitationalParameter(orbitData.getCentralBody(entity)); // G * M

            int centralBodyId = orbitData.getCentralBody(entity);

            double n = Math.sqrt(mu / (a * a * a));  // mean motion
            double M = (n * (timeDays - t0));       // mean anomaly

            double E = solveKepler((float)M, e);            // eccentric anomaly
            double theta = 2f * Math.atan2(
                Math.sqrt(1 + e) * Math.sin(E / 2),
                Math.sqrt(1 - e) * Math.cos(E / 2)
            );

            double r = a * (1 - e * Math.cos(E));

            double orbitX = r * Math.cos(theta);
            double orbitY = r * Math.sin(theta);

            // Rotate by omega
            double cosW = Math.cos(omega);
            double sinW = Math.sin(omega);

            double rotatedX = cosW * orbitX - sinW * orbitY;
            double rotatedY = sinW * orbitX + cosW * orbitY;

            // Add central body's position
            double cx = em.hcs.getLocalX(centralBodyId);
            double cy = em.hcs.getLocalY(centralBodyId);

            em.hcs.add(entity, centralBodyId,
                (cx + rotatedX),
                (cy + rotatedY),
                0,
                0);
        }
    }

    private static float solveKepler(float M, float e) {
        float E = M;
        float epsilon = 1e-5f;
        for (int i = 0; i < 5; i++) {
            float f = E - e * (float)Math.sin(E) - M;
            float fPrime = 1 - e * (float)Math.cos(E);
            float delta = f / fPrime;
            E -= delta;
            if (Math.abs(delta) < epsilon) break;
        }
        return E;
    }

    private static final double G_CONSTANT = 4 * Math.PI * Math.PI / (365.25*365.25);
    private double getGravitationalParameter(int body) {
        return G_CONSTANT * Units.convert(em.masses.getMass(body), Units.Mass.TON, Units.Mass.SOLAR_MASS);
    }


}
