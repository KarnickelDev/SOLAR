package karnickeldev.solar.network.net.handlers;

import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.network.packets.WorldUpdatePacket;
import karnickeldev.solar.util.Logger;
import karnickeldev.solar.world.ClientWorld;
import karnickeldev.solar.world.WorldManager;

/**
 * @author KarnickelDev
 * @since 03.07.2025
 **/
public class WorldUpdateHandler implements PacketHandler<WorldUpdatePacket> {
    @Override
    public void handle(int clientId, WorldUpdatePacket packet) {
        WorldManager<ClientWorld> worldManager = GameContext.get().getWorldManager();
        int worldId = packet.getWorldId();
        if(worldManager.containsWorld(worldId)) {
            Logger.error("double world creation");
        } else {
            worldManager.addWorld(new ClientWorld(worldId));
            Logger.log(Logger.GENERAL, "Added new World " + worldId);
        }
    }

    @Override
    public Class<WorldUpdatePacket> getPacketClass() {
        return WorldUpdatePacket.class;
    }
}
