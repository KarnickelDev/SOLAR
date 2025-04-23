package karnickeldev.solar.ecs.components;

import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.util.MathUtil;

import java.util.Arrays;
import java.util.BitSet;

public class MassComponent implements Component {

    private static int CAPACITY = 64;

    private float[] mass = new float[CAPACITY];
    private final BitSet hasComponent = new BitSet(CAPACITY);


    @Override
    public void ensureCapacity(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= CAPACITY) {
            CAPACITY = Math.max(2 * CAPACITY, 2 << (MathUtil.ld(index)+1));
            mass = Arrays.copyOf(mass, CAPACITY);
        }
    }

    public void add(int entityId, float mass) {
        ensureCapacity(entityId);
        int index = EntityManager.extractIndex(entityId);
        this.mass[index] = mass;
        hasComponent.set(index);
    }

    public void remove(int entityId) {
        hasComponent.clear(EntityManager.extractIndex(entityId));
    }

    public boolean has(int entityId) {
        return hasComponent.get(EntityManager.extractIndex(entityId));
    }

    public float getMass(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= CAPACITY) return 0;
        return mass[index];
    }

}
