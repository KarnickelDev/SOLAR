package karnickeldev.solar.network.net.handlers;

import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.network.packets.ServerPerformanceMetricsPacket;

/**
 * @author : KarnickelDev
 * @since : 03.07.2025
 **/
public class ServerPerformanceMetricsHandler implements PacketHandler<ServerPerformanceMetricsPacket> {
    @Override
    public void handle(int clientId, ServerPerformanceMetricsPacket packet) {
        SolarMain.tps = packet.tps;
    }

    @Override
    public Class<ServerPerformanceMetricsPacket> getPacketClass() {
        return ServerPerformanceMetricsPacket.class;
    }
}
