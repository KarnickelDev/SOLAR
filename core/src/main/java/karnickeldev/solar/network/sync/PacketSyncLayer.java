package karnickeldev.solar.network.sync;

import karnickeldev.solar.network.net.handlers.HandlerRegistry;
import karnickeldev.solar.network.net.handlers.PacketHandler;
import karnickeldev.solar.network.net.listener.ClientNetworkListener;
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

    public static final long syncDelayMicros = 20 * 1_000;

    private final Queue<GameStatePacket> buffer = new PriorityQueue<>(Comparator.comparingLong(GameStatePacket::getSimTimeMicros));

    private long currentTimeMicros;

    private int nextExpectedSequence = 0;

    public PacketSyncLayer() {
    }

    public synchronized void receivePacket(GameStatePacket packet) {
        buffer.add(packet);
    }

    public synchronized void update(long currentSimTimeMicros) {
        this.currentTimeMicros = currentSimTimeMicros;

        while (!buffer.isEmpty()) {
            GameStatePacket next = buffer.peek();
            if (next.getSimTimeMicros() < currentTimeMicros - syncDelayMicros) {
                buffer.poll();
                //listener.onPacketReceived(next);
                PacketHandler<Packet> handler = HandlerRegistry.getHandler(next);
                handler.handle(0, next);
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
