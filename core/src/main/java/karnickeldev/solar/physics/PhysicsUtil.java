package karnickeldev.solar.physics;

import karnickeldev.solar.ecs.EntityFactory;
import karnickeldev.solar.ecs.EntityManager;

public class PhysicsUtil {

    private static final double SMALLEST_ACCELERATION = 1e-9f;

    public static long estimateSOIStar(double mass) {
        return (long) (Units.toSU(8e4, Units.Length.AU)
            * Math.pow(mass / Units.toSU(1, Units.Mass.SOLAR_MASS), 2f/5f));
    }

    public static long estimateSOIPlanet(double mass) {
        // distance where acceleration from gravity equals SMALLEST_ACCELERATION
        double distanceMinForce = Math.sqrt((Units.G_KM_TON * mass) / SMALLEST_ACCELERATION);

        return (long) distanceMinForce;
    }

    public static void initializePlanetoidAtPeriapsis(EntityManager em, int entityId) {
        if(!em.isValid(entityId) || !EntityFactory.isStaticPlanetoid(em, entityId)) {
            throw new RuntimeException("Cant initialize Entity as Planetoid");
        }

        float a = em.orbitData.getSemiMajorAxis(entityId);
        float e = em.orbitData.getEccentricity(entityId);
        float omega = em.orbitData.getOmega(entityId);

        float r = a * (1 - e);

        // Rotate point (r, 0) by omega
        float x = (float)(Math.cos(omega) * r);
        float y = (float)(Math.sin(omega) * r);

        // Add central body's position
        int centralId = em.orbitData.getCentralBody(entityId);
        double cx = em.positions.getX(centralId);
        double cy = em.positions.getY(centralId);

        em.positions.add(entityId, cx + x, cy + y);
    }

    public static void initializePlanetoidAtPeriapsisHCS(EntityManager em, int entityId) {
        if(!em.isValid(entityId) || !EntityFactory.isStaticPlanetoid(em, entityId)) {
            //throw new RuntimeException("Cant initialize Entity as Planetoid");
        }

        float a = em.orbitData.getSemiMajorAxis(entityId);
        float e = em.orbitData.getEccentricity(entityId);
        float omega = em.orbitData.getOmega(entityId);

        float r = a * (1 - e);

        // Rotate point (r, 0) by omega
        float x = (float)(Math.cos(omega) * r);
        float y = (float)(Math.sin(omega) * r);

        em.hcs.add(entityId, em.orbitData.getCentralBody(entityId), x, y, 0, 0);
    }

}
