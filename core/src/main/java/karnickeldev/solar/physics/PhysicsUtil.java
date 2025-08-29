package karnickeldev.solar.physics;

import karnickeldev.solar.ecs.ServerECS;
import karnickeldev.solar.ecs.components.OrbitDataComponent;

public class PhysicsUtil {

    private static final double SMALLEST_ACCELERATION = 1e-9f;

    public static long estimateSOIStar(double mass) {
        return (long) (Units.toSU(8e4, Units.Length.AU)
            * Math.pow(mass / Units.toSU(1, Units.Mass.SOLAR_MASS), 2f / 5f));
    }

    public static long estimateSOIPlanet(double mass) {
        // distance where acceleration from gravity equals SMALLEST_ACCELERATION
        double distanceMinForce = Math.sqrt((Units.G_KM_TON * mass) / SMALLEST_ACCELERATION);

        return (long) distanceMinForce;
    }

    public static void initializePlanetoidAtPeriapsisHCS(ServerECS ecs, int entityId) {
//        if(!em.isValid(entityId) || !EntityFactory.isStaticPlanetoid(em, entityId)) {
//            //throw new RuntimeException("Cant initialize Entity as Planetoid");
//        }

        OrbitDataComponent orbitData = ecs.getComponentRegistry().get(OrbitDataComponent.class);
        float a = orbitData.getSemiMajorAxis(entityId);
        float e = orbitData.getEccentricity(entityId);
        float omega = orbitData.getOmega(entityId);

        float r = a * (1 - e);

        // Rotate point (r, 0) by omega
        float x = (float) (Math.cos(omega) * r);
        float y = (float) (Math.sin(omega) * r);

        ecs.hcs.add(entityId, orbitData.getCentralBody(entityId), x, y);
    }

}
