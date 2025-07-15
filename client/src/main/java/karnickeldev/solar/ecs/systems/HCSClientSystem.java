package karnickeldev.solar.ecs.systems;

import com.badlogic.gdx.Gdx;
import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.ecs.components.ComponentSnapshotProvider;
import karnickeldev.solar.ecs.components.HCSPositionComponent;
import karnickeldev.solar.ecs.components.HCSPositionSnapshot;
import karnickeldev.solar.network.sync.PacketSyncLayer;
import karnickeldev.solar.simulation.execution.SimulationManager;
import karnickeldev.solar.util.Logger;
import karnickeldev.solar.util.MathUtil;

import java.util.*;

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

    public void update(HCSPositionSnapshot snapshot) {
        curr = (curr + 1) % 2;
        prev = (prev + 1) % 2;
        componentBuffer[curr].applySnapshot(snapshot);
        if(previousSnapshot == null) {
            componentBuffer[prev].applySnapshot(snapshot);
        }

        previousSnapshot = latestSnapshot;
        latestSnapshot = snapshot;
    }

    public double getAlpha() {
        HCSPositionSnapshot a = previousSnapshot;
        HCSPositionSnapshot b = latestSnapshot;
        if (a == null || b == null) return 0;

        long simA = a.simTimeMicros;
        long simB = b.simTimeMicros;

        if (simB == simA) return 0;

        long curr = GameContext.get().getTimeSyncManager().getCurrentTimeMicros();

        double alpha = (double)(curr - simB) / ((double)(simB - simA));
        return MathUtil.clamp(alpha, 0.0, 3); // slightly allow extrapolation
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
