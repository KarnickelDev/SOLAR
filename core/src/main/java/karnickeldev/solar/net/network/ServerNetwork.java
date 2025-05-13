package karnickeldev.solar.net.network;

import karnickeldev.solar.net.packets.Packet;

public interface ServerNetwork extends Network {

    void start();

    void shutdown();


    void broadcast(Packet packet);

    void sendToClient(int id, Packet packet);

    boolean updateNetwork();

}
