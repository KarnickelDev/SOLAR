package karnickeldev.solar.ecs.components;

import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.util.MathUtil;

import java.util.Arrays;
import java.util.BitSet;

public class SphereOfInfluenceComponent implements Component {

    private static int CAPACITY = 16;

    private long[] spheresOfInfluence = new long[CAPACITY];
    private final BitSet hasComponent = new BitSet(CAPACITY);


    @Override
    public void ensureCapacity(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= CAPACITY) {
            CAPACITY = Math.max(2 * CAPACITY, 2 << (MathUtil.ld(index)+1));
            spheresOfInfluence = Arrays.copyOf(spheresOfInfluence, CAPACITY);
        }
    }

    public void add(int entityId, long sphereOfInfluence) {
        ensureCapacity(entityId);
        int index = EntityManager.extractIndex(entityId);
        this.spheresOfInfluence[index] = sphereOfInfluence;
        hasComponent.set(index);
    }

    public void remove(int entityId) {
        hasComponent.clear(EntityManager.extractIndex(entityId));
    }

    public boolean has(int entityId) {
        return hasComponent.get(EntityManager.extractIndex(entityId));
    }

    public long getSphereOfInfluence(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= CAPACITY) return 0;
        return spheresOfInfluence[index];
    }

}
