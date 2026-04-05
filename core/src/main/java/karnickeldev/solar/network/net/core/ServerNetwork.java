package karnickeldev.solar.network.net.core;

import karnickeldev.solar.network.packets.Packet;

public interface ServerNetwork extends Network {

    /**
     * Tries to start the network
     * @return True if successfully
     */
    boolean start();

    /** Closes the network */
    void shutdown();

    /**
     * Queues sending the Packet to all connected clients
     * @param packet The Packet
     */
    void broadcast(Packet packet);

    /**
     * Queues sending a Packet to a Client
     * @param clientId Client to receive the Packet
     * @param packet The Packet
     */
    void sendToClient(int clientId, Packet packet);

    /**
     * Queues sending the Packet to all connected clients EXCEPT the given clientId
     * @param clientId The client to exclude from the broadcast
     * @param packet The Packet
     */
    void broadcastExcept(int clientId, Packet packet);

    /** Flushes queued Packets, thus sending them */
    void flush();

}
