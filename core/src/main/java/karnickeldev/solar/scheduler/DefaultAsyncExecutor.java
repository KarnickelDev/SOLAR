package karnickeldev.solar.scheduler;

import karnickeldev.solar.util.Logger;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author KarnickelDev
 * @since 24.11.2025
 **/
public final class DefaultAsyncExecutor implements AsyncExecutor {

    private final ExecutorService pool;

    public DefaultAsyncExecutor(ExecutorService pool) {
        this.pool = pool;
    }

    @Override
    public Future<?> submit(Runnable task) {
        return pool.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                Logger.error("[" + this.getClass().getSimpleName() + "]",
                    "Error dispatching: " + task, e.getCause());
            }
        });
    }

    @Override
    public void shutdown() {
        pool.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return pool.shutdownNow();
    }
}
