package karnickeldev.solar.net.network;

import karnickeldev.solar.net.packets.Packet;

public interface ClientNetwork extends Network {

    void connect();

    void disconnect();

    boolean isConnected();

    void send(Packet packet);

    boolean updateNetwork();

}
