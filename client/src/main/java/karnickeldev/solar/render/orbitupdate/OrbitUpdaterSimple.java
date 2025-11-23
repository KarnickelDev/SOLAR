package karnickeldev.solar.render.orbitupdate;

import karnickeldev.solar.ecs.ClientECS;
import karnickeldev.solar.ecs.components.MassComponent;
import karnickeldev.solar.ecs.components.OrbitDataComponent;
import karnickeldev.solar.physics.Units;

/**
 * A very simple and therefor slow (single-threaded, no SIMD) Version of an OrbitUpdater for Reference
 * @apiNote VERY BAD PERFORMANCE, DON'T ACTUALLY USE
 * @author KarnickelDev
 * @since 22.11.2025
 **/
public final class OrbitUpdaterSimple implements OrbitUpdater {

    @Override
    public void waitAndSwap() {}

    @Override
    public void shutdown() {}

    @Override
    public void prepare(ClientECS ecs) {}

    @Override
    public boolean isWarmupActive() {return false;}

    @Override
    public int warmupProgress() {return 0;}

    @Override
    public int warmupTarget() {return 0;}

    private final FrameData frameData = new FrameData();

    @Override
    public FrameData getFrameData() {
        return frameData;
    }

    @Override
    public void startCompute(long time, ClientECS ecs) {
        int total = ecs.getEntityManager().getCapacityUsed();

        OrbitDataComponent orbitData = ecs.getComponentRegistry().get(OrbitDataComponent.class);
        MassComponent mass = ecs.getComponentRegistry().get(MassComponent.class);
        double simTimeSec = time / 1e6;

        int validCount = 0;
        for (int ent = orbitData.hasComponent.nextSetBit(0); ent >= 0 && ent < total; ent = orbitData.hasComponent.nextSetBit(ent + 1)) {
            if (!ecs.getEntityManager().isValid(ent)) continue;

            int parent = orbitData.getCentralBody(ent);

            double a = orbitData.getSemiMajorAxis(ent) * Units.toSU(1, Units.Length.AU);
            double e = orbitData.getEccentricity(ent);
            double mu = Units.G_KM_TON * mass.getMass(parent);
            double n = Math.sqrt(mu / (a * a * a));
            double M = n * (simTimeSec - orbitData.getT0(ent));
            double E = solveKepler(M, e);
            double theta = 2.0 * Math.atan2(Math.sqrt(1 + e) * Math.sin(E / 2), Math.sqrt(1 - e) * Math.cos(E / 2));

            double r = a * (1 - e * Math.cos(E));
            double ox = r * Math.cos(theta);
            double oy = r * Math.sin(theta);

            double omega = orbitData.getOmega(ent);
            double cosW = Math.cos(omega);
            double sinW = Math.sin(omega);

            frameData.entityIds[validCount] = ent;
            frameData.parentIds[validCount] = parent;

            frameData.posX[validCount] = cosW * ox - sinW * oy;
            frameData.posY[validCount] = sinW * ox + cosW * oy;

            validCount++;
        }
        frameData.validCount = validCount;
    }

    // physics computation helper
    private static double solveKepler(double M, double e) {
        double E = (e < 0.8) ? M : Math.PI;
        for (int i = 0; i < 5; i++) {
            double f = E - e * Math.sin(E) - M;
            double fp = 1 - e * Math.cos(E);
            double d = f / fp;
            E -= d;
            if (Math.abs(d) < 1e-5) break;
        }
        return E;
    }

}
