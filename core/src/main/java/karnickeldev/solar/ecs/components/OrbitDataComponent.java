package karnickeldev.solar.ecs.components;

import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.util.MathUtil;

import java.util.Arrays;
import java.util.BitSet;

public class OrbitDataComponent implements Component {

    private static int CAPACITY = 16;

    private float[] semiMajorAxis = new float[CAPACITY];
    private float[] eccentricity = new float[CAPACITY];
    private float[] omega = new float[CAPACITY];        // Argument of periapsis (radians)
    private float[] t0 = new float[CAPACITY];           // Time of periapsis passage

    private int[] centralBody = new int[CAPACITY];

    private final BitSet hasComponent = new BitSet(CAPACITY);


    @Override
    public void ensureCapacity(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= CAPACITY) {
            CAPACITY = Math.max(2 * CAPACITY, 2 << (MathUtil.ld(index)+1));
            semiMajorAxis = Arrays.copyOf(semiMajorAxis, CAPACITY);
            eccentricity = Arrays.copyOf(eccentricity, CAPACITY);
            omega = Arrays.copyOf(omega, CAPACITY);
            t0 = Arrays.copyOf(t0, CAPACITY);
            centralBody = Arrays.copyOf(centralBody, CAPACITY);
        }
    }

    public void add(int entityId, float semiMajorAxis, float eccentricity, float omega, float t0,
                    int centralBody) {
        ensureCapacity(entityId);
        int index = EntityManager.extractIndex(entityId);
        this.semiMajorAxis[index] = semiMajorAxis;
        this.eccentricity[index] = eccentricity;
        this.omega[index] = omega;
        this.t0[index] = t0;
        this.centralBody[index] = centralBody;
        hasComponent.set(index);
    }

    public void add(int entityId, OrbitData orbitData) {
        add(entityId, orbitData.getSemiMajorAxis(), orbitData.getEccentricity(), orbitData.getOmega(),
            orbitData.getT0(), orbitData.getCentralBody().getEntityId());
    }

    public void remove(int entityId) {
        hasComponent.clear(EntityManager.extractIndex(entityId));
    }

    public boolean has(int entityId) {
        return hasComponent.get(EntityManager.extractIndex(entityId));
    }

    public float getSemiMajorAxis(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= CAPACITY) return 0;
        return semiMajorAxis[index];
    }

    public float getEccentricity(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= CAPACITY) return 0;
        return eccentricity[index];
    }

    public float getOmega(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= CAPACITY) return 0;
        return omega[index];
    }

    public float getT0(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= CAPACITY) return 0;
        return t0[index];
    }

    public int getCentralBody(int entityId) {
        int index = EntityManager.extractIndex(entityId);
        if(index >= CAPACITY) return 0;
        return centralBody[index];
    }
}
