package karnickeldev.solar.ecs.components;

import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.util.MathUtil;

import java.util.Arrays;
import java.util.BitSet;

public class NameComponent implements Component {

    private static int CAPACITY = 64;
    private static final String noname = "NO_NAME";

    private String[] names = new String[CAPACITY];
    private final BitSet hasComponent = new BitSet(CAPACITY);


    @Override
    public void ensureCapacity(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= CAPACITY) {
            CAPACITY = Math.max(2 * CAPACITY, 2 << (MathUtil.ld(index)+1));
            names = Arrays.copyOf(names, CAPACITY);
        }
    }

    public void add(int entityId, String name) {
        ensureCapacity(entityId);
        int index = EntityManager.extractIndex(entityId);
        this.names[index] = name;
        hasComponent.set(index);
    }

    public void remove(int entityId) {
        hasComponent.clear(EntityManager.extractIndex(entityId));
    }

    public boolean has(int entityId) {
        return hasComponent.get(EntityManager.extractIndex(entityId));
    }

    public String getName(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= CAPACITY) return noname;
        return names[index];
    }

}
