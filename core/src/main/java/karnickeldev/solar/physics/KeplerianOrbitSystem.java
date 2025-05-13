package karnickeldev.solar.physics;

import karnickeldev.solar.ecs.ServerECS;
import karnickeldev.solar.ecs.components.*;
import karnickeldev.solar.ecs.components.server.HCSPositionSnapshot;
import karnickeldev.solar.ecs.components.server.HCSServerSystem;
import karnickeldev.solar.net.network.ServerNetwork;
import karnickeldev.solar.net.packets.ECSUpdatePacket;
import karnickeldev.solar.net.packets.EntityLifecyclePacket;

import java.util.List;

public class KeplerianOrbitSystem {

    private final ServerECS ecs;
    private final OrbitDataComponent orbitData;
    private final ServerNetwork serverNetwork;
    HCSServerSystem hcs = new HCSServerSystem();
    boolean first = true;
    private long tick;


    public KeplerianOrbitSystem(ServerECS ecs, ServerNetwork serverNetwork) {
        this.serverNetwork = serverNetwork;

        this.ecs = ecs;
        this.orbitData = ecs.getComponentRegistry().get(OrbitDataComponent.class);

        hcs.getSend().setDirty();
        hcs.getCurrent().setDirty();
        hcs.getNext().setDirty();
        for (Component comp : ecs.getComponentRegistry().getAll()) {
            if (comp instanceof DirtyFlagComponent) ((DirtyFlagComponent) comp).setDirty();
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

        final HCSPositionSnapshot positionSnapshot = hcs.getCurrent().createSnapshot(tick);

        List<ComponentSnapshot> snapshots = ecs.getComponentRegistry().createAllSnapshots(tick);
        snapshots.add(positionSnapshot);

        final ECSUpdatePacket ecsUpdatePacket = new ECSUpdatePacket(tick, snapshots.toArray(new ComponentSnapshot[0]));

        if (first) {
            first = false;
            int[] createdEntities = new int[ecs.getEntityManager().getAll()];
            for (int i = 0; i < ecs.getEntityManager().getAll(); i++) {
                createdEntities[i] = i;
            }
            final EntityLifecyclePacket lifecyclePacket = new EntityLifecyclePacket(createdEntities, new int[0], tick);
            serverNetwork.broadcast(lifecyclePacket);
        }
        serverNetwork.broadcast(ecsUpdatePacket);

        tick++;
    }

    private double getGravitationalParameter(int body) {
        return Units.G_KM_TON * ecs.getComponentRegistry().get(MassComponent.class).getMass(body);
    }

}
