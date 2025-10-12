package karnickeldev.solar.ecs.systems;

import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.ecs.components.ComponentSnapshotProvider;
import karnickeldev.solar.ecs.components.HCSPositionComponent;
import karnickeldev.solar.ecs.components.HCSPositionSnapshot;
import karnickeldev.solar.network.sync.PacketSyncLayer;
import karnickeldev.solar.util.MathUtil;

public class HCSClientSystem implements ComponentSnapshotProvider<HCSPositionSnapshot> {

    private final HCSPositionComponent[] componentBuffer = new HCSPositionComponent[2];
    private int prev = 0;
    private int curr = 1;

    private HCSPositionSnapshot latestSnapshot, previousSnapshot;

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

    public void swapBuffers() {
        curr = (curr + 1) % 2;
        prev = (prev + 1) % 2;
    }

    public void update(HCSPositionSnapshot snapshot) {
        swapBuffers();

        componentBuffer[curr].applySnapshot(snapshot);
        if(previousSnapshot == null) {
            componentBuffer[prev].applySnapshot(snapshot);
        }

        previousSnapshot = latestSnapshot;
        latestSnapshot = snapshot;

        //Logger.log("update");
    }

    long last = 0;

    public double getAlpha() {
        HCSPositionSnapshot a = previousSnapshot;
        HCSPositionSnapshot b = latestSnapshot;
        if (a == null || b == null) return 0;

        long simA = a.simTimeMicros;
        long simB = b.simTimeMicros;

        if (simB == simA) return 0;

        long curr = GameContext.get().getClock().getFrameClockTime();

        double alpha = (double)(curr - simB) / ((double)(simB - simA));

        //Logger.log("alpha=" + alpha + ", simA: " + simA + ", simB: " + simB + ", diff: " + (curr - last));

        last = curr;

        return MathUtil.clamp(alpha, 0, 1.15); // slightly allow extrapolation
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
