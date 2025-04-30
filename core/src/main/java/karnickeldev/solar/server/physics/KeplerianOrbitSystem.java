package karnickeldev.solar.server.physics;

import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.ecs.components.OrbitDataComponent;
import karnickeldev.solar.physics.Units;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class KeplerianOrbitSystem {

    public static Lock lock = new ReentrantLock();

    private final EntityManager em;
    private final OrbitDataComponent orbitData;

    public KeplerianOrbitSystem(EntityManager entityManager) {
        this.em = entityManager;
        this.orbitData = entityManager.orbitData;
    }

    public void updateHCS(double timeDays) {
        for (int entity = 0; entity < em.getAll(); entity++) {
            if(!em.isValid(entity) || !orbitData.has(entity)) continue;

            double a = orbitData.getSemiMajorAxis(entity) * 1.5e8;

            float e = orbitData.getEccentricity(entity);
            float omega = orbitData.getOmega(entity);
            float t0 = orbitData.getT0(entity);

            int centralBodyId = orbitData.getCentralBody(entity);

            double mu = getGravitationalParameter(centralBodyId); // G * M

            double n = Math.sqrt(mu / (a * a * a));  // mean motion
            double M = (n * ((timeDays) - t0));       // mean anomaly

            double E = solveKepler((float)M, e);            // eccentric anomaly
            double theta = 2 * Math.atan2(
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
            double cx = em.hcs.getPhysicsLocalX(centralBodyId);
            double cy = em.hcs.getPhysicsLocalY(centralBodyId);

            double globalX = cx + rotatedX;
            double globalY = cy + rotatedY;

            em.hcs.add(entity, centralBodyId,
                rotatedX,
                rotatedY,
                globalX,
                globalY
            );
        }
        em.hcs.syncRenderBuffers();
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

    private static final double G_CONSTANT = 4 * Math.PI * Math.PI / (365.25 * 365.25);
    private double getGravitationalParameter(int body) {
        double constant =  G_CONSTANT * Units.convert(em.masses.getMass(body), Units.Mass.TON, Units.Mass.SOLAR_MASS);
        double G = Units.G_KM_TON * em.masses.getMass(body);
        return G;
    }


}
