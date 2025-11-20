package karnickeldev.solar.ecs.components;

import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.util.MathUtil;

import java.util.Arrays;
import java.util.BitSet;

/**
 * @author KarnickelDev
 * @since 19.09.2025
 **/
public class AppearanceComponent extends DirtyFlagComponent implements ComponentSnapshotProvider<AppearanceSnapshot> {

    private final BitSet hasComponent = new BitSet(EntityManager.MAX_ENTITIES);

    private short[] entityType = new short[EntityManager.MAX_ENTITIES];
    private short[] seed = new short[EntityManager.MAX_ENTITIES];
    private String[] params = new String[EntityManager.MAX_ENTITIES];

    @Override
    public void ensureCapacity(int entityId) {
        // nop
    }

    public void add(int entityId, short entityType, short seed, String params) {
        ensureCapacity(entityId);
        int index = EntityManager.extractIndex(entityId);

        this.entityType[index] = entityType;
        this.seed[index] = seed;
        this.params[index] = params;

        hasComponent.set(index);
        dirty.set(index);
    }

    public void remove(int entityId) {
        hasComponent.clear(EntityManager.extractIndex(entityId));
    }

    public boolean has(int entityId) {
        return hasComponent.get(EntityManager.extractIndex(entityId));
    }

    public short getEntityType(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        return entityType[index];
    }

    public short getSeed(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        return seed[index];
    }

    public String getParams(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        String p = params[index];
        return p == null ? "" : p;
    }


    @Override
    public AppearanceSnapshot createSnapshot(long simTimeMicros) {
        int size = dirty.cardinality();
        if (size < 1) return null;

        AppearanceSnapshot snap = new AppearanceSnapshot(size, simTimeMicros);
        for (int entity = 1; entity < entityType.length; entity++) {
            if (!isDirty(entity)) continue;
            int index = EntityManager.extractIndex(entity);
            snap.addChange(entity, entityType[index], seed[index], params[index]);
        }
        dirty.clear();
        return snap;
    }

    @Override
    public AppearanceSnapshot createFullSnapshot(long simTimeMicros) {
        int size = hasComponent.cardinality();
        if (size < 1) return null;

        AppearanceSnapshot snap = new AppearanceSnapshot(size, simTimeMicros);
        for (int entity = 1; entity < entityType.length; entity++) {
            if (!has(entity)) continue;
            int index = EntityManager.extractIndex(entity);
            snap.addChange(entity, entityType[index], seed[index], params[index]);
        }

        return snap;
    }

    @Override
    public void applySnapshot(AppearanceSnapshot snapshot) {
        throw new IllegalStateException("should never be called");
    }
}
