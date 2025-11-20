package karnickeldev.solar.network.packets;

import io.netty.buffer.ByteBuf;

/**
 * @author KarnickelDev
 * @since 28.06.2025
 **/
public class HandshakeResponsePacket extends Packet {

    public static final byte SUCCESS = 0;
    public static final byte FAILURE = 16;

    private byte result;

    public HandshakeResponsePacket(byte result) {
        super(PacketTypes.HANDSHAKE_RESPONSE.getType(), (short)0);
        this.result = result;
    }

    public boolean isSuccess() {
        return result == SUCCESS;
    }

    @Override
    public boolean isFastHandled() {
        return true;
    }

    @Override
    protected void writeBody(ByteBuf out) {
        out.writeByte(result);
    }

    @Override
    protected void readBody(ByteBuf in) {
        result = in.readByte();
    }

    public static HandshakeResponsePacket create(ByteBuf in) {
        HandshakeResponsePacket pkt = new HandshakeResponsePacket(FAILURE);
        pkt.readBody(in);
        return pkt;
    }
}
