package karnickeldev.solar.net.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author : KarnickelDev
 * @since : 06.06.2025
 **/
public class PingPacket implements Packet {

    public final long clientSendTime;

    protected PingPacket(long clientSendTime) {
        this.clientSendTime = clientSendTime;
    }

    @Override
    public boolean isFastHandled() {
        return true;
    }

    @Override
    public short getType() {
        return PacketTypes.PING.getType();
    }

    @Override
    public int getWorldId() {
        return -1;
    }

    @Override
    public long getSimTimeMicros() {
        return -1;
    }

    @Override
    public int getSequenceId() {
        return 0;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeLong(clientSendTime);
    }

    public static PingPacket deserialize(DataInputStream in) throws IOException {
        return PacketFactory.createPingPacket(in.readLong());
    }
}
