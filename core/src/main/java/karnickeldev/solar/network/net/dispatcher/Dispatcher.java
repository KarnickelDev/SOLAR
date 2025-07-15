package karnickeldev.solar.network.net.dispatcher;

import java.util.List;

public interface Dispatcher {

    /** queues a task (executed by update()) */
    void dispatch(Runnable run);

    /** executes all tasks */
    void update();

    /**
     * updates until done or time limit reached
     * @param ms The time limit
     * @return True if all tasks done, False if there are tasks left in the queue
     */
    boolean update(int ms);

    /** The current progress in range [0; 1] */
    float getProgress();

    /** Initiates an orderly shutdown. No new tasks are accepted, and already dispatched tasks are finished*/
    void shutdown();

    /**
     * Immediate shutdown, dispatched tasks are not finished
     * @return A list of unfinished tasks
     */
    List<Runnable> shutdownNow();
}
