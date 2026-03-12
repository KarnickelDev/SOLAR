package karnickeldev.solar.worldview.transform;

import karnickeldev.solar.ecs.EntityManager;

/**
 * @author KarnickelDev
 * @since 24.02.2026
 **/
public final class WorldTransformData {
    int anchorCount;

    // Absolute world position per anchor
    final short[] anchorSX = new short[EntityManager.MAX_ENTITIES];
    final double[] anchorLX = new double[EntityManager.MAX_ENTITIES];
    final short[] anchorSY = new short[EntityManager.MAX_ENTITIES];
    final double[] anchorLY = new double[EntityManager.MAX_ENTITIES];

    public WorldTransformData() {}

    public int getAnchorCount() {
        return anchorCount;
    }

    public short[] getAnchorSX() {
        return anchorSX;
    }

    public double[] getAnchorLX() {
        return anchorLX;
    }

    public short[] getAnchorSY() {
        return anchorSY;
    }

    public double[] getAnchorLY() {
        return anchorLY;
    }
}
