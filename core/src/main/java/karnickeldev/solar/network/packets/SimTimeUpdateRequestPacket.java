package karnickeldev.solar.network.packets;

import io.netty.buffer.ByteBuf;

/**
 * @author : KarnickelDev
 * @since : 15.07.2025
 **/
public class SimTimeUpdateRequestPacket extends Packet {

    private byte simSpeedIndex;
    private boolean pause;

    public SimTimeUpdateRequestPacket(byte simSpeedIndex, boolean pause) {
        super(PacketTypes.SIM_TIME_UPDATE_REQUEST.getType(), (short) 0);

        this.simSpeedIndex = simSpeedIndex;
        this.pause = pause;
    }

    public byte getSimSpeedIndex() {
        return simSpeedIndex;
    }

    public boolean isPause() {
        return pause;
    }

    @Override
    protected void writeBody(ByteBuf out) {
        out.writeByte(simSpeedIndex);
        out.writeBoolean(pause);
    }

    @Override
    protected void readBody(ByteBuf in) {
        simSpeedIndex = in.readByte();
        pause = in.readBoolean();
    }

    public static SimTimeUpdateRequestPacket create(ByteBuf in) {
        SimTimeUpdateRequestPacket pkt = new SimTimeUpdateRequestPacket((byte)0,false);
        pkt.readBody(in);
        return pkt;
    }
}
