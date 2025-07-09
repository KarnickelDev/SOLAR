package karnickeldev.solar.network.packets;

import io.netty.buffer.ByteBuf;

/**
 * @author : KarnickelDev
 * @since : 28.06.2025
 **/
public class TestCamPacket extends Packet {

    public int clientId;
    public double x, y;

    public TestCamPacket() {
        super(PacketTypes.TEST_CAM.getType(), (short)0);
    }

    @Override
    protected void writeBody(ByteBuf out) {
        out.writeInt(clientId);
        out.writeDouble(x);
        out.writeDouble(y);
    }

    @Override
    protected void readBody(ByteBuf in) {
        clientId = in.readInt();
        x = in.readDouble();
        y = in.readDouble();
    }

    public static TestCamPacket create(ByteBuf in) {
        TestCamPacket pkt = new TestCamPacket();
        pkt.readBody(in);
        return pkt;
    }
}
