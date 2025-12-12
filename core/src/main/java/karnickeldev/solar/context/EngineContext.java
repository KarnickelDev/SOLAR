package karnickeldev.solar.context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author KarnickelDev
 * @since 25.11.2025
 **/
public final class EngineContext {

    private static EngineContext INSTANCE;

    public static EngineContext get() {
        return INSTANCE;
    }

    public static void init(int asyncThreads) {
        if(INSTANCE != null) throw new IllegalStateException("EngineContext already initialized");

        INSTANCE = new EngineContext(Math.max(1, asyncThreads));
    }

    private final ExecutorService asyncPool;

    private EngineContext(int asyncThreads) {
        asyncPool = Executors.newFixedThreadPool(asyncThreads, r ->
            Thread.ofPlatform().name("AsyncWorker").daemon(true).priority(Thread.NORM_PRIORITY).unstarted(r)
        );
    }

    public ExecutorService async() {
        return asyncPool;
    }

    public void shutdown() {
        asyncPool.shutdown();
    }

}
