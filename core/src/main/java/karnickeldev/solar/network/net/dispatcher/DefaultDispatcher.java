package karnickeldev.solar.network.net.dispatcher;

import karnickeldev.solar.util.MathUtil;
import org.junit.runner.notification.RunListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@RunListener.ThreadSafe
public class DefaultDispatcher implements Dispatcher {

    private final ConcurrentLinkedQueue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();

    private int completed = 0;
    private float task_count = 0;

    private volatile boolean alive = true;

    @Override
    public void dispatch(Runnable task) {
        if(alive) {
            taskQueue.offer(task);
            completed = 0;
            task_count = taskQueue.size();
        }
    }

    @Override
    public void update() {
        Runnable task;
        while ((task = taskQueue.poll()) != null) {
            task.run();
            completed++;
        }
    }

    @Override
    public boolean update(int ms) {
        long start = System.nanoTime();
        Runnable task;
        while ((System.nanoTime() - start) / 1_000_000 < ms && (task = taskQueue.poll()) != null) {
            task.run();
            completed++;
        }
        return taskQueue.isEmpty();
    }

    @Override
    public float getProgress() {
        if(task_count == 0) return 1f;
        return MathUtil.clamp(completed / task_count, 0, 1);
    }

    @Override
    public void shutdown() {
        alive = false;
    }

    @Override
    public List<Runnable> shutdownNow() {
        alive = false;
        List<Runnable> pendingTasks = new ArrayList<>(taskQueue.size());
        while(!taskQueue.isEmpty()) pendingTasks.add(taskQueue.poll());

        return pendingTasks;
    }

}
