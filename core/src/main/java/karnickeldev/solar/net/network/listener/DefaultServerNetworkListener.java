package karnickeldev.solar.net.network.listener;

import karnickeldev.solar.ecs.components.ComponentSnapshot;
import karnickeldev.solar.net.packets.*;
import karnickeldev.solar.net.packets.PingPongPacket;
import karnickeldev.solar.net.packets.FullSnapshotRequestPacket;
import karnickeldev.solar.net.packets.PingPacket;
import karnickeldev.solar.net.server.Server;
import karnickeldev.solar.simulation.execution.SimulationManager;
import karnickeldev.solar.util.Logger;
import karnickeldev.solar.world.ServerWorld;

import java.util.List;

public class DefaultServerNetworkListener implements ServerNetworkListener {

    @Override
    public void onClientConnected(int clientId) {
        Logger.log(Logger.SERVER, "Client connected");
    }

    @Override
    public void onClientDisconnected(int clientId) {
        Logger.log(Logger.SERVER, "Client disconnected");
    }

    @Override
    public void onPacketReceived(int clientId, Packet packet) {

        long now = System.nanoTime();

        if(packet.getType() == PacketTypes.PING.getType()) {
            PingPacket p = (PingPacket) packet;
            PingPongPacket pp = PacketFactory.createPingPongPacket(p.clientSendTime, now);
            pp.serverSendTime = now;
            Server.getInstance().getServerNetwork().sendToClient(clientId, pp);
        }

        if(packet.getType() == PacketTypes.FULL_SNAPSHOT_REQUEST.getType()) {
            FullSnapshotRequestPacket p = (FullSnapshotRequestPacket) packet;
            int worldId = p.getWorldId();
            if(!Server.getInstance().getWorldManager().containsWorld(worldId)) {
                Logger.error(clientId + " requested Full Snapshot of unknown World");
                return;
            }

            Logger.log(Logger.NETWORK, "Resyncing client " + clientId);

            ServerWorld world = Server.getInstance().getWorldManager().getWorld(worldId);
            FullSnapshotPacket fullSnapshot = PacketFactory.createFullSnapshotPacket(world);
            Server.getInstance().getServerNetwork().sendToClient(clientId, fullSnapshot);
        }

    }
}
