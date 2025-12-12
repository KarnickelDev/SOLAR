package karnickeldev.solar.scheduler;

import karnickeldev.solar.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author KarnickelDev
 * @since 24.11.2025
 **/
public final class DefaultTimerScheduler implements TimerScheduler {

    private final PriorityQueue<ScheduledTask> queue = new PriorityQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Object lock = new Object();

    public DefaultTimerScheduler() {

    }

    @Override
    public Cancellable schedule(Runnable task, long delayMillis) {
        if(!running.get()) return null;
        ScheduledTask st = new ScheduledTask(System.currentTimeMillis() + delayMillis, 0, task);
        synchronized (lock) {
            queue.add(st);
        }
        return st;
    }

    @Override
    public Cancellable scheduleAtFixedRate(Runnable task, long initialDelayMillis, long periodMillis) {
        if(!running.get()) return null;
        ScheduledTask st = new ScheduledTask(System.currentTimeMillis() + initialDelayMillis, periodMillis, task);
        synchronized (lock) {
            queue.add(st);
        }
        return st;
    }

    @Override
    public void update(long nowMillis) {
        update(nowMillis, -1);
    }

    @Override
    public boolean update(long nowMillis, long ms) {
        boolean empty = false;
        while(ms <= 0 || System.nanoTime() < nowMillis + ms) {
            ScheduledTask t;
            synchronized (lock) {
                t = queue.peek();
                if(t == null || t.getTimeDue() > nowMillis) break;
                queue.poll();
                empty = queue.isEmpty();
            }

            if(!t.isCancelled()) {
                t.runSafely();
                if(t.isRepeating() && ! t.isCancelled()) {
                    t.reschedule();
                    synchronized (lock) {
                        queue.add(t);
                        empty = queue.isEmpty();
                    }
                }
            }
        }

        return empty;
    }

    @Override
    public void shutdown() {
        running.set(false);
    }

    @Override
    public List<ScheduledTask> shutdownNow() {
        running.set(false);
        List<ScheduledTask> pendingTasks = new ArrayList<>();
        synchronized (lock) {
            while(!queue.isEmpty()) {
                ScheduledTask t = queue.poll();
                if(!t.isCancelled()) pendingTasks.add(queue.poll());
            }
        }
        return pendingTasks;
    }


}
