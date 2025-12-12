package karnickeldev.solar.scheduler;

import karnickeldev.solar.util.Logger;
import org.junit.runner.notification.RunListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@RunListener.ThreadSafe
public class DefaultDispatcher implements Dispatcher {

    private final ConcurrentLinkedQueue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();

    private volatile boolean alive = true;

    @Override
    public void dispatch(Runnable task) {
        if(task == null) return;
        if(alive) taskQueue.offer(task);
    }

    @Override
    public void update() {
        update(-1);
    }

    @Override
    public boolean update(int ms) {
        long start = System.nanoTime();
        Runnable task;
        while ((ms <= 0 || (System.nanoTime() - start) / 1_000_000 < ms) && (task = taskQueue.poll()) != null) {
            try {
                task.run();
            } catch (Exception e) {
                Logger.error("[" + this.getClass().getSimpleName() + "]","Error dispatching: " + task, e.getCause());
            }
        }
        return taskQueue.isEmpty();
    }

    @Override
    public void shutdown() {
        alive = false;
    }

    @Override
    public List<Runnable> shutdownNow() {
        alive = false;
        List<Runnable> pendingTasks = new ArrayList<>(taskQueue);
        taskQueue.clear();
        return pendingTasks;
    }

}
