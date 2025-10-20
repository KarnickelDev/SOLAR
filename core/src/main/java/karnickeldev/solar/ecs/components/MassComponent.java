package karnickeldev.solar.ecs.components;

import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.util.MathUtil;

import java.util.Arrays;
import java.util.BitSet;

public class MassComponent extends DirtyFlagComponent implements ComponentSnapshotProvider<MassSnapshot> {

    private final BitSet hasComponent = new BitSet(EntityManager.MAX_ENTITIES);
    private double[] mass = new double[EntityManager.MAX_ENTITIES];

    @Override
    public void ensureCapacity(int entityId) {
        // nop
    }

    public void add(int entityId, double mass) {
        ensureCapacity(entityId);
        int index = EntityManager.extractIndex(entityId);
        this.mass[index] = mass;
        hasComponent.set(index);
        dirty.set(index);
    }

    public void remove(int entityId) {
        hasComponent.clear(EntityManager.extractIndex(entityId));
    }

    public boolean has(int entityId) {
        return hasComponent.get(EntityManager.extractIndex(entityId));
    }

    public double getMass(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        return mass[index];
    }

    @Override
    public MassSnapshot createSnapshot(long tick) {
        int size = dirty.cardinality();
        if (size < 1) return null;

        MassSnapshot snap = new MassSnapshot(size, tick);

        for (int entity = 1; entity < mass.length; entity++) {
            if (!isDirty(entity)) continue;
            snap.addChange(entity, mass[EntityManager.extractIndex(entity)]);
        }
        dirty.clear();

        return snap;
    }

    @Override
    public MassSnapshot createFullSnapshot(long tick) {
        int size = hasComponent.cardinality();
        if (size < 1) return null;

        MassSnapshot snap = new MassSnapshot(size, tick);

        for (int entity = 1; entity < mass.length; entity++) {
            if (!has(entity)) continue;
            snap.addChange(entity, mass[EntityManager.extractIndex(entity)]);
        }
        dirty.clear();

        return snap;
    }

    @Override
    public void applySnapshot(MassSnapshot snapshot) {
        int count = snapshot.getChangedCount();
        for (int i = 0; i < count; i++) {
            add(snapshot.entities[i], snapshot.masses[i]);
        }
    }
}
