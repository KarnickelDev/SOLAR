package karnickeldev.solar.network.packets;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * @author KarnickelDev
 * @since 27.06.2025
 **/
public class DisconnectPacket extends Packet{

    private String reason;

    public DisconnectPacket(String reason) {
        super(PacketTypes.DISCONNECT.getType(), (short)0);
        this.reason = reason;
    }

    @Override
    protected void writeBody(ByteBuf out) {
        out.writeInt(reason.length());
        out.writeCharSequence(reason, StandardCharsets.US_ASCII);
    }

    @Override
    protected void readBody(ByteBuf in) {
        int length = in.readInt();
        reason = in.readString(length, StandardCharsets.US_ASCII);
    }

    public static DisconnectPacket create(ByteBuf in) {
        DisconnectPacket pkt = new DisconnectPacket("");
        pkt.readBody(in);
        return pkt;
    }
}
