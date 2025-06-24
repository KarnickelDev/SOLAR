package karnickeldev.solar.network.net.handlers;

import karnickeldev.solar.network.packets.Packet;

/**
 * @author : KarnickelDev
 * @since : 20.06.2025
 **/
public interface PacketHandler<T extends Packet> {

    void handle(T packet);

    short getPacketType();

    Class<T> getPacketClass();

}
