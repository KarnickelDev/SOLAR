package karnickeldev.solar.ecs.components;

import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.ecs.Tag;
import karnickeldev.solar.util.Logger;
import karnickeldev.solar.util.MathUtil;

import java.util.Arrays;

public class TagComponent extends DirtyFlagComponent implements ComponentSnapshotProvider<TagSnapshot> {

    private int[] tagMasks = new int[EntityManager.MAX_ENTITIES];

    @Override
    public void ensureCapacity(int index) {
        // nop
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
        tagMasks[index] &= ~tag.bit;
    }

    public boolean has(int entityId, Tag tag) {
        int index = entityId & EntityManager.INDEX_MASK;
        return (tagMasks[index] & tag.bit) != 0;
    }

    public boolean matches(int entityId, Tag.Group group) {
        int index = entityId & EntityManager.INDEX_MASK;
        return group.matches(tagMasks[index]);
    }

    public int get(int entityId) {
        int index = entityId & EntityManager.INDEX_MASK;
        return tagMasks[index];
    }

    public void clearAll(int entityId) {
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

        for (int entity = dirty.nextSetBit(0); entity >= 0; entity = dirty.nextSetBit(entity+1)) {
            snap.addChange(entity, tagMasks[EntityManager.extractIndex(entity)]);
        }
        dirty.clear();

        return snap;
    }

    @Override
    public TagSnapshot createFullSnapshot(long tick) {
        int size = tagMasks.length;
        if (size < 1) return null;

        TagSnapshot snap = new TagSnapshot(size, tick);

        for (int entity = 1; entity < size; entity++) {
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
