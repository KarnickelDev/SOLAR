package karnickeldev.solar.ecs.components;

import io.netty.buffer.ByteBuf;
import karnickeldev.solar.util.NettyUtil;

public class HCSPositionSnapshot implements ComponentSnapshot {


    public final int[] entities;
    public final short[] sectorX;
    public final short[] sectorY;
    public final double[] localX;
    public final double[] localY;
    public final int[] parent;
    public final long simTimeMicros;
    private final int size;
    private int count = 0;

    public HCSPositionSnapshot(int size, long tick) {
        this.size = size;

        this.simTimeMicros = tick;
        entities = new int[size];
        sectorX = new short[size];
        sectorY = new short[size];
        localX = new double[size];
        localY = new double[size];
        parent = new int[size];
    }

    public void addChange(int entityId, int parentId, short sectorX, double localX, short sectorY, double localY) {
        if (count >= size)
            throw new RuntimeException("Error creating Snapshot (tried to add " + count + " entities, limit is " + size + ")");

        entities[count] = entityId;
        parent[count] = parentId;

        this.sectorX[count] = sectorX;
        this.sectorY[count] = sectorY;
        this.localX[count] = localX;
        this.localY[count] = localY;

        count++;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for(int i = 0; i < count; i++) {
            builder.append(entities[i]).append(", ").append(parent[i]).append(", ")
                .append(sectorX[i]).append(':').append(localX[i]).append(", ")
                .append(sectorY[i]).append(':').append(localY[i]);
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
            c.sectorX[i] = sectorX[i];
            c.sectorY[i] = sectorY[i];
            c.localX[i] = localX[i];
            c.localY[i] = localY[i];
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

            out.writeShort(sectorX[i]);
            out.writeShort(sectorY[i]);

            out.writeDouble(localX[i]);
            out.writeDouble(localY[i]);
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

            short sectorX = in.readShort();
            short sectorY = in.readShort();

            double localX = in.readDouble();
            double localY = in.readDouble();

            snapshot.addChange(entity, parent, sectorX, localX, sectorY, localY);
        }

        return snapshot;
    }

}
