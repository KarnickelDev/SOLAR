package karnickeldev.solar.ui.core;

/**
 * @author KarnickelDev
 * @since 20.06.2026
 **/
public final class Align {

    // Horizontal
    public static final byte LEFT   = 1 << 0; // 00000001
    public static final byte CENTER = 1 << 1; // 00000010
    public static final byte RIGHT  = 1 << 2; // 00000100

    // Vertical
    public static final byte TOP    = 1 << 3; // 00001000
    public static final byte MIDDLE = 1 << 4; // 00010000
    public static final byte BOTTOM = 1 << 5; // 00100000

    private static boolean hasSingleBit(int value) {
        return value != 0 && (value & (value - 1)) == 0;
    }

    public static byte of(byte horizontal, byte vertical) {
        if (!hasSingleBit(horizontal & (LEFT | CENTER | RIGHT)))
            throw new IllegalArgumentException("Must specify exactly one horizontal alignment");

        if (!hasSingleBit(vertical & (TOP | MIDDLE | BOTTOM)))
            throw new IllegalArgumentException("Must specify exactly one vertical alignment");

        return (byte) (horizontal | vertical);
    }

    public static boolean isLeft(byte align) {
        return (align & LEFT) != 0;
    }

    public static boolean isRight(byte align) {
        return (align & RIGHT) != 0;
    }

    public static boolean isCenter(byte align) {
        return (align & CENTER) != 0;
    }

    public static boolean isBottom(byte align) {
        return (align & BOTTOM) != 0;
    }

    public static boolean isTop(byte align) {
        return (align & TOP) != 0;
    }

    public static boolean isMiddle(byte align) {
        return (align & MIDDLE) != 0;
    }


    public static boolean isLeft(int align) {
        return (align & LEFT) != 0;
    }

    public static boolean isRight(int align) {
        return (align & RIGHT) != 0;
    }

    public static boolean isCenter(int align) {
        return (align & CENTER) != 0;
    }

    public static boolean isBottom(int align) {
        return (align & BOTTOM) != 0;
    }

    public static boolean isTop(int align) {
        return (align & TOP) != 0;
    }

    public static boolean isMiddle(int align) {
        return (align & MIDDLE) != 0;
    }

}
