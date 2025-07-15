package karnickeldev.solar.network.net.handlers;

import karnickeldev.solar.context.ServerContext;
import karnickeldev.solar.network.packets.SimTimeUpdateRequestPacket;
import karnickeldev.solar.simulation.execution.SimulationManager;

/**
 * @author : KarnickelDev
 * @since : 15.07.2025
 **/
public class SimTimeUpdateRequestHandler implements PacketHandler<SimTimeUpdateRequestPacket> {
    @Override
    public void handle(int clientId, SimTimeUpdateRequestPacket packet) {
        ServerContext.get().getServer().getDispatcher().dispatch(() -> ServerContext.get().getServer().getSimulationManagerThread().getSimulationManager().mulSimSpeed(packet.getSimSpeed()));
    }

    @Override
    public Class<SimTimeUpdateRequestPacket> getPacketClass() {
        return SimTimeUpdateRequestPacket.class;
    }
}
