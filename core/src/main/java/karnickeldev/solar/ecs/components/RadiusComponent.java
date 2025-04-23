package karnickeldev.solar.ecs.components;

import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.util.MathUtil;

import java.util.Arrays;
import java.util.BitSet;

public class RadiusComponent implements Component {

    private static int CAPACITY = 64;

    private int[] radius = new int[CAPACITY];
    private final BitSet hasComponent = new BitSet(CAPACITY);


    @Override
    public void ensureCapacity(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= CAPACITY) {
            CAPACITY = Math.max(2 * CAPACITY, 2 << (MathUtil.ld(index)+1));
            radius = Arrays.copyOf(radius, CAPACITY);
        }
    }

    public void add(int entityId, int radius) {
        ensureCapacity(entityId);
        int index = EntityManager.extractIndex(entityId);
        this.radius[index] = radius;
        hasComponent.set(index);
    }

    public void remove(int entityId) {
        hasComponent.clear(EntityManager.extractIndex(entityId));
    }

    public boolean has(int entityId) {
        return hasComponent.get(EntityManager.extractIndex(entityId));
    }

    public int getRadius(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= CAPACITY) return 0;
        return radius[index];
    }

}
