package karnickeldev.solar.scheduler;

import karnickeldev.solar.logging.Logger;

import java.util.List;

/**
 * @author KarnickelDev
 * @since 24.11.2025
 **/
public interface TimerScheduler {

    Cancellable schedule(Runnable task, long delayMillis);

    Cancellable scheduleAtFixedRate(Runnable task, long initialDelayMillis, long periodMillis);

    void update(long nowMillis);

    boolean update(long nowMillis, long ms);

    void shutdown();

    List<ScheduledTask> shutdownNow();


    class ScheduledTask implements Cancellable, Comparable<ScheduledTask> {

        private final long period;
        private long time;
        private final Runnable action;
        private volatile boolean cancelled = false;

        public ScheduledTask(long time, long period, Runnable task) {
            this.time = time;
            this.period = period;
            this.action = task;
        }

        public void runSafely() {
            try {
                if(!cancelled) action.run();
            } catch (Exception e) {
                Logger.get("TimerScheduler").error(e.getCause(),"Error in TimerScheduler task " + action);
            }
        }

        public long getTimeDue() {
            return time;
        }

        public long getPeriod() {
            return period;
        }

        @Override
        public int compareTo(ScheduledTask o) {
            return Long.compare(this.time, o.time);
        }

        public void reschedule() {
            time += period;
        }

        @Override
        public void cancel() {
            cancelled = true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        public boolean isRepeating() {
            return period > 0;
        }

        public boolean isDue(long nowMillis) {
            return nowMillis >= time;
        }
    }

}
