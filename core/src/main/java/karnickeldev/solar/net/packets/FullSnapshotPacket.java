package karnickeldev.solar.net.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author : KarnickelDev
 * @since : 22.06.2025
 **/
public class FullSnapshotPacket implements Packet {

    private final int worldId;
    private final long simTimeMicros;

    public final Packet[] packets;

    protected FullSnapshotPacket(int worldId, long simTimeMicros, Packet... packets) {
        this.worldId = worldId;
        this.simTimeMicros = simTimeMicros;
        this.packets = packets;
    }

    @Override
    public short getType() {
        return PacketTypes.FULL_SNAPSHOT.getType();
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
        out.writeLong(simTimeMicros);
        out.writeInt(packets.length);
        for(Packet p: packets) {
            out.writeShort(p.getType());
            p.serialize(out);
        }
    }

    public static FullSnapshotPacket deserialize(DataInputStream in) throws IOException {
        int worldId = in.readInt();
        long simTimeMicros = in.readLong();
        int count = in.readInt();
        Packet[] packets = new Packet[count];
        for (int i = 0; i < count; i++) {
            short type = in.readShort();
            packets[i] = PacketRegistry.deserializePacket(type, in);
        }

        return new FullSnapshotPacket(worldId, simTimeMicros, packets);
    }
}
