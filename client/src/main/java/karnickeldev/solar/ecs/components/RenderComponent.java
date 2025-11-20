package karnickeldev.solar.ecs.components;

import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.util.Logger;
import karnickeldev.solar.util.MathUtil;

import java.util.Arrays;
import java.util.BitSet;

/**
 * @author KarnickelDev
 * @since 19.09.2025
 **/
public class RenderComponent implements ComponentSnapshotProvider<AppearanceSnapshot> {

    private int CAPACITY = 64;
    private final BitSet hasComponent = new BitSet(CAPACITY);

    private short[] entityType = new short[CAPACITY];

    @Override
    public void ensureCapacity(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if (index >= CAPACITY) {
            int oldCapacity = CAPACITY;
            CAPACITY = Math.max(2 * CAPACITY, 2 << (MathUtil.ld(index) + 1));
            entityType = Arrays.copyOf(entityType, CAPACITY);
        }
    }

    public void add(int entityId, short entityType) {
        ensureCapacity(entityId);
        int index = EntityManager.extractIndex(entityId);

        this.entityType[index] = entityType;

        hasComponent.set(index);
    }

    public short getEntityType(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if (index >= CAPACITY) return 0;
        return entityType[index];
    }

    public boolean has(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= CAPACITY) return false;
        return hasComponent.get(index);
    }

    @Override
    public AppearanceSnapshot createSnapshot(long simTimeMicros) {
        throw new IllegalStateException("should never be called");
    }

    @Override
    public AppearanceSnapshot createFullSnapshot(long simTimeMicros) {
        throw new IllegalStateException("should never be called");
    }

    @Override
    public void applySnapshot(AppearanceSnapshot snapshot) {
        Logger.log("Applied AppearanceSnapshot to RenderComponent");
        for(int i = 0; i < snapshot.getChangedCount(); i++) {
            add(snapshot.entityIds[i], snapshot.entityTypes[i]);
        }
    }
}
