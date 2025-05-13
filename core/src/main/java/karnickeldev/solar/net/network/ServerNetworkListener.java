package karnickeldev.solar.net.network;

import karnickeldev.solar.net.packets.Packet;

public interface ServerNetworkListener {

    void onClientConnected(int clientId);

    void onClientDisconnected(int clientId);

    void onPacketReceived(int clientId, Packet packet);

}
