package karnickeldev.solar.ecs.components.server;

import karnickeldev.solar.ecs.components.HCSPositionComponent;
import karnickeldev.solar.util.Logger;

public class HCSServerSystem {

    private HCSPositionComponent[] componentBuffer = new HCSPositionComponent[3];
    private int curr = 0;
    private int next = 1;
    private int send = 2;

    private boolean initialized = false;

    public HCSServerSystem() {
        for (int i = 0; i < componentBuffer.length; i++) {
            componentBuffer[i] = new HCSPositionComponent();
        }
    }

    public HCSPositionComponent getCurrent() {
        return componentBuffer[curr];
    }

    public HCSPositionComponent getNext() {
        return componentBuffer[next];
    }

    public HCSPositionComponent getSend() {
        return componentBuffer[send];
    }

    /**
     * Swap Buffers, should be done AFTER updates (writes) are finished
     */
    public void swapBuffers() {
        int oldCurr = curr;
        curr = next;
        next = send;
        send = oldCurr;

        componentBuffer[next].clearDirty();

        // make sure one-time-only changes are present in all buffer objects
        if(!initialized) {
            initialized = true;
            for(int i = 0; i < componentBuffer.length; i++) {
                if(i == curr) continue;
                for(int e = 0; e < getCurrent().getCapacity(); e++) {
                    if(!getCurrent().has(e)) continue;
                    componentBuffer[i].add(
                        e,
                        getCurrent().getParent(e),
                        getCurrent().getLocalX(e),
                        getCurrent().getLocalY(e)
                    );
                }
            }
        }
    }

    public double getLocalX(int entityId) {
        return componentBuffer[curr].getLocalX(entityId);
    }

    public double getLocalY(int entityId) {
        return componentBuffer[curr].getLocalY(entityId);
    }

    public void add(int entityId, int parentId, double x, double y) {
        componentBuffer[next].add(entityId, parentId, x, y);
    }

}
