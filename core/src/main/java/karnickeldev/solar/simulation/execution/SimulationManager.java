package karnickeldev.solar.simulation.execution;

import karnickeldev.solar.context.ServerContext;
import karnickeldev.solar.network.net.dispatcher.Dispatcher;
import karnickeldev.solar.network.packets.Packet;
import karnickeldev.solar.network.packets.PacketFactory;
import karnickeldev.solar.network.packets.ServerPerformanceMetricsPacket;
import karnickeldev.solar.util.Logger;
import karnickeldev.solar.util.datastructures.BitMask;
import karnickeldev.solar.world.ServerWorld;
import karnickeldev.solar.world.World;
import karnickeldev.solar.world.WorldManager;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final Dispatcher dispatcher;

    private long globalSimTimeMicros;

    public final AtomicBoolean running = new AtomicBoolean(true);

    private long schedulerNanosPerTick;
    private byte tickRate;

    public static SimSpeedController simSpeedController = new SimSpeedController();

    private final BitMask errno = new BitMask();

    public boolean paused = false;

    public SimulationManager(int simulationThreadCount, WorldManager<ServerWorld> worldManager, Dispatcher dispatcher) {
        this.worldManager = worldManager;
        this.dispatcher = dispatcher;

        simulationThreadPool = Executors.newFixedThreadPool(3, new SimulationThreadFactory("SimThread"));

        scheduledTasks = new PriorityQueue<>(simulationThreadCount);

        tickRate = 20;
        schedulerNanosPerTick = 1_000_000_000L / tickRate;

        // TODO: initialize from save-file
        globalSimTimeMicros = 0;

        simSpeedController.setSpeedPreset(1);
    }


    public void setSimSpeed(byte v) {
        simSpeedController.setSpeedPreset(v);
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

    boolean init = false;

    @Override
    public void run() {
        long tickEnd;
        long tickStart;

        long simulationStartTime = System.nanoTime();

        while(running.get()) {
            /*
            Simulate one "simulation time tick" (actual world ticks executed depend on sim speed and world TickRate)
             */
            tickStart = System.nanoTime();

            dispatcher.update();

            List<SimulationTask> tasks;
            synchronized (schedulerLock) {
                 tasks = new ArrayList<>(scheduledTasks);
                 scheduledTasks.clear();
            }

            if(!paused || !init) {
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
            }

            for(SimulationTask t: tasks) {
                ServerContext.get().getServer().getServerNetwork().broadcast(new ServerPerformanceMetricsPacket(
                    t.getWorld().getID(),
                    t.getWorld().getWorldTime().getSimTimeMicros(),
                    t.tpsTracker.getTPS(),
                    t.tpsTracker.getTPS()
                ));
            }

            Packet timeStamp = PacketFactory.createTimestampPacket(globalSimTimeMicros, paused ? 0 : simSpeedController.getCurrentSimSpeed(),
                simSpeedController.getPresetIndex(), paused);
            ServerContext.get().getServer().getServerNetwork().broadcast(timeStamp);
            Packet ecsUpdatePacket = PacketFactory.createECSUpdatePacket(
                globalSimTimeMicros, worldManager.getActiveWorld()
            );
            ServerContext.get().getServer().getServerNetwork().broadcast(ecsUpdatePacket);
            ServerContext.get().getServer().getServerNetwork().flush();

            tickEnd = System.nanoTime();

            //System.out.println(simSpeedController.getCurrentSimSp
            // eed());

            simSpeedController.update(1f / tickRate);

            //globalSimTimeMicros = Math.round(((System.nanoTime() - simulationStartTime) / 1000d) * simSpeed);
            if(!paused) globalSimTimeMicros += Math.round((schedulerNanosPerTick / 1000d) * simSpeedController.getCurrentSimSpeed());

//            tmp = (tmp + 1) % 100;
//            if(tmp == 0) {
//                for(SimulationTask task: tasks) {
//                    Logger.log(task+"");
//                }
//                Logger.log(" ");
//            }

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
            long sleepMS = (nextTick - tickEnd) / 1_000_000;
            sleepMS -= 2;

            if(sleepMS > 0) {
                try {
                    //noinspection BusyWait
                    Thread.sleep(sleepMS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Logger.log(Logger.SERVER, "Interrupted while passive waiting, going to active wait!");
                }
            }

            while(System.nanoTime() < nextTick) Thread.onSpinWait();
            init = true;
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
