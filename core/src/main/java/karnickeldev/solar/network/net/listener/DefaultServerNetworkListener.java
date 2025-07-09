package karnickeldev.solar.network.net.listener;

import karnickeldev.solar.context.ServerContext;
import karnickeldev.solar.network.packets.*;
import karnickeldev.solar.network.packets.PingPongPacket;
import karnickeldev.solar.network.packets.FullSnapshotRequestPacket;
import karnickeldev.solar.network.packets.PingPacket;
import karnickeldev.solar.network.server.Server;
import karnickeldev.solar.util.Logger;
import karnickeldev.solar.world.ServerWorld;

public class DefaultServerNetworkListener implements ServerNetworkListener {

    @Override
    public void onClientConnected(int clientId) {
        Logger.log(Logger.SERVER, "Client connected: " + clientId);
    }

    @Override
    public void onClientDisconnected(int clientId) {
        Logger.log(Logger.SERVER, "Client disconnected: " + clientId);
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
                Logger.error(clientId + " requested Full Snapshot of unknown World");
                return;
            }

            Logger.log(Logger.NETWORK, "Resyncing client " + clientId);

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
