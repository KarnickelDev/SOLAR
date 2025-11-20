package karnickeldev.solar.network.packets;

import io.netty.buffer.ByteBuf;
import karnickeldev.solar.network.net.core.Deserializer;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author KarnickelDev
 * @since 21.06.2025
 **/
public class PacketRegistry {

    private static final Map<Short, PacketFactory> factories = new HashMap<>();

    public static void register(short type, PacketFactory factory) {
        if(factories.putIfAbsent(type, factory) != null)
            throw new IllegalStateException("Duplicate packet type registration: " + type);
    }

    public static Packet create(short type, ByteBuf in) {
        PacketFactory factory = factories.get(type);
        if(factory == null) throw new IllegalArgumentException("Unknown packet type: " + type);
        return factory.create(in);
    }

    public interface PacketFactory {
        Packet create(ByteBuf in);
    }

}
