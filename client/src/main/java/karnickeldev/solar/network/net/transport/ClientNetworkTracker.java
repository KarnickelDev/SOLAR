package karnickeldev.solar.network.net.transport;

import io.netty.channel.ChannelId;
import karnickeldev.solar.network.packets.PacketTypes;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author KarnickelDev
 * @since 08.10.2025
 **/
public class ClientNetworkTracker implements NetworkTracker {

    private static final long REFRESH_NS = 1_000_000_000L;

    private final AtomicLong lastRefresh = new AtomicLong(0);

    private final AtomicInteger bytesRead = new AtomicInteger(0);
    private final AtomicInteger bytesSend = new AtomicInteger(0);

    public static volatile float UP = 0;
    public static volatile float DOWN = 0;

    @Override
    public void onPacketRead(ChannelId clientId, short packetType, int bytes) {
        if(packetType == PacketTypes.FULL_SNAPSHOT.getType()) {
            System.out.println("FullSnapshot: " + bytes + " Byte");
        }

        bytesRead.addAndGet(bytes);
        update();
    }

    @Override
    public void onPacketWrite(ChannelId clientId, short packetType, int bytes) {
        bytesSend.addAndGet(bytes);
        update();
    }

    private void update() {
        long now = System.nanoTime();
        if(now - lastRefresh.get() < REFRESH_NS) return;

        lastRefresh.set(now);

        int read = bytesRead.getAndSet(0);
        int write = bytesSend.getAndSet(0);

        UP = write / 1e3f;
        DOWN = read / 1e3f;
    }

}
