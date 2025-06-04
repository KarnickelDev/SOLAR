package karnickeldev.solar.net.sync;

/**
 * @author : KarnickelDev
 * @since : 06.06.2025
 **/
public class SimTimeEstimator {

    private float localSimSpeed = 1f;
    private float remoteSimSpeed = 1f;
    private long lastReceivedSimTime;
    private long lastLocalReceiveTime;

    public void updateRemoteSimSpeed(float remoteSimSpeed, long simSpeedReceiveNanos, long currentLocalNanos) {
        this.remoteSimSpeed = remoteSimSpeed;
        if(estimateSimTime(currentLocalNanos) >= simSpeedReceiveNanos) {
            localSimSpeed = remoteSimSpeed;
        }
    }

    public void updateRemoteSimTime(long simTimeMicros) {
        lastReceivedSimTime = simTimeMicros;
        lastLocalReceiveTime = System.nanoTime();
    }

    public long estimateSimTime(long currentLocalNanos) {
        long deltaNanos = currentLocalNanos - lastLocalReceiveTime;
        long deltaSimMicros = Math.round(deltaNanos / 1000d * localSimSpeed);
        return lastReceivedSimTime + deltaSimMicros;
    }

    public void reset() {
        lastReceivedSimTime = 0;
        lastLocalReceiveTime = 0;
    }

}
