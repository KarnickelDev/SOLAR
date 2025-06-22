package karnickeldev.solar.net.network.handlers;

import karnickeldev.solar.net.packets.PingPongPacket;
import karnickeldev.solar.util.Logger;

/**
 * @author : KarnickelDev
 * @since : 20.06.2025
 **/
public class PingPongHandler implements PacketHandler<PingPongPacket> {


    @Override
    public void handle(PingPongPacket packet) {
        long rtt = System.nanoTime() - packet.clientSendTime;
        Logger.log("Ping: " + (rtt/(2_000_000)) + "ms");
    }

    @Override
    public short getPacketType() {
        return 0;
    }

    @Override
    public Class<PingPongPacket> getPacketClass() {
        return PingPongPacket.class;
    }
}
