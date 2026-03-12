package karnickeldev.solar.util;

/**
 * @author KarnickelDev
 * @since 19.02.2026
 **/
public final class WorldPos {

    public double lx, ly;
    public short sx, sy;

    @Override
    public String toString() {
        return "POS{sx=" + sx + ",lx=" + lx + ",sy=" + sy + ",ly=" + ly + '}';
    }

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

    public WorldPos set(WorldPos other) {
        this.sx = other.sx;
        this.lx = other.lx;
        this.sy = other.sy;
        this.ly = other.ly;
        return this;
    }
}
