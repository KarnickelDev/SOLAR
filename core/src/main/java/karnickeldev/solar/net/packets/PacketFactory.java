package karnickeldev.solar.net.packets;

import karnickeldev.solar.ecs.components.Component;
import karnickeldev.solar.ecs.components.ComponentSnapshot;
import karnickeldev.solar.ecs.components.HCSPositionSnapshot;
import karnickeldev.solar.world.ServerWorld;
import karnickeldev.solar.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PacketFactory {

    public static Packet createEntityLifecyclePacket(long tick, int worldId, int[] createdEntities, int[] destroyedEntities, boolean fullSnapshot) {
        return new EntityLifecyclePacket(worldId, createdEntities, destroyedEntities, tick, fullSnapshot);
    }

    public static Packet createFullEntityLifecyclePacket(long tick, World world) {
        int count = world.getECS().getEntityManager().getAll();
        List<Integer> created = new ArrayList<>();
        List<Integer> destroyed = new ArrayList<>();
        for(int i = 0; i < count; i++) {
            if(world.getECS().getEntityManager().isValid(i)) {
                created.add(i);
            } else {
                destroyed.add(i);
            }
        }
        return createEntityLifecyclePacket(tick, world.getID(),
            created.stream().mapToInt(Integer::intValue).toArray(),
            destroyed.stream().mapToInt(Integer::intValue).toArray(),
            true);
    }

    public static Packet createWorldUpdatePacket(int worldId, long tick) {
        return new WorldUpdatePacket(worldId, tick);
    }

    public static Packet createECSUpdatePacket(long tick, int worldId, ComponentSnapshot... snapshots) {
        return new ECSUpdatePacket(worldId, tick, snapshots);
    }

    public static Packet createECSUpdatePacket(long tick, ServerWorld world) {
        List<ComponentSnapshot> snaps = world.getECS().getComponentRegistry().createAllSnapshots(tick);
        snaps.add(world.getECS().hcs.getCurrent().createSnapshot(tick));
        snaps.removeAll(Collections.singleton(null));
        if(snaps.isEmpty()) return null;
        return new ECSUpdatePacket(world.getID(), tick, snaps.toArray(ComponentSnapshot[]::new));
    }

}
