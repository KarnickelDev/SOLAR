package karnickeldev.solar.world;

import karnickeldev.solar.network.net.core.PingTracker;
import karnickeldev.solar.network.sync.PacketSyncLayer;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author KarnickelDev
 * @since 01.07.2025
 **/
public final class ClientClock {

    private static final double MAX_DRIFT_MICROS = 300_000;
    private static final long SOFT_CATCHUP_MICROS = 5_000;

    private record ClockSnapshot(long simTimeAnchor, long realTimeAnchor, double simSpeed) {}

    private final AtomicReference<ClockSnapshot> snapshot = new AtomicReference<>(new ClockSnapshot(0,0,0));

    private final ClientSimSpeedController simSpeedController;

    private volatile boolean paused = false;

    private volatile byte targetSimSpeedIndex = 1;

    private volatile long frameClockTime = 0;
    private volatile long lastComputedSimTime = 0;


    public ClientClock() {
        this.simSpeedController = new ClientSimSpeedController(this);
    }

    public ClientSimSpeedController getSimSpeedController() {
        return simSpeedController;
    }

    public boolean isPaused() {
        return paused;
    }

    public void updateClockData(long simTimeMicros, long receiveMicros, double simSpeed, byte targetSimSpeedIndex) {
        long pingEstimate = (long) (PingTracker.getAvgRTT() / 2f);

        // Adjust anchor to when the server was actually at simTime
        long adjustedAnchor = receiveMicros + pingEstimate;

        // update simSpeed index
        this.targetSimSpeedIndex = targetSimSpeedIndex;
        this.paused = simSpeed <= 0;

        ClockSnapshot old = snapshot.get();
        long blendedAnchor;
        if(old.realTimeAnchor > 0) {
            long error = adjustedAnchor - old.realTimeAnchor;
            double blendFactor = Math.clamp(Math.abs(error) / MAX_DRIFT_MICROS, 0.5, 0.9);
            blendedAnchor = old.realTimeAnchor + (long) (error * blendFactor);
        } else {
            blendedAnchor = adjustedAnchor;
        }

       snapshot.set(new ClockSnapshot(simTimeMicros, blendedAnchor, Math.max(0, simSpeed)));

    }

    public long estimateSimTimeAt(long queryMicros) {
        ClockSnapshot snap = snapshot.get();
        long dt = queryMicros - snap.realTimeAnchor;

        long estimatedSimTime;
        if (snap.simSpeed <= 0) {
            estimatedSimTime = snap.simTimeAnchor;
        } else {
            estimatedSimTime = snap.simTimeAnchor + (long) (dt * snap.simSpeed);
        }

        long minAllowed = lastComputedSimTime - SOFT_CATCHUP_MICROS;
        estimatedSimTime = Math.max(minAllowed, estimatedSimTime);
        lastComputedSimTime = estimatedSimTime;
        return estimatedSimTime;
    }

    public int getTargetSimSpeedIndex() {
        return targetSimSpeedIndex;
    }

    public void updateFrameClockTime() {
        frameClockTime = estimateSimTimeAt((System.nanoTime() / 1000) - PacketSyncLayer.syncDelayMicros);
    }

    public long getFrameClockTime() {
        return frameClockTime;
    }

    public long nowSimSeconds() {
        return frameClockTime / 1_000_000;
    }

}
