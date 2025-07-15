package karnickeldev.solar.network.packets;

import io.netty.buffer.ByteBuf;

/**
 * @author : KarnickelDev
 * @since : 15.07.2025
 **/
public class SimTimeUpdateRequestPacket extends Packet {

    private float simSpeed;
    private boolean pause;

    public SimTimeUpdateRequestPacket(float simSpeed, boolean pause) {
        super(PacketTypes.SIM_TIME_UPDATE_REQUEST.getType(), (short) 0);

        this.simSpeed = simSpeed;
        this.pause = pause;
    }

    public float getSimSpeed() {
        return simSpeed;
    }

    public boolean isPause() {
        return pause;
    }

    @Override
    protected void writeBody(ByteBuf out) {
        out.writeFloat(simSpeed);
        out.writeBoolean(pause);
    }

    @Override
    protected void readBody(ByteBuf in) {
        simSpeed = in.readFloat();
        pause = in.readBoolean();
    }

    public static SimTimeUpdateRequestPacket create(ByteBuf in) {
        SimTimeUpdateRequestPacket pkt = new SimTimeUpdateRequestPacket(0,false);
        pkt.readBody(in);
        return pkt;
    }
}
