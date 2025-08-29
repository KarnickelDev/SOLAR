package karnickeldev.solar.network.packets;

import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author : KarnickelDev
 * @since : 22.06.2025
 **/
public class FullSnapshotPacket extends GameStatePacket {

    private int worldId;
    private long simTimeMicros;

    public Packet[] packets;

    protected FullSnapshotPacket(int worldId, long simTimeMicros, Packet... packets) {
        super(PacketTypes.FULL_SNAPSHOT.getType(), (short)0);
        this.worldId = worldId;
        this.simTimeMicros = simTimeMicros;
        this.packets = packets;
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
        out.writeLong(simTimeMicros);
        out.writeInt(packets.length);
        for(Packet p: packets) {
            out.writeShort(p.getType());
            p.writeBody(out);
        }
    }

    public void readBody(ByteBuf in) {
        worldId = in.readInt();
        simTimeMicros = in.readLong();
        int count = in.readInt();
        packets = new Packet[count];
        for (int i = 0; i < count; i++) {
            short type = in.readShort();
            packets[i] = PacketRegistry.create(type, in);
        }
    }

    public static FullSnapshotPacket create(ByteBuf in) {
        FullSnapshotPacket pkt = new FullSnapshotPacket(0,0);
        pkt.readBody(in);
        return pkt;
    }
}
