package karnickeldev.solar.ecs.components;

import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.util.MathUtil;

import java.util.Arrays;
import java.util.BitSet;

public class PositionComponent implements Component {

    private int CAPACITY = 64;
    private final BitSet hasComponent = new BitSet(CAPACITY);
    private double[] x = new double[CAPACITY];
    private double[] y = new double[CAPACITY];

    @Override
    public void ensureCapacity(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if (index >= CAPACITY) {
            CAPACITY = Math.max(2 * CAPACITY, 2 << (MathUtil.ld(index) + 1));
            x = Arrays.copyOf(x, CAPACITY);
            y = Arrays.copyOf(y, CAPACITY);
        }
    }

    public void add(int entityId, double x, double y) {
        ensureCapacity(entityId);
        int index = EntityManager.extractIndex(entityId);
        this.x[index] = x;
        this.y[index] = y;
        hasComponent.set(index);
    }

    public void remove(int entityId) {
        hasComponent.clear(EntityManager.extractIndex(entityId));
    }

    public boolean has(int entityId) {
        return hasComponent.get(EntityManager.extractIndex(entityId));
    }

    public double getX(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if (index >= CAPACITY) return 0;
        return x[index];
    }

    public double getY(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if (index >= CAPACITY) return 0;
        return y[index];
    }
}
