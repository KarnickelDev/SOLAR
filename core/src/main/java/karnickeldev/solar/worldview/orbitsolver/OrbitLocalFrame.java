package karnickeldev.solar.worldview.orbitsolver;

import karnickeldev.solar.ecs.EntityManager;

/**
 * @author KarnickelDev
 * @since 24.02.2026
 **/
public final class OrbitLocalFrame {
    private int validCount = 0;
    public final short[] sectorX = new short[EntityManager.MAX_ENTITIES];
    public final double[] localX = new double[EntityManager.MAX_ENTITIES];
    public final short[] sectorY = new short[EntityManager.MAX_ENTITIES];
    public final double[] localY = new double[EntityManager.MAX_ENTITIES];

    public void setValidCount(int validCount) {
        this.validCount = validCount;
    }

    public int getValidCount() {
        return validCount;
    }

}
