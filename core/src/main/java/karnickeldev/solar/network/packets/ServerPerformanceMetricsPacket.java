package karnickeldev.solar.network.packets;

import io.netty.buffer.ByteBuf;

public class ServerPerformanceMetricsPacket extends Packet {

    private int worldId;
    private long simTimeMicros;

    public float tps;
    public float delay;

    public ServerPerformanceMetricsPacket(int worldId, long simTimeMicros, float tps, float delay) {
        super(PacketTypes.SERVER_PERFORMANCE_METRICS.getType(), (short)0);
        this.tps = tps;
        this.delay = delay;
        this.simTimeMicros = simTimeMicros;
        this.worldId = worldId;
    }


    public int getWorldId() {
        return worldId;
    }

    public long getSimTimeMicros() {
        return simTimeMicros;
    }

    @Override
    public void writeBody(ByteBuf out) {
        out.writeInt(worldId);
        out.writeLong(simTimeMicros);
        out.writeFloat(tps);
        out.writeFloat(delay);
    }

    public void readBody(ByteBuf in) {
        worldId = in.readInt();
        simTimeMicros = in.readLong();
        tps = in.readFloat();
        delay = in.readFloat();
    }

    public static ServerPerformanceMetricsPacket create(ByteBuf in) {
        ServerPerformanceMetricsPacket pkt = new ServerPerformanceMetricsPacket(0,0,0,0);
        pkt.readBody(in);
        return pkt;
    }
}
