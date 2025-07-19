package karnickeldev.solar.world;

import karnickeldev.solar.util.Logger;

import java.util.*;

/**
 * @author : KarnickelDev
 * @since : 01.07.2025
 **/
public class TimeSyncManager {
    // Snapshot timing
    private long simTimeAtSnapshot;      // server-sim time (micros) at snapshot
    private long localReceiveMicros;     // wall time (micros) when snapshot was received

    // Speed control
    private double simSpeed = 1;
    private double prevSpeed;

    public synchronized void updateFromSnapshot(long simTimeMicros, long receiveMicros, double newSimSpeed, long activationTime) {
        // Start new timing window
        this.simTimeAtSnapshot = simTimeMicros;
        this.localReceiveMicros = receiveMicros;

        this.prevSpeed = this.simSpeed;
        this.simSpeed = newSimSpeed;


        Logger.log("update simSpeed: " + newSimSpeed + " at: " + simTimeMicros);
    }

    public synchronized long estimateSimTimeNow() {
        return estimateSimTimeAt(nowMicros());
    }

    public synchronized long estimateSimTimeAt(long queryMicros) {
        long dt = queryMicros - localReceiveMicros;
        double speed = simSpeed;
        long time = simTimeAtSnapshot + (long) (dt * speed);

        return time;
    }

    private long nowMicros() {
        return System.nanoTime() / 1000L;
    }
}


