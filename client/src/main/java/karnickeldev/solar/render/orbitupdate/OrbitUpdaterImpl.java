package karnickeldev.solar.render.orbitupdate;

import karnickeldev.solar.ecs.ClientECS;
import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.ecs.components.OrbitDataComponent;
import karnickeldev.solar.util.Logger;
import karnickeldev.solar.util.spinbarrier.PhaserBarrier;
import karnickeldev.solar.util.spinbarrier.SyncBarrier;
import karnickeldev.solar.util.threadlayout.ThreadAffinity;
import karnickeldev.solar.util.threadlayout.ClientThreadLayout;
import karnickeldev.solar.util.threadlayout.ThreadContext;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fast OrbitUpdater Implementation using Multi-Threading and SIMD
 * on interleaved chunks of prepared dense data for cache locality
 * @apiNote DEFAULT IMPLEMENTATION
 * @see karnickeldev.solar.render.orbitupdate.OrbitWorker
 * @author KarnickelDev
 * @since 16.10.2025
 **/
public final class OrbitUpdaterImpl implements OrbitUpdater {

    // frame data
    private volatile OrbitFrameContext orbitFrameCtx;

    // frame data (double buffering)
    private final FrameData frameDataA = new FrameData();
    private final FrameData frameDataB = new FrameData();
    public FrameData currFrameData = frameDataA;
    public FrameData nextFrameData = frameDataB;

    // thread coordination
    private final SyncBarrier barrier;
    private volatile boolean running = true;
    private final int workerCount;

    private final Thread[] workerThreads;

    // Async warmup
    private final AtomicInteger warmupCursor = new AtomicInteger(0);
    private volatile boolean warmupActive = false;

    private final OrbitSoA orbitSoA = new OrbitSoA(EntityManager.MAX_ENTITIES);

    public OrbitUpdaterImpl(ClientThreadLayout threadLayout) {
        ThreadContext context = threadLayout.getOrbitWorkerContext();
        this.workerCount = context.getThreadCount();
        this.barrier = new PhaserBarrier(workerCount + 1); // +1 for main thread

        workerThreads = new Thread[workerCount];

        // Start worker threads once and keep references
        for (int id = 0; id < workerCount; id++) {
            OrbitWorker w = new OrbitWorker(id, this, barrier);

            String name = "OrbitWorker-" + id;
            workerThreads[id] = Thread.ofPlatform().name(name).priority(Thread.MAX_PRIORITY-1)
                .unstarted(() -> {
                    Logger.log(Logger.STARTUP, "Starting " + name);
                    // Pin the OS thread to chosen core
                    if(context.useCoreAffinity()) ThreadAffinity.pinToCore(context.nextCpuId());
                    w.run();
                });
        }

    }

    public int getWorkerCount() {
        return workerCount;
    }

    public boolean isRunning() {
        return running;
    }

    OrbitFrameContext getOrbitFrameCtx() {
        return orbitFrameCtx;
    }

    public FrameData getFrameData() {
        return currFrameData;
    }

    OrbitSoA getOrbitSoA() {
        return orbitSoA;
    }

    /** Holds up-to-date data needed for computations every frame */
    public static final class OrbitSoA {
        public int count;

        public int[] entityIds;
        public double[] a;
        public double[] mu;
        public double[] e;
        public double[] t0;
        public double[] omega;

        public OrbitSoA(int capacity) {
            entityIds = new int[capacity];
            a = new double[capacity];
            mu = new double[capacity];
            e = new double[capacity];
            t0 = new double[capacity];
            omega = new double[capacity];
        }

    }

    private void swapFrameData() {
        FrameData tmp = currFrameData;
        currFrameData = nextFrameData;
        nextFrameData = tmp;
    }

    public void prepare(ClientECS ecs) {
        OrbitDataComponent orbitData = ecs.getComponentRegistry().get(OrbitDataComponent.class);
        int total = ecs.getEntityManager().getCapacityUsed();

        int validCount = 0;
        for (int ent = orbitData.hasComponent.nextSetBit(0); ent >= 0 && ent < total; ent = orbitData.hasComponent.nextSetBit(ent + 1)) {
            if (!ecs.getEntityManager().isValid(ent)) continue;
            nextFrameData.entityIds[validCount] = ent;
            nextFrameData.parentIds[validCount] = orbitData.getCentralBody(ent);
            validCount++;
        }
        nextFrameData.validCount = validCount;

        orbitFrameCtx = new OrbitFrameContext(ecs, 0.0, nextFrameData);

        // Start warmup
        warmupCursor.set(0);
        warmupActive = true;

        for(Thread t: workerThreads) t.start();
    }

    // request a slice to warmup/pretouch
    public int requestWarmupSlice(int sliceSize) {
        if(!warmupActive) return -1;
        int start = warmupCursor.getAndAdd(sliceSize);
        if(start >= nextFrameData.validCount) {
            warmupActive = false;
            return -1;
        }
        return start;
    }

    public void startCompute(long time, ClientECS ecs) {
        // Preload ECS data references
        OrbitDataComponent orbitDataRef = ecs.getComponentRegistry().get(OrbitDataComponent.class);

        double simTimeSec = time / 1e6;

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
        orbitSoA.count = validCount;

        // set orbit frame context for workers
        this.orbitFrameCtx = new OrbitFrameContext(ecs, simTimeSec, nextFrameData);

        // Start workers for this frame
        // workers will wake, compute into their local buffers, then await again (to signal done)
        barrier.await();

        // main thread may proceed to render while workers compute
    }

    public void waitAndSwap() {
        // wait for workers to finish computing localOut buffers
        barrier.await();

        // swap buffer for next frame
        swapFrameData();
    }

    public void shutdown() {
        running = false;
        // wake workers so they can break out
        barrier.await();
    }

    public boolean isWarmupActive() {
        return warmupActive;
    }

    public int warmupProgress() {
        return warmupCursor.get();
    }

    public int warmupTarget() {
        return nextFrameData.validCount;
    }

}
