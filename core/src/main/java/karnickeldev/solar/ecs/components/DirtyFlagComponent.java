package karnickeldev.solar.ecs.components;

import karnickeldev.solar.ecs.EntityManager;

import java.util.BitSet;

public abstract class DirtyFlagComponent {

    protected final BitSet dirty = new BitSet(16);

    public void markDirty(int entityId) {
        dirty.set(EntityManager.extractIndex(entityId));
    }

    public boolean isDirty(int entityId) {
        return dirty.get(EntityManager.extractIndex(entityId));
    }

    public void clearDirty() {
        dirty.clear();
    }

    public void setDirty() {
        dirty.set(0, dirty.length());
    }

    public int getDirtyAmount() {
        return dirty.cardinality();
    }

}
