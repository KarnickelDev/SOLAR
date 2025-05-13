package karnickeldev.solar.net.network;

import karnickeldev.solar.net.packets.Packet;

public interface ClientNetworkListener {

    void onPacketReceived(Packet packet);

    void onDisconnected();

    void onConnected();

}
