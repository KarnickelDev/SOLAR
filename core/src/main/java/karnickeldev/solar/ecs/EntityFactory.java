package karnickeldev.solar.ecs;

import karnickeldev.solar.ecs.components.*;
import karnickeldev.solar.physics.PhysicsUtil;

public class EntityFactory {

    public static int createGhostObject(ServerECS ecs, String name, double x, double y) {
        int entity = ecs.getEntityManager().create();
        ecs.getComponentRegistry().get(TagComponent.class).add(entity, Tags.GHOST_OBJECT);
        ecs.getComponentRegistry().get(NameComponent.class).add(entity, name);
        ecs.hcs.add(entity, EntityManager.NO_ENTITY, x, y);
        return entity;
    }


    public static int createStar(ServerECS ecs, String name, double x, double y, double mass, float radius,
                                 long sphereOfInfluence) {
        int entity = ecs.getEntityManager().create();
        ecs.getComponentRegistry().get(TagComponent.class).add(entity, Tags.STAR);
        ecs.getComponentRegistry().get(NameComponent.class).add(entity, name);
        ecs.getComponentRegistry().get(MassComponent.class).add(entity, mass);
        ecs.getComponentRegistry().get(RadiusComponent.class).add(entity, radius);
        ecs.getComponentRegistry().get(SphereOfInfluenceComponent.class).add(entity, sphereOfInfluence);
        ecs.hcs.add(entity, EntityManager.NO_ENTITY, x, y);
        return entity;
    }

    public static int createStar(ServerECS ecs, String name, double x, double y, double mass, float radius) {
        return createStar(ecs, name, x, y, mass, radius, PhysicsUtil.estimateSOIStar(mass));
    }

    public static int createStaticPlanetoidHCS(ServerECS ecs, String name, double mass, float radius,
                                               long sphereOfInfluence, float semiMajorAxis, float eccentricity, float omega,
                                               float t0, int centralBody) {
        int entity = ecs.getEntityManager().create();
        ecs.getComponentRegistry().get(TagComponent.class).add(entity, Tags.PLANET);
        ecs.getComponentRegistry().get(NameComponent.class).add(entity, name);
        ecs.getComponentRegistry().get(MassComponent.class).add(entity, mass);
        ecs.getComponentRegistry().get(RadiusComponent.class).add(entity, radius);
        ecs.getComponentRegistry().get(SphereOfInfluenceComponent.class).add(entity, sphereOfInfluence);
        ecs.getComponentRegistry().get(OrbitDataComponent.class).add(entity, semiMajorAxis, eccentricity, omega, t0, centralBody);

        PhysicsUtil.initializePlanetoidAtPeriapsisHCS(ecs, entity);
        return entity;
    }

}
