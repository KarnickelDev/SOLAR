package karnickeldev.solar.net.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ServerPerformanceMetricsPacket implements Packet {

    private final int worldId;
    private final long simTimeMicros;

    public final float tps;
    public final float delay;

    public ServerPerformanceMetricsPacket(int worldId, long simTimeMicros, float tps, float delay) {
        this.tps = tps;
        this.delay = delay;
        this.simTimeMicros = simTimeMicros;
        this.worldId = worldId;
    }

    @Override
    public short getType() {
        return PacketTypes.SERVER_PERFORMANCE_METRICS.getType();
    }

    @Override
    public int getWorldId() {
        return worldId;
    }

    @Override
    public long getSimTimeMicros() {
        return simTimeMicros;
    }

    @Override
    public int getSequenceId() {
        return 0;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(worldId);
        out.writeLong(simTimeMicros);
        out.writeFloat(tps);
        out.writeFloat(delay);
    }

    public static ServerPerformanceMetricsPacket deserialize(DataInputStream in) throws IOException {
        int worldId = in.readInt();
        long simTimeMicros = in.readLong();
        float tps = in.readFloat();
        float delay = in.readFloat();
        return new ServerPerformanceMetricsPacket(worldId, simTimeMicros, tps, delay);
    }
}
