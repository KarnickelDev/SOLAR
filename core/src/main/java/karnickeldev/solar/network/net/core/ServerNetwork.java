package karnickeldev.solar.network.net.core;

import karnickeldev.solar.network.packets.Packet;

public interface ServerNetwork extends Network {

    void start();

    void shutdown();


    void broadcast(Packet packet);

    void sendToClient(int id, Packet packet);

    boolean updateNetwork();

}
