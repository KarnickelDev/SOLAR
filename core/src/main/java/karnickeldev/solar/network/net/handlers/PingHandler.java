package karnickeldev.solar.network.net.handlers;

import karnickeldev.solar.context.ServerContext;
import karnickeldev.solar.network.packets.PacketFactory;
import karnickeldev.solar.network.packets.PingPacket;
import karnickeldev.solar.network.packets.PingPongPacket;

/**
 * @author KarnickelDev
 * @since 24.06.2025
 **/
public class PingHandler implements PacketHandler<PingPacket> {

    @Override
    public void handle(int clientId, PingPacket packet) {
        PingPongPacket pp = PacketFactory.createPingPongPacket(packet.getClientSendTime());
        ServerContext.get().getServer().getServerNetwork().sendToClient(clientId, pp);
    }


    @Override
    public Class<PingPacket> getPacketClass() {
        return PingPacket.class;
    }
}
