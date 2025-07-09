package karnickeldev.solar.world;

import karnickeldev.solar.network.sync.PacketSyncLayer;
import karnickeldev.solar.simulation.execution.SimulationManager;

/**
 * @author : KarnickelDev
 * @since : 01.07.2025
 **/
public class TimeEstimator {

    private long currentSimTimeMicros = 0;
    private long lastTickNanos = 0;
    private long lastEstimate = 0;

    private float simSpeed = SimulationManager.simSpeed;

    public TimeEstimator() {}

    public void setSimTime(long simTimeMicros) {
        currentSimTimeMicros = simTimeMicros;
        lastTickNanos = System.nanoTime();
    }

    public void advance(long micros) {
        currentSimTimeMicros += micros;
        lastTickNanos = System.nanoTime();
    }

    public long getSimTime() {
        return currentSimTimeMicros;
    }

    public void setSimSpeed(float simSpeed) {
        this.simSpeed = simSpeed;
    }

    public float getSimSpeed() {
        return simSpeed;
    }

    public long getSimTimeEstimate() {
        long rawEstimate = currentSimTimeMicros + (long)(((System.nanoTime() - lastTickNanos)/1e3) * simSpeed);
        long estimate = rawEstimate + PacketSyncLayer.syncDelayMicros;
        long r = Math.max(estimate, lastEstimate);
        lastEstimate = estimate;
        return r;
    }

}
