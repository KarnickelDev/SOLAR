package karnickeldev.solar.simulation.execution;

import karnickeldev.solar.util.Logger;
import karnickeldev.solar.world.ServerWorld;
import karnickeldev.solar.world.TickPerformanceTracker;

/**
 * @author : KarnickelDev
 * @since : 01.06.2025
 **/
public class SimulationTask implements Comparable<SimulationTask> {

    private static final byte MAX_TICK_RATE = 100;

    private static final long PROMOTE_TIMEOUT = 1_500_000_000L;

    public enum Priority implements Comparable<Priority> {
        LOW(20),
        REDUCED(40),
        NORMAL(60),
        HIGH(80),
        VERY_HIGH(100),
        ;

        private final byte tickRate;
        private final int microsPerTick;
        Priority(int tickRate) {
            this.tickRate = (byte) Math.min(MAX_TICK_RATE, tickRate);
            this.microsPerTick = 1_000_000 / tickRate;
        }
        public byte getTickRate() {
            return tickRate;
        }
        public int getMicrosPerTick() {
            return microsPerTick;
        }
    }

    static {
        // enforce correct order of Priorities
        Priority[] priorities = Priority.values();
        if(priorities.length >= Byte.MAX_VALUE) throw new IllegalStateException(SimulationTask.class.getSimpleName()
            + " Can not have that many Priorities!");
        if(priorities[priorities.length-1].getTickRate() > MAX_TICK_RATE) throw new IllegalStateException(
            SimulationTask.class.getSimpleName() + "Priorities are capped at a tick rate of " + MAX_TICK_RATE + "Hz");
        for(int i = 1; i < priorities.length; i++) {
            if(priorities[i-1].getTickRate() >= priorities[i].getTickRate())
                throw new IllegalStateException(SimulationTask.class.getSimpleName() +
                    " Priorities need to be ordered(initialized) strongly ascending by tick rate");
        }
    }

    private final ServerWorld world;

    private Priority priority;

    public final TickPerformanceTracker tpsTracker;

    private short badTickStats = 0;
    private short goodTickStats = 0;
    private long lastPriorityChange = 0;

    public SimulationTask(ServerWorld world) {
        this.world = world;
        tpsTracker = new TickPerformanceTracker(Priority.NORMAL.getTickRate());
        setPriority(Priority.NORMAL);
    }

    @Override
    public String toString() {
        return "SimTask(wID:" + getWorld().getID() + ",p:" + getPriority() + ",tps:" + Math.round(tpsTracker.getTPS())
            + ",avg:" + (tpsTracker.getAvgTickDuration()/1e6f) + ')';
    }

    public void setPriority(Priority priority) {
        this.priority = (priority == null) ? Priority.NORMAL : priority;
        tpsTracker.setTickRate(this.priority.getTickRate());
    }

    public Priority getPriority() {
        return this.priority;
    }

    public byte getDynamicPriority() {
        // this is safe because we statically enforce ordinal() to fit into byte
        return (byte) priority.ordinal();
    }

    @Override
    public int compareTo(SimulationTask other) {
        return Byte.compare(this.getDynamicPriority(), other.getDynamicPriority());
    }

    /**
     * Promotes this world to a higher tick rate
     * @return True if the priority changed
     */
    public boolean promote() {
        int oldOrder = priority.ordinal();
        for(Priority currPriority: Priority.values()) {
            if(currPriority.ordinal() == oldOrder+1) {
                setPriority(currPriority);
                return true;
            }
        }
        return false;
    }

    /**
     * Demotes this world to a lower tick rate
     * @return True if the priority changed
     */
    public boolean demote() {
        int oldOrder = priority.ordinal();
        for(Priority currPriority: Priority.values()) {
            if(currPriority.ordinal() == oldOrder-1) {
                setPriority(currPriority);
                return true;
            }
        }
        return false;
    }

    void maybeDemoteOrPromote() {
        long tickBudgetNanos = 1_000_000_000L / priority.getTickRate();
        float overloadRatio = (float)tpsTracker.getAvgTickDuration() / tickBudgetNanos;

        if (overloadRatio > 0.95f || tpsTracker.getTPS()+3 < priority.getTickRate()) { // consistently over budget
            badTickStats++;
            goodTickStats = 0;
        } else {
            goodTickStats++;
            badTickStats = 0;
        }

        if (badTickStats > 16) {
            badTickStats = 0;
            if(demote()) {
                lastPriorityChange = System.nanoTime();
            }
        } else if (goodTickStats > 64 && overloadRatio < 0.55f) {
            goodTickStats = 0;
            if(System.nanoTime() - lastPriorityChange >= PROMOTE_TIMEOUT && promote()) {
                lastPriorityChange = System.nanoTime();
            }
        }
    }

    public void runUntil(long targetSimTimeMicros) {
        long timeAccumulatorMicros = targetSimTimeMicros - world.getWorldTime().getSimTimeMicros();
        long tickIntervalMicros = Math.round(1_000_000L / (double)priority.getTickRate() * SimulationManager.simSpeed);

        while (timeAccumulatorMicros >= tickIntervalMicros) {
            long start = System.nanoTime();

            world.update(tickIntervalMicros);
            world.getWorldTime().advance(tickIntervalMicros);

            tpsTracker.recordTick(start, System.nanoTime());

            timeAccumulatorMicros -= tickIntervalMicros;
            //TODO: re-enable
            //maybeDemoteOrPromote();
        }


    }

    public ServerWorld getWorld() {
        return world;
    }
}
