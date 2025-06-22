package karnickeldev.solar.simulation;


import karnickeldev.solar.net.network.dispatcher.MainThreadDispatcher;
import karnickeldev.solar.net.network.core.ServerNetwork;
import karnickeldev.solar.net.server.ServerPerformanceMetrics;
import karnickeldev.solar.physics.KeplerianOrbitSystem;
import karnickeldev.solar.util.Logger;

public class SimulationExecutor {

    public final int tickRate;

    private final Thread physicsThreadExecutor;
    private final PhysicsThread physicsThread;
    private final Simulation simulation;

    public SimulationExecutor(ServerNetwork serverNetwork, ServerPerformanceMetrics performanceMetrics, KeplerianOrbitSystem os, MainThreadDispatcher dispatcher, int tickRate) {
        this.tickRate = tickRate;
        simulation = new Simulation(os, this.tickRate);
        physicsThread = new PhysicsThread(tickRate, simulation, dispatcher, performanceMetrics, serverNetwork);
        physicsThreadExecutor = new Thread(physicsThread);
    }

    public void start() {
        physicsThreadExecutor.start();
    }

    public boolean isRunning() {
        return physicsThread.isRunning();
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
