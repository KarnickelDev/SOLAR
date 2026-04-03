package karnickeldev.solar.network.net.handlers;

import karnickeldev.solar.context.ServerContext;
import karnickeldev.solar.logging.LogTag;
import karnickeldev.solar.logging.Logger;
import karnickeldev.solar.network.packets.FullSnapshotPacket;
import karnickeldev.solar.network.packets.FullSnapshotRequestPacket;
import karnickeldev.solar.network.packets.PacketFactory;
import karnickeldev.solar.world.ServerWorld;

/**
 * @author KarnickelDev
 * @since 03.07.2025
 **/
public class FullSnapshotRequestHandler implements PacketHandler<FullSnapshotRequestPacket> {

    private final Logger logger;

    public FullSnapshotRequestHandler() {
        logger = Logger.get(LogTag.ECS);
    }

    @Override
    public void handle(int clientId, FullSnapshotRequestPacket packet) {
        int worldId = packet.getWorldId();
        if(!ServerContext.get().getServer().getWorldManager().containsWorld(worldId)) {
            logger.error(clientId + " requested Full Snapshot of unknown World");
            return;
        }

        logger.info("Resyncing client " + clientId);

        ServerWorld world = ServerContext.get().getServer().getWorldManager().getWorld(worldId);
        FullSnapshotPacket fullSnapshot = PacketFactory.createFullSnapshotPacket(world);
        ServerContext.get().getServer().getServerNetwork().sendToClient(clientId, fullSnapshot);
    }

    @Override
    public Class<FullSnapshotRequestPacket> getPacketClass() {
        return FullSnapshotRequestPacket.class;
    }
}
