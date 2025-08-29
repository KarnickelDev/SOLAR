package karnickeldev.solar.ecs.components;

import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.util.MathUtil;

import java.util.Arrays;
import java.util.BitSet;

public class MassComponent extends DirtyFlagComponent implements ComponentSnapshotProvider<MassSnapshot> {

    private int CAPACITY = 64;
    private final BitSet hasComponent = new BitSet(CAPACITY);
    private double[] mass = new double[CAPACITY];

    @Override
    public void ensureCapacity(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if (index >= CAPACITY) {
            int oldCapacity = CAPACITY;
            CAPACITY = Math.max(2 * CAPACITY, 2 << (MathUtil.ld(index) + 1));
            mass = Arrays.copyOf(mass, CAPACITY);
            dirty.clear(oldCapacity, CAPACITY);
        }
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
        if (index >= CAPACITY) return 0;
        return mass[index];
    }

    @Override
    public MassSnapshot createSnapshot(long tick) {
        int size = dirty.cardinality();
        if (size < 1) return null;

        MassSnapshot snap = new MassSnapshot(size, tick);

        for (int entity = 0; entity < CAPACITY; entity++) {
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

        for (int entity = 0; entity < CAPACITY; entity++) {
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
