package karnickeldev.solar.network.net.handlers;

import karnickeldev.solar.network.net.DefaultClientNetworkListener;
import karnickeldev.solar.network.packets.TestCamPacket;

/**
 * @author KarnickelDev
 * @since 03.07.2025
 **/
public class TestCamHandler implements PacketHandler<TestCamPacket> {
    @Override
    public void handle(int clientId, TestCamPacket packet) {
        double x = packet.x;
        double y = packet.y;

        DefaultClientNetworkListener.clientCamPos.put(packet.clientId, new Double[]{x,y});
    }

    @Override
    public Class<TestCamPacket> getPacketClass() {
        return TestCamPacket.class;
    }
}
