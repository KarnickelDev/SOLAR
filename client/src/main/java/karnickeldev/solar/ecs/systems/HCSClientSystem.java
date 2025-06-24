package karnickeldev.solar.ecs.systems;

import karnickeldev.solar.ecs.components.ComponentSnapshotProvider;
import karnickeldev.solar.ecs.components.HCSPositionComponent;
import karnickeldev.solar.ecs.components.HCSPositionSnapshot;
import karnickeldev.solar.simulation.execution.SimulationManager;
import karnickeldev.solar.simulation.execution.SimulationManagerThread;
import karnickeldev.solar.simulation.execution.SimulationTask;
import karnickeldev.solar.util.Logger;
import karnickeldev.solar.util.MathUtil;

public class HCSClientSystem implements ComponentSnapshotProvider<HCSPositionSnapshot> {

    private final HCSPositionComponent[] componentBuffer = new HCSPositionComponent[2];
    private int prev = 0;
    private int curr = 1;

    public static float simSpeed = 3600f;

    private long previousUpdate, currentUpdate;

    private HCSPositionSnapshot lastSnapshot, previousSnapshot;

    public HCSClientSystem() {
        for (int i = 0; i < componentBuffer.length; i++) {
            componentBuffer[i] = new HCSPositionComponent();
        }
    }

    public HCSPositionComponent getCurrent() {
        return componentBuffer[curr];
    }

    public HCSPositionComponent getPrevious() {
        return componentBuffer[prev];
    }

    public void update(HCSPositionSnapshot snapshot) {
        // make sure snapshot is new
        if (lastSnapshot != null && snapshot.simTimeMicros <= lastSnapshot.simTimeMicros) return;

        curr = (curr + 1) % 2;
        prev = (prev + 1) % 2;
        componentBuffer[curr].applySnapshot(snapshot);

        if(lastSnapshot == null) {
            componentBuffer[prev].applySnapshot(snapshot);
        }


        previousSnapshot = lastSnapshot;
        lastSnapshot = snapshot;
        previousUpdate = currentUpdate;
        currentUpdate = System.nanoTime();
    }

    public double getAlpha() {
        if(lastSnapshot == null || previousSnapshot == null) return 0;

        long now = System.nanoTime();
        long elapsedMicros = (now - currentUpdate) / 1000;

        long simDelta = (lastSnapshot.simTimeMicros - previousSnapshot.simTimeMicros);
        if (simDelta <= 0) return 0; // Avoid divide by zero or bad data

        double alpha = (double)(elapsedMicros) / (simDelta / (SimulationManager.simSpeed));
        return MathUtil.clamp(alpha, 0, 1);
    }

    public double getInterpolatedX(int entityID, double alpha) {
        double a = componentBuffer[prev].getLocalX(entityID);
        double b = componentBuffer[curr].getLocalX(entityID);
        return MathUtil.lerp(a, b, alpha);
    }

    public double getInterpolatedY(int entityID, double alpha) {
        double a = componentBuffer[prev].getLocalY(entityID);
        double b = componentBuffer[curr].getLocalY(entityID);
        return MathUtil.lerp(a, b, alpha);
    }

    @Override
    public void applySnapshot(HCSPositionSnapshot snapshot) {
        if(snapshot != null) this.update(snapshot.copy());
    }

    @Override
    public void ensureCapacity(int entityId) {
        throw new UnsupportedOperationException("Not supported for " + this.getClass().getName());
    }

    @Override
    public HCSPositionSnapshot createSnapshot(long tick) {
        throw new UnsupportedOperationException("Not supported for " + this.getClass().getName());
    }

    @Override
    public HCSPositionSnapshot createFullSnapshot(long simTimeMicros) {
        throw new UnsupportedOperationException("Not supported for " + this.getClass().getName());
    }
}
