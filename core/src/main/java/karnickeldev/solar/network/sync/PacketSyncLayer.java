package karnickeldev.solar.network.sync;

import karnickeldev.solar.network.net.handlers.HandlerRegistry;
import karnickeldev.solar.network.net.handlers.PacketHandler;
import karnickeldev.solar.network.net.listener.ClientNetworkListener;
import karnickeldev.solar.network.packets.ECSUpdatePacket;
import karnickeldev.solar.network.packets.FullSnapshotPacket;
import karnickeldev.solar.network.packets.GameStatePacket;
import karnickeldev.solar.network.packets.Packet;
import karnickeldev.solar.util.Logger;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeMap;

/**
 * @author : KarnickelDev
 * @since : 06.06.2025
 **/
public class PacketSyncLayer {

    public static final long syncDelayMicros = 100 * 1_000;

    private static class BufferEntry {
        public final GameStatePacket pkt;
        public final long arrivalMicros;

        public BufferEntry(GameStatePacket pkt, long arrivalMicros) {
            this.arrivalMicros = arrivalMicros;
            this.pkt = pkt;
        }
    }

    private final Queue<BufferEntry> buffer = new PriorityQueue<>(Comparator.comparingLong(p -> p.pkt.getSimTimeMicros()));

    private long currentTimeMicros = 0;

    private int nextExpectedSequence = 0;

    public PacketSyncLayer() {}



    public synchronized void receivePacket(GameStatePacket packet) {
        buffer.add(new BufferEntry(packet, System.nanoTime() / 1000L));
    }

    public synchronized void update(long currentSimTimeMicros) {
        this.currentTimeMicros = currentSimTimeMicros;

        while (!buffer.isEmpty()) {
            BufferEntry next = buffer.peek();
            if (next.pkt.getSimTimeMicros() < currentTimeMicros) {
                buffer.poll();

                Logger.log("delay=" + ((System.nanoTime() / 1000) - next.arrivalMicros));

                PacketHandler<Packet> handler = HandlerRegistry.getHandler(next.pkt);
                handler.handle(0, next.pkt);
            } else {
                break;
            }
        }
    }

    public int getNextExpectedSequence() {
        return nextExpectedSequence;
    }

    public int getBufferedPacketCount() {
        return buffer.size();
    }

}
