package karnickeldev.solar.ecs.components;

import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.util.Logger;
import karnickeldev.solar.util.MathUtil;

import java.util.Arrays;

public class TagComponent extends DirtyFlagComponent implements ComponentSnapshotProvider<TagSnapshot> {

    private int CAPACITY = 64;

    private int[] tagMasks = new int[CAPACITY];

    @Override
    public void ensureCapacity(int index) {
        if (index >= CAPACITY) {
            int oldCapacity = CAPACITY;
            CAPACITY = Math.max(2 * CAPACITY, 2 << (MathUtil.ld(index) + 1));
            tagMasks = Arrays.copyOf(tagMasks, CAPACITY);
            dirty.clear(oldCapacity, CAPACITY);
        }
    }

    public void add(int entityId, int tagMask) {
        int index = EntityManager.extractIndex(entityId);
        ensureCapacity(index);
        tagMasks[entityId & EntityManager.INDEX_MASK] |= tagMask;
        dirty.set(index);
    }

    public void remove(int entityId, int tagMask) {
        int index = entityId & EntityManager.INDEX_MASK;
        if (index >= CAPACITY) return;
        tagMasks[index] &= ~tagMask;
    }

    public boolean has(int entityId, int tagMask) {
        int index = entityId & EntityManager.INDEX_MASK;
        return index < CAPACITY && (tagMasks[index] & tagMask) != 0;
    }

    public int get(int entityId) {
        int index = entityId & EntityManager.INDEX_MASK;
        return tagMasks[index];
    }

    public void clearAll(int entityId) {
        int index = entityId & EntityManager.INDEX_MASK;
        if (index >= CAPACITY) return;
        tagMasks[entityId & EntityManager.INDEX_MASK] = 0;
    }

    @Override
    public TagSnapshot createSnapshot(long tick) {
        int size = dirty.cardinality();
        if (size < 1) return null;

        TagSnapshot snap = new TagSnapshot(size, tick);

        for (int entity = 0; entity < CAPACITY; entity++) {
            if (!isDirty(entity)) continue;
            snap.addChange(entity, tagMasks[EntityManager.extractIndex(entity)]);
        }
        dirty.clear();

        return snap;
    }

    @Override
    public TagSnapshot createFullSnapshot(long tick) {
        int size = CAPACITY;
        if (size < 1) return null;

        TagSnapshot snap = new TagSnapshot(size, tick);

        for (int entity = 0; entity < CAPACITY; entity++) {
            snap.addChange(entity, tagMasks[EntityManager.extractIndex(entity)]);
        }
        dirty.clear();

        return snap;
    }

    @Override
    public void applySnapshot(TagSnapshot snapshot) {
        int count = snapshot.getChangedCount();
        for (int i = 0; i < count; i++) {
            add(snapshot.entities[i], snapshot.tagMasks[i]);
        }
    }
}
