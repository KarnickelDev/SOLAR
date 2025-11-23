package karnickeldev.solar.util.threadlayout;

import java.util.ArrayList;
import java.util.List;

/**
 * @author KarnickelDev
 * @since 23.11.2025
 **/
@SuppressWarnings("ClassCanBeRecord")
public final class ClientThreadLayout {

    private final boolean useCoreAffinity;

    private final boolean isDedicatedServer;

    private final ThreadContext reserved;

    private final ThreadContext mainContext;

    private final ThreadContext simulationContext;

    private final ThreadContext orbitWorkerContext;

    private final ThreadContext asyncTasksContext;

    private ClientThreadLayout(boolean useCoreAffinity, boolean isDedicatedServer,
                               ThreadContext reserved,
                               ThreadContext simulationContext,
                               ThreadContext mainContext,
                               ThreadContext orbitWorkerContext,
                               ThreadContext asyncTasksContext) {
        this.useCoreAffinity = useCoreAffinity;
        this.isDedicatedServer = isDedicatedServer;
        this.reserved = reserved;
        this.simulationContext = simulationContext;
        this.mainContext = mainContext;
        this.orbitWorkerContext = orbitWorkerContext;
        this.asyncTasksContext = asyncTasksContext;
    }

    /** turn pinning threads to cores on/off */
    public boolean useCoreAffinity() {
        return useCoreAffinity;
    }

    /** main thread, either render or server-scheduler */
    public boolean isDedicatedServer() {
        return isDedicatedServer;
    }

    /** reserved threads, for example Core 0 for OS */
    public ThreadContext getReservedContext() {
        return reserved;
    }

    /** server simulation threads */
    public ThreadContext getSimulationContext() {
        return simulationContext;
    }

    /** main thread, either render or server-scheduler */
    public ThreadContext getMainContext() {
        return mainContext;
    }

    /** client render OrbitWorker threads */
    public ThreadContext getOrbitWorkerContext() {
        return orbitWorkerContext;
    }

    /** threads for async background tasks */
    public ThreadContext getAsyncTasksContext() {
        return asyncTasksContext;
    }

    public static ClientThreadLayout create(int reservedCores, boolean useCoreAffinity, boolean isDedicatedServer) {
        CpuLayout cpuLayout = new CpuLayout();

        int freeCores = Math.max(0, cpuLayout.getPhysicalCores() - reservedCores);
        short nThreads = cpuLayout.getLogicalCores();
        short smtLevel = cpuLayout.getSmtLevel();

        // reserve whole cores for OS, starting from Core 0
        List<Integer> reservedThreads = new ArrayList<>();
        for(int core = 0; core < Math.max(1, reservedCores); core++) {
            reservedThreads.addAll(cpuLayout.logicalSiblingsOf(core));
        }
        ThreadContext reserved = new ThreadContext("reserved", false, reservedThreads);

        if(freeCores >= 7) {
            if(isDedicatedServer) {
                return new ClientThreadLayout(useCoreAffinity, false,
                    reserved,
                    new ThreadContext("sim", useCoreAffinity, List.of(nThreads-5*smtLevel, nThreads-6*smtLevel)),
                    new ThreadContext("main", useCoreAffinity, List.of(nThreads-4*smtLevel)),
                    new ThreadContext("orbit-worker", useCoreAffinity, List.of(nThreads-smtLevel, nThreads-2*smtLevel, nThreads-3*smtLevel)),
                    new ThreadContext("background")
                );
            } else {
                return new ClientThreadLayout(useCoreAffinity, false,
                    reserved,
                    new ThreadContext("sim", useCoreAffinity, List.of(nThreads-5*smtLevel, nThreads-6*smtLevel)),
                    new ThreadContext("main", useCoreAffinity, List.of(nThreads-4*smtLevel)),
                    new ThreadContext("orbit-worker", useCoreAffinity, List.of(nThreads-smtLevel, nThreads-2*smtLevel, nThreads-3*smtLevel)),
                    new ThreadContext("background")
                );
            }
        }

        if(freeCores >= 5) {
            if(isDedicatedServer) {
                return new ClientThreadLayout(useCoreAffinity, true,
                    reserved,
                    new ThreadContext("sim"),
                    new ThreadContext("main", useCoreAffinity, List.of(nThreads-smtLevel)),
                    new ThreadContext("orbit-worker", useCoreAffinity, List.of(nThreads-2*smtLevel, nThreads-3*smtLevel)),
                    new ThreadContext("background", useCoreAffinity, cpuLayout.logicalSiblingsOf(nThreads-4*smtLevel))
                );
            } else {
                return new ClientThreadLayout(useCoreAffinity, false,
                    reserved,
                    new ThreadContext("sim", useCoreAffinity, List.of(nThreads-4*smtLevel, nThreads-5*smtLevel)),
                    new ThreadContext("main", useCoreAffinity, List.of(nThreads-3*smtLevel)),
                    new ThreadContext("orbit-worker", useCoreAffinity, List.of(nThreads-smtLevel, nThreads-2*smtLevel)),
                    new ThreadContext("background")
                );
            }
        }

        if(freeCores >= 3) {
            if(isDedicatedServer) {
                return new ClientThreadLayout(useCoreAffinity, true,
                    reserved,
                    new ThreadContext("sim"),
                    new ThreadContext("main", useCoreAffinity, List.of(nThreads-3*smtLevel)),
                    new ThreadContext("orbit-worker", useCoreAffinity, List.of(nThreads-smtLevel, nThreads-2*smtLevel)),
                    new ThreadContext("background")
                );
            } else {
                return new ClientThreadLayout(useCoreAffinity, false,
                    reserved,
                    new ThreadContext("sim", useCoreAffinity, List.of(nThreads-3*smtLevel)),
                    new ThreadContext("main", useCoreAffinity, List.of(nThreads-2*smtLevel)),
                    new ThreadContext("orbit-worker", useCoreAffinity, List.of(nThreads-smtLevel)),
                    new ThreadContext("background")
                );
            }
        }


        // fallback to normal os scheduling
        return new ClientThreadLayout(false, isDedicatedServer,
            new ThreadContext("reserved"),
            new ThreadContext("sim"),
            new ThreadContext("main"),
            new ThreadContext("orbit-worker"),
            new ThreadContext("background")
        );
    }

    @Override
    public String toString() {
        return "ThreadLayout[" +
            "coreAffinity=" + useCoreAffinity() + "," +
            "isDedicated=" + isDedicatedServer() + "," +
            "sim=" + getSimulationContext().getCpuIds() + "," +
            "main=" + getMainContext().getCpuIds() + "," +
            "orbitWorker=" + getOrbitWorkerContext().getCpuIds() + "," +
            "background=" + getAsyncTasksContext().getCpuIds() +
            "]";
    }

}
