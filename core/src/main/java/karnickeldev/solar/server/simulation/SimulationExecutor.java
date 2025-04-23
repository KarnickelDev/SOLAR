package karnickeldev.solar.server.simulation;


import karnickeldev.solar.core.Logger;
import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.level.StarSystem;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimulationExecutor {

    public final int tickRate;

    private final Thread physicsThreadExecutor;
    private final PhysicsThread physicsThread;
    private final Simulation simulation;

    public SimulationExecutor(EntityManager entityManager, int tickRate) {
        this.tickRate = tickRate;

        simulation = new Simulation(entityManager, this.tickRate);
        physicsThread = new PhysicsThread(tickRate, simulation);
        physicsThreadExecutor = new Thread(physicsThread);
    }

    public void start() {
        physicsThreadExecutor.start();
    }


    public void stop() {
        Logger.log(Logger.SERVER, "Shutting down PhysicsThread...");
        physicsThread.stop();
        try {
            physicsThreadExecutor.join();
        } catch (InterruptedException e) {
            physicsThreadExecutor.interrupt();
            Logger.error(Logger.SERVER, "Shutdown of PhysicsThread was interrupted!", e);
        }
    }

}
