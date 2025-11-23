package karnickeldev.solar.simulation.execution;

import karnickeldev.solar.network.net.dispatcher.Dispatcher;
import karnickeldev.solar.util.Logger;
import karnickeldev.solar.util.MathUtil;
import karnickeldev.solar.util.threadlayout.ThreadContext;
import karnickeldev.solar.world.ServerWorld;
import karnickeldev.solar.world.WorldManager;

/**
 * @author KarnickelDev
 * @since 05.06.2025
 **/
public class SimulationManagerThread {

    private final SimulationManager simulationManager;
    private final Thread thread;

    public SimulationManagerThread(int threadCount, WorldManager<ServerWorld> worldManager, Dispatcher dispatcher, ThreadContext threadContext) {
        simulationManager = new SimulationManager(threadCount, worldManager, dispatcher, threadContext);
        thread = new Thread(simulationManager, "SimulationManager");

        thread.setPriority((int)MathUtil.lerp(Thread.NORM_PRIORITY, Thread.MAX_PRIORITY, 0.6));
    }

    public SimulationManager getSimulationManager() {
        return simulationManager;
    }

    public void start() {
        if(thread.isAlive()) return;

        thread.start();
    }

    public void stop() {
        simulationManager.stop();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Logger.error(Logger.SERVER, "Error stopping " + thread.getName());
            throw new RuntimeException(e);
        }
    }

}
