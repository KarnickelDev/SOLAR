package karnickeldev.solar.net.packets;

import karnickeldev.solar.net.network.core.Deserializer;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : KarnickelDev
 * @since : 21.06.2025
 **/
public class PacketRegistry {

    private static final Map<Short, Deserializer<? extends Packet>> deserializers = new HashMap<>();

    public static <T extends Packet> void register(short type, Deserializer<T> deserializer) {
        deserializers.put(type, deserializer);
    }

    public static Packet deserializePacket(short type, DataInputStream in) throws IOException {
        Deserializer<? extends Packet> deserializer = deserializers.get(type);
        if (deserializer == null) {
            throw new IOException("Unknown packet type: " + type);
        }
        return deserializer.deserialize(in);
    }
}
