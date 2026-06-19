package karnickeldev.solar.ecs.components;

import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.util.SplitCoordMath;
import karnickeldev.solar.util.WorldPos;

import java.util.BitSet;

public class HCSPositionComponent extends DirtyFlagComponent implements ComponentSnapshotProvider<HCSPositionSnapshot> {

    public final BitSet hasComponent = new BitSet(EntityManager.MAX_ENTITIES);
    public final int[] parentIds = new int[EntityManager.MAX_ENTITIES];
    public final double[] localX = new double[EntityManager.MAX_ENTITIES];
    public final double[] localY = new double[EntityManager.MAX_ENTITIES];
    public final short[] sectorX = new short[EntityManager.MAX_ENTITIES];
    public final short[] sectorY = new short[EntityManager.MAX_ENTITIES];

    @Override
    public void ensureCapacity(int index) {
        // nop
    }

    public int getCapacity() {
        return parentIds.length;
    }

    public void add(int entityId, int parentId, short sectorX, double localX, short sectorY, double localY) {
        int index = EntityManager.extractIndex(entityId);
        ensureCapacity(index);

        parentIds[index] = parentId;

        this.sectorX[index] = sectorX;
        this.sectorY[index] = sectorY;

        this.localX[index] = localX;
        this.localY[index] = localY;

        dirty.set(index);
        hasComponent.set(index);
    }

    public void addAndSplit(int entityId, int parentId, double x, double y) {
        WorldPos c = new WorldPos();
        SplitCoordMath.split(c, x, y);
        add(entityId, parentId, c.sx, c.lx, c.sy, c.ly);
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

    public short getSectorX(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        return sectorX[index];
    }

    public short getSectorY(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        return sectorY[index];
    }

    public double getLocalX(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        return localX[index];
    }

    public double getLocalY(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        return localY[index];
    }

    public double getTotalX(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        return SplitCoordMath.combine(sectorX[index], localX[index]);
    }

    public double getTotalY(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        return SplitCoordMath.combine(sectorY[index], localY[index]);
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
                getSectorX(entity), getLocalX(entity),
                getSectorY(entity), getLocalY(entity)
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
                getSectorX(entity), getLocalX(entity),
                getSectorY(entity), getLocalY(entity)
            );
        }
        return snapshot;
    }

    @Override
    public void applySnapshot(HCSPositionSnapshot snapshot) {
        for (int i = 0; i < snapshot.getChangedCount(); i++) {
            int entity = snapshot.entities[i];
            add(entity, snapshot.parent[i],
                snapshot.sectorX[i], snapshot.localX[i],
                snapshot.sectorY[i], snapshot.localY[i]
            );
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for(int i = 0; i < hasComponent.length(); i++) {
            builder.append(i).append(": ")
                .append(sectorX[i]).append(':').append(localX[i]).append(", ")
                .append(sectorY[i]).append(':').append(localY[i]).append(", ")
                .append(parentIds[i]).append("\n");
        }

        return builder.toString();
    }
}

