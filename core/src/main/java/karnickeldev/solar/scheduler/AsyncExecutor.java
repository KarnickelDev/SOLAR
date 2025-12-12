package karnickeldev.solar.scheduler;

import java.util.List;
import java.util.concurrent.Future;

/**
 * @author KarnickelDev
 * @since 24.11.2025
 **/
public interface AsyncExecutor {

    Future<?> submit(Runnable task);

    void shutdown();

    List<Runnable> shutdownNow();
}
