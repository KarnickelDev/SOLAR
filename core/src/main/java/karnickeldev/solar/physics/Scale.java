package karnickeldev.solar.physics;

public class Scale {

    public static final byte SCALE_KM = 0;
    public static final byte SCALE_AU = 1;


    public static double getScaleFactorForGravityConstant(byte scale) {
        if(scale == SCALE_AU) {
            return Units.convert(1, Units.Length.KILOMETER, Units.Length.AU);
        }
        return 1d;
    }

    public static Units.Convertible getUnit(byte scale) {
        if(scale == SCALE_AU) return Units.Length.AU;
        return Units.Length.KILOMETER;
    }

}
