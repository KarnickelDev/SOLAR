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

    private MassSnapshot(long tick, int[] entities, double[] masses) {
        this.tick = tick;
        this.size = entities.length;
        this.count = entities.length;
        this.entities = entities;
        this.masses = masses;
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
        out.writeLong(tick);
        out.writeInt(count);
        for(int i = 0; i < count; i++) {
            out.writeInt(entities[i]);
            out.writeDouble(masses[i]);
        }
    }

    public static MassSnapshot deserialize(DataInputStream in) throws IOException {
        long tick = in.readLong();
        int count = in.readInt();

        int[] entities = new int[count];
        double[] masses = new double[count];
        for(int i = 0; i < count; i++) {
            entities[i] = in.readInt();
            masses[i] = in.readDouble();
        }

        return new MassSnapshot(tick, entities, masses);
    }
}
