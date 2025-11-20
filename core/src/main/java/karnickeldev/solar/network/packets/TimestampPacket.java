package karnickeldev.solar.network.packets;

import io.netty.buffer.ByteBuf;

/**
 * @author KarnickelDev
 * @since 06.10.2025
 **/
public class TimestampPacket extends GameStatePacket {

    private long simTimeMicros;

    private float currentSimSpeed;
    private byte targetSimSpeedIndex;
    private boolean paused;

    protected TimestampPacket(long simTimeMicros, float currentSimSpeed, byte targetSimSpeedIndex, boolean paused) {
        super(PacketTypes.TIMESTAMP.getType(), (short) 0);

        this.simTimeMicros = simTimeMicros;
        this.currentSimSpeed = currentSimSpeed;
        this.targetSimSpeedIndex = targetSimSpeedIndex;
        this.paused = paused;
    }

    @Override
    public boolean isFastHandled() {
        return true;
    }

    @Override
    public long getSimTimeMicros() {
        return simTimeMicros;
    }

    public boolean isPaused() {
        return paused;
    }

    public float getCurrentSimSpeed() {
        return currentSimSpeed;
    }

    public byte getTargetSimSpeedIndex() {
        return targetSimSpeedIndex;
    }

    @Override
    protected void writeBody(ByteBuf out) {
        out.writeLong(simTimeMicros);
        out.writeFloat(currentSimSpeed);
        out.writeByte(targetSimSpeedIndex);
        out.writeBoolean(paused);
    }

    @Override
    protected void readBody(ByteBuf in) {
        simTimeMicros = in.readLong();
        currentSimSpeed = in.readFloat();
        targetSimSpeedIndex = in.readByte();
        paused = in.readBoolean();
    }

    public static TimestampPacket create(ByteBuf in) {
        TimestampPacket p = new TimestampPacket(0,0,(byte) 0, false);
        p.readBody(in);
        return p;
    }

}
