package karnickeldev.solar.network.packets;

import karnickeldev.solar.context.ServerContext;
import karnickeldev.solar.ecs.components.ComponentSnapshot;
import karnickeldev.solar.simulation.execution.SimulationManager;
import karnickeldev.solar.world.ServerWorld;
import karnickeldev.solar.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PacketFactory {

    public static PingPacket createPingPacket(long clientSendTime) {
        return new PingPacket(clientSendTime);
    }

    public static PingPongPacket createPingPongPacket(long clientSendTime) {
        return new PingPongPacket(clientSendTime);
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

    public static ECSUpdatePacket createECSUpdatePacket(long simTime, int worldId, float simSpeed, byte targetSimSpeedIndex, boolean paused, ComponentSnapshot... snapshots) {
        return new ECSUpdatePacket(worldId, simTime, simSpeed, targetSimSpeedIndex, paused, snapshots);
    }

    public static ECSUpdatePacket createECSUpdatePacket(long simTime, float simSpeed, byte targetSimSpeedIndex, boolean paused, ServerWorld world) {
        List<ComponentSnapshot> snaps = world.getECS().getComponentRegistry().createAllSnapshots(simTime);
        snaps.add(world.getECS().hcs.getCurrent().createSnapshot(simTime));
        snaps.removeAll(Collections.singleton(null));
        if(snaps.isEmpty()) return null;

        ECSUpdatePacket pkt = new ECSUpdatePacket(world.getID(), simTime, simSpeed, targetSimSpeedIndex, paused, snaps.toArray(ComponentSnapshot[]::new));
        return pkt;
    }

    public static ECSUpdatePacket createFullECSUpdatePacket(long simTime, float simSpeed, byte targetSimSpeedIndex, boolean paused, ServerWorld world) {
        List<ComponentSnapshot> snaps = world.getECS().getComponentRegistry().createFullSnapshot(simTime);
        snaps.add(world.getECS().hcs.getCurrent().createFullSnapshot(simTime));
        snaps.removeAll(Collections.singleton(null));
        if(snaps.isEmpty()) return null;


        return new ECSUpdatePacket(world.getID(), simTime, simSpeed, targetSimSpeedIndex, paused, snaps.toArray(ComponentSnapshot[]::new));
    }

    public static FullSnapshotPacket createFullSnapshotPacket(ServerWorld world) {
        Packet[] packets = new Packet[] {
            createWorldUpdatePacket(world.getID(), world.getWorldTime().getSimTimeMicros()),
            createFullEntityLifecyclePacket(world.getWorldTime().getSimTimeMicros(), world),
            createFullECSUpdatePacket(world.getWorldTime().getSimTimeMicros(),
                SimulationManager.simSpeedController.getCurrentSimSpeed(), SimulationManager.simSpeedController.getPresetIndex(),
                ServerContext.get().getServer().getSimulationManagerThread().getSimulationManager().paused, world)
        };
        return new FullSnapshotPacket(world.getID(), world.getWorldTime().getSimTimeMicros(), packets);
    }

}
