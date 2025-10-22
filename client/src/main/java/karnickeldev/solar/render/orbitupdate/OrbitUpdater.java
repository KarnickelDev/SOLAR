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

/**
 * @author : KarnickelDev
 * @since : 16.10.2025
 **/
public class OrbitUpdater {

    // frame data
    private ClientECS ecsRef;
    private volatile OrbitFrameContext orbitFrameCtx;

    // frame data (double buffering)
    private final FrameData frameDataA = new FrameData();
    private final FrameData frameDataB = new FrameData();
    public FrameData currFrameData = frameDataA;
    private FrameData nextFrameData = frameDataB;

    // thread coordination
    private final SyncBarrier barrier;
    private volatile boolean running = true;
    private final int workerCount;


    // worker helpers
    private final OrbitWorker[] workers;

    public OrbitUpdater() {
        this.workerCount = 4;
        this.barrier = new PhaserBarrier(workerCount + 1); // +1 for main thread
        this.workers = new OrbitWorker[this.workerCount];

        // Start worker threads once and keep references

        for (int i = 0; i < workerCount; i++) {
            final int id = i;
            OrbitWorker w = new OrbitWorker(id, this, barrier);
            workers[i] = w;

            Thread t = Thread.ofPlatform().name("OrbitWorker-" + id).priority(Thread.MAX_PRIORITY)
                .start(() -> {
                    // Pin the OS thread to chosen core before doing any touching of worker-local buffers.
                    // keep your affinity choice (adjust core numbers as appropriate)
                    ThreadAffinity.pinToCore(6 + id);
                    w.run();
                });

            try {
                Thread.sleep(2); // small stagger to help determinism
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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

    // frame data inner class
    public static final class FrameData {
        public final double[] posX;
        public final double[] posY;

        final int[] entityIds;
        final int[] parentIds;

        public int validCount;

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

    public void startCompute(long time, ClientECS ecs) {
        // Preload ECS data references
        ecsRef = ecs;
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
        this.orbitFrameCtx = new OrbitFrameContext(ecs, simTimeSec, nextFrameData);

        int totalValid = validCount;           // number of entities to process this frame
        int base = totalValid / workerCount;
        int rem  = totalValid % workerCount;
        int offset = 0;
        for (int w = 0; w < workerCount; w++) {
            int extra = (w < rem) ? 1 : 0;     // distribute remainder to first 'rem' workers
            int s = offset;
            int e = s + base + extra;         // e is exclusive
            workers[w].assignedStart = s;
            workers[w].assignedEnd   = e;     // exclusive
            offset = e;
        }

        // Start workers for this frame
        // workers will wake, compute into their local buffers, then await again (to signal done)
        barrier.await();

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

}
