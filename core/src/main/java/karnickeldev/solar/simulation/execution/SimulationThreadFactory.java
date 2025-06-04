package karnickeldev.solar.simulation.execution;

import karnickeldev.solar.util.MathUtil;

import java.util.concurrent.ThreadFactory;

/**
 * @author : KarnickelDev
 * @since : 04.06.2025
 **/
public class SimulationThreadFactory implements ThreadFactory {
    private final String namePrefix;
    private int count = 0;

    public SimulationThreadFactory(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    @Override
    public synchronized Thread newThread(Runnable r) {
        Thread t = new Thread(r);

        t.setName(namePrefix + "-" + count++);
        t.setPriority((int) MathUtil.lerp(Thread.NORM_PRIORITY, Thread.MAX_PRIORITY, 0.6));
        t.setDaemon(false);
//        t.setUncaughtExceptionHandler((thread, throwable) -> {
//            Logger.log("[Thread " + thread.getName() + "] Uncaught exception: " + throwable);
//        });

        return t;
    }
}
