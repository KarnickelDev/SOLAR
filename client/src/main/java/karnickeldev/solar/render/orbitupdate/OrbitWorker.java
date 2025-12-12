package karnickeldev.solar.render.orbitupdate;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.ecs.components.MassComponent;
import karnickeldev.solar.ecs.components.OrbitDataComponent;
import karnickeldev.solar.physics.Units;
import karnickeldev.solar.util.Logger;
import karnickeldev.solar.util.spinbarrier.SyncBarrier;

/**
 * A worker runnable for multithreading that computes some chunks of entity orbits
 * using SIMD and dense data for cache locality
 * @see karnickeldev.solar.render.orbitupdate.OrbitUpdaterImpl
 * @author KarnickelDev
 * @since 21.10.2025
 **/
public final class OrbitWorker implements Runnable {

    private static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;
    private static final int CHUNK_SIZE;

    static {
        int vectorLen = SPECIES.length();
        int perEntityBytes = 128; // estimate bytes-per-entity in hot arrays
        int cacheSize = 256 * 1024; // estimated cache size (either 32 for L1 or 256 for L2 seem to work well)
        CHUNK_SIZE = Math.max(vectorLen, ((cacheSize / perEntityBytes) / vectorLen) * vectorLen); // round to vector multiple
        Logger.log("[OrbitWorker] ", "OrbitWorker using chunks of size: " + CHUNK_SIZE);
    }

    // for (per-frame) sync with OrbitUpdater
    private final OrbitUpdaterImpl parent;
    private final SyncBarrier barrier;

    // local scratch for E and theta (per-worker)
    private final double[] EArr = new double[CHUNK_SIZE];
    private final double[] thetaArr = new double[CHUNK_SIZE];

    // local copy of worker-count
    private final int workerCount;

    private final int chunk_size = CHUNK_SIZE;

    public final int id;

    OrbitWorker(int id, OrbitUpdaterImpl parent, SyncBarrier barrier) {
        this.id = id;
        this.parent = parent;
        this.barrier = barrier;
        this.workerCount = parent.getWorkerCount();
    }

    @Override
    public void run() {

        /*
        Interleaved chunk sections for automatic work balancing with static slices.
        Thread1[0-100, 300-400, ...]; Thread2[100-200, 400-500, ...]
        DON'T FORGET THE +1, WE NEED IT TO CAPTURE ALL ENTITIES
        */
        int[] chunks = new int[(EntityManager.MAX_ENTITIES / (workerCount * chunk_size)) + 1];
        for(int i = 0; i < chunks.length; i++) {
            chunks[i] = chunk_size * ((workerCount*i) + id);
        }

        while(parent.isRunning() && !barrier.isTerminated()) {

            // async warmup/pretouch
            while(parent.isWarmupActive()) {
                int start = parent.requestWarmupSlice(chunk_size);
                if (start == -1) break;
                int end = Math.min(start + chunk_size, parent.warmupTarget());

                OrbitUpdaterImpl.FrameData fd = parent.nextFrameData;
                // hopefully touch pages on this cpu core so they don't cause page faults
                for (int i = start; i < end; i++) {
                    fd.posX[i] = 0;
                    fd.posY[i] = 0;
                    fd.entityIds[i] = 0;
                    fd.parentIds[i] = 0;
                }
            }

            int validCount = parent.nextFrameData.validCount;

            if(parent.getOrbitFrameCtx() != null) {
                OrbitDataComponent orbitDataRef = parent.getOrbitFrameCtx().ecs().getComponentRegistry().get(OrbitDataComponent.class);
                MassComponent mass = parent.getOrbitFrameCtx().ecs().getComponentRegistry().get(MassComponent.class);
                OrbitUpdaterImpl.OrbitSoA orbitSoA = parent.getOrbitSoA();

                // TODO: only recalculate on changes
                for (int chunk : chunks) {
                    for (int i = chunk; i < Math.min(chunk + chunk_size, validCount); i++) {
                        int ent = parent.nextFrameData.entityIds[i];
                        int p = parent.nextFrameData.parentIds[i];
                        orbitSoA.entityIds[i] = ent;
                        orbitSoA.a[i] = orbitDataRef.getSemiMajorAxis(ent) * Units.toSU(1, Units.Length.AU);
                        orbitSoA.mu[i] = Units.G_KM_TON * mass.getMass(p);
                        orbitSoA.e[i] = orbitDataRef.eccentricity[ent];
                        orbitSoA.t0[i] = orbitDataRef.t0[ent];
                        orbitSoA.omega[i] = orbitDataRef.omega[ent];
                    }
                }
            }

            // Wait for main thread to start the frame (startCompute calls barrier.await())
            barrier.await();

            // if shutdown was requested or barrier was force-terminated, exit loop BEFORE computing.
            if(!parent.isRunning() || barrier.isTerminated()) break;

            OrbitUpdaterImpl.OrbitSoA soa = parent.getOrbitSoA(); // hot buffer

            // iterate interleaved chunks (indices are into SoA arrays)
            for (int chunkStart : chunks) {
                if (chunkStart >= validCount) break;
                int chunkEnd = Math.min(chunkStart + chunk_size, validCount);

                // if shutdown occurred mid-loop, break out quickly
                if (!parent.isRunning() || barrier.isTerminated()) break;

                computeRangeSoA(soa, chunkStart, chunkEnd);
            }

            // signal done
            barrier.await();
        }

        Logger.log("[OrbitWorker] ", "Stopped OrbitWorker-" + id);
    }

    private void computeRangeSoA(OrbitUpdaterImpl.OrbitSoA soa, int low, int high) {
        int count = high - low;
        int vecLen = SPECIES.length();

        double simTimeSec = parent.getOrbitFrameCtx().simTimeSec();

        // Step 1: scalar Kepler solve (we compute E and theta into local arrays)
        for (int i = 0; i < count; i++) {
            int idx = low + i;
            double a = soa.a[idx];
            double e = soa.e[idx];
            double mu = soa.mu[idx];
            double n = Math.sqrt(mu / (a * a * a));
            double M = n * (simTimeSec - soa.t0[idx]);
            double E = solveKepler(M, e);
            double theta = 2.0 * Math.atan2(Math.sqrt(1 + e) * Math.sin(E / 2), Math.sqrt(1 - e) * Math.cos(E / 2));
            EArr[i] = E;
            thetaArr[i] = theta;
        }

        // Step 2: SIMD vectorized position compute using linear SoA inputs
        int loopBound = SPECIES.loopBound(count);
        int local = 0;
        for (; local < loopBound; local += vecLen) {
            int base = low + local; // dense base index into SoA arrays

            DoubleVector vA = DoubleVector.fromArray(SPECIES, soa.a, base);
            DoubleVector vEcc = DoubleVector.fromArray(SPECIES, EArr, local);
            DoubleVector vTheta = DoubleVector.fromArray(SPECIES, thetaArr, local);


            // NOTE: soa.e and soa.omega are linear, so we can load them directly
            DoubleVector vE = DoubleVector.fromArray(SPECIES, soa.e, base);
            DoubleVector vOmega = DoubleVector.fromArray(SPECIES, soa.omega, base);


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


            vRotX.intoArray(parent.nextFrameData.posX, base);
            vRotY.intoArray(parent.nextFrameData.posY, base);

        }

        // Tail scalar (remainder of entities that don't fill another SIMD lane)
        for (; local < count; local++) {
            int idx = low + local;

            double a = soa.a[idx];
            double e = soa.e[idx];
            double omega = soa.omega[idx];
            double E = EArr[local];
            double theta = thetaArr[local];

            double r = a * (1 - e * Math.cos(E));
            double ox = r * Math.cos(theta);
            double oy = r * Math.sin(theta);
            double cosW = Math.cos(omega);
            double sinW = Math.sin(omega);

            parent.nextFrameData.posX[idx] = cosW * ox - sinW * oy;
            parent.nextFrameData.posY[idx] = sinW * ox + cosW * oy;

        }
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
