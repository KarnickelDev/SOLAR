package karnickeldev.solar.network.net.handlers;

import karnickeldev.solar.context.ServerContext;
import karnickeldev.solar.network.packets.SimTimeUpdateRequestPacket;
import karnickeldev.solar.simulation.execution.SimSpeedController;

/**
 * @author KarnickelDev
 * @since 15.07.2025
 **/
public class SimTimeUpdateRequestHandler implements PacketHandler<SimTimeUpdateRequestPacket> {
    @Override
    public void handle(int clientId, SimTimeUpdateRequestPacket packet) {
        ServerContext.get().getServer().getScheduler().schedule(
            () -> ServerContext.get().getServer().getSimulationManagerThread().getSimulationManager().paused = packet.isPause()
        );

        if(packet.getSimSpeedIndex() < 0 || packet.getSimSpeedIndex() >= SimSpeedController.SPEED_PRESETS.length) return;

        ServerContext.get().getServer().getScheduler().schedule(
            () -> ServerContext.get().getServer().getSimulationManagerThread().getSimulationManager().setSimSpeed(packet.getSimSpeedIndex())
        );
    }

    @Override
    public Class<SimTimeUpdateRequestPacket> getPacketClass() {
        return SimTimeUpdateRequestPacket.class;
    }
}
