package karnickeldev.solar.net.network.listener;

import karnickeldev.solar.net.packets.Packet;

public interface ClientNetworkListener {

    void onPacketReceived(Packet packet);

    void onDisconnected();

    void onConnected();

}
