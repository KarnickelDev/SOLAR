package karnickeldev.solar.util;

import karnickeldev.solar.physics.Units;

/**
 * @author KarnickelDev
 * @since 21.02.2026
 **/
public class WorldDelta {

    public int dsx, dsy;
    public double dlx, dly;

    public double toDoubleX() {
        return dlx + dsx * Units.Length.AU.getBaseFactor();
    }

    public double toDoubleY() {
        return dly + dsy * Units.Length.AU.getBaseFactor();
    }

    public static WorldDelta delta(WorldDelta delta, WorldPos a, WorldPos b) {
        delta.dsx = a.sx - b.sx;
        delta.dsy = a.sy - b.sy;
        delta.dlx = a.lx - b.lx;
        delta.dly = a.ly - b.ly;
        return delta;
    }

}
