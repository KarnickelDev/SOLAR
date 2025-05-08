package karnickeldev.solar.ecs.components;

import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.util.MathUtil;

import java.util.Arrays;
import java.util.BitSet;

public class TripleBufferedPositionComponent implements Component {

    private static int CAPACITY = 64;

    private double[] oldX = new double[CAPACITY];
    private double[] oldY = new double[CAPACITY];

    private double[] currX = new double[CAPACITY];
    private double[] currY = new double[CAPACITY];

    private double[] nextX = new double[CAPACITY];
    private double[] nextY = new double[CAPACITY];

    private BitSet hasComponent = new BitSet(CAPACITY);
    private BitSet hasNextComponent = new BitSet(CAPACITY);


    @Override
    public void ensureCapacity(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= CAPACITY) {
            CAPACITY = Math.max(2 * CAPACITY, 2 << (MathUtil.ld(index)+1));
            currX = Arrays.copyOf(currX, CAPACITY);
            currY = Arrays.copyOf(currY, CAPACITY);
            nextX = Arrays.copyOf(nextX, CAPACITY);
            nextY = Arrays.copyOf(nextY, CAPACITY);
            oldX = Arrays.copyOf(oldX, CAPACITY);
            oldY = Arrays.copyOf(oldY, CAPACITY);
        }
    }

    public synchronized void add(int entityId, double x, double y) {
        ensureCapacity(entityId);
        int index = EntityManager.extractIndex(entityId);
        this.currX[index] = x;
        this.currY[index] = y;
        hasComponent.set(index);
    }

    public synchronized void remove(int entityId) {
        hasComponent.clear(EntityManager.extractIndex(entityId));
    }

    public synchronized boolean has(int entityId) {
        return hasComponent.get(EntityManager.extractIndex(entityId));
    }

    public synchronized double getX(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= CAPACITY) return 0;
        return currX[index];
    }

    public synchronized double getY(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= CAPACITY) return 0;
        return currY[index];
    }

    public synchronized void addNext(int entityId, double x, double y) {
        ensureCapacity(entityId);
        int index = EntityManager.extractIndex(entityId);
        this.nextX[index] = x;
        this.nextY[index] = y;
        hasNextComponent.set(index);
    }

    public synchronized void advance() {
        double[] tmp_x = oldX;
        double[] tmp_y = oldY;

        BitSet tmp = hasComponent;

        oldX = currX;
        oldY = currY;

        currX = nextX;
        currY = nextY;
        hasComponent = hasNextComponent;

        nextX = tmp_x;
        nextY = tmp_y;
        hasNextComponent = tmp;
    }

    public synchronized double getInterpolatedX(int entityId, float alpha) {
        return lerp(oldX[entityId], currX[entityId], alpha);
    }

    public synchronized double getInterpolatedY(int entityId, float alpha) {
        return lerp(oldY[entityId], currY[entityId], alpha);
    }

    private static double lerp(double a, double b, double alpha) {
        return a + (b - a) * alpha;
    }

}
