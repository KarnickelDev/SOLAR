package karnickeldev.solar.ecs.components;

import io.netty.buffer.ByteBuf;
import karnickeldev.solar.util.NettyUtil;

public class HCSPositionSnapshot implements ComponentSnapshot {


    public final int[] entities;
    public final double[] position;
    public final int[] parent;
    public final long simTimeMicros;
    private final int size;
    private int count = 0;

    public HCSPositionSnapshot(int size, long tick) {
        this.size = size;

        this.simTimeMicros = tick;
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
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for(int i = 0; i < count; i++) {
            builder.append(entities[i]).append(", ").append(parent[i]).append(", ").append(position[2 * i]).append(", ").append(position[2 * i + 1]);
            if(i != count-1) builder.append(", ");
        }
        builder.append(']');
        return builder.toString();
    }

    @Override
    public int getChangedCount() {
        return count;
    }

    public HCSPositionSnapshot copy() {
        HCSPositionSnapshot c = new HCSPositionSnapshot(size, simTimeMicros);
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
    public void serialize(ByteBuf out) {
        out.writeLong(simTimeMicros);
        out.writeInt(count);

        int lastId = 0;
        int lastParent = 0;
        for (int i = 0; i < count; i++) {
            NettyUtil.writeVarInt(out, entities[i] - lastId);
            lastId = entities[i];

            //out.writeInt(parent[i]);
            NettyUtil.writeVarInt(out, parent[i] - lastParent);
            lastParent = parent[i];

            out.writeDouble(position[2 * i]);
            out.writeDouble(position[2 * i + 1]);
        }
    }

    public static HCSPositionSnapshot deserialize(ByteBuf in) {
        long tick = in.readLong();
        int count = in.readInt();

        HCSPositionSnapshot snapshot = new HCSPositionSnapshot(count, tick);

        int lastId = 0;
        int lastParent = 0;
        for (int i = 0; i < count; i++) {
            int entity = lastId + NettyUtil.readVarInt(in);
            lastId = entity;

            int parent = lastParent + NettyUtil.readVarInt(in);
            lastParent = parent;

            double x = in.readDouble();
            double y = in.readDouble();

            snapshot.addChange(entity, parent, x, y);
        }

        return snapshot;
    }

}
