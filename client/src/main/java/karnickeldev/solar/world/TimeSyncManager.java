package karnickeldev.solar.world;

/**
 * @author : KarnickelDev
 * @since : 01.07.2025
 **/
public class TimeSyncManager {

    private long lastSnapshotSimTime = 0;
    private long lastSnapshotReceiveTime = 0;
    private float simSpeed = 1f;
    private float prevSimSpeed = 1f;


    public synchronized void reportSample(long simTime, long receiveMicros, float simSpeed) {
        this.lastSnapshotSimTime = simTime;
        this.lastSnapshotReceiveTime = receiveMicros;
        this.prevSimSpeed = this.simSpeed;
        this.simSpeed = simSpeed;
    }

    public synchronized long estimateSimTime(long currentMicros) {
        long elapsedMicros = currentMicros - lastSnapshotReceiveTime;
        return lastSnapshotSimTime + Math.round(elapsedMicros * (double)prevSimSpeed);
    }

    public synchronized float getSimSpeed() {
        return prevSimSpeed;
    }
}

