package karnickeldev.solar.world;


/**
 * @author : KarnickelDev
 * @since : 01.07.2025
 **/
public class ClientClock {
    // Snapshot timing
    private long simTimeAtSnapshot;      // server-sim time (micros) at snapshot
    private long localReceiveMicros;     // wall time (micros) when snapshot was received

    // Speed control
    private double simSpeed = 1;
    private double prevSpeed;

    public synchronized void updateFromSnapshot(long simTimeMicros, long receiveMicros, double newSimSpeed) {
        // Start new timing window
        this.simTimeAtSnapshot = simTimeMicros;
        this.localReceiveMicros = receiveMicros;

        this.prevSpeed = this.simSpeed;
        this.simSpeed = newSimSpeed;
    }

    public synchronized double getSimSpeed() {
        return simSpeed;
    }

    public synchronized long estimateSimTimeNow() {
        return estimateSimTimeAt(nowMicros());
    }

    public synchronized long estimateSimTimeAt(long queryMicros) {
        long dt = queryMicros - localReceiveMicros;
        double speed = prevSpeed;
        long time = simTimeAtSnapshot + (long) (dt * speed);

        return time;
    }

    private long nowMicros() {
        return System.nanoTime() / 1000L;
    }

    public long nowSimSeconds() {
        return estimateSimTimeNow() / 1_000_000;
    }

}


