package karnickeldev.solar.util.threadlyout;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds the CPU-Cores allocated to a "task".
 * A task can be distributed on multiple threads
 * @author KarnickelDev
 * @since 22.11.2025
 **/
public final class ThreadContext {

    private final String name;
    private final List<Integer> cpuIds;
    private final AtomicInteger roundRobin = new AtomicInteger();

    public ThreadContext(String name, List<Integer> cpuIds) {
        this.name = name;
        this.cpuIds = cpuIds;
    }

    public String getName() {
        return name;
    }

    public List<Integer> getCpuIds() {
        return cpuIds;
    }

    public int nextCpuId() {
        if (cpuIds.isEmpty()) return -1;
        int idx = Math.abs(roundRobin.getAndIncrement());
        return cpuIds.get(idx % cpuIds.size());
    }
}
