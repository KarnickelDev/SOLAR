package karnickeldev.solar.world;

import karnickeldev.solar.network.net.core.PingTracker;
import karnickeldev.solar.network.sync.PacketSyncLayer;
import karnickeldev.solar.util.MathUtil;

/**
 * @author : KarnickelDev
 * @since : 01.07.2025
 **/
public class TimeSyncManager {

    private static final double ALPHA = 0.05;

    private long smoothedOffsetMicros = 0;
    private long lastLocalMicros = 0;          // When the last sync sample was recorded
    private long lastServerSimMicros = 0;      // What the server sim time was at that point

    private boolean initialized = false;

    private float simSpeed = 0;

    public synchronized boolean isInitialized() {
        return initialized;
    }

    public synchronized void setSimSpeed(float newSimSpeed) {
        // Recompute the current simTime with old speed before changing
        long nowMicros = System.nanoTime() / 1000L;
        lastServerSimMicros = getSimTimeEstimate(nowMicros);  // Lock in current estimate
        lastLocalMicros = nowMicros;                          // Reset local reference
        this.simSpeed = newSimSpeed;
    }

    public synchronized float getSimSpeed() {
        return simSpeed;
    }

    public synchronized void recordSyncSample(long serverSimMicros, long receiveMicros) {
        long offset = serverSimMicros - receiveMicros;

        if (!initialized) {
            smoothedOffsetMicros = offset;
            initialized = true;
        } else {
            smoothedOffsetMicros = (long)(ALPHA * offset + (1 - ALPHA) * smoothedOffsetMicros);
        }

        // Update reference points for time estimate
        lastLocalMicros = receiveMicros;
        lastServerSimMicros = serverSimMicros;
    }

    /**
     * Returns the current estimated server sim time in microseconds,
     * based on the last known serverSimTime + simSpeed * time since then
     */
    public synchronized long getSimTimeEstimate() {
        long nowMicros = System.nanoTime() / 1000L;
        return getSimTimeEstimate(nowMicros);
    }


    private long getSimTimeEstimate(long nowMicros) {
        if (!initialized) return 0;

        long elapsedLocalMicros = nowMicros - lastLocalMicros;
        long simDelta = (long)(elapsedLocalMicros * simSpeed);
        return lastServerSimMicros + simDelta;
    }

    public synchronized long getCurrentTimeMicros() {
        return getSimTimeEstimate() - PacketSyncLayer.syncDelayMicros;
    }

    public synchronized long getSmoothedOffsetMicros() {
        return smoothedOffsetMicros;
    }
}

