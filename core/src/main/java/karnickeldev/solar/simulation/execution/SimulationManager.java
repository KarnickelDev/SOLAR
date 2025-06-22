package karnickeldev.solar.simulation.execution;

import karnickeldev.solar.net.network.dispatcher.MainThreadDispatcher;
import karnickeldev.solar.util.Logger;
import karnickeldev.solar.util.datastructures.BitMask;
import karnickeldev.solar.world.ServerWorld;
import karnickeldev.solar.world.World;
import karnickeldev.solar.world.WorldManager;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * @author : KarnickelDev
 * @since : 01.06.2025
 **/
public class SimulationManager implements Runnable {

    private static final byte INTERRUPT = 1;
    private static final byte TIMEOUT = 2;
    private static final byte EXCEPTION_IN_TICK = 3;
    private static final byte EXCEPTION_IN_SCHEDULE = 4;
    private static final byte SHUTDOWN_TIMEOUT = 5;

    private final Object schedulerLock = new Object();

    private final ExecutorService simulationThreadPool;

    private final PriorityQueue<SimulationTask> scheduledTasks;
    private final Map<World, SimulationTask> worldSimulationTaskMap = new HashMap<>();

    private final WorldManager<ServerWorld> worldManager;
    private final MainThreadDispatcher mainThreadDispatcher;

    private long globalSimTimeMicros;

    public final AtomicBoolean running = new AtomicBoolean(true);

    private long schedulerNanosPerTick;
    private byte tickRate;

    public static float simSpeed = 3600f;

    private final BitMask errno = new BitMask();

    public SimulationManager(int simulationThreadCount, WorldManager<ServerWorld> worldManager, MainThreadDispatcher mainThreadDispatcher) {
        this.worldManager = worldManager;
        this.mainThreadDispatcher = mainThreadDispatcher;

        simulationThreadPool = Executors.newFixedThreadPool(3, new SimulationThreadFactory("SimThread"));

        scheduledTasks = new PriorityQueue<>(simulationThreadCount);

        tickRate = 100;
        schedulerNanosPerTick = 1_000_000_000L / tickRate;

        // TODO: initialize from save-file
        globalSimTimeMicros = 0;
    }

    public void registerWorld(ServerWorld world) {
        synchronized (schedulerLock) {
            if(worldSimulationTaskMap.containsKey(world)) return;

            SimulationTask task = new SimulationTask(world);
            Objects.requireNonNull(task.getWorld().getNetwork());
            worldSimulationTaskMap.put(world, task);
            scheduledTasks.add(task);
        }
    }

    public void unregisterWorld(ServerWorld world) {
        synchronized (schedulerLock) {
            SimulationTask task = worldSimulationTaskMap.remove(world);
            if(task != null) scheduledTasks.remove(task);
        }
    }

    public void setPriority(ServerWorld world, SimulationTask.Priority priority) {
        synchronized (schedulerLock) {
            SimulationTask task = worldSimulationTaskMap.get(world);
            if(task == null) throw new RuntimeException(world.getClass().getSimpleName() + ' ' + world + " not registered");

            scheduledTasks.remove(task);
            task.setPriority(priority);
            scheduledTasks.add(task);
        }
    }

    public void stop() {
        if(running.compareAndSet(true, false)) {
            synchronized(this) {
                notifyAll();
            }
        }
    }

    int tmp = 0;

    @Override
    public void run() {
        long tickEnd;
        long tickStart;

        long simulationStartTime = System.nanoTime();

        globalSimTimeMicros = 10000;

        while(running.get()) {
            /*
            Simulate one "simulation time tick" (actual world ticks executed depend on sim speed and world TickRate)
             */
            tickStart = System.nanoTime();

            mainThreadDispatcher.update();

            List<SimulationTask> tasks;
            synchronized (schedulerLock) {
                 tasks = new ArrayList<>(scheduledTasks);
                 scheduledTasks.clear();
            }

            CountDownLatch latch = new CountDownLatch(tasks.size());

            for(SimulationTask task: tasks) {
                simulationThreadPool.submit(() -> {
                    try {
                        task.runUntil(globalSimTimeMicros);
                    } catch (Exception e) {
                        Logger.error("Simulation error: " + e.getMessage());
                        errno.set(EXCEPTION_IN_TICK);
                        running.set(false);
                    } finally {
                        latch.countDown();
                    }
                    });
            }

            try {
                // Synchronize wall-clock
                if(!latch.await(60, TimeUnit.SECONDS)) {
                    errno.set(TIMEOUT);
                    break;
                }
            } catch (InterruptedException e) {
                errno.set(INTERRUPT);
                Thread.currentThread().interrupt();
                break;
            }

            tickEnd = System.nanoTime();


            globalSimTimeMicros = Math.round(((System.nanoTime() - simulationStartTime) / 1000d) * simSpeed);

            tmp = (tmp + 1) % 100;
            if(tmp == 0) {
                for(SimulationTask task: tasks) {
                    Logger.log(task+"");
                }
                Logger.log(" ");
            }

            // Re-add tasks for next round
            synchronized (schedulerLock) {
                try {
                    scheduledTasks.addAll(tasks);
                } catch (Exception e) {
                    Logger.error("Scheduling error: " + e.getMessage());
                    errno.set(EXCEPTION_IN_SCHEDULE);
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            long nextTick = tickStart + schedulerNanosPerTick;
            if(nextTick > tickEnd) LockSupport.parkNanos(nextTick - tickEnd);
        }

        // shutdown logic
        running.set(false);

        simulationThreadPool.shutdown();
        try {
            boolean orderlyShutdown = simulationThreadPool.awaitTermination(3, TimeUnit.SECONDS);
            if (!orderlyShutdown) {
                Logger.log(Thread.currentThread().getName() + " did not shutdown in time");
                errno.set(SHUTDOWN_TIMEOUT);
                simulationThreadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            simulationThreadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        logShutdown(errno);
    }



    /**
     * Logs a shutdown message (including crash reasons)
     * @param errno Contains error codes
     */
    private static void logShutdown(BitMask errno) {
        if (errno.getMask() == 0) {
            Logger.log(Logger.SERVER, Thread.currentThread().getName() + " shutdown gracefully");
        } else {
            StringBuilder error = new StringBuilder();
            if(errno.test(INTERRUPT)) {
                error.append("interrupt ");
            }
            if(errno.test(EXCEPTION_IN_TICK)) {
                error.append("tick_exception ");
            }
            if(errno.test(EXCEPTION_IN_SCHEDULE)) {
                error.append("schedule_exception ");
            }
            if(errno.test(TIMEOUT)) {
                error.append("tick_timeout ");
            }
            if(errno.test(SHUTDOWN_TIMEOUT)) {
                error.append("shutdown_timeout ");
            }
            if(error.length() == 0) error.append("UNKNOWN");

            Logger.error(Logger.SERVER, Thread.currentThread().getName() + "crashed from " + error);
            Logger.log(Logger.SERVER, Thread.currentThread().getName() + " shutdown because of " + error);
        }
    }

}
