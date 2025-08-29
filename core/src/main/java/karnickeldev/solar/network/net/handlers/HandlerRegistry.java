package karnickeldev.solar.network.net.handlers;

import karnickeldev.solar.network.packets.Packet;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : KarnickelDev
 * @since : 20.06.2025
 **/
public class HandlerRegistry {

    private static final Map<Short, PacketHandler<? extends Packet>> handlers = new HashMap<>();

    public static void registerHandler(short packetType, PacketHandler<? extends Packet> handler) {
        handlers.put(packetType, handler);
    }

    public static void unregisterHandler(short packetType) {
        handlers.remove(packetType);
    }

    public static PacketHandler<? extends Packet> getHandler(short packetType) {
        return handlers.get(packetType);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Packet> PacketHandler<T> getHandler(T packet) {
        return (PacketHandler<T>) handlers.get(packet.getType());
    }

}
