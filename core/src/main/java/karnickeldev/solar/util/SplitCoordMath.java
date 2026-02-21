package karnickeldev.solar.util;

import karnickeldev.solar.physics.Units;

/**
 * @author KarnickelDev
 * @since 19.02.2026
 **/
public final class SplitCoordMath {

    private SplitCoordMath() {}

    private static final double AU = Units.toSU(1, Units.Length.AU);
    private static final double invAU = 1.0 / AU;

    public static double delta(short s1, double l1, short s2, double l2) {
        return (s1 - s2) * AU + (l1 - l2);
    }

    public static double combine(short sector, double local) {
        return local + sector * AU;
    }

    public static double toDoubleX(WorldPos c) {
        return c.lx + c.sx * AU;
    }

    public static double toDoubleY(WorldPos c) {
        return c.ly + c.sy * AU;
    }

    public static WorldPos subInPlace(WorldPos a, short bsx, double blx, short bsy, double bly) {
        a.lx -= blx;
        a.ly -= bly;
        a.sx -= bsx;
        a.sy -= bsy;
        return a;
    }

    public static WorldPos addInPlace(WorldPos a, short bsx, double blx, short bsy, double bly) {
        a.lx += blx;
        a.ly += bly;
        a.sx += bsx;
        a.sy += bsy;
        return a;
    }

    public static WorldPos split(WorldPos c, double x, double y) {
        short sx = (short) Math.floor(x * invAU);
        double lx = x - sx * AU;
        short sy = (short) Math.floor(y * invAU);
        double ly = y - sy * AU;
        return c.set(sx, lx, sy, ly);
    }

    public static void normalizeAbsolutePos(WorldPos a) {
        while(a.lx >= AU) {
            a.lx -= AU;
            a.sx++;
        }
        while(a.lx < 0) {
            a.lx += AU;
            a.sx--;
        }
        while(a.ly >= AU) {
            a.ly -= AU;
            a.sy++;
        }
        while(a.ly < 0) {
            a.ly += AU;
            a.sy--;
        }
    }

}
