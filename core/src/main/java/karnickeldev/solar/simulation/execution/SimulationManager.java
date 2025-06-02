package karnickeldev.solar.simulation.execution;

import karnickeldev.solar.util.Logger;
import karnickeldev.solar.world.ServerWorld;
import karnickeldev.solar.world.World;
import karnickeldev.solar.world.WorldManager;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;

/**
 * @author : KarnickelDev
 * @since : 01.06.2025
 **/
public class SimulationManager implements Runnable {

    private final ExecutorService highPriorityThreadPool;
    private final ExecutorService lowPriorityThreadPool;

    private final PriorityQueue<SimulationTask> scheduledTasks;
    private final Map<World, SimulationTask> worldSimulationTaskMap = new HashMap<>();

    private final WorldManager<ServerWorld> worldManager;

    // TODO: initialize from save-file
    private long globalSimTimeMicros = 0;

    private volatile boolean running = true;

    private long schedulerNanosPerTick;

    public SimulationManager(int simulationThreadCount, WorldManager<ServerWorld> worldManager) {
        this.worldManager = worldManager;

        int highPriorityThreads = (3*(simulationThreadCount-1)) / (4*(simulationThreadCount-1));
        int lowPriorityThreads = (simulationThreadCount-1) - highPriorityThreads;

        highPriorityThreadPool = Executors.newFixedThreadPool(Math.max(1, highPriorityThreads));
        lowPriorityThreadPool = Executors.newFixedThreadPool(Math.max(1, lowPriorityThreads));

        scheduledTasks = new PriorityQueue<>(simulationThreadCount);

        int tickRate = SimulationTask.Priority.values()[SimulationTask.Priority.values().length-1].getTickRate();
        schedulerNanosPerTick = 1_000_000_000L / tickRate;
    }

    public void registerWorld(ServerWorld world) {
        if(worldSimulationTaskMap.containsKey(world)) return;

        SimulationTask task = new SimulationTask(world);
        worldSimulationTaskMap.put(world, task);
        scheduledTasks.add(task);
    }

    public void unregisterWorld(ServerWorld world) {
        SimulationTask task = worldSimulationTaskMap.remove(world);
        if(task != null) scheduledTasks.remove(task);
    }

    public void setPriority(ServerWorld world, SimulationTask.Priority priority) {
        SimulationTask task = worldSimulationTaskMap.get(world);
        if(task == null) throw new RuntimeException(world.getClass().getSimpleName() + ' ' + world + " not registered");

        scheduledTasks.remove(task);
        task.setPriority(priority);
        scheduledTasks.add(task);
    }

    @Override
    public void run() {
        long tickEnd;
        long tickStart;
        while(running) {
            /*
            Simulate one "simulation time tick" (actual world ticks executed depend on sim speed and world TickRate)
             */
            tickStart = System.nanoTime();
            SimulationTask[] tasks = scheduledTasks.toArray(new SimulationTask[0]);

            CountDownLatch latch = new CountDownLatch(tasks.length);

            for(SimulationTask task: tasks) {
                ExecutorService service = getExecutorService(task);
                service.submit(() -> {
                    task.runUntil(globalSimTimeMicros);
                    latch.countDown();
                    });
            }

            try {
                latch.await(); // Synchronize wall-clock
            } catch (InterruptedException e) {
                Logger.log(Logger.SERVER, Thread.currentThread().getName() + " interrupted");
                Thread.currentThread().interrupt();
                break;
            }

            tickEnd = System.nanoTime();

            globalSimTimeMicros += 1_000_000;

            // Re-add tasks for next round
            scheduledTasks.addAll(List.of(tasks));

            long nextTick = tickStart + schedulerNanosPerTick;
            if(nextTick > tickEnd) {
                LockSupport.parkNanos(nextTick - tickEnd);
            }
        }

        // shutdown logging
        if (running) {
            running = false;
            Logger.error(Logger.SERVER, "Server was shutdown due to being interrupted");
        } else {
            Logger.log(Logger.SERVER, "Physics Thread shutdown gracefully");
        }

    }

    private ExecutorService getExecutorService(SimulationTask task) {
        return this.highPriorityThreadPool;
    }

}
