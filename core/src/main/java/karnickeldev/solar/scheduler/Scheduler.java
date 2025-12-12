package karnickeldev.solar.scheduler;

import java.util.concurrent.Future;

/**
 * @author KarnickelDev
 * @since 24.11.2025
 **/
public interface Scheduler {

    Dispatcher main();

    AsyncExecutor async();

    TimerScheduler timer();

    default void shutdown() {
        timer().shutdown();
        main().shutdown();
    }

    default void schedule(Runnable task) {
        main().dispatch(task);
    }

    default Cancellable schedule(Runnable task, long delayMillis) {
        return timer().schedule(task, delayMillis);
    }

    default Future<?> scheduleAsync(Runnable task) {
        return async().submit(task);
    }

    default Cancellable scheduleAsync(Runnable task, long delayMillis) {
        return timer().schedule(() -> async().submit(task), delayMillis);
    }

    default Cancellable scheduleRepeating(Runnable task, long intervalMillis, long initialDelayMillis) {
        return timer().scheduleAtFixedRate(task, intervalMillis, initialDelayMillis);
    }

    default Cancellable scheduleAsyncRepeating(Runnable task, long intervalMillis, long initialDelayMillis) {
        return timer().scheduleAtFixedRate(() -> async().submit(task), intervalMillis, initialDelayMillis);
    }
}
