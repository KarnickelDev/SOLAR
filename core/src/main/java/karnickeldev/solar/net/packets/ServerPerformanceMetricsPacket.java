package karnickeldev.solar.net.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ServerPerformanceMetricsPacket implements Packet {

    public final float tps;
    public final float delay;

    private final long tick;

    public ServerPerformanceMetricsPacket(long tick, float tps, float delay) {
        this.tps = tps;
        this.delay = delay;
        this.tick = tick;
    }

    @Override
    public short getType() {
        return PacketTypes.SERVER_PERFORMANCE_METRICS.getType();
    }

    @Override
    public long getCreationTick() {
        return tick;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {

    }

    @Override
    public Packet deserialize(DataInputStream in) throws IOException {
        return null;
    }
}
