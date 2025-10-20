package karnickeldev.solar.ecs;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Queue;

public class EntityManager {

    // bit masks for entity ID. most significant bit still unused
    private static final int GENERATION_BITS = 14;  // bits for generation
    private static final int ENTITY_BITS = 17;      // bits for entityId

    // Max supported concurrent entities in this EntityManager, 131_072
    public static final int MAX_ENTITIES = (1 << ENTITY_BITS);
    public static final int INDEX_MASK = (1 << ENTITY_BITS) - 1;
    public static final int GENERATION_MASK = ((1 << GENERATION_BITS) - 1) << ENTITY_BITS;

    public static final int NO_ENTITY = 0;

    // stores the correct generation for entities, used to verify if entity ID still valid (or stale)
    private final byte[] generations = new byte[MAX_ENTITIES]; // DON'T USE MAX_ENTITIES (causes off-by-one)

    // bitset for alive entityIds for faster iteration etc.
    private final BitSet used = new BitSet(MAX_ENTITIES);

    // list of free entity ID's, prefer over new IDs to reduce segmentation
    private final Queue<Integer> freeIndices = new ArrayDeque<>();

    // incrementing counter for entity id's
    // ALWAYS START AT 1 (0 reserved for NO_ENTITY)
    private int nextFree = 1;

    public static int extractIndex(int entityId) {
        return entityId & INDEX_MASK;
    }

    public static int extractGeneration(int entityId) {
        return (entityId & GENERATION_MASK) >>> ENTITY_BITS;
    }

    public int create() {
        int index;
        if (!freeIndices.isEmpty()) {
            index = freeIndices.poll();
        } else {
            index = nextFree++;
        }
        if (index >= MAX_ENTITIES) throw new RuntimeException("Entity limit reached");

        int gen = generations[index];
        used.set(index);
        int entityId = (gen << ENTITY_BITS) | index;
        if(entityId == NO_ENTITY) throw new RuntimeException("Tried using NO_ENTITY entityId");
        return entityId;
    }

    public void importEntity(int entityId) {
        if(entityId == NO_ENTITY) throw new RuntimeException("Cannot import NO_ENTITY");

        int index = entityId & INDEX_MASK;
        int generation = (entityId & GENERATION_MASK) >>> ENTITY_BITS;

        if (generations[index] > generation) throw new RuntimeException("Probably ECS de-sync");

        // ensure generation table matches
        byte currentGen = generations[index];
        if(currentGen != (byte) generation) {
            generations[index] = (byte) generation;
        }

        // mask as alive
        used.set(index);

        // remove from free list
        freeIndices.remove(index);

        // expand nextFree pointer
        if (index >= nextFree) {
            nextFree = index + 1;
        }
    }

    public void importEntities(int[] entityIds, int count) {
        // track the highest index seen so we can bump nextFree once.
        int maxIndex = nextFree;

        for (int i = 0; i < count; i++) {
            int entityId = entityIds[i];
            if (entityId == NO_ENTITY) continue;

            int index = entityId & INDEX_MASK;
            int generation = (entityId & GENERATION_MASK) >>> ENTITY_BITS;

            // Set generation directly
            generations[index] = (byte) generation;

            // no need to search/remove in freeIndices
            // instead, we rebuild freeIndices later
            used.set(index);

            // track the highest index to adjust nextFree later
            if (index >= maxIndex) maxIndex = index + 1;
        }

        // Bump nextFree only once
        if (maxIndex > nextFree) nextFree = maxIndex;

        rebuildFreeList();
    }

    public void rebuildFreeList() {
        freeIndices.clear();
        for (int i = used.nextClearBit(0); i >= 0 && i < nextFree; i = used.nextClearBit(i+1)) {
            freeIndices.offer(i);
        }
    }

    public void destroy(int entityId) {
        int index = entityId & INDEX_MASK;
        if(index == NO_ENTITY) return;
        generations[index]++;
        used.clear(index);
        freeIndices.offer(index);
    }

    public boolean isValid(int entityId) {
        int index = entityId & INDEX_MASK;
        return index != NO_ENTITY && (generations[entityId & INDEX_MASK] == ((entityId & GENERATION_MASK) >>> ENTITY_BITS));
    }

    public int getActive() {
        return used.cardinality();
    }

    public int getCapacityUsed() {
        return nextFree;
    }

}

