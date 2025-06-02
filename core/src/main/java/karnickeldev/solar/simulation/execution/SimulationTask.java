package karnickeldev.solar.simulation.execution;

import karnickeldev.solar.world.ServerWorld;

/**
 * @author : KarnickelDev
 * @since : 01.06.2025
 **/
public class SimulationTask implements Comparable<SimulationTask> {

    private static final byte MAX_TICK_RATE = 120;

    public enum Priority implements Comparable<Priority> {
        LOW(20),
        NORMAL(40),
        HIGH(60),
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
        for(int i = 1; i < priorities.length; i++) {
            if(priorities[i-1].getTickRate() >= priorities[i].getTickRate())
                throw new IllegalStateException(SimulationTask.class.getSimpleName() +
                    " Priorities need to be ordered(initialized) strongly ascending by tick rate");
        }
    }

    private final ServerWorld world;

    private Priority priority;
    private boolean tickCatchupAllowed = true;

    public SimulationTask(ServerWorld world) {
        this.world = world;
        setPriority(Priority.NORMAL);
    }

    public void setPriority(Priority priority) {
        this.priority = (priority == null) ? Priority.NORMAL : priority;
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

    public void promote() {
        int oldOrder = priority.ordinal();
        for(Priority currPriority: Priority.values()) {
            if(currPriority.ordinal() > oldOrder) {
                setPriority(currPriority);
                return;
            }
        }
    }

    public void demote() {
        int oldOrder = priority.ordinal();
        for(Priority currPriority: Priority.values()) {
            if(currPriority.ordinal() < oldOrder) {
                setPriority(currPriority);
                return;
            }
        }
    }

    public void runUntil(long targetSimTimeMacros) {
        while(world.getWorldTime().getSimTimeMicros() + priority.microsPerTick <= targetSimTimeMacros) {
            world.getWorldTime().advance(priority.microsPerTick);
            world.update(world.getWorldTime().getSimTimeMicros());
        }
    }

    public ServerWorld getWorld() {
        return world;
    }

    public boolean isTickCatchupAllowed() {
        return tickCatchupAllowed;
    }

    public void enableTickCatchup() {
        tickCatchupAllowed = true;
    }

    public void disableTickCatchup() {
        tickCatchupAllowed = false;
    }
}
