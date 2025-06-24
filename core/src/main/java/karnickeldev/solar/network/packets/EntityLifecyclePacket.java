package karnickeldev.solar.network.packets;

import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class EntityLifecyclePacket extends Packet {

    public int[] createdEntityIds;
    public int[] destroyedEntityIds;
    private long simTimeMicros;
    private int worldId;
    public boolean fullSnapshot;

    protected EntityLifecyclePacket(int worldId, int[] createdEntities, int[] destroyedEntities, long simTimeMicros, boolean fullSnapshot) {
        super(PacketTypes.ENTITY_LIFECYCLE.getType(), (short)0);
        this.worldId = worldId;
        this.simTimeMicros = simTimeMicros;
        this.createdEntityIds = new int[createdEntities.length];
        this.destroyedEntityIds = new int[destroyedEntities.length];
        this.fullSnapshot = fullSnapshot;
        System.arraycopy(destroyedEntities, 0, this.destroyedEntityIds, 0, destroyedEntities.length);
        System.arraycopy(createdEntities, 0, this.createdEntityIds, 0, createdEntities.length);
    }

    public int getWorldId() {
        return worldId;
    }

    public long getSimTimeMicros() {
        return simTimeMicros;
    }


    @Override
    public void writeBody(ByteBuf out) {
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

    @Override
    public void readBody(ByteBuf in) {
        worldId = in.readInt();
        fullSnapshot = in.readBoolean();
        simTimeMicros = in.readLong();

        int createdCount = in.readInt();
        createdEntityIds = new int[createdCount];
        for(int i = 0; i < createdCount; i++) {
            createdEntityIds[i] = in.readInt();
        }

        int destroyedCount = in.readInt();
        destroyedEntityIds = new int[destroyedCount];
        for (int i = 0; i < destroyedCount; i++) {
            destroyedEntityIds[i] = in.readInt();
        }
    }

    public static EntityLifecyclePacket create(ByteBuf in) {
        EntityLifecyclePacket pkt = new EntityLifecyclePacket(0,null,null,0,false);
        pkt.readBody(in);
        return pkt;
    }
}
