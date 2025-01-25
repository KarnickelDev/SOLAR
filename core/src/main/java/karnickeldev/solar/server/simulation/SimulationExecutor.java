package karnickeldev.solar.server.simulation;


import karnickeldev.solar.core.Logger;
import karnickeldev.solar.gamestate.GameState;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimulationExecutor {

    private static final short TICK_RATE = 60;
    private static final int TICK_INTERVAL = 1000 / TICK_RATE;  // in milliseconds

    private final ScheduledExecutorService executor;
    private final Simulation simulation;

    public SimulationExecutor(GameState initialGameState) {
        executor = Executors.newSingleThreadScheduledExecutor();
        simulation = new Simulation(initialGameState, TICK_INTERVAL);
    }

    public void start() {
        executor.scheduleAtFixedRate(simulation, 0, TICK_INTERVAL, TimeUnit.MILLISECONDS);
    }


    public void stop(long timeout_millis) {
        executor.shutdown();

        try {
            if (!executor.awaitTermination(timeout_millis, TimeUnit.MILLISECONDS)) {
                Logger.log(Logger.SERVER, "Server did not terminate in the specified time.");

                executor.shutdownNow();
                Logger.log(Logger.SERVER, "Server was forcefully shut down.");

                // Wait again to ensure termination
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    Logger.error(Logger.SERVER, "Server did not terminate cleanly.");
                }
            } else {
                Logger.log(Logger.SERVER, "Server shutdown completed gracefully.");
            }
        } catch (InterruptedException e) {
            Logger.error(Logger.SERVER, "Shutdown was interrupted");
            executor.shutdownNow();
            Thread.currentThread().interrupt(); // Preserve interrupt status
        }

    }

    public GameState getCurrentGameState() {
        return simulation.getGameState();
    }

}
