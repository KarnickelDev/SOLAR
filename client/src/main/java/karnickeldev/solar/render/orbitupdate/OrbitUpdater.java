package karnickeldev.solar.render.orbitupdate;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
import karnickeldev.solar.ecs.ClientECS;
import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.ecs.components.HCSPositionComponent;
import karnickeldev.solar.ecs.components.MassComponent;
import karnickeldev.solar.ecs.components.OrbitDataComponent;
import karnickeldev.solar.physics.Units;
import karnickeldev.solar.util.spinbarrier.PhaserBarrier;
import karnickeldev.solar.util.ThreadAffinity;
import karnickeldev.solar.util.spinbarrier.SyncBarrier;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : KarnickelDev
 * @since : 16.10.2025
 **/
public class OrbitUpdater {

    private static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;

    // frame data
    private volatile double simTimeSec;
    private double[] eArrRef, omegaArrRef, t0ArrRef;
    private ClientECS ecsRef;

    // thread coordination
    private final int workerCount;
    private final SyncBarrier barrier;
    private volatile boolean running = true;

    // frame data (double buffering)
    private final FrameData frameDataA = new FrameData();
    private final FrameData frameDataB = new FrameData();
    public FrameData currFrameData = frameDataA;
    private FrameData nextFrameData = frameDataB;

    // worker helpers
    private final Worker[] workers;
    private final AtomicInteger nextIndex = new AtomicInteger(0); // kept but not used for primary scheduling

    private volatile int frameValidEntityCount = 0;

    public OrbitUpdater() {
        this.workerCount = 3;
        this.barrier = new PhaserBarrier(workerCount + 1); // +1 for main thread
        this.workers = new Worker[this.workerCount];

        // Start worker threads once and keep references
        for (int i = 0; i < workerCount; i++) {
            final int id = i;
            Worker w = new Worker(id);
            workers[i] = w;

            Thread t = Thread.ofPlatform().name("OrbitWorker-" + id).priority(Thread.MAX_PRIORITY)
                .start(() -> {
                    // Pin the OS thread to chosen core before doing any touching of worker-local buffers.
                    // keep your affinity choice (adjust core numbers as appropriate)
                    ThreadAffinity.pinToCore(6 + 2*id);
                    w.run();
                });

            try {
                Thread.sleep(2); // small stagger to help determinism
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        workers[0].assignedStart = 0;
        workers[0].assignedEnd = 33000;
        workers[1].assignedStart = 33000;
        workers[1].assignedEnd = 66000;
        workers[2].assignedStart = 66000;
        workers[2].assignedEnd = 100_100;
    }

    public int getWorkerCount() {
        return workerCount;
    }

    public boolean isRunning() {
        return running;
    }

    // frame data inner class
    public static final class FrameData {
        public final double[] posX;
        public final double[] posY;

        private final int[] entityIds;
        private final int[] parentIds;

        private int validCount;

        private FrameData() {
            posX = new double[EntityManager.MAX_ENTITIES];
            posY = new double[EntityManager.MAX_ENTITIES];
            entityIds = new int[EntityManager.MAX_ENTITIES];
            parentIds = new int[EntityManager.MAX_ENTITIES];
        }
    }

    private void swapFrameData() {
        FrameData tmp = currFrameData;
        currFrameData = nextFrameData;
        nextFrameData = tmp;
    }

    public void copyToHCS() {
        HCSPositionComponent current = ecsRef.hcs.getCurrent();
        for (int e = 0; e < currFrameData.validCount; e++) {
            current.add(
                currFrameData.entityIds[e],
                currFrameData.parentIds[e],
                currFrameData.posX[e], currFrameData.posY[e]
            );
        }
    }

    // Worker inner class
    private class Worker implements Runnable {
        private static final int chunkSize;

        static {
            int vectorLen = SPECIES.length();
            int perEntityBytes = 64; // estimate hot arrays
            int cacheSize = 32 * 1024; // estimated cache size (either 32 for L1 or 256 for L2 seems to work well)
            chunkSize = 2048;
        }

        // per-worker temporaries (kept per-thread to avoid sharing)
        private final double[] tmpE = new double[SPECIES.length()];
        private final double[] tmpOmega = new double[SPECIES.length()];

        private final double[] aArr = new double[chunkSize];
        private final double[] muArr = new double[chunkSize];
        private final double[] EArr = new double[chunkSize];
        private final double[] thetaArr = new double[chunkSize];

        // per-workeroutput buffers allocated inside run (to ensure first-touch on pinned core)
        private double[] localOutPosX;
        private double[] localOutPosY;

        // assigned range for this frame (set by main thread)
        public volatile int assignedStart = 0;
        public volatile int assignedEnd = 0;

        public final int id;

        private Worker(int id) {
            this.id = id;
            // do NOT allocate large localOut buffers here — allocate inside run() after pinning
        }

        @Override
        public void run() {
            // Allocate per-worker outputs after the thread was pinned (ensures first-touch happens on pinned core).
            int perWorkerCap = (EntityManager.MAX_ENTITIES + workerCount) / workerCount;
            localOutPosX = new double[perWorkerCap];
            localOutPosY = new double[perWorkerCap];

            // Force page-touching to bind pages to this thread's core/CCX.
            // Write a value every 512 doubles (~4KB pages * safety) to ensure pages are touched.
            for (int i = 0; i < perWorkerCap; i += 512) {
                localOutPosX[i] = 0.0;
                localOutPosY[i] = 0.0;
            }

            while (running) {
                // Wait for main thread to start the frame (startCompute calls barrier.await())
                barrier.await();

                if (!running) break;

                int start = assignedStart;
                int end   = assignedEnd;
                for (int chunkStart = start; chunkStart < end; chunkStart += chunkSize) {
                    int chunkEnd = Math.min(chunkStart + chunkSize, end);
                    computeRangeIntoLocal(chunkStart, chunkEnd, simTimeSec, eArrRef, t0ArrRef, omegaArrRef,
                        tmpE, tmpOmega, aArr, muArr, EArr, thetaArr, localOutPosX, localOutPosY);
                }

                // signal done
                barrier.await();
            }
        }

        // computeRange variant that writes into per-worker localOut arrays at offset 0..(end-start)
        private void computeRangeIntoLocal(int low, int high, double simTimeSec, double[] eArr, double[] t0Arr,
                                           double[] omegaArr, double[] tmpE, double[] tmpOmega,
                                           double[] aArr, double[] muArr, double[] EArr, double[] thetaArr,
                                           double[] outX, double[] outY) {

            OrbitDataComponent orbitDataRef = ecsRef.getComponentRegistry().get(OrbitDataComponent.class);
            MassComponent massComponent = ecsRef.getComponentRegistry().get(MassComponent.class);

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
                double theta = 2.0 * Math.atan2(Math.sqrt(1 + e) * Math.sin(E / 2),
                    Math.sqrt(1 - e) * Math.cos(E / 2));
                EArr[local] = E;
                thetaArr[local] = theta;
            }

            // Step 2 — SIMD orbit position: vectorized portion
            int i = low;
            int loopBound = SPECIES.loopBound(high);
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

                // write into local output at offset localBase
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
    }

    public void startCompute(long time, ClientECS ecs) {
        // Preload ECS data references
        ecsRef = ecs;
        OrbitDataComponent orbitDataRef = ecs.getComponentRegistry().get(OrbitDataComponent.class);

        eArrRef = orbitDataRef.eccentricity;
        omegaArrRef = orbitDataRef.omega;
        t0ArrRef = orbitDataRef.t0;
        simTimeSec = time / 1e6;

        int validCount = 0;
        int total = ecs.getEntityManager().getCapacityUsed();

        for (int ent = orbitDataRef.hasComponent.nextSetBit(0); ent >= 0 && ent < total; ent = orbitDataRef.hasComponent.nextSetBit(ent + 1)) {
            if (!ecs.getEntityManager().isValid(ent)) continue;

            nextFrameData.entityIds[validCount] = ent;
            int centralBodyId = orbitDataRef.getCentralBody(ent);
            nextFrameData.parentIds[validCount] = centralBodyId;
            validCount++;
        }

        nextFrameData.validCount = validCount;
        frameValidEntityCount = validCount;

        // Deterministic per-worker contiguous ranges for this frame
        int totalValid = validCount;
        int per = (totalValid + workerCount - 1) / workerCount;

        // reset any dynamic index (kept for compatibility)
        nextIndex.set(0);

        // Start workers for this frame
        barrier.await(); // workers will wake, compute into their local buffers, then await again (to signal done)

        // main thread may proceed to render while workers compute
    }

    public void waitAndSwap() {
        // wait for workers to finish computing localOut buffers
        barrier.await();

        // deterministic merge/copy worker local outputs into nextFrameData.posX/posY
        for (int w = 0; w < workerCount; w++) {
            int s = workers[w].assignedStart;
            int e = workers[w].assignedEnd;
            if (e <= s) continue;
            int len = e - s;
            // copy from worker local buffer at offset 0..len into nextFrameData arrays at s..s+len-1
            System.arraycopy(workers[w].localOutPosX, 0, nextFrameData.posX, s, len);
            System.arraycopy(workers[w].localOutPosY, 0, nextFrameData.posY, s, len);
            // copy entityId and parentId already prepared in startCompute
        }

        // swap buffer for next frame
        swapFrameData();
    }

    public void shutdown() {
        running = false;
        // wake workers so they can break out
        barrier.await();
    }

    // physics computation helper reused by Workers (kept for completeness)
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
