package karnickeldev.solar.ecs.components;

import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.ecs.Tag;
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

    public void add(int entityId, Tag tag) {
        int index = EntityManager.extractIndex(entityId);
        ensureCapacity(index);

        boolean change = true;
        if(tag.exclusive) {
            for(Tag t: Tag.values()) {
                if(has(entityId, t) && t.category.equals(tag.category)) {
                    change =  false;
                    Logger.error(Logger.ENTITY, "Tried to overwrite exclusive Tag");
                    break;
                }
            }
        }
        if(change) tagMasks[index] |= tag.impliedMask();

        dirty.set(index);
    }

    private void add(int entityId, int tagMask) {
        int index = EntityManager.extractIndex(entityId);
        ensureCapacity(index);

        tagMasks[index] |= tagMask;
        dirty.set(index);
    }

    public void remove(int entityId, Tag tag) {
        int index = entityId & EntityManager.INDEX_MASK;
        if (index >= CAPACITY) return;
        tagMasks[index] &= ~tag.bit;
    }

    public boolean has(int entityId, Tag tag) {
        int index = entityId & EntityManager.INDEX_MASK;
        return index < CAPACITY && (tagMasks[index] & tag.bit) != 0;
    }

    public boolean matches(int entityId, Tag.Group group) {
        int index = entityId & EntityManager.INDEX_MASK;
        return index < CAPACITY && group.matches(tagMasks[index]);
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

    public String toString(int entityId) {
        StringBuilder builder = new StringBuilder(EntityManager.extractIndex(entityId));

        for(Tag t : Tag.values()) {
            if(has(entityId, t)) builder.append(t.name());
        }

        return builder.toString();
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
