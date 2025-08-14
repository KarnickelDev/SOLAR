package karnickeldev.solar.world;


import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : KarnickelDev
 * @since : 01.07.2025
 **/
public class ClientClock {
    // Snapshot timing
    private long simTimeAtSnapshot;      // server-sim time (micros) at snapshot
    private long localReceiveMicros;     // wall time (micros) when snapshot was received

    // Speed control
    private final AtomicInteger targetSimSpeedIndex = new AtomicInteger(0);
    private double simSpeed = 1;
    private double prevSpeed;

    // pausing
    private final AtomicBoolean paused = new AtomicBoolean(true);

    private final ClientSimSpeedController clientSimSpeedController;

    private long lastSimTime = 0;

    public ClientClock() {
        clientSimSpeedController = new ClientSimSpeedController(this);
    }
    private double driftFactor = 1.0;   // starts at normal speed
    private long lastReceiveMicros = 0;
    private long lastSimTimeMicros = 0;

    public synchronized void updateFromSnapshot(long simTimeMicros, long receiveMicros, double newSimSpeed) {
        // Start new timing window
        this.simTimeAtSnapshot = simTimeMicros;
        this.localReceiveMicros = receiveMicros;

        this.prevSpeed = this.simSpeed;
        this.simSpeed = newSimSpeed;
    }

    public void updateTargetSimSpeedIndex(int index) {
        targetSimSpeedIndex.set(index);
    }

    public void setPaused(boolean paused) {
        this.paused.set(paused);
    }

    public int getTargetSimSpeedIndex() {
        return targetSimSpeedIndex.get();
    }

    public boolean isPaused() {
        return paused.get();
    }

    public ClientSimSpeedController getSimSpeedController() {
        return clientSimSpeedController;
    }

    public synchronized double getSimSpeed() {
        return simSpeed;
    }

    public synchronized long estimateSimTimeNow() {
        return estimateSimTimeAt(nowMicros());
    }

    public synchronized long estimateSimTimeAt(long queryMicros) {
        if(paused.get()) return lastSimTime;

        long dt = queryMicros - localReceiveMicros;
        double speed = prevSpeed;

        lastSimTime = simTimeAtSnapshot + (long) (dt * speed);
        return lastSimTime;
    }

    private long nowMicros() {
        return System.nanoTime() / 1000L;
    }

    public long nowSimSeconds() {
        return estimateSimTimeNow() / 1_000_000;
    }

}


