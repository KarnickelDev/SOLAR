package karnickeldev.solar.net.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class WorldUpdatePacket implements Packet {

    public final int worldId;
    private final long tick;

    protected WorldUpdatePacket(int worldId, long tick) {
        this.worldId = worldId;
        this.tick = tick;
    }

    @Override
    public short getType() {
        return PacketTypes.WORLD_UPDATE.getType();
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
