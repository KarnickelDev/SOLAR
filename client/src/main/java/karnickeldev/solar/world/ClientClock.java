package karnickeldev.solar.world;

import karnickeldev.solar.network.net.core.PingTracker;
import karnickeldev.solar.network.sync.PacketSyncLayer;
import karnickeldev.solar.simulation.execution.SimSpeedController;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author : KarnickelDev
 * @since : 01.07.2025
 **/
public class ClientClock {

    private static final int BUFFER_SIZE = 8;

    private static class TimeSegment {
        final long simTimeAtAnchor; // server simTime at anchor (microseconds)
        final long realTimeAnchor;  // local wall time (microseconds), adjusted for RTT
        final double simSpeed;      // sim speed during this segment

        private TimeSegment(long simTimeAtAnchor, long realTimeAnchor, double simSpeed) {
            this.simTimeAtAnchor = simTimeAtAnchor;
            this.realTimeAnchor = realTimeAnchor;
            this.simSpeed = simSpeed;
        }

        @Override
        public String toString() {
            return '{' + simTimeAtAnchor + "," + realTimeAnchor + "," + simSpeed + '}';
        }
    }

    private final Deque<TimeSegment> timeline = new ArrayDeque<>();

    private final ClientSimSpeedController simSpeedController;

    private boolean paused = false;

    private byte targetSimSpeedIndex = 1;

    private long frameClockTime = 0;


    public ClientClock() {
        this.simSpeedController = new ClientSimSpeedController(this);
    }

    public ClientSimSpeedController getSimSpeedController() {
        return simSpeedController;
    }

    public boolean isPaused() {
        return paused;
    }

    public synchronized void addSegment(long simTimeMicros, long receiveMicros, double simSpeed, byte targetSimSpeedIndex) {
        long pingEstimate = Math.round(PingTracker.getAvgRTT() / 2d);

        // Adjust anchor to when the server was actually at simTime
        long adjustedAnchor = receiveMicros - pingEstimate;

        this.paused = simSpeed <= 0;

        // update simSpeed index
        this.targetSimSpeedIndex = targetSimSpeedIndex;

        // Insert new segment
        timeline.add(new TimeSegment(simTimeMicros, adjustedAnchor, Math.max(0, simSpeed)));
        if (timeline.size() > BUFFER_SIZE) {
            timeline.poll();
        }
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

    public synchronized long estimateSimTimeNow() {
        return estimateSimTimeAt(System.nanoTime() / 1000);
    }

    private long last = 0;
    public synchronized long estimateSimTimeAt(long queryMicros) {
        if (timeline.isEmpty()) return 0;

        // Find latest segment not after delayedQuery
        TimeSegment seg = null;
        for (TimeSegment s : timeline) {
            if (s.realTimeAnchor <= queryMicros) {
                seg = s;
            } else break;
        }
        if(seg == null) seg = timeline.getFirst();

        long dt = queryMicros - seg.realTimeAnchor;
        if (seg.simSpeed <= 0) {
            return seg.simTimeAtAnchor; // paused
        }

        long time = seg.simTimeAtAnchor + (long)(dt * seg.simSpeed);
        time = Math.max(time, last);
        //Logger.log("dt: " + (long)((time - last) / (seg.simSpeed)));
        last = time;
        return time;
    }

    public long nowSimSeconds() {
        return estimateSimTimeAt((System.nanoTime() / 1000) - PacketSyncLayer.syncDelayMicros) / 1_000_000;
    }

}
