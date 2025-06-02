package karnickeldev.solar.ecs.systems;

import karnickeldev.solar.ecs.components.ComponentSnapshotProvider;
import karnickeldev.solar.ecs.components.HCSPositionComponent;
import karnickeldev.solar.ecs.components.HCSPositionSnapshot;
import karnickeldev.solar.util.MathUtil;

public class HCSClientSystem implements ComponentSnapshotProvider<HCSPositionSnapshot> {

    private final HCSPositionComponent[] componentBuffer = new HCSPositionComponent[2];
    private int prev = 0;
    private int curr = 1;

    private long lastUpdate;

    private HCSPositionSnapshot lastSnapshot;

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
        if (lastSnapshot != null && snapshot.tick <= lastSnapshot.tick) return;

        curr = (curr + 1) % 2;
        prev = (prev + 1) % 2;
        componentBuffer[curr].applySnapshot(snapshot);
        if(lastSnapshot == null) componentBuffer[prev].applySnapshot(snapshot);

        lastSnapshot = snapshot;
        lastUpdate = System.nanoTime();
    }

    public double getAlpha(int tickRate) {
        return (System.nanoTime() - lastUpdate) * (tickRate * 1e-9);
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
        this.update(snapshot);
    }

    @Override
    public void ensureCapacity(int entityId) {
        throw new UnsupportedOperationException("Not supported for " + this.getClass().getName());
    }

    @Override
    public HCSPositionSnapshot createSnapshot(long tick) {
        throw new UnsupportedOperationException("Not supported for " + this.getClass().getName());
    }
}
