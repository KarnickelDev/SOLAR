package karnickeldev.solar.scheduler;

import karnickeldev.solar.context.EngineContext;

/**
 * @author KarnickelDev
 * @since 24.11.2025
 **/
public class DefaultScheduler implements Scheduler {

    private final Dispatcher main;
    private final AsyncExecutor async;
    private final TimerScheduler timer;

    public DefaultScheduler() {
        this.main = new DefaultDispatcher();
        this.async = new DefaultAsyncExecutor(EngineContext.get().async());
        this.timer = new DefaultTimerScheduler();
    }

    @Override
    public Dispatcher main() {
        return main;
    }

    @Override
    public AsyncExecutor async() {
        return async;
    }

    @Override
    public TimerScheduler timer() {
        return timer;
    }
}
