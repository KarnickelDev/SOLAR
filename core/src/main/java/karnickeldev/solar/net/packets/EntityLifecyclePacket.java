package karnickeldev.solar.net.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class EntityLifecyclePacket implements Packet {

    public final int[] createdEntityIds;
    public final int[] destroyedEntityIds;
    private final long simTimeMicros;
    private final int worldId;
    public final boolean fullSnapshot;

    protected EntityLifecyclePacket(int worldId, int[] createdEntities, int[] destroyedEntities, long simTimeMicros, boolean fullSnapshot) {
        this.worldId = worldId;
        this.simTimeMicros = simTimeMicros;
        this.createdEntityIds = new int[createdEntities.length];
        this.destroyedEntityIds = new int[destroyedEntities.length];
        this.fullSnapshot = fullSnapshot;
        System.arraycopy(destroyedEntities, 0, this.destroyedEntityIds, 0, destroyedEntities.length);
        System.arraycopy(createdEntities, 0, this.createdEntityIds, 0, createdEntities.length);
    }

    @Override
    public short getType() {
        return PacketTypes.ENTITY_LIFECYCLE.getType();
    }

    @Override
    public int getWorldId() {
        return worldId;
    }

    @Override
    public long getSimTimeMicros() {
        return simTimeMicros;
    }

    @Override
    public int getSequenceId() {
        return 0;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(worldId);
        out.writeBoolean(fullSnapshot);
        out.writeLong(simTimeMicros);

        out.writeInt(createdEntityIds.length);
        for (int createdEntityId : createdEntityIds) {
            out.writeInt(createdEntityId);
        }
        out.writeInt(destroyedEntityIds.length);
        for (int destroyedEntityId : destroyedEntityIds) {
            out.writeInt(destroyedEntityId);
        }
    }

    public static Packet deserialize(DataInputStream in) throws IOException {
        int worldId = in.readInt();
        boolean fullSnapshot = in.readBoolean();
        long simTimeMicros = in.readLong();

        int createdCount = in.readInt();
        int[] createdEntities = new int[createdCount];
        for(int i = 0; i < createdCount; i++) {
            createdEntities[i] = in.readInt();
        }

        int destroyedCount = in.readInt();
        int[] destroyedEntities = new int[destroyedCount];
        for (int i = 0; i < destroyedCount; i++) {
            destroyedEntities[i] = in.readInt();
        }

        return PacketFactory.createEntityLifecyclePacket(simTimeMicros, worldId, createdEntities, destroyedEntities, fullSnapshot);
    }
}
