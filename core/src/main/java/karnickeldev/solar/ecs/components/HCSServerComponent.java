package karnickeldev.solar.ecs.components;

import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.util.MathUtil;

import java.util.Arrays;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicReference;

public class HCSServerComponent implements Component {

    private static int CAPACITY = 64;

    private int[] parentIds = new int[CAPACITY];

    private double[] localPos = new double[2*CAPACITY];
    private double[] worldPos = new double[2*CAPACITY];

    private double[] prevLocalPos = new double[2*CAPACITY];
    private double[] currLocalPos = new double[2*CAPACITY];
    private double[] nextLocalPos = new double[2*CAPACITY];

    private boolean[] dirtyFlags = new boolean[CAPACITY];
    private final BitSet hasComponent = new BitSet(CAPACITY);

    public final AtomicReference<RenderBuffer> buffer = new AtomicReference<>();

    public static class RenderBuffer {
        public final double[] curr, prev;

        public RenderBuffer(double[] curr, double[] prev) {
            this.curr = curr;
            this.prev = prev;
        }
    }

    public HCSServerComponent() {
        buffer.set(new RenderBuffer(currLocalPos, prevLocalPos));
    }

    @Override
    public void ensureCapacity(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if (index >= CAPACITY) {
            CAPACITY = Math.max(2 * CAPACITY, 2 << (MathUtil.ld(index) + 1));
            parentIds = Arrays.copyOf(parentIds, CAPACITY);
            dirtyFlags = Arrays.copyOf(dirtyFlags, CAPACITY);

            localPos = Arrays.copyOf(localPos, 2*CAPACITY);
            worldPos = Arrays.copyOf(worldPos, 2*CAPACITY);

            prevLocalPos = Arrays.copyOf(prevLocalPos, 2*CAPACITY);
            currLocalPos = Arrays.copyOf(currLocalPos, 2*CAPACITY);
            nextLocalPos = Arrays.copyOf(nextLocalPos, 2*CAPACITY);

            buffer.set(new RenderBuffer(currLocalPos, prevLocalPos));
        }
    }

    public void add(int entityId, int parentId, double localX, double localY, double worldX, double worldY) {
        ensureCapacity(entityId);
        int index = EntityManager.extractIndex(entityId);

        parentIds[index] = parentId;

        this.localPos[2*index] = localX;
        this.localPos[(2*index)+1] = localY;

        this.nextLocalPos[2*index] = localX;
        this.nextLocalPos[(2*index)+1] = localY;

        this.worldPos[2*index] = worldX;
        this.worldPos[(2*index)+1] = worldY;

        dirtyFlags[index] = true;
        hasComponent.set(index);
    }

    public void remove(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        hasComponent.clear(index);
    }

    public boolean has(int entityId) {
        return hasComponent.get(EntityManager.extractIndex(entityId));
    }

    public void markDirty(int entityId) {
        dirtyFlags[EntityManager.extractIndex(entityId)] = true;
    }

    public boolean isDirty(int entityId) {
        return dirtyFlags[EntityManager.extractIndex(entityId)];
    }

    public int getParent(int entityId) {
        return parentIds[EntityManager.extractIndex(entityId)];
    }

    public double getPhysicsLocalX(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= 2*CAPACITY) return 0;
        return localPos[2*index];
    }

    public double getPhysicsLocalY(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= 2*CAPACITY) return 0;
        return localPos[(2*index)+1];
    }

    public double getWorldX(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= 2*CAPACITY) return 0;
        return worldPos[2*index];
    }

    public double getWorldY(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= 2*CAPACITY) return 0;
        return worldPos[(2*index)+1];
    }

    /**
     * Atomically swap Buffers to update render Positions
     * ONLY call from Physics Thread
     */
    public void syncRenderBuffers() {
        double[] tmp = prevLocalPos;
        prevLocalPos = currLocalPos;
        currLocalPos = nextLocalPos;
        nextLocalPos = tmp;

        buffer.set(new RenderBuffer(currLocalPos, prevLocalPos));
    }

    public RenderBuffer getLocals() {
        return buffer.get();
    }

}

