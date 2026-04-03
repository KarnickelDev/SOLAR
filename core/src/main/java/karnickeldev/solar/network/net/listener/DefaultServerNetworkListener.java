package karnickeldev.solar.network.net.listener;

import karnickeldev.solar.context.ServerContext;
import karnickeldev.solar.logging.LogTag;
import karnickeldev.solar.logging.Logger;
import karnickeldev.solar.network.packets.*;
import karnickeldev.solar.world.ServerWorld;

public class DefaultServerNetworkListener implements ServerNetworkListener {

    private final Logger logger;

    public DefaultServerNetworkListener() {
        logger = Logger.get(LogTag.SERVER);
    }

    @Override
    public void onClientConnected(int clientId) {
        logger.info("Client connected " + clientId);
    }

    @Override
    public void onClientDisconnected(int clientId) {
        logger.info("Client disconnected: " + clientId);
    }

    @Override
    public void onPacketReceived(int clientId, Packet packet) {

        if(packet.getType() == PacketTypes.PING.getType()) {
            PingPacket p = (PingPacket) packet;
            PingPongPacket pp = PacketFactory.createPingPongPacket(p.getClientSendTime());
            ServerContext.get().getServer().getServerNetwork().sendToClient(clientId, pp);
        }

        if(packet.getType() == PacketTypes.FULL_SNAPSHOT_REQUEST.getType()) {
            FullSnapshotRequestPacket p = (FullSnapshotRequestPacket) packet;
            int worldId = p.getWorldId();
            if(!ServerContext.get().getServer().getWorldManager().containsWorld(worldId)) {
                logger.error(clientId + " requested FullSnapshot of unknown World");
                return;
            }

            logger.info("Resyncing client " + clientId);

            ServerWorld world = ServerContext.get().getServer().getWorldManager().getWorld(worldId);
            FullSnapshotPacket fullSnapshot = PacketFactory.createFullSnapshotPacket(world);
            ServerContext.get().getServer().getServerNetwork().sendToClient(clientId, fullSnapshot);
        }

        if(packet.getType() == PacketTypes.TEST_CAM.getType()) {
            TestCamPacket p = (TestCamPacket) packet;
            p.clientId = clientId;
            ServerContext.get().getServer().getServerNetwork().broadcast(p);
        }

    }
}
