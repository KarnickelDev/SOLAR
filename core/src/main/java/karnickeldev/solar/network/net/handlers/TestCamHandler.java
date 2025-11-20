package karnickeldev.solar.network.net.handlers;

import karnickeldev.solar.context.ServerContext;
import karnickeldev.solar.network.packets.TestCamPacket;

/**
 * @author KarnickelDev
 * @since 03.07.2025
 **/
public class TestCamHandler implements PacketHandler<TestCamPacket> {
    @Override
    public void handle(int clientId, TestCamPacket packet) {
        packet.clientId = clientId;
        ServerContext.get().getServer().getServerNetwork().broadcast(packet);
    }

    @Override
    public Class<TestCamPacket> getPacketClass() {
        return TestCamPacket.class;
    }
}
