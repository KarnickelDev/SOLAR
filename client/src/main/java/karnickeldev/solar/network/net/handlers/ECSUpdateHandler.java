package karnickeldev.solar.network.net.handlers;

import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.ecs.components.ComponentSnapshot;
import karnickeldev.solar.ecs.components.OrbitDataSnapshot;
import karnickeldev.solar.network.packets.ECSUpdatePacket;
import karnickeldev.solar.util.Logger;
import karnickeldev.solar.world.ClientWorld;
import karnickeldev.solar.world.WorldManager;

/**
 * @author KarnickelDev
 * @since 03.07.2025
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

        Logger.log("Received ECS Update for World " + worldId + "(changed components: " + packet.getSnapshots().length + ")");

        worldManager.getWorld(worldId).getECS().getComponentRegistry().applyAllSnapshots(snapshots);

        // TODO: there should be a better way
        for(ComponentSnapshot snapshot : snapshots) {
            if(snapshot instanceof OrbitDataSnapshot) {
                GameContext.get().getWorldManager().getWorld(worldId).getOrbitGraphSystem().notifyChange();
                break;
            }
        }
    }

    @Override
    public Class<ECSUpdatePacket> getPacketClass() {
        return ECSUpdatePacket.class;
    }
}
