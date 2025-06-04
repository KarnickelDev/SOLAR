package karnickeldev.solar.net.network;

import karnickeldev.solar.net.packets.*;
import karnickeldev.solar.net.server.Server;
import karnickeldev.solar.util.Logger;

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

    }
}
