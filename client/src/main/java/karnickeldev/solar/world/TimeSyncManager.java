package karnickeldev.solar.world;

/**
 * @author : KarnickelDev
 * @since : 01.07.2025
 **/
import java.util.LinkedList;
import java.util.ListIterator;

public class TimeSyncManager {

    private static final double ALPHA = 0.05;

    private long smoothedOffsetMicros = 0;
    private boolean initialized = false;

    // Reference point for interpolation
    private long lastSyncLocalMicros = 0;
    private long lastSyncServerSimMicros = 0;

    // Each entry marks when a new simSpeed starts in server sim time
    private static class SimSpeedEvent {
        long serverSimMicros;
        float simSpeed;

        SimSpeedEvent(long serverSimMicros, float simSpeed) {
            this.serverSimMicros = serverSimMicros;
            this.simSpeed = simSpeed;
        }
    }

    private final LinkedList<SimSpeedEvent> simSpeedEvents = new LinkedList<>();

    public synchronized boolean isInitialized() {
        return initialized;
    }

    public synchronized void recordSyncSample(long serverSimMicros, long receiveMicros, float simSpeed) {
        long offset = serverSimMicros - receiveMicros;

        if (!initialized) {
            smoothedOffsetMicros = offset;
            initialized = true;
        } else {
            smoothedOffsetMicros = (long)(ALPHA * offset + (1 - ALPHA) * smoothedOffsetMicros);
        }

        lastSyncLocalMicros = receiveMicros;
        lastSyncServerSimMicros = serverSimMicros;

        // Clean up any old simSpeed events before this time
        while (!simSpeedEvents.isEmpty() &&
            simSpeedEvents.getFirst().serverSimMicros <= serverSimMicros) {
            simSpeedEvents.removeFirst();
        }

        // Add the current simSpeed if this is the first or it's changed
        if (simSpeedEvents.isEmpty() || simSpeedEvents.getLast().simSpeed != simSpeed) {
            simSpeedEvents.add(new SimSpeedEvent(serverSimMicros, simSpeed));
        }
    }

    /**
     * Called when a new simSpeed is received from the server,
     * along with the sim time at which it should take effect.
     */
    public synchronized void addSimSpeedChange(long serverSimMicros, float newSimSpeed) {
        if (!simSpeedEvents.isEmpty()) {
            if (simSpeedEvents.getLast().serverSimMicros == serverSimMicros) {
                simSpeedEvents.getLast().simSpeed = newSimSpeed;
                return;
            } else if (simSpeedEvents.getLast().serverSimMicros > serverSimMicros) {
                // Drop if it's outdated
                return;
            }
        }

        simSpeedEvents.add(new SimSpeedEvent(serverSimMicros, newSimSpeed));
    }

    /**
     * Returns the current estimated sim time based on local time and simSpeed segments.
     */
    public synchronized long getSimTimeEstimate() {
        long nowMicros = System.nanoTime() / 1000L;
        return getSimTimeEstimate(nowMicros);
    }

    private long getSimTimeEstimate(long localNowMicros) {
        if (!initialized) return 0;

        long serverNowEstimate = localNowMicros + smoothedOffsetMicros;

        long simTime = lastSyncServerSimMicros;
        long simBase = lastSyncServerSimMicros;
        long localBase = lastSyncLocalMicros;

        ListIterator<SimSpeedEvent> it = simSpeedEvents.listIterator();
        float currentSpeed = it.hasNext() ? it.next().simSpeed : 1.0f;

        while (it.hasNext()) {
            SimSpeedEvent next = it.next();
            if (next.serverSimMicros <= simBase) {
                currentSpeed = next.simSpeed;
                continue;
            }

            long deltaSim = next.serverSimMicros - simBase;
            long deltaLocal = (long)(deltaSim / currentSpeed);

            if (localBase + deltaLocal >= localNowMicros) {
                break;
            }

            simTime += deltaSim;
            localBase += deltaLocal;
            simBase = next.serverSimMicros;
            currentSpeed = next.simSpeed;
        }

        long remainingLocal = localNowMicros - localBase;
        simTime += (long)(remainingLocal * currentSpeed);

        return simTime;
    }

    /**
     * Estimated current sim time adjusted for interpolation delay (used for rendering).
     */
    public synchronized long getRenderSimTime(long interpolationDelayMicros) {
        return getSimTimeEstimate() - interpolationDelayMicros;
    }

    public synchronized float getCurrentSimSpeed() {
        return simSpeedEvents.isEmpty() ? 1.0f : simSpeedEvents.getLast().simSpeed;
    }
}


