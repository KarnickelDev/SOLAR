package karnickeldev.solar.ecs.components.server;

import karnickeldev.solar.ecs.components.ComponentSnapshot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class HCSPositionSnapshot implements ComponentSnapshot {


    public final int[] entities;
    public final double[] position;
    public final int[] parent;
    public final long tick;
    private final int size;
    private int count = 0;

    public HCSPositionSnapshot(int size, long tick) {
        this.size = size;

        this.tick = tick;
        entities = new int[size];
        position = new double[2 * size];
        parent = new int[size];
    }

    public void addChange(int entityId, int parentId, double newX, double newY) {
        if (count >= size)
            throw new RuntimeException("Error creating Snapshot (tried to add " + count + " entities, limit is " + size + ")");

        entities[count] = entityId;
        parent[count] = parentId;
        position[2 * count] = newX;
        position[2 * count + 1] = newY;

        count++;
    }

    @Override
    public int getChangedCount() {
        return count;
    }

    public HCSPositionSnapshot copy() {
        HCSPositionSnapshot c = new HCSPositionSnapshot(size, tick);
        c.count = count;
        for (int i = 0; i < c.size; i++) {
            c.entities[i] = entities[i];
            c.parent[i] = parent[i];
            c.position[2 * i] = position[2 * i];
            c.position[2 * i + 1] = position[2 * i + 1];
        }

        return c;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeLong(tick);
        out.writeInt(count);
        for (int i = 0; i < count; i++) {
            out.writeInt(entities[i]);
            out.writeInt(parent[i]);
            out.writeDouble(position[2 * i]);
            out.writeDouble(position[2 * i + 1]);
        }
    }

    @Override
    public HCSPositionSnapshot deserialize(DataInputStream in) throws IOException {
        long tick = in.readLong();
        int count = in.readInt();

        HCSPositionSnapshot snapshot = new HCSPositionSnapshot(count, tick);

        for (int i = 0; i < count; i++) {
            int entity = in.readInt();
            int parent = in.readInt();
            double x = in.readDouble();
            double y = in.readDouble();

            snapshot.addChange(entity, parent, x, y);
        }

        return snapshot;
    }

}
