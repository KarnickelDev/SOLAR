package karnickeldev.solar.ecs.components.client;

import karnickeldev.solar.ecs.components.server.HCSPositionComponent;
import karnickeldev.solar.ecs.components.server.HCSPositionSnapshot;
import karnickeldev.solar.util.MathUtil;

public class HCSClientSystem {

    private final HCSPositionComponent[] componentBuffer = new HCSPositionComponent[2];
    private int prev = 0;
    private int curr = 1;

    private HCSPositionSnapshot lastSnapshot;

    public HCSClientSystem() {
        for(int i = 0; i < componentBuffer.length; i++) {
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
        if(lastSnapshot != null && snapshot.tick <= lastSnapshot.tick) return;

        curr = (curr + 1) % 2;
        prev = (prev + 1) % 2;
        componentBuffer[curr].applySnapshot(snapshot);

        lastSnapshot = snapshot;
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
}
