package karnickeldev.solar.ecs.systems;

import com.badlogic.gdx.utils.Null;
import karnickeldev.solar.ecs.ServerECS;
import karnickeldev.solar.ecs.SystemGroup;
import karnickeldev.solar.ecs.components.MassComponent;
import karnickeldev.solar.ecs.components.OrbitDataComponent;
import karnickeldev.solar.ecs.components.server.HCSServerSystem;
import karnickeldev.solar.net.network.ServerNetwork;
import karnickeldev.solar.net.packets.Packet;
import karnickeldev.solar.net.packets.PacketFactory;
import karnickeldev.solar.physics.Units;
import karnickeldev.solar.simulation.execution.SimulationManager;
import karnickeldev.solar.util.Logger;
import karnickeldev.solar.world.ServerWorld;
import karnickeldev.solar.world.World;

import java.util.Objects;

/**
 * @author : KarnickelDev
 * @since : 31.05.2025
 **/
public class KeplerianOrbitSystem<T extends World> implements ECSSystem {

    private final ServerWorld world;
    private final ServerNetwork serverNetwork;

    private boolean first = true;
    private long tick = 0;

    public KeplerianOrbitSystem(ServerWorld world) {
        this.world = world;
        this.serverNetwork = Objects.requireNonNull(world.getNetwork());
    }

    @Override
    public void update(long time) {
        ServerECS ecs = world.getECS();
        HCSServerSystem hcs = ecs.hcs;
        OrbitDataComponent orbitData = ecs.getComponentRegistry().get(OrbitDataComponent.class);
        MassComponent massComponent = ecs.getComponentRegistry().get(MassComponent.class);

        double simTimeSec = world.getWorldTime().getSimTimeMicros() / 1e6;

        for (int entity = 0; entity < ecs.getEntityManager().getAll(); entity++) {
            if (!ecs.getEntityManager().isValid(entity) || !orbitData.has(entity)) continue;

            double a = orbitData.getSemiMajorAxis(entity) * Units.toSU(1, Units.Length.AU);

            float e = orbitData.getEccentricity(entity);
            float omega = orbitData.getOmega(entity);
            float t0 = orbitData.getT0(entity);

            int centralBodyId = orbitData.getCentralBody(entity);

            double mu = Units.G_KM_TON * massComponent.getMass(centralBodyId); // G * M

            double n = Math.sqrt(mu / (a * a * a));     // mean motion
            double M = (n * ((simTimeSec) - t0));       // mean anomaly

            double E = solveKepler((float) M, e);       // eccentric anomaly
            double theta = 2 * Math.atan2(
                Math.sqrt(1 + e) * Math.sin(E / 2),
                Math.sqrt(1 - e) * Math.cos(E / 2)
            );

            double r = a * (1 - e * Math.cos(E));

            double orbitX = r * Math.cos(theta);
            double orbitY = r * Math.sin(theta);

            // Rotate by omega
            double cosW = Math.cos(omega);
            double sinW = Math.sin(omega);

            double rotatedX = cosW * orbitX - sinW * orbitY;
            double rotatedY = sinW * orbitX + cosW * orbitY;

            hcs.add(entity, centralBodyId, rotatedX, rotatedY);
        }
        hcs.swapBuffers();

        final Packet ecsUpdatePacket = PacketFactory.createECSUpdatePacket(world.getWorldTime().getSimTimeMicros(), SimulationManager.simSpeed, world);
        if (first) {
            first = false;

            Packet worldPacket = PacketFactory.createWorldUpdatePacket(world.getID(), world.getWorldTime().getSimTimeMicros());
            serverNetwork.broadcast(worldPacket);

            Packet lifecyclePacket = PacketFactory.createFullEntityLifecyclePacket(world.getWorldTime().getSimTimeMicros(), world);
            serverNetwork.broadcast(lifecyclePacket);
        }
        if(ecsUpdatePacket != null && time != 0) serverNetwork.broadcast(ecsUpdatePacket);
    }


    @Override
    public byte priority() {
        return 0;
    }

    @Override
    public SystemGroup getGroup() {
        return SystemGroup.UPDATE;
    }

    private static float solveKepler(float M, float e) {
        float E = M;
        float epsilon = 1e-5f;
        for (int i = 0; i < 5; i++) {
            float f = E - e * (float) Math.sin(E) - M;
            float fPrime = 1 - e * (float) Math.cos(E);
            float delta = f / fPrime;
            E -= delta;
            if (Math.abs(delta) < epsilon) break;
        }
        return E;
    }

}
