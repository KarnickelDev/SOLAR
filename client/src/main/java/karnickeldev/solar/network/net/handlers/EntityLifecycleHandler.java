package karnickeldev.solar.network.net.handlers;

import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.ecs.ClientECS;
import karnickeldev.solar.logging.LogTag;
import karnickeldev.solar.logging.Logger;
import karnickeldev.solar.network.packets.EntityLifecyclePacket;
import karnickeldev.solar.world.ClientWorld;
import karnickeldev.solar.world.WorldManager;

/**
 * @author KarnickelDev
 * @since 03.07.2025
 **/
public class EntityLifecycleHandler implements PacketHandler<EntityLifecyclePacket> {

    private final Logger logger;

    public EntityLifecycleHandler() {
        this.logger = Logger.get(LogTag.ECS);
    }

    @Override
    public void handle(int clientId, EntityLifecyclePacket packet) {
        int worldId = packet.getWorldId();
        WorldManager<ClientWorld> worldManager = GameContext.get().getWorldManager();

        if(!worldManager.containsWorld(worldId)) {
            logger.error("Error handling EntityLifecyclePacket: unknown WorldId " + worldId);
            return;
        }

        logger.debug("Received EntityLifecyclePacket for world " + worldId
            + " (created: " + packet.createdEntityIds.length + ", destroyed: "  + packet.destroyedEntityIds.length + ")");

        ClientECS ecs = worldManager.getWorld(worldId).getECS();

        for (int i = 0; i < packet.destroyedEntityIds.length; i++) {
            ecs.getEntityManager().destroy(packet.destroyedEntityIds[i]);
        }
        for (int i = 0; i < packet.createdEntityIds.length; i++) {
            ecs.getEntityManager().importEntity(packet.createdEntityIds[i]);
        }
    }

    @Override
    public Class<EntityLifecyclePacket> getPacketClass() {
        return EntityLifecyclePacket.class;
    }
}
