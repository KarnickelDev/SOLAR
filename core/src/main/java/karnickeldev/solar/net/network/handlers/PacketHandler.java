package karnickeldev.solar.net.network.handlers;

import karnickeldev.solar.net.packets.Packet;

/**
 * @author : KarnickelDev
 * @since : 20.06.2025
 **/
public interface PacketHandler<T extends Packet> {

    void handle(T packet);

    short getPacketType();

    Class<T> getPacketClass();

}
