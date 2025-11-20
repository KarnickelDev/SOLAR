package karnickeldev.solar.world;


import karnickeldev.solar.network.net.core.PingTracker;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author KarnickelDev
 * @since 01.07.2025
 **/
public class ClientClockOld {


    private static class TimeSegment {
        final long simTimeAtAnchor;   // server simTime (µs) at anchor
        final long realTimeAnchor;    // local wall time (µs), adjusted for RTT
        final double simSpeed;        // sim speed during this segment

        TimeSegment(long simTimeAtAnchor, long realTimeAnchor, double simSpeed) {
            this.simTimeAtAnchor = simTimeAtAnchor;
            this.realTimeAnchor = realTimeAnchor;
            this.simSpeed = simSpeed;
        }
    }

    // Small deque of segments (last few snapshots)
    private final Deque<TimeSegment> timeline = new ArrayDeque<>();

    // Delay buffer to smooth jitter (adjust to taste, e.g. 100ms = 100_000 µs)
    private long interpolationDelayMicros = 100_000;

    // RTT/one-way delay estimate
    private long estimatedOneWayDelayMicros = 50_000; // start with 50ms

    // Current pause state
    private boolean paused = false;

    private final ClientSimSpeedController clientSimSpeedController;


    public ClientClockOld() {
        clientSimSpeedController = new ClientSimSpeedController(null);
    }

    public synchronized void updateFromSnapshot(
        long simTimeMicros,
        long receiveMicros,
        double newSimSpeed
    ) {
        // Update one-way delay estimate (smoothed)
        estimatedOneWayDelayMicros = Math.round(PingTracker.getAvgRTT() / 2);

        // Adjust anchor to when the server was actually at simTime
        long adjustedAnchor = receiveMicros - estimatedOneWayDelayMicros;

        // Insert new segment
        timeline.addLast(new TimeSegment(simTimeMicros, adjustedAnchor, newSimSpeed));
        if (timeline.size() > 8) {
            timeline.removeFirst();
        }
    }

    public void updateTargetSimSpeedIndex(int index) {

    }

    public synchronized void setPaused(boolean paused, long simTime, long receive) {
        if (this.paused != paused) {
            this.paused = paused;
            // Insert a segment with speed=0 when paused, or restore last known speed when unpaused
            double simSpeed = paused ? 0.0 : getLastKnownSimSpeed();
            timeline.addLast(new TimeSegment(simTime, receive, simSpeed));
            if (timeline.size() > 8) {
                timeline.removeFirst();
            }
        }
    }

    public int getTargetSimSpeedIndex() {
        return 1;
    }

    public boolean isPaused() {
        return paused;
    }

    public ClientSimSpeedController getSimSpeedController() {
        return clientSimSpeedController;
    }

    private double getLastKnownSimSpeed() {
        return timeline.isEmpty() ? 1.0 : timeline.getLast().simSpeed;
    }

    public synchronized long estimateSimTimeNow() {
        return estimateSimTimeAt(nowMicros());
    }

    long last = 0;
    public synchronized long estimateSimTimeAt(long queryMicros) {
        if (timeline.isEmpty()) return 0;

        long delayedQuery = nowMicros() - interpolationDelayMicros;

        // Find latest segment not after delayedQuery
        TimeSegment seg = null;
        for (TimeSegment s : timeline) {
            if (s.realTimeAnchor <= delayedQuery) {
                seg = s;
            } else break;
        }
        if (seg == null) seg = timeline.getFirst();

        long dt = delayedQuery - seg.realTimeAnchor;
        if (seg.simSpeed == 0.0) {
            return seg.simTimeAtAnchor; // paused
        }

        long t = seg.simTimeAtAnchor + (long)(dt * seg.simSpeed);
        //Logger.log("time: " + t + ", delta: " + ((t - last) / seg.simSpeed));
        last = t;
        return t;
    }

    private long nowMicros() {
        return System.nanoTime() / 1000L;
    }

    public long nowSimSeconds() {
        return estimateSimTimeNow() / 1_000_000;
    }

}


