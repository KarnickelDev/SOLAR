package karnickeldev.solar.network.packets;

import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author : KarnickelDev
 * @since : 06.06.2025
 **/
public class PingPongPacket extends Packet {

    private long clientSendTime;

    protected PingPongPacket(long clientSendTime) {
        super(PacketTypes.PONG.getType(), (short)0);
        this.clientSendTime = clientSendTime;
    }

    public long getClientSendTime() {
        return clientSendTime;
    }

    @Override
    public boolean isFastHandled() {
        return true;
    }

    @Override
    public boolean shouldUseChecksum() {
        return false;
    }

    @Override
    protected void writeBody(ByteBuf out) {
        out.writeLong(clientSendTime);
    }

    @Override
    protected void readBody(ByteBuf in) {
        clientSendTime = in.readLong();
    }

    public static PingPongPacket create(ByteBuf in) {
        PingPongPacket pkt = new PingPongPacket(0);
        pkt.readBody(in);
        return pkt;
    }

}
