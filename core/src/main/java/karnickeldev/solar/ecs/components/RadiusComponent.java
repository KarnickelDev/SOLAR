package karnickeldev.solar.ecs.components;

import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.util.MathUtil;

import java.util.Arrays;
import java.util.BitSet;

public class RadiusComponent extends DirtyFlagComponent implements ComponentSnapshotProvider<RadiusSnapshot> {

    private int CAPACITY = 64;
    private final BitSet hasComponent = new BitSet(CAPACITY);
    private float[] radius = new float[CAPACITY];

    @Override
    public void ensureCapacity(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if (index >= CAPACITY) {
            int oldCapacity = CAPACITY;
            CAPACITY = Math.max(2 * CAPACITY, 2 << (MathUtil.ld(index) + 1));
            radius = Arrays.copyOf(radius, CAPACITY);
            dirty.clear(oldCapacity, CAPACITY);
        }
    }

    public void add(int entityId, float radius) {
        ensureCapacity(entityId);
        int index = EntityManager.extractIndex(entityId);
        this.radius[index] = radius;
        hasComponent.set(index);
        dirty.set(index);
    }

    public void remove(int entityId) {
        hasComponent.clear(EntityManager.extractIndex(entityId));
    }

    public boolean has(int entityId) {
        return hasComponent.get(EntityManager.extractIndex(entityId));
    }

    public float getRadius(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if (index >= CAPACITY) return 0;
        return radius[index];
    }

    @Override
    public RadiusSnapshot createSnapshot(long simTimeMicros) {
        int size = dirty.cardinality();
        if (size < 1) return null;

        RadiusSnapshot snap = new RadiusSnapshot(size, simTimeMicros);
        for (int entity = 0; entity < CAPACITY; entity++) {
            if (!isDirty(entity)) continue;
            snap.addChange(entity, radius[EntityManager.extractIndex(entity)]);
        }
        dirty.clear();
        return snap;
    }

    @Override
    public RadiusSnapshot createFullSnapshot(long simTimeMicros) {
        int size = hasComponent.cardinality();
        if (size < 1) return null;

        RadiusSnapshot snap = new RadiusSnapshot(size, simTimeMicros);
        for (int entity = 0; entity < CAPACITY; entity++) {
            if (!has(entity)) continue;
            snap.addChange(entity, radius[EntityManager.extractIndex(entity)]);
        }
        dirty.clear();
        return snap;
    }

    @Override
    public void applySnapshot(RadiusSnapshot snapshot) {
        int count = snapshot.getChangedCount();
        for (int i = 0; i < count; i++) {
            add(snapshot.entities[i], snapshot.radius[i]);
        }
    }
}
