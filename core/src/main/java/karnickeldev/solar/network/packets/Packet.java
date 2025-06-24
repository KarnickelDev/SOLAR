package karnickeldev.solar.network.packets;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;

public abstract class Packet {

    // shared fields
    protected final short type;
    protected final short sequenceId;

    protected Packet(short type, short sequenceId) {
        this.type = type;
        this.sequenceId = sequenceId;
    }

    public short getType() {
        return type;
    }

    public short getSequenceId() {
        return sequenceId;
    }

    /**
     * If true, this packet should be processes by the NetworkThread
     */
    public boolean isFastHandled() {
        return false;
    }

    public final void write(ByteBuf out) {
        out.writeShort(type);
        out.writeShort(sequenceId);

        writeBody(out);

        if(shouldUseChecksum()) {
            out.writeInt(computeChecksum(out));
        }
    }

    public static Packet read(ByteBuf in) {
        short type = in.readShort();
        short sequenceId = in.readShort();

        Packet packet = PacketRegistry.create(type, in);
        if(packet.shouldUseChecksum()) {
            int checksum = in.readInt();
            int expected = computeChecksum(in);
            if(checksum != expected) throw new CorruptedFrameException("Checksum mismatch for packet type " + type);
        }
        return packet;
    }

    protected abstract void writeBody(ByteBuf out);

    protected abstract void readBody(ByteBuf in);

    protected boolean shouldUseChecksum() {
        return false;
    }

    protected static int computeChecksum(ByteBuf buf) {
        // Cheap, fast hash TODO: improve
        int hash = 17;
        for (int i = 0; i < buf.readableBytes(); i++) {
            hash = 31 * hash + buf.getByte(i);
        }
        return hash;
    }

}
