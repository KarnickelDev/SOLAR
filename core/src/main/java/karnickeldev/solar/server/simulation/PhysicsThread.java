package karnickeldev.solar.server.simulation;

import karnickeldev.solar.core.Logger;
import karnickeldev.solar.server.servers.DefaultServer;

public class PhysicsThread implements Runnable {

    private final int tickRate;
    private final long nanosPerTick;

    private final Runnable simulation;

    private volatile boolean running = true;

    public PhysicsThread(int tickRate, Runnable simulation) {
        this.tickRate = tickRate;
        nanosPerTick = 1_000_000_000 / this.tickRate;
        this.simulation = simulation;
    }

    public void stop() {
        // logging is done in this Threads executor
        running = false;
    }

    @Override
    public void run() {
        long previousTime = System.nanoTime();
        long accumulator = 0;

        while(running) {
            long tickStartTime = System.nanoTime();
            long delta = tickStartTime - previousTime;

            previousTime = tickStartTime;
            accumulator += delta;

            // catch up if previous tick took longer than one tick
            int steps = 0;
            while(accumulator >= nanosPerTick) {
                simulation.run();
                accumulator -= nanosPerTick;
                DefaultServer.tpsCount.tick(steps > 0);
                steps++;

                // avoid spiral of death
                if(steps > 8) {
                    steps = 0;
                    accumulator = 0;
                    Logger.log(Logger.SERVER, "Simulation load very high");
                }
            }


            // wait until next tick
            long sleepNS = nanosPerTick - (System.nanoTime() - tickStartTime);
            long sleepMS = (sleepNS / 1_000_000) - 2;   // Thread.sleep() can be delayed > 1ms, so add buffer
            if(sleepMS > 0) {
                try {
                    Thread.sleep(sleepMS);
                } catch (InterruptedException e) {
                    Logger.error(Logger.SERVER, "Physics Thread was interrupted", e);
                    break;
                }
            }
            while(System.nanoTime() < tickStartTime + nanosPerTick) {
                Thread.onSpinWait();
            }
        }

        if(running) {
            running = false;
            Logger.error(Logger.SERVER, "Server was shutdown due to being interrupted");
        } else {
            Logger.log(Logger.SERVER, "Physics Thread shutdown gracefully");
        }

    }

}
