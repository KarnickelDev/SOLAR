package karnickeldev.solar.simulation;

import karnickeldev.solar.net.network.MainThreadDispatcher;
import karnickeldev.solar.net.network.ServerNetwork;
import karnickeldev.solar.net.packets.ServerPerformanceMetricsPacket;
import karnickeldev.solar.net.server.ServerPerformanceMetrics;
import karnickeldev.solar.util.Logger;

public class PhysicsThread implements Runnable {

    private final long nanosPerTick;

    private final Runnable simulation;
    private final MainThreadDispatcher dispatcher;
    private final ServerPerformanceMetrics tpsCounter;
    private final ServerNetwork serverNetwork;
    long tick = 0;
    private volatile boolean running = true;

    public PhysicsThread(int tickRate, Runnable simulation, MainThreadDispatcher dispatcher, ServerPerformanceMetrics tpsCounter, ServerNetwork serverNetwork) {
        nanosPerTick = 1_000_000_000 / tickRate;
        this.simulation = simulation;
        this.dispatcher = dispatcher;
        this.tpsCounter = tpsCounter;
        this.serverNetwork = serverNetwork;
    }

    public void stop() {
        // logging is done in this Threads executor
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        long previousTime = System.nanoTime();
        long accumulator = 0;

        while (running) {
            long tickStartTime = System.nanoTime();
            long delta = tickStartTime - previousTime;

            previousTime = tickStartTime;
            accumulator += delta;

            dispatcher.update();

            // catch up if previous tick took longer than one tick
            int steps = 0;
            while (accumulator >= nanosPerTick) {
                simulation.run();
                accumulator -= nanosPerTick;
                tpsCounter.tick(steps > 0);
                tick++;
                steps++;

                // avoid spiral of death
                if (steps > 8) {
                    steps = 0;
                    accumulator = 0;
                    Logger.log(Logger.SERVER, "Simulation load very high");
                }
            }

            serverNetwork.broadcast(new ServerPerformanceMetricsPacket(tick, tpsCounter.getTPS(), tpsCounter.getDelayedness()));

            // wait until next tick
            long sleepNS = nanosPerTick - (System.nanoTime() - tickStartTime);
            long sleepMS = (sleepNS / 1_000_000) - 2;   // Thread.sleep() can be delayed > 1ms, so add buffer
            if (sleepMS > 0) {
                try {
                    //noinspection BusyWait
                    Thread.sleep(sleepMS);
                } catch (InterruptedException e) {
                    Logger.error(Logger.SERVER, "Physics Thread was interrupted", e);
                    break;
                }
            }
            while (System.nanoTime() < tickStartTime + nanosPerTick) {
                Thread.onSpinWait();
            }
        }

        if (running) {
            running = false;
            Logger.error(Logger.SERVER, "Server was shutdown due to being interrupted");
        } else {
            Logger.log(Logger.SERVER, "Physics Thread shutdown gracefully");
        }

    }

}
