package karnickeldev.solar.world;

import karnickeldev.solar.simulation.SimulationTimeProvider;

/**
 * @author : KarnickelDev
 * @since : 02.06.2025
 **/
public class WorldTime implements SimulationTimeProvider {

    private long tickCount;
    private long simTimeMicros;

    public WorldTime(long initialSimTimeMicros) {
        this.simTimeMicros = initialSimTimeMicros;
        this.tickCount = 0;
    }

    public void advance(long tickDurationMicros) {
        tickCount++;
        simTimeMicros += tickDurationMicros;
    }

    public void set(long simTimeMicros) {
        this.simTimeMicros = simTimeMicros;
    }

    public long getTickCount() {
        return tickCount;
    }

    public long getSimTimeMicros() {
        return simTimeMicros;
    }
}
