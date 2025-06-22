package karnickeldev.solar.net.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author : KarnickelDev
 * @since : 22.06.2025
 **/
public class FullSnapshotRequestPacket implements Packet {

    private final int worldId;

    public FullSnapshotRequestPacket(int worldId) {
        this.worldId = worldId;
    }

    @Override
    public short getType() {
        return PacketTypes.FULL_SNAPSHOT_REQUEST.getType();
    }

    @Override
    public int getWorldId() {
        return worldId;
    }

    @Override
    public long getSimTimeMicros() {
        return 0;
    }

    @Override
    public int getSequenceId() {
        return 0;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(worldId);
    }

    public static FullSnapshotRequestPacket deserialize(DataInputStream in) throws IOException {
        return new FullSnapshotRequestPacket(in.readInt());
    }
}
