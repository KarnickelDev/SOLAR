package karnickeldev.solar.net.packets;

import karnickeldev.solar.ecs.components.ComponentSnapshot;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PacketRegistry {

    private static final int TYPE_INVALID = 0;

    private static final Map<Integer, ComponentSnapshot> typeToSnapshot = new HashMap<>();
    private static final Map<Class<?>, Integer> classToType = new HashMap<>();

    public static void register(int id, Class<?> clazz, ComponentSnapshot snapshot) {
        typeToSnapshot.put(id, snapshot);
        classToType.put(clazz, id);
    }

    public static int getTypeId(Class<?> clazz) {
        return classToType.getOrDefault(clazz, TYPE_INVALID);
    }

    public static ComponentSnapshot deserialize(int id, DataInputStream in) throws IOException {
        ComponentSnapshot snapshot = typeToSnapshot.get(id);
        return snapshot.deserialize(in);
    }
}
