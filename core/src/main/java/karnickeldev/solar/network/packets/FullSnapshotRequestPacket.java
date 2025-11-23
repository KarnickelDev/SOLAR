package karnickeldev.solar.network.packets;

import io.netty.buffer.ByteBuf;

/**
 * @author KarnickelDev
 * @since 22.06.2025
 **/
public class FullSnapshotRequestPacket extends Packet {

    private int worldId;

    public FullSnapshotRequestPacket(int worldId) {
        super(PacketTypes.FULL_SNAPSHOT_REQUEST.getType(), (short)0);
        this.worldId = worldId;
    }

    public int getWorldId() {
        return worldId;
    }

    @Override
    public void writeBody(ByteBuf out) {
        out.writeInt(worldId);
    }

    @Override
    public void readBody(ByteBuf in) {
        worldId = in.readInt();
    }

    public static FullSnapshotRequestPacket create(ByteBuf in) {
        FullSnapshotRequestPacket pkt = new FullSnapshotRequestPacket(0);
        pkt.readBody(in);
        return pkt;
    }
}
