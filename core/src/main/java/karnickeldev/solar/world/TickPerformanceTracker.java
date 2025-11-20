package karnickeldev.solar.world;

/**
 * @author KarnickelDev
 * @since 07.06.2025
 **/

public class TickPerformanceTracker {

    private final Object lock = new Object();

    private int tps_tick_count;
    private int avg_duration_tick_count;

    private long lastTick;

    private long totalTickDuration;
    private long avgTickDuration;

    private float tps;


    public TickPerformanceTracker(byte tickRate) {
        setTickRate(tickRate);
    }

    public void setTickRate(byte tickRate) {
        synchronized (lock) {
            // reset average duration
            avgTickDuration = 1_000_000_000 / tickRate;
            totalTickDuration = avgTickDuration;
            avg_duration_tick_count = 1;

            // reset tps
            tps = tickRate;
            tps_tick_count = 0;
            lastTick = System.nanoTime();
        }
    }

    public void recordTick(long startNanos, long endNanos) {
        tps_tick_count++;
        avg_duration_tick_count++;

        totalTickDuration += (endNanos - startNanos);

        avgTickDuration = totalTickDuration / avg_duration_tick_count;

        long now = System.nanoTime();

        boolean updateAvgDur = avg_duration_tick_count > 50;
        boolean updateTPS = now - lastTick > 1_000_000_000;

        if(updateAvgDur || updateTPS) {
            synchronized (lock) {
                if(updateAvgDur) {
                    avg_duration_tick_count = 1;
                    totalTickDuration = avgTickDuration;
                }
                if(updateTPS) {
                    tps = tps_tick_count;
                    tps_tick_count = 0;
                    lastTick = now;
                }
            }
        }
    }

    public float getTPS() {
        synchronized (lock) {
            return tps;
        }
    }

    /**
     * Average duration of a tick of this world
     * @return averaged tick duration in nanoseconds
     */
    public long getAvgTickDuration() {
        synchronized (lock) {
            return avgTickDuration;
        }
    }

}
