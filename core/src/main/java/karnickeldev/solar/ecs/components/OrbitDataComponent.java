package karnickeldev.solar.ecs.components;

import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.util.MathUtil;

import java.util.Arrays;
import java.util.BitSet;

public class OrbitDataComponent extends DirtyFlagComponent implements ComponentSnapshotProvider<OrbitDataSnapshot> {

    public final BitSet hasComponent = new BitSet(EntityManager.MAX_ENTITIES);

    public final double[] semiMajorAxis = new double[EntityManager.MAX_ENTITIES];
    public final double[] eccentricity = new double[EntityManager.MAX_ENTITIES];
    public final double[] omega = new double[EntityManager.MAX_ENTITIES];        // Argument of periapsis (radians)
    public final double[] t0 = new double[EntityManager.MAX_ENTITIES];           // Time of periapsis passage
    public final int[] centralBody = new int[EntityManager.MAX_ENTITIES];

    @Override
    public void ensureCapacity(int entityId) {
        // nop
    }

    public void add(int entityId, float semiMajorAxis, float eccentricity, float omega, float t0,
                    int centralBody) {
        ensureCapacity(entityId);
        int index = EntityManager.extractIndex(entityId);
        this.semiMajorAxis[index] = semiMajorAxis;
        this.eccentricity[index] = eccentricity;
        this.omega[index] = omega;
        this.t0[index] = t0;
        this.centralBody[index] = centralBody;
        hasComponent.set(index);
    }

    public void add(int entityId, OrbitData orbitData) {
        add(entityId, orbitData.getSemiMajorAxis(), orbitData.getEccentricity(), orbitData.getOmega(),
            orbitData.getT0(), orbitData.getCentralBody().getEntityId());
    }

    public void remove(int entityId) {
        hasComponent.clear(EntityManager.extractIndex(entityId));
    }

    public boolean has(int entityId) {
        return hasComponent.get(EntityManager.extractIndex(entityId));
    }

    public float getSemiMajorAxis(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        return (float) semiMajorAxis[index];
    }

    public float getEccentricity(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        return (float) eccentricity[index];
    }

    public float getOmega(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        return (float) omega[index];
    }

    public float getT0(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        return (float) t0[index];
    }

    public int getCentralBody(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        return centralBody[index];
    }

    @Override
    public OrbitDataSnapshot createSnapshot(long tick) {
        int size = getDirtyAmount();
        if (size < 1) return null;

        OrbitDataSnapshot snapshot = new OrbitDataSnapshot(size, tick);

        for (int entity = 1; entity < 1 + EntityManager.MAX_ENTITIES; entity++) {
            if (!isDirty(entity)) continue;

            snapshot.addChange(
                entity,
                getSemiMajorAxis(entity),
                getEccentricity(entity),
                getOmega(entity),
                getT0(entity),
                getCentralBody(entity)
            );
        }
        return snapshot;
    }

    @Override
    public OrbitDataSnapshot createFullSnapshot(long tick) {
        int size = 1 + EntityManager.MAX_ENTITIES;

        OrbitDataSnapshot snapshot = new OrbitDataSnapshot(size, tick);

        for (int entity = 1; entity < 1 + EntityManager.MAX_ENTITIES; entity++) {
            if (!has(entity)) continue;

            snapshot.addChange(
                entity,
                getSemiMajorAxis(entity),
                getEccentricity(entity),
                getOmega(entity),
                getT0(entity),
                getCentralBody(entity)
            );
        }
        return snapshot;
    }

    @Override
    public void applySnapshot(OrbitDataSnapshot snapshot) {
        for (int i = 0; i < snapshot.getChangedCount(); i++) {
            int entity = snapshot.entities[i];
            add(entity, snapshot.semiMajorAxis[i], snapshot.eccentricity[i], snapshot.omega[i], snapshot.t0[i], snapshot.centralBody[i]);
        }
    }

}
