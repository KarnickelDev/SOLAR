package karnickeldev.solar.ecs.registries;

import io.netty.buffer.ByteBuf;
import karnickeldev.solar.ecs.components.ComponentSnapshot;
import karnickeldev.solar.network.net.core.Deserializer;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SnapshotRegistry {

    private static final short TYPE_INVALID = -1;

    private static final Map<Short, Deserializer<? extends ComponentSnapshot>> typeToDeserializer = new HashMap<>();
    private static final Map<Class<?>, Short> classToType = new HashMap<>();

    public static void register(short id, Class<?> clazz, Deserializer<ComponentSnapshot> deserializer) {
        typeToDeserializer.put(id, deserializer);
        classToType.put(clazz, id);
    }

    public static short getTypeId(Class<?> clazz) {
        return classToType.getOrDefault(clazz, TYPE_INVALID);
    }

    public static ComponentSnapshot deserialize(short id, ByteBuf in) {
        return typeToDeserializer.get(id).deserialize(in);
    }
}
