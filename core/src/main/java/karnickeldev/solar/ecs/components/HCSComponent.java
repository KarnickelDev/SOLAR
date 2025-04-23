package karnickeldev.solar.ecs.components;

import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.physics.Units;
import karnickeldev.solar.util.MathUtil;

import java.util.Arrays;
import java.util.BitSet;

public class HCSComponent implements Component {

    private static int CAPACITY = 64;

    private int[] parentIds = new int[CAPACITY];

    private double[] localX = new double[CAPACITY];
    private double[] localY = new double[CAPACITY];
    private double[] oldX = new double[CAPACITY];
    private double[] oldY = new double[CAPACITY];

    private double[] worldX = new double[CAPACITY];
    private double[] worldY = new double[CAPACITY];

    private boolean[] dirtyFlags = new boolean[CAPACITY];
    private final BitSet hasComponent = new BitSet(CAPACITY);

    @Override
    public void ensureCapacity(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if (index >= CAPACITY) {
            CAPACITY = Math.max(2 * CAPACITY, 2 << (MathUtil.ld(index) + 1));
            parentIds = Arrays.copyOf(parentIds, CAPACITY);
            dirtyFlags = Arrays.copyOf(dirtyFlags, CAPACITY);

            localX = Arrays.copyOf(localX, CAPACITY);
            localY = Arrays.copyOf(localY, CAPACITY);
            oldX = Arrays.copyOf(oldX, CAPACITY);
            oldY = Arrays.copyOf(oldY, CAPACITY);

            worldX = Arrays.copyOf(worldX, CAPACITY);
            worldY = Arrays.copyOf(worldY, CAPACITY);
        }
    }

    public void add(int entityId, int parentId, double localX, double localY, double worldX, double worldY) {
        ensureCapacity(entityId);
        int index = EntityManager.extractIndex(entityId);

        parentIds[index] = parentId;

        oldX[index] = this.localX[index];
        oldY[index] = this.localY[index];

        this.localX[index] = localX;
        this.localY[index] = localY;

        this.worldX[index] = localX * Units.Length.AU.getBaseFactor();
        this.worldY[index] = localY * Units.Length.AU.getBaseFactor();

        dirtyFlags[index] = true;
        hasComponent.set(index);
    }

    public void remove(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        hasComponent.clear(index);
    }

    public boolean has(int entityId) {
        return hasComponent.get(EntityManager.extractIndex(entityId));
    }

    public void markDirty(int entityId) {
        dirtyFlags[EntityManager.extractIndex(entityId)] = true;
    }

    public boolean isDirty(int entityId) {
        return dirtyFlags[EntityManager.extractIndex(entityId)];
    }

    public int getParent(int entityId) {
        return parentIds[EntityManager.extractIndex(entityId)];
    }

    public double getLocalX(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= CAPACITY) return 0;
        return localX[index];
    }

    public double getLocalY(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= CAPACITY) return 0;
        return localY[index];
    }

    public double getOldX(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= CAPACITY) return 0;
        return oldX[index];
    }

    public double getOldY(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= CAPACITY) return 0;
        return oldY[index];
    }

    public double getWorldX(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= CAPACITY) return 0;
        return worldX[index];
    }

    public double getWorldY(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= CAPACITY) return 0;
        return worldY[index];
    }

}

