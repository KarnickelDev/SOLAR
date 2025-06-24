package karnickeldev.solar.network.net.listener;

import karnickeldev.solar.network.packets.Packet;

public interface ClientNetworkListener {

    void onPacketReceived(Packet packet);

    void onDisconnected();

    void onConnected();

}
