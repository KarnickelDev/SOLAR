package karnickeldev.solar.ecs.components;

import karnickeldev.solar.ecs.EntityManager;

import java.util.BitSet;

public class HCSPositionComponent extends DirtyFlagComponent implements ComponentSnapshotProvider<HCSPositionSnapshot> {

    public final BitSet hasComponent = new BitSet(EntityManager.MAX_ENTITIES);
    public final int[] parentIds = new int[EntityManager.MAX_ENTITIES];
    public final double[] localX = new double[EntityManager.MAX_ENTITIES];
    public final double[] localY = new double[EntityManager.MAX_ENTITIES];

    @Override
    public void ensureCapacity(int index) {
        // nop
    }

    public int getCapacity() {
        return parentIds.length;
    }

    public void add(int entityId, int parentId, double localX, double localY) {
        int index = EntityManager.extractIndex(entityId);
        ensureCapacity(index);

        parentIds[index] = parentId;

        this.localX[index] = localX;
        this.localY[index] = localY;

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
        return parentIds[index];
    }

    public double getLocalX(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        return localX[index];
    }

    public double getLocalY(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        return localY[index];
    }

    @Override
    public HCSPositionSnapshot createSnapshot(long tick) {
        int size = getDirtyAmount();
        if (size < 1) return null;

        HCSPositionSnapshot snapshot = new HCSPositionSnapshot(size, tick);

        for (int entity = 1; entity < getCapacity(); entity++) {
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
        int size = getCapacity();
        if (size < 1) return null;

        HCSPositionSnapshot snapshot = new HCSPositionSnapshot(size, tick);

        for (int entity = 1; entity < getCapacity(); entity++) {
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
            builder.append(i).append(": ").append(localX[i]).append(", ").append(localY[i]).append(", ").append(parentIds[i]).append("\n");
        }

        return builder.toString();
    }
}

