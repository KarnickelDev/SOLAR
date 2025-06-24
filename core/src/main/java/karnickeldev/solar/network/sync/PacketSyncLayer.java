package karnickeldev.solar.network.sync;

import karnickeldev.solar.network.net.handlers.HandlerRegistry;
import karnickeldev.solar.network.net.handlers.PacketHandler;
import karnickeldev.solar.network.packets.Packet;
import karnickeldev.solar.util.Logger;

import java.util.TreeMap;

/**
 * @author : KarnickelDev
 * @since : 06.06.2025
 **/
public class PacketSyncLayer {

    private final TreeMap<Integer, Packet> buffer = new TreeMap<>();


    private final SimTimeEstimator timeEstimator = new SimTimeEstimator();

    private int nextExpectedSequence = 0;

    public PacketSyncLayer() {

    }

    public void receivePacket(Packet packet) {
        int seq = packet.getSequenceId();
        if(seq < nextExpectedSequence) {
            Logger.error("Duplicate Packet detected!");
            return;
        }
        buffer.put(seq, packet);
    }

    public void update() {
        while(true) {
            Packet packet = buffer.get(nextExpectedSequence);
            if(packet == null) break;   // gap detected, stop processing

            dispatch(packet);
            buffer.remove(nextExpectedSequence);
            nextExpectedSequence++;
        }
    }

    private void dispatch(Packet packet) {
        short type = packet.getType();

        PacketHandler<Packet> handler = HandlerRegistry.getHandler(packet);
        if(handler == null) {
            Logger.log(Logger.SYNC, "No handler registered for Packet type " + type);
        } else {
            // handle packet
            handler.handle(packet);
        }
    }

    public int getNextExpectedSequence() {
        return nextExpectedSequence;
    }

    public int getBufferedPacketCount() {
        return buffer.size();
    }

}
