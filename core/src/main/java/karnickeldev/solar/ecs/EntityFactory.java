package karnickeldev.solar.ecs;

import karnickeldev.solar.ecs.components.OrbitData;
import karnickeldev.solar.physics.PhysicsUtil;
import karnickeldev.solar.physics.Units;

public class EntityFactory {

    public static boolean isGhostObject(EntityManager em, int entityId) {
        return em.isValid(entityId)
            && em.tags.has(entityId, Tags.GHOST_OBJECT)
            && em.names.has(entityId)
            && em.positions.has(entityId);
    }

    public static int createGhostObject(EntityManager em, String name, double x, double y) {
        int entity = em.create();
        em.tags.add(entity, Tags.GHOST_OBJECT);
        em.names.add(entity, name);
        em.positions.add(entity, x, y);
        assert isGhostObject(em, entity);
        return entity;
    }

    public static boolean isStar(EntityManager em, int entityId) {
        return em.isValid(entityId)
            && em.tags.has(entityId, Tags.STAR)
            && em.names.has(entityId)
            && em.positions.has(entityId)
            && em.masses.has(entityId)
            && em.radius.has(entityId)
            && em.sphereOfInfluence.has(entityId);
    }

    public static int createStar(EntityManager em, String name, double x, double y, double mass, int radius,
                                 long sphereOfInfluence) {
        int entity = em.create();
        em.tags.add(entity, Tags.STAR);
        em.names.add(entity, name);
        em.positions.add(entity, x, y);
        em.masses.add(entity, mass);
        em.radius.add(entity, radius);
        em.sphereOfInfluence.add(entity, sphereOfInfluence);

        // this is important! without this, rendering the star will interpolate between
        // actual position and an uninitialized previous position, causing flickering
        em.positions.addNext(entity, x, y);
        em.positions.advance();

        assert isStar(em, entity);
        return entity;
    }

    public static int createStar(EntityManager em, String name, double x, double y, double mass, int radius) {
        return createStar(em, name, x, y, mass, radius, PhysicsUtil.estimateSOIStar(mass));
    }

    public static boolean isStaticPlanetoid(EntityManager em, int entityId) {
        return em.isValid(entityId)
            && em.tags.has(entityId, Tags.PLANET)
            && em.names.has(entityId)
            && em.masses.has(entityId)
            && em.radius.has(entityId)
            && em.sphereOfInfluence.has(entityId)
            && em.positions.has(entityId)
            && em.orbitData.has(entityId);
    }

    public static int createStaticPlanetoidHCS(EntityManager em, String name, double mass, int radius,
                                            long sphereOfInfluence, float semiMajorAxis, float eccentricity, float omega,
                                            float t0, int centralBody, int scale) {
        int entity = em.create();
        em.tags.add(entity, Tags.PLANET);
        em.names.add(entity, name);
        em.masses.add(entity, mass);
        em.radius.add(entity, radius);
        em.sphereOfInfluence.add(entity, sphereOfInfluence);
        em.orbitData.add(entity, semiMajorAxis, eccentricity, omega, t0, centralBody);

        em.hcs.add(entity, centralBody, 0,0,0,0);

        PhysicsUtil.initializePlanetoidAtPeriapsisHCS(em, entity);
        assert isStaticPlanetoid(em, entity);
        return entity;
    }

}
