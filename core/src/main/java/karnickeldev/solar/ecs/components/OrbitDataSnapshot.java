package karnickeldev.solar.ecs.components;

import io.netty.buffer.ByteBuf;
import karnickeldev.solar.util.NettyUtil;

/**
 * @author KarnickelDev
 * @since 12.10.2025
 **/
public class OrbitDataSnapshot implements ComponentSnapshot {

    public final int[] entities;
    public final double[] semiMajorAxis;
    public final double[] eccentricity;
    public final double[] omega;
    public final long[] t0;
    public final int[] centralBody;


    public final long simTimeMicros;
    private final int size;
    private int count = 0;

    public OrbitDataSnapshot(int size, long simTimeMicros) {
        this.size = size;

        this.simTimeMicros = simTimeMicros;
        entities = new int[size];
        semiMajorAxis = new double[size];
        eccentricity = new double[size];
        omega = new double[size];
        t0 = new long[size];
        centralBody = new int[size];
    }

    public void addChange(int entityId, double a, double e, double o, long to, int centralBody) {
        if (count >= size)
            throw new RuntimeException("Error creating Snapshot (tried to add " + count + " entities, limit is " + size + ")");

        entities[count] = entityId;
        semiMajorAxis[count] = a;
        eccentricity[centralBody] = e;
        omega[count] = o;
        t0[count] = to;
        this.centralBody[count] = centralBody;

        count++;
    }

    @Override
    public int getChangedCount() {
        return count;
    }

    @Override
    public void serialize(ByteBuf out) {
        out.writeLong(simTimeMicros);
        out.writeInt(count);

        // use delta encoding + var-int for entityId / parentId
        int lastId = 0;
        int lastParent = 0;
        for (int i = 0; i < count; i++) {
            NettyUtil.writeVarInt(out, entities[i] - lastId);
            lastId = entities[i];

            NettyUtil.writeVarInt(out, centralBody[i] - lastParent);
            lastParent = centralBody[i];

            out.writeDouble(semiMajorAxis[i]);
            out.writeDouble(eccentricity[i]);
            out.writeDouble(omega[i]);
            out.writeLong(t0[i]);
        }
    }

    public static OrbitDataSnapshot deserialize(ByteBuf in) {
        long tick = in.readLong();
        int count = in.readInt();

        OrbitDataSnapshot snapshot = new OrbitDataSnapshot(count, tick);

        // use delta encoding + var-int for entityId / parentId
        int lastId = 0;
        int lastParent = 0;
        for (int i = 0; i < count; i++) {
            int entity = lastId + NettyUtil.readVarInt(in);
            lastId = entity;

            int parent = lastParent + NettyUtil.readVarInt(in);
            lastParent = parent;

            double a = in.readDouble();
            double e = in.readDouble();
            double o = in.readDouble();
            long t0 = in.readLong();

            snapshot.addChange(entity, a, e, o, t0, parent);
        }

        return snapshot;
    }
}
