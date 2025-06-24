package karnickeldev.solar.network.net.dispatcher;

import java.util.concurrent.ConcurrentLinkedQueue;

public class DefaultMainThreadDispatcher implements MainThreadDispatcher {

    private final ConcurrentLinkedQueue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();

    @Override
    public void dispatch(Runnable task) {
        taskQueue.offer(task);
    }

    @Override
    public void update() {
        Runnable task;
        while ((task = taskQueue.poll()) != null) {
            task.run();
        }
    }

    public void update(int ns) {
        long start = System.nanoTime();
        Runnable task;
        while ((System.nanoTime() - start) < ns && (task = taskQueue.poll()) != null) {
            task.run();
        }
    }

}
