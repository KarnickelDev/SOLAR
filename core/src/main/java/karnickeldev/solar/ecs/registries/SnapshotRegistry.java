package karnickeldev.solar.ecs.registries;

import karnickeldev.solar.ecs.components.ComponentSnapshot;
import karnickeldev.solar.net.network.core.Deserializer;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SnapshotRegistry {

    private static final int TYPE_INVALID = -1;

    private static final Map<Integer, Deserializer<? extends ComponentSnapshot>> typeToDeserializer = new HashMap<>();
    private static final Map<Class<?>, Integer> classToType = new HashMap<>();

    public static void register(int id, Class<?> clazz, Deserializer<ComponentSnapshot> deserializer) {
        typeToDeserializer.put(id, deserializer);
        classToType.put(clazz, id);
    }

    public static int getTypeId(Class<?> clazz) {
        return classToType.getOrDefault(clazz, TYPE_INVALID);
    }

    public static ComponentSnapshot deserialize(int id, DataInputStream in) throws IOException {
        return typeToDeserializer.get(id).deserialize(in);
    }
}
