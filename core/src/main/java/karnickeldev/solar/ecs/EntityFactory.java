package karnickeldev.solar.ecs;

import karnickeldev.solar.assetmanager.Asset;
import karnickeldev.solar.ecs.components.*;
import karnickeldev.solar.physics.PhysicsUtil;
import karnickeldev.solar.util.WorldPos;
import karnickeldev.solar.util.SplitCoordMath;

public class EntityFactory {

    public static int createGhostObject(ServerECS ecs, String name, short sectorX, double localX, short sectorY, double localY) {
        int entity = ecs.getEntityManager().create();
        ecs.getComponentRegistry().get(TagComponent.class).add(entity, Tag.GHOST_OBJECT);
        ecs.getComponentRegistry().get(NameComponent.class).add(entity, name);
        ecs.hcs.add(entity, EntityManager.NO_ENTITY, sectorX, localX, sectorY, localY);
        return entity;
    }


    public static int createStar(ServerECS ecs, String name, double x, double y, double mass, float radius, long sphereOfInfluence) {
        int entity = ecs.getEntityManager().create();
        ecs.getComponentRegistry().get(TagComponent.class).add(entity, Tag.STAR);
        ecs.getComponentRegistry().get(NameComponent.class).add(entity, name);
        ecs.getComponentRegistry().get(MassComponent.class).add(entity, mass);
        ecs.getComponentRegistry().get(RadiusComponent.class).add(entity, radius);
        ecs.getComponentRegistry().get(SphereOfInfluenceComponent.class).add(entity, sphereOfInfluence);
        WorldPos c = new WorldPos();
        SplitCoordMath.split(c, x, y);
        ecs.hcs.add(entity, EntityManager.NO_ENTITY, c.sx, c.lx, c.sy, c.ly);

        // texture
        ecs.getComponentRegistry().get(AppearanceComponent.class).add(entity, (short) Asset.DEBUG_CIRCLE.ordinal(), (short)0, "");

        return entity;
    }

    public static int createStar(ServerECS ecs, String name, double x, double y, double mass, float radius) {
        return createStar(ecs, name, x, y, mass, radius, PhysicsUtil.estimateSOIStar(mass));
    }

    public static int createStaticPlanetoidHCS(ServerECS ecs, String name, double mass, float radius,
                                               long sphereOfInfluence, float semiMajorAxis, float eccentricity, float omega,
                                               long t0, int centralBody) {
        int entity = ecs.getEntityManager().create();
        ecs.getComponentRegistry().get(TagComponent.class).add(entity, Tag.PLANET);
        ecs.getComponentRegistry().get(NameComponent.class).add(entity, name);
        ecs.getComponentRegistry().get(MassComponent.class).add(entity, mass);
        ecs.getComponentRegistry().get(RadiusComponent.class).add(entity, radius);
        ecs.getComponentRegistry().get(SphereOfInfluenceComponent.class).add(entity, sphereOfInfluence);
        ecs.getComponentRegistry().get(OrbitDataComponent.class).add(entity, semiMajorAxis, eccentricity, omega, t0, centralBody);

        // texture
        ecs.getComponentRegistry().get(AppearanceComponent.class).add(entity, (short) Asset.DEBUG_CIRCLE.ordinal(), (short)0, "");

        PhysicsUtil.initializePlanetoidAtPeriapsisHCS(ecs, entity);
        return entity;
    }

}
