package karnickeldev.solar.ecs;

import karnickeldev.solar.util.Logger;

import java.util.ArrayDeque;
import java.util.Queue;

public class EntityManager {

    // how many bits get used for generation. WARNING: ensure fits in datatype
    private static final int GENERATION_BITS = 8;
    // How many bits are used for entityId, reserve some bits for the "generation"
    private static final int ENTITY_BITS = 32 - GENERATION_BITS;

    // Max supported concurrent entities in this EntityManager, ca. 16 million
    public static final int MAX_ENTITIES = (1 << ENTITY_BITS) - 1;
    public static final int INDEX_MASK = (1 << ENTITY_BITS) - 1;
    public static final int GENERATION_MASK = ~INDEX_MASK;
    // Any Entity with this Index is invalid, used for error return values
    public static int NO_ENTITY = -1;
    private final byte[] generations = new byte[MAX_ENTITIES];
    private final Queue<Integer> freeIndices = new ArrayDeque<>();
    private int nextFree = 0;

    public static int extractIndex(int entityId) {
        return entityId & INDEX_MASK;
    }

    public static int extractGeneration(int entityId) {
        return entityId & GENERATION_MASK;
    }

    public int create() {
        int index;
        if (!freeIndices.isEmpty()) {
            index = freeIndices.poll();
        } else {
            index = nextFree++;
            if (index >= MAX_ENTITIES) throw new RuntimeException("Entity limit reached");
        }
        int gen = generations[index];

        int entityId = (gen << ENTITY_BITS) | index;
        if(entityId == NO_ENTITY) throw new RuntimeException("Tried using reserved entityId");
        return entityId;
    }

    public void registerEntity(int entityId) {
        if(entityId == NO_ENTITY) throw new RuntimeException("Tried using reserved entityId");
        int index = extractIndex(entityId);
        int generation = extractGeneration(entityId);

        if(index < nextFree && !freeIndices.contains(index)) {
            Logger.error("Double entity creation");
            return;
        }

        if (generations[index] > generation) throw new RuntimeException("Probably ECS de-sync");
        generations[index] = (byte) generation;

        if (index >= nextFree) {
            nextFree = index + 1;
        }

        // Don't add to freeIndices — it's a live entity now
    }

    public void destroy(int entityId) {
        assert entityId != NO_ENTITY;
        int index = entityId & INDEX_MASK;
        generations[index]++;
        freeIndices.offer(index);
    }

    public boolean isValid(int entityId) {
        return entityId != NO_ENTITY &&
            (generations[entityId & INDEX_MASK] == (entityId & GENERATION_MASK));
    }

    public int getActive() {
        return nextFree - freeIndices.size();
    }

    public int getAll() {
        return nextFree;
    }

}

