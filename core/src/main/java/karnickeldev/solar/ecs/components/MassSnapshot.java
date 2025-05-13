package karnickeldev.solar.ecs.components;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MassSnapshot implements ComponentSnapshot {

    public final long tick;
    public final int[] entities;
    public final double[] masses;
    private final int size;
    private int count = 0;

    public MassSnapshot(int size, long tick) {
        this.tick = tick;
        this.size = size;

        entities = new int[size];
        masses = new double[size];
    }


    public void addChange(int entityId, double mass) {
        if (count >= size) throw new RuntimeException("Snapshot too small");
        entities[count] = entityId;
        masses[count] = mass;

        count++;
    }

    @Override
    public int getChangedCount() {
        return count;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {

    }

    @Override
    public ComponentSnapshot deserialize(DataInputStream in) throws IOException {
        return null;
    }
}
