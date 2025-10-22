package karnickeldev.solar.render.orbitupdate;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.ecs.components.MassComponent;
import karnickeldev.solar.ecs.components.OrbitDataComponent;
import karnickeldev.solar.physics.Units;
import karnickeldev.solar.util.spinbarrier.SyncBarrier;

/**
 * @author : KarnickelDev
 * @since : 21.10.2025
 **/
public class OrbitWorker implements Runnable {

    private static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;
    private static final int CHUNK_SIZE;

    static {
        int vectorLen = SPECIES.length();
        int perEntityBytes = 64; // estimate hot arrays
        int cacheSize = 256 * 1024; // estimated cache size (either 32 for L1 or 256 for L2 seem to work well)
        CHUNK_SIZE = Math.max(vectorLen, ((cacheSize / perEntityBytes) / vectorLen) * vectorLen); // round to vector multiple
    }

    // for (per-frame) sync with OrbitUpdater
    private final OrbitUpdater parent;
    private final SyncBarrier barrier;

    // local copy of worker-count
    private final int workerCount;

    // per-worker temporaries (kept per-thread to avoid sharing)
    private final double[] tmpE = new double[SPECIES.length()];
    private final double[] tmpOmega = new double[SPECIES.length()];

    private final int chunk_size = CHUNK_SIZE;
    private final double[] aArr = new double[chunk_size];
    private final double[] muArr = new double[chunk_size];
    private final double[] EArr = new double[chunk_size];
    private final double[] thetaArr = new double[chunk_size];

    // per-worker output buffers allocated inside run (to ensure first-touch on pinned core)
    double[] localOutPosX;
    double[] localOutPosY;

    // assigned range for this frame (set by main thread)
    volatile int assignedStart = 0;
    volatile int assignedEnd = 0;

    public final int id;

    OrbitWorker(int id, OrbitUpdater parent, SyncBarrier barrier) {
        this.id = id;
        this.parent = parent;
        this.barrier = barrier;
        this.workerCount = parent.getWorkerCount();

        // do NOT allocate large localOut buffers here — allocate inside run() after pinning
    }

    @Override
    public void run() {
        // Allocate per-worker outputs after the thread was pinned (ensures first-touch happens on pinned core).
        int perWorkerCap = (EntityManager.MAX_ENTITIES + workerCount - 1) / workerCount;
        localOutPosX = new double[perWorkerCap];
        localOutPosY = new double[perWorkerCap];

        // force page-touching to bind pages to this thread's core.
        // write a value every 256 doubles (~4KB pages * safety=2) to ensure pages are touched.
        for (int i = 0; i < perWorkerCap; i += 256) {
            localOutPosX[i] = 0.0;
            localOutPosY[i] = 0.0;
        }

        while (parent.isRunning()) {
            // Wait for main thread to start the frame (startCompute calls barrier.await())
            barrier.await();

            if (!parent.isRunning()) break;

            int start = assignedStart;
            int end = assignedEnd;
            for (int chunkStart = start; chunkStart < end; chunkStart += chunk_size) {
                int chunkEnd = Math.min(chunkStart + chunk_size, end);
                computeRangeIntoLocal(chunkStart, chunkEnd, parent.getOrbitFrameCtx(),
                    tmpE, tmpOmega, aArr, muArr, EArr, thetaArr, localOutPosX, localOutPosY);
            }

            // signal done
            barrier.await();
        }
    }

    // computeRange variant that writes into per-worker localOut arrays at offset
    private void computeRangeIntoLocal(int low, int high, OrbitFrameContext ctx, double[] tmpE, double[] tmpOmega,
                                       double[] aArr, double[] muArr, double[] EArr, double[] thetaArr,
                                       double[] outX, double[] outY) {

        OrbitDataComponent orbitDataRef = ctx.ecs().getComponentRegistry().get(OrbitDataComponent.class);
        MassComponent massComponent = ctx.ecs().getComponentRegistry().get(MassComponent.class);

        OrbitUpdater.FrameData nextFrameData = ctx.nextFrameData();

        double simTimeSec = ctx.simTimeSec();
        double[] eArr = orbitDataRef.eccentricity;
        double[] t0Arr = orbitDataRef.t0;
        double[] omegaArr = orbitDataRef.omega;

        int localOffset = low - assignedStart;

        // Step: fill aArr and muArr for this subrange
        for (int i = low; i < high; i++) {
            int local = i - low;
            aArr[local] = orbitDataRef.getSemiMajorAxis(i) * Units.toSU(1, Units.Length.AU);
            muArr[local] = Units.G_KM_TON * massComponent.getMass(nextFrameData.parentIds[i]);
        }

        // Step 1 — scalar Kepler solve
        for (int i = low; i < high; i++) {
            int local = i - low;
            int ent = nextFrameData.entityIds[i];
            double a = aArr[local];
            double e = eArr[ent];
            double mu = muArr[local];
            double n = Math.sqrt(mu / (a * a * a));
            double M = n * (simTimeSec - t0Arr[ent]);
            double E = solveKepler(M, e);
            double theta = 2.0 * Math.atan2(Math.sqrt(1 + e) * Math.sin(E / 2), Math.sqrt(1 - e) * Math.cos(E / 2));
            EArr[local] = E;
            thetaArr[local] = theta;
        }

        // Step 2 — SIMD orbit position: vectorized portion
        int i = low;
        int loopBound = low + SPECIES.loopBound(high - low);
        for (; i < loopBound; i += SPECIES.length()) {
            int localBase = i - low;
            DoubleVector vA = DoubleVector.fromArray(SPECIES, aArr, localBase);
            DoubleVector vEcc = DoubleVector.fromArray(SPECIES, EArr, localBase);
            DoubleVector vTheta = DoubleVector.fromArray(SPECIES, thetaArr, localBase);

            // gather per-lane e and omega from global arrays using entityIds
            for (int lane = 0; lane < SPECIES.length(); lane++) {
                int entIdx = nextFrameData.entityIds[i + lane];
                tmpE[lane] = eArr[entIdx];
                tmpOmega[lane] = omegaArr[entIdx];
            }
            DoubleVector vE = DoubleVector.fromArray(SPECIES, tmpE, 0);
            DoubleVector vOmega = DoubleVector.fromArray(SPECIES, tmpOmega, 0);
            DoubleVector one = DoubleVector.broadcast(SPECIES, 1.0);
            DoubleVector vCosE = vEcc.lanewise(VectorOperators.COS);
            DoubleVector vR = vA.mul(one.sub(vE.mul(vCosE)));

            DoubleVector vCosTheta = vTheta.lanewise(VectorOperators.COS);
            DoubleVector vSinTheta = vTheta.lanewise(VectorOperators.SIN);
            DoubleVector vX = vR.mul(vCosTheta);
            DoubleVector vY = vR.mul(vSinTheta);
            DoubleVector vCosW = vOmega.lanewise(VectorOperators.COS);
            DoubleVector vSinW = vOmega.lanewise(VectorOperators.SIN);
            DoubleVector vRotX = vCosW.mul(vX).sub(vSinW.mul(vY));
            DoubleVector vRotY = vSinW.mul(vX).add(vCosW.mul(vY));

            // write into local output at offset
            vRotX.intoArray(outX, localOffset + localBase);
            vRotY.intoArray(outY, localOffset + localBase);
        }

        // Tail
        for (; i < high; i++) {
            int local = i - low;
            int ent = nextFrameData.entityIds[i];
            double a = aArr[local];
            double e = eArr[ent];
            double omega = omegaArr[ent];
            double E = EArr[local];
            double theta = thetaArr[local];
            double r = a * (1 - e * Math.cos(E));
            double ox = r * Math.cos(theta);
            double oy = r * Math.sin(theta);
            double cosW = Math.cos(omega);
            double sinW = Math.sin(omega);
            outX[localOffset + local] = cosW * ox - sinW * oy;
            outY[localOffset + local] = sinW * ox + cosW * oy;
        }
    }

    // physics computation helper
    private static double solveKepler(double M, double e) {
        double E = M + e * Math.sin(M);
        for (int i = 0; i < 6; i++) {
            double f = E - e * Math.sin(E) - M;
            double fp = 1 - e * Math.cos(E);
            double d = f / fp;
            E -= d;
            if (Math.abs(d) < 1e-6) break;
        }
        return E;
    }

}
