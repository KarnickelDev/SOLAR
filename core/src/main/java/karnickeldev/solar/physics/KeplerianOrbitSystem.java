package karnickeldev.solar.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import karnickeldev.solar.ecs.ServerECS;
import karnickeldev.solar.ecs.components.*;
import karnickeldev.solar.ecs.components.HCSPositionSnapshot;
import karnickeldev.solar.ecs.components.server.HCSServerSystem;
import karnickeldev.solar.net.network.ServerNetwork;
import karnickeldev.solar.net.packets.ECSUpdatePacket;
import karnickeldev.solar.net.packets.EntityLifecyclePacket;
import karnickeldev.solar.net.packets.Packet;
import karnickeldev.solar.net.packets.PacketFactory;
import karnickeldev.solar.world.ServerWorld;
import karnickeldev.solar.world.World;
import karnickeldev.solar.world.WorldManager;

import java.util.Arrays;
import java.util.List;

public class KeplerianOrbitSystem {

    private final WorldManager<ServerWorld> worldManager;
    private final ServerNetwork serverNetwork;
    boolean first = true;
    private long tick;

    private MassComponent massComponent;

    public KeplerianOrbitSystem(WorldManager<ServerWorld> worldManager, ServerNetwork serverNetwork) {
        this.serverNetwork = serverNetwork;

        this.worldManager = worldManager;

        for(ServerWorld world: worldManager.getWorlds()) {
            world.getECS().hcs.getNext().setDirty();
            for (Component comp : world.getECS().getComponentRegistry().getAll()) {
                if (comp instanceof DirtyFlagComponent) ((DirtyFlagComponent) comp).setDirty();
            }
        }
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

    public void updateHCS(double timeDays) {
        if(Gdx.input.isKeyPressed(Input.Keys.C)) {
            worldManager.changeWorld(2);
            worldManager.getWorld(2).getECS().hcs.getNext().setDirty();
        }
        ServerECS ecs = worldManager.getActiveWorld().getECS();
        HCSServerSystem hcs = ecs.hcs;
        OrbitDataComponent orbitData = ecs.getComponentRegistry().get(OrbitDataComponent.class);
        massComponent = ecs.getComponentRegistry().get(MassComponent.class);

        for (int entity = 0; entity < ecs.getEntityManager().getAll(); entity++) {
            if (!ecs.getEntityManager().isValid(entity) || !orbitData.has(entity)) continue;

            double a = orbitData.getSemiMajorAxis(entity) * Units.toSU(1, Units.Length.AU);

            float e = orbitData.getEccentricity(entity);
            float omega = orbitData.getOmega(entity);
            float t0 = orbitData.getT0(entity);

            int centralBodyId = orbitData.getCentralBody(entity);

            double mu = getGravitationalParameter(centralBodyId); // G * M

            double n = Math.sqrt(mu / (a * a * a));  // mean motion
            double M = (n * ((timeDays) - t0));       // mean anomaly

            double E = solveKepler((float) M, e);            // eccentric anomaly
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

        final Packet ecsUpdatePacket = PacketFactory.createECSUpdatePacket(tick, worldManager.getActiveWorld());

        if (first) {
            first = false;
            for(ServerWorld world: worldManager.getWorlds()) {
                Packet worldPacket = PacketFactory.createWorldUpdatePacket(world.getID(), tick);
                serverNetwork.broadcast(worldPacket);
            }
            for(ServerWorld world: worldManager.getWorlds()) {
                Packet lifecyclePacket = PacketFactory.createFullEntityLifecyclePacket(tick, world);
                serverNetwork.broadcast(lifecyclePacket);
            }
        }

        serverNetwork.broadcast(ecsUpdatePacket);

        tick++;
    }

    private double getGravitationalParameter(int body) {
        return Units.G_KM_TON * massComponent.getMass(body);
    }

}
