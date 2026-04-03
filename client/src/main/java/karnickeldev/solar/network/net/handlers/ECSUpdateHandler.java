package karnickeldev.solar.network.net.handlers;

import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.ecs.components.ComponentSnapshot;
import karnickeldev.solar.ecs.components.OrbitDataSnapshot;
import karnickeldev.solar.logging.LogTag;
import karnickeldev.solar.logging.Logger;
import karnickeldev.solar.network.packets.ECSUpdatePacket;
import karnickeldev.solar.world.ClientWorld;
import karnickeldev.solar.world.WorldManager;

/**
 * @author KarnickelDev
 * @since 03.07.2025
 **/
public class ECSUpdateHandler implements PacketHandler<ECSUpdatePacket> {

    private final Logger logger;

    public ECSUpdateHandler() {
        this.logger = Logger.get(LogTag.ECS);
    }

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
            logger.warn("Received ECS Update for unknown World " + worldId);
            return;
        }

        logger.debug("Received ECS Update for World " + worldId + "(changed components: " + packet.getSnapshots().length + ")");

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
