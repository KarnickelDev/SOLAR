package karnickeldev.solar.network.net.channel;

import karnickeldev.solar.network.packets.Packet;

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
