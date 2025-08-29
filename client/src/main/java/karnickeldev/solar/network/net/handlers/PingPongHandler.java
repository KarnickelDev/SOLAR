package karnickeldev.solar.network.net.handlers;

import karnickeldev.solar.network.packets.PingPongPacket;
import karnickeldev.solar.network.net.core.PingTracker;

/**
 * @author : KarnickelDev
 * @since : 20.06.2025
 **/
public class PingPongHandler implements PacketHandler<PingPongPacket> {


    @Override
    public void handle(int clientId, PingPongPacket packet) {
        long rtt = System.nanoTime() - packet.getClientSendTime();
        PingTracker.add(rtt / 1_000);
    }

    @Override
    public Class<PingPongPacket> getPacketClass() {
        return PingPongPacket.class;
    }
}
