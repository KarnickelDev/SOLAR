package karnickeldev.solar.ecs.components;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * @author KarnickelDev
 * @since 19.09.2025
 **/
public class AppearanceSnapshot implements ComponentSnapshot {

    public final int[] entityIds;
    public final short[] entityTypes;
    public final short[] seeds;
    public final String[] params;
    public final long simTimeMicros;

    private final int size;
    private int count = 0;

    protected AppearanceSnapshot(int size, long simTimeMicros) {
        this.size = size;
        this.simTimeMicros = simTimeMicros;

        this.entityIds = new int[size];
        this.entityTypes = new short[size];
        this.seeds = new short[size];
        this.params = new String[size];
    }

    public void addChange(int entityId, short entityType, short seed, String params) {
        if (count >= size) throw new RuntimeException("AppearanceSnapshot too large");

        entityIds[count] = entityId;
        entityTypes[count] = entityType;
        seeds[count] = seed;
        this.params[count] = params;

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
        for (int i = 0; i < count; i++) {
            out.writeInt(entityIds[i]);
            out.writeShort(entityTypes[i]);
            out.writeShort(seeds[i]);
            out.writeInt(params[i].length());
            out.writeCharSequence(params[i], StandardCharsets.UTF_8);
        }
    }

    public static AppearanceSnapshot deserialize(ByteBuf in) {
        long simTimeMicros = in.readLong();
        int count = in.readInt();

        AppearanceSnapshot snap = new AppearanceSnapshot(count, simTimeMicros);

        for (int i = 0; i < count; i++) {
            int entityId = in.readInt();
            short type = in.readShort();
            short seed = in.readShort();
            int paramLength = in.readInt();
            String param = in.readCharSequence(paramLength, StandardCharsets.UTF_8).toString();
            snap.addChange(entityId, type, seed, param);
        }
        return snap;
    }
}
