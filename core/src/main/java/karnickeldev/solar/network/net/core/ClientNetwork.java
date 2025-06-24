package karnickeldev.solar.network.net.core;

import karnickeldev.solar.network.packets.Packet;

public interface ClientNetwork extends Network {

    void connect();

    void disconnect();

    boolean isConnected();

    void send(Packet packet);

    boolean updateNetwork();

}
