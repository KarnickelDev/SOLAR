package karnickeldev.solar.network.net.listener;

import karnickeldev.solar.network.packets.Packet;

public interface ServerNetworkListener {

    void onClientConnected(int clientId);

    void onClientDisconnected(int clientId);

    void onPacketReceived(int clientId, Packet packet);

}
