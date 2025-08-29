package karnickeldev.solar.simulation;

import karnickeldev.solar.network.net.dispatcher.Dispatcher;
import karnickeldev.solar.network.net.core.ServerNetwork;
import karnickeldev.solar.network.packets.ServerPerformanceMetricsPacket;
import karnickeldev.solar.network.server.ServerPerformanceMetrics;
import karnickeldev.solar.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author : KarnickelDev
 * @since : 30.05.2025
**/
public class FixedTimestepRunnable implements Runnable {

    private static final byte MAX_CATCHUP_LOOPS = 8;
    private static final long PERFORMANCE_REPORT_TIMEOUT = Math.round(1e9 * ServerPerformanceMetrics.TPS_UPDATE_THRESHOLD);

    private final Runnable simulationStep;
    private final long nanosPerTick;

    private final Dispatcher dispatcher;
    private final ServerNetwork serverNetwork;
    private final ServerPerformanceMetrics performanceMetrics;

    private final List<SimulationTickListener> tickListener;

    private volatile boolean running = true;

    long tick;

    public FixedTimestepRunnable(int tickRate, Runnable simulationStep, Dispatcher dispatcher,
                                 ServerNetwork serverNetwork, ServerPerformanceMetrics performanceMetrics) {
        nanosPerTick = 1_000_000_000 / tickRate;

        this.simulationStep = simulationStep;
        this.dispatcher = dispatcher;
        this.serverNetwork = serverNetwork;
        this.performanceMetrics = performanceMetrics;

        this.tickListener = new ArrayList<>(4);

        ExecutorService s = Executors.newFixedThreadPool(5);
    }


    public void stop() {
        running = false;
    }

    public void addTickListener(SimulationTickListener listener) {
        if(tickListener.contains(listener)) {
            Logger.debug("Tried adding a tick-listener twice");
            return;
        }
        tickListener.add(listener);
    }

    public void removeTickListener(SimulationTickListener listener) {
        tickListener.remove(listener);
    }

    @Override
    public void run() {
        long previousStartTime = System.nanoTime();
        long accumulator = 0;

        while(running) {
            long lastPerformanceReport = System.nanoTime();

            long currentStartTime = System.nanoTime();
            long delta = currentStartTime - previousStartTime;

            previousStartTime = currentStartTime;
            accumulator += delta;

            dispatcher.update();

            // catch up if previous tick took longer than one tick
            int steps = 0;
            while (accumulator >= nanosPerTick) {

                for(SimulationTickListener listener: tickListener) listener.onTickStart(tick);

                simulationStep.run();

                long systemTime = System.nanoTime();
                for(SimulationTickListener listener: tickListener) listener.onTickEnd(tick, (int)(systemTime - currentStartTime));

                accumulator -= nanosPerTick;
                performanceMetrics.tick(steps > 0);
                tick++;
                steps++;

                // avoid spiral of death
                if (steps > MAX_CATCHUP_LOOPS) {
                    steps = 0;
                    accumulator = 0;
                    Logger.log(Logger.SERVER, "Simulation load very high");
                }
            }

            long systemTime = System.nanoTime();
            if(systemTime - lastPerformanceReport >= PERFORMANCE_REPORT_TIMEOUT) {
                serverNetwork.broadcast(new ServerPerformanceMetricsPacket(0, tick, performanceMetrics.getTPS(), performanceMetrics.getDelayedness()));
                lastPerformanceReport = systemTime;
            }

            // wait until next tick
            long sleepNS = nanosPerTick - (System.nanoTime() - currentStartTime);
            long sleepMS = (sleepNS / 1_000_000) - 2;   // Thread.sleep() can be delayed > 1ms, so add buffer
            if (sleepMS > 0) {
                try {
                    //noinspection BusyWait
                    Thread.sleep(sleepMS);
                } catch (InterruptedException e) {
                    Logger.error(Logger.SERVER, "Thread " + Thread.currentThread().getName() + " was interrupted", e);
                    break;
                }
            }
            while (System.nanoTime() < currentStartTime + nanosPerTick) Thread.onSpinWait();
        }

        // shutdown logging
        if (running) {
            running = false;
            Logger.error(Logger.SERVER, "Server was shutdown due to being interrupted");
        } else {
            Logger.log(Logger.SERVER, "Physics Thread shutdown gracefully");
        }
    }

}
