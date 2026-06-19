package karnickeldev.solar.network.net.core;

import karnickeldev.solar.network.packets.Packet;

public interface ClientNetwork extends Network {

    /**
     * Tries to connect to the network
     * @return True if successfully connected
     */
    boolean connect();

    /** Disconnects from the network and closes it */
    void disconnect();

    /**
     * @return True if connected to the network
     */
    boolean isConnected();

    /**
     * Queues a Packet to the network (flush() to send)
     * @param packet Packet to send
     */
    void send(Packet packet);

}
