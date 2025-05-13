package karnickeldev.solar.net.network;

import karnickeldev.solar.net.packets.Packet;

public interface NetworkInterface {

    void sendToServer(Packet packet);

    void sendToClient(int clientId, Packet packet);

    void broadcast(Packet packet);

    void onPacketReceived();

    void update();
}
