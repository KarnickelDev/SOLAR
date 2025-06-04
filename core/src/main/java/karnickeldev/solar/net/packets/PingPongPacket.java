package karnickeldev.solar.net.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author : KarnickelDev
 * @since : 06.06.2025
 **/
public class PingPongPacket implements Packet {

    public final long clientSendTime;
    public final long serverReceiveTime;
    public long serverSendTime;

    protected PingPongPacket(long clientSendTime, long serverReceiveTime) {
        this.clientSendTime = clientSendTime;
        this.serverReceiveTime = serverReceiveTime;
        this.serverSendTime = -1;
    }

    @Override
    public boolean isFastHandled() {
        return true;
    }

    @Override
    public short getType() {
        return PacketTypes.PONG.getType();
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

    }

    @Override
    public Packet deserialize(DataInputStream in) throws IOException {
        return null;
    }
}
