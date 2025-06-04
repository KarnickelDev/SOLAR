package karnickeldev.solar.net.network.channel;

import karnickeldev.solar.net.packets.Packet;

import java.util.List;

/**
 * @author : KarnickelDev
 * @since : 06.06.2025
 **/
public interface Channel {

    void send(int clientId, Packet packet);

    List<Packet> pollReceived();

    boolean update();

    void close();

}
