package karnickeldev.solar.ecs.components;

import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.util.MathUtil;

import java.util.Arrays;
import java.util.BitSet;

public class HCSPositionComponent extends DirtyFlagComponent implements ComponentSnapshotProvider<HCSPositionSnapshot> {

    private int CAPACITY = 64;
    private final BitSet hasComponent = new BitSet(CAPACITY);
    private int[] parentIds = new int[CAPACITY];
    private double[] localPos = new double[2 * CAPACITY];

    @Override
    public void ensureCapacity(int index) {
        if (index >= CAPACITY) {
            int oldCapacity = CAPACITY;
            CAPACITY = Math.max(2 * CAPACITY, 2 << (MathUtil.ld(index) + 1));

            parentIds = Arrays.copyOf(parentIds, CAPACITY);
            localPos = Arrays.copyOf(localPos, 2 * CAPACITY);

            dirty.clear(oldCapacity, CAPACITY);
            hasComponent.clear(oldCapacity, CAPACITY);
        }
    }

    public int getCapacity() {
        return CAPACITY;
    }

    public void add(int entityId, int parentId, double localX, double localY) {
        int index = EntityManager.extractIndex(entityId);
        ensureCapacity(index);

        parentIds[index] = parentId;

        this.localPos[2 * index] = localX;
        this.localPos[(2 * index) + 1] = localY;

        dirty.set(index);
        hasComponent.set(index);
    }

    public void remove(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        hasComponent.clear(index);
    }

    public boolean has(int entityId) {
        return hasComponent.get(EntityManager.extractIndex(entityId));
    }

    public int getParent(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= CAPACITY) return EntityManager.NO_ENTITY;
        return parentIds[index];
    }

    public double getLocalX(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if (index >= CAPACITY) return 0;
        return localPos[2 * index];
    }

    public double getLocalY(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if (index >= CAPACITY) return 0;
        return localPos[(2 * index) + 1];
    }

    @Override
    public HCSPositionSnapshot createSnapshot(long tick) {
        int size = getDirtyAmount();
        if (size < 1) return null;

        HCSPositionSnapshot snapshot = new HCSPositionSnapshot(size, tick);

        for (int entity = 0; entity < getCapacity(); entity++) {
            if (!isDirty(entity)) continue;

            snapshot.addChange(
                entity,
                getParent(entity),
                getLocalX(entity), getLocalY(entity)
            );
        }
        return snapshot;
    }

    @Override
    public HCSPositionSnapshot createFullSnapshot(long tick) {
        int size = CAPACITY;
        if (size < 1) return null;

        HCSPositionSnapshot snapshot = new HCSPositionSnapshot(size, tick);

        for (int entity = 0; entity < getCapacity(); entity++) {
            if (!has(entity)) continue;

            snapshot.addChange(
                entity,
                getParent(entity),
                getLocalX(entity), getLocalY(entity)
            );
        }
        return snapshot;
    }

    @Override
    public void applySnapshot(HCSPositionSnapshot snapshot) {
        for (int i = 0; i < snapshot.getChangedCount(); i++) {
            int entity = snapshot.entities[i];
            add(entity, snapshot.parent[i], snapshot.position[2 * i], snapshot.position[2 * i + 1]);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for(int i = 0; i < hasComponent.length(); i++) {
            builder.append(i).append(": ").append(localPos[2 * i]).append(", ").append(localPos[2 * i + 1]).append(", ").append(parentIds[i]).append("\n");
        }

        return builder.toString();
    }
}

