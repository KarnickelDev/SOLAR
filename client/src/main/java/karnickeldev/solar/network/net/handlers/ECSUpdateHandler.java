package karnickeldev.solar.network.net.handlers;

import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.ecs.components.ComponentSnapshot;
import karnickeldev.solar.network.packets.ECSUpdatePacket;
import karnickeldev.solar.util.Logger;
import karnickeldev.solar.world.ClientWorld;
import karnickeldev.solar.world.WorldManager;

/**
 * @author : KarnickelDev
 * @since : 03.07.2025
 **/
public class ECSUpdateHandler implements PacketHandler<ECSUpdatePacket> {
    @Override
    public void handle(int clientId, ECSUpdatePacket packet) {
        WorldManager<ClientWorld> worldManager = GameContext.get().getWorldManager();
        ComponentSnapshot[] snapshots = packet.getSnapshots();

        int worldId = packet.getWorldId();
        if(worldManager.containsWorld(worldId)) {
            if(worldId != worldManager.getActiveWorld().getID()) {
                worldManager.changeWorld(worldId);
            }
        } else {
            Logger.error("Received ECS Update for unknown World " + worldId);
            return;
        }

        GameContext.get().getTimeSyncManager().reportSample(packet.getSimTimeMicros(), (System.nanoTime() / 1000), packet.getSimSpeed());

        worldManager.getWorld(worldId).getECS().getComponentRegistry().applyAllSnapshots(snapshots);
    }

    @Override
    public Class<ECSUpdatePacket> getPacketClass() {
        return ECSUpdatePacket.class;
    }
}
