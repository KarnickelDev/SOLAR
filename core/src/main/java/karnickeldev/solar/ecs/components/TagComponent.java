package karnickeldev.solar.ecs.components;

import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.util.MathUtil;

import java.util.Arrays;

public class TagComponent implements Component {

    private static int CAPACITY = 64;

    private int[] tagMasks = new int[CAPACITY];

    @Override
    public void ensureCapacity(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= CAPACITY) {
            CAPACITY = Math.max(2 * CAPACITY, 2 << (MathUtil.ld(index)+1));
            tagMasks = Arrays.copyOf(tagMasks, CAPACITY);
        }
    }

    public void add(int entityId, int tagMask) {
        ensureCapacity(entityId);
        tagMasks[entityId & EntityManager.INDEX_MASK] |= tagMask;
    }

    public void remove(int entityId, int tagMask) {
        int index = entityId & EntityManager.INDEX_MASK;
        if(index >= CAPACITY) return;
        tagMasks[index] &= ~tagMask;
    }

    public boolean has(int entityId, int tagMask) {
        int index = entityId & EntityManager.INDEX_MASK;
        return index < CAPACITY && (tagMasks[index] & tagMask) != 0;
    }

    public void clearAll(int entityId) {
        int index = entityId & EntityManager.INDEX_MASK;
        if(index >= CAPACITY) return;
        tagMasks[entityId & EntityManager.INDEX_MASK] = 0;
    }

}
