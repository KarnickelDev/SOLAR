package karnickeldev.solar.network.sync;

import karnickeldev.solar.network.net.handlers.HandlerRegistry;
import karnickeldev.solar.network.net.handlers.PacketHandler;
import karnickeldev.solar.network.packets.GameStatePacket;
import karnickeldev.solar.network.packets.Packet;
import karnickeldev.solar.util.Logger;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * @author KarnickelDev
 * @since 06.06.2025
 **/
public final class PacketSyncLayer {

    public static final long syncDelayMicros = 100 * 1_000;

    private record BufferEntry(GameStatePacket pkt, long arrivalMicros) {}

    private final Queue<BufferEntry> buffer = new PriorityQueue<>(Comparator.comparingLong(p -> p.pkt.getSimTimeMicros()));
    private final Object lock = new Object();

    public PacketSyncLayer() {}


    public void receivePacket(GameStatePacket packet) {
        long nowMicros = System.nanoTime() / 1000;
        synchronized (lock) {
            buffer.add(new BufferEntry(packet, nowMicros));
        }
    }

    public void update(long playbackSimTimeMicros) {
        while(true) {
            BufferEntry entry;
            synchronized (lock) {
                entry = buffer.peek();
                if(entry == null || entry.pkt.getSimTimeMicros() > playbackSimTimeMicros) {
                    break;
                }
                buffer.poll();

                try {
                    PacketHandler<Packet> handler = HandlerRegistry.getHandler(entry.pkt);
                    handler.handle(0, entry.pkt);
                } catch (Throwable t) {
                    Logger.error(Logger.SYNC, "Exception handling buffered packet", t);
                }
            }
        }
    }

    public int getBufferedPacketCount() {
        synchronized (lock) {
            return buffer.size();
        }
    }

}
