package karnickeldev.solar.util.threadlayout;

import karnickeldev.solar.logging.LogTag;
import karnickeldev.solar.logging.Logger;

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
    private final boolean useAffinity;

    public ThreadContext(String name, boolean useAffinity, List<Integer> cpuIds) {
        this.name = name;
        this.cpuIds = cpuIds;
        this.useAffinity = useAffinity && cpuIds != null && !cpuIds.isEmpty();
    }

    public ThreadContext(String name) {
        this(name, false, null);
    }

    public String getName() {
        return name;
    }

    public List<Integer> getCpuIds() {
        return cpuIds;
    }

    /**
     * Safe access to number of threads, ALWAYS at least 1
     * @return number of threads for this task
     */
    public int getThreadCount() {
        return cpuIds == null ? 1 : cpuIds.size();
    }

    public int nextCpuId() {
        if (cpuIds.isEmpty()) {
            Logger.get(LogTag.GENERAL).error("empty list of CPU cores, using defaults");
            return 0;
        }
        int idx = Math.abs(roundRobin.getAndIncrement());
        return cpuIds.get(idx % cpuIds.size());
    }

    public boolean useCoreAffinity() {
        return useAffinity;
    }
}
