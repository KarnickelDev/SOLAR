package karnickeldev.solar.ecs.components;

import io.netty.buffer.ByteBuf;

/**
 * @author : KarnickelDev
 * @since : 19.09.2025
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

    }
}
