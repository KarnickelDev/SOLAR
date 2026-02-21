package karnickeldev.solar.util;

/**
 * @author KarnickelDev
 * @since 19.02.2026
 **/
public final class WorldPos {

    public double lx, ly;
    public short sx, sy;

    public WorldPos set(short sx, double lx, short sy, double ly) {
        this.sx = sx;
        this.lx = lx;
        this.sy = sy;
        this.ly = ly;
        return this;
    }

    public WorldPos setFromArray(short[] sectorX, double[] localX, short[] sectorY, double[] localY, int idx) {
        this.sx = sectorX[idx];
        this.lx = localX[idx];
        this.sy = sectorY[idx];
        this.ly = localY[idx];
        return this;
    }
}
