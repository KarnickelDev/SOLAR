package karnickeldev.solar.net.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class EntityLifecyclePacket implements Packet {

    public final int[] createdEntityIds;
    public final int[] destroyedEntityIds;
    public final long tick;
    public final int worldId;
    public final boolean fullSnapshot;

    protected EntityLifecyclePacket(int worldId, int[] createdEntities, int[] destroyedEntities, long tick, boolean fullSnapshot) {
        this.worldId = worldId;
        this.tick = tick;
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
    public long getCreationTick() {
        return tick;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {

    }

    @Override
    public Packet deserialize(DataInputStream in) throws IOException {
        return null;
    }
}
