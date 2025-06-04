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

    public static PingPacket createPingPacket(long clientSendTime) {
        return new PingPacket(clientSendTime);
    }

    public static PingPongPacket createPingPongPacket(long clientSendTime, long serverReceiveTime) {
        return new PingPongPacket(clientSendTime, serverReceiveTime);
    }

    public static EntityLifecyclePacket createEntityLifecyclePacket(long simTime, int worldId, int[] createdEntities, int[] destroyedEntities, boolean fullSnapshot) {
        return new EntityLifecyclePacket(worldId, createdEntities, destroyedEntities, simTime, fullSnapshot);
    }

    public static EntityLifecyclePacket createFullEntityLifecyclePacket(long simTime, World world) {
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
        return createEntityLifecyclePacket(simTime, world.getID(),
            created.stream().mapToInt(Integer::intValue).toArray(),
            destroyed.stream().mapToInt(Integer::intValue).toArray(),
            true);
    }

    public static WorldUpdatePacket createWorldUpdatePacket(int worldId, long simTime) {
        return new WorldUpdatePacket(worldId, simTime);
    }

    public static ECSUpdatePacket createECSUpdatePacket(long simTime, int worldId, float simSpeed, ComponentSnapshot... snapshots) {
        return new ECSUpdatePacket(worldId, simTime, simSpeed, snapshots);
    }

    public static ECSUpdatePacket createECSUpdatePacket(long simTime, float simSpeed, ServerWorld world) {
        List<ComponentSnapshot> snaps = world.getECS().getComponentRegistry().createAllSnapshots(simTime);
        snaps.add(world.getECS().hcs.getCurrent().createSnapshot(simTime));
        snaps.removeAll(Collections.singleton(null));
        if(snaps.isEmpty()) return null;
        return new ECSUpdatePacket(world.getID(), simTime, simSpeed, snaps.toArray(ComponentSnapshot[]::new));
    }

}
