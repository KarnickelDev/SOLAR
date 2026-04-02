package karnickeldev.solar.worldview.orbitsolver.backends;

import karnickeldev.solar.util.Logger;
import karnickeldev.solar.util.spinbarrier.PhaserBarrier;
import karnickeldev.solar.util.spinbarrier.SyncBarrier;
import karnickeldev.solar.util.threadlayout.ClientThreadLayout;
import karnickeldev.solar.util.threadlayout.ThreadAffinity;
import karnickeldev.solar.util.threadlayout.ThreadContext;
import karnickeldev.solar.worldview.orbitsolver.OrbitJob;
import karnickeldev.solar.worldview.orbitsolver.OrbitMathKernel;
import karnickeldev.solar.worldview.orbitsolver.mathkernel.OrbitDataSoA;
import karnickeldev.solar.worldview.orbitsolver.OrbitExecutionBackend;
import karnickeldev.solar.worldview.orbitsolver.OrbitLocalFrame;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author KarnickelDev
 * @since 03.03.2026
 **/
public class PersistentOrbitWorkerBackend implements OrbitExecutionBackend {

    private final int workerCount;
    private final Thread[] workers;
    private final SyncBarrier barrier;

    private final int chunkSize;

    private final AtomicBoolean running = new AtomicBoolean(true);

    private volatile OrbitJob[] jobs;
    private volatile int jobCount;
    private volatile OrbitLocalFrame out;
    private volatile long simTimeMicros;

    public PersistentOrbitWorkerBackend(ClientThreadLayout threadLayout, int chunkSize) {
        Logger.log("[OrbitWorker] ", "OrbitWorker using chunks of size: " + chunkSize);

        this.chunkSize = chunkSize;
        ThreadContext context = threadLayout.getOrbitWorkerContext();
        this.workerCount = context.getThreadCount();

        this.barrier = new PhaserBarrier(workerCount + 1); // +1 main thread
        this.workers = new Thread[workerCount];

        for (int i = 0; i < workerCount; i++) {
            final int workerId = i;
            String name = "OrbitWorker-" + workerId;
            workers[i] = Thread.ofPlatform()
                .name(name)
                .priority(Thread.MAX_PRIORITY-1)
                .unstarted(() -> {
                    Logger.log(Logger.STARTUP, "Starting " + name);
                    if(context.useCoreAffinity()) ThreadAffinity.pinToCore(context.nextCpuId());
                    workerLoop(workerId);
                });

            workers[i].start();
        }
    }

    public PersistentOrbitWorkerBackend(int workerCount, int chunkSize) {
        this.chunkSize = chunkSize;
        this.workerCount = workerCount;

        this.barrier = new PhaserBarrier(workerCount + 1); // +1 main thread
        this.workers = new Thread[workerCount];

        for (int i = 0; i < workerCount; i++) {
            final int workerId = i;
            String name = "OrbitWorker-" + workerId;
            workers[i] = Thread.ofPlatform()
                .name(name)
                .priority(Thread.MAX_PRIORITY-1)
                .unstarted(() -> {
                    Logger.log(Logger.STARTUP, "Starting " + name);
                    workerLoop(workerId);
                });

            workers[i].start();
        }
    }

    @Override
    public void startExecute(OrbitJob[] jobs, OrbitLocalFrame out, long simTimeMicros) {
        this.jobs = jobs;
        this.jobCount = jobs.length;

        this.out = out;
        this.simTimeMicros = simTimeMicros;

        // release workers
        barrier.await();
    }

    @Override
    public void waitUntilFinished() {
        barrier.await();
    }

    @Override
    public void shutdown() {
        if (!running.compareAndSet(true, false)) return;

        Logger.log("[OrbitSolver] ", "stopping...");

        // tell workers to stop and break any waiting barrier
        try {
            barrier.forceTermination();
        } catch (Throwable t) {
            Logger.error("[OrbitSolver] ", "Failed to force-terminate barrier", t);
        }

        // Interrupt alive worker threads to break any blocking operations
        for (Thread t : workers) {
            if (t == null) continue;
            try {
                if (t.isAlive()) t.interrupt();
            } catch (Throwable ignored) {}
        }

        // Join with a per-thread timeout
        final long timeoutMS = 1000;
        for (Thread t : workers) {
            if (t == null) continue;
            if (!t.isAlive()) continue;
            try {
                t.join(timeoutMS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Throwable ignored) {}
        }

        // As final check, log threads still alive
        for (Thread t : workers) {
            if (t != null && t.isAlive()) {
                Logger.error("[OrbitSolver] ", "Orbit worker failed to stop: " + t.getName());
            }
        }

        Logger.log("[OrbitSolver] ", "shutdown complete");
    }

    private void workerLoop(int workerId) {
        while(running.get() && !barrier.isTerminated()) {
            // Wait for main thread to start the frame (also calls barrier.await())
            barrier.await();

            // if shutdown was requested or barrier was force-terminated, exit loop BEFORE computing.
            if(!running.get() || barrier.isTerminated()) break;

            // Snapshot frame parameters locally (important!)
            OrbitJob[] localJobs = this.jobs;
            int localJobCount = this.jobCount;
            OrbitLocalFrame localOut = this.out;
            long localTime = this.simTimeMicros;

            for(int j = 0; j < localJobCount; j++) {
                OrbitJob job = localJobs[j];

                OrbitDataSoA soa = job.soa;
                OrbitMathKernel kernel = job.kernel;

                int start = job.start;
                int end = job.end;

                int stride = workerCount * chunkSize;
                int workerStart = start + workerId * chunkSize;

                for (int base = workerStart; base < end; base += stride) {
                    int high = Math.min(base + chunkSize, end);
                    if (base >= high) break;

                    kernel.computeRange(soa, localOut, localTime, base, high);
                }
            }

            // signal done
            barrier.await();
        }

        Logger.log("[OrbitWorker] ", "Stopped OrbitWorker-" + workerId);
    }

}
