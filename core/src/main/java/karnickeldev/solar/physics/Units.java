package karnickeldev.solar.physics;

public class Units {

    public interface Convertible {
        float getBaseFactor();
    }

    public static double G_SI = 6.6743015e-11;
    public static double G_KM_TON = G_SI * 1e-6;

    public enum Length implements Convertible {
        AU(1.495979e8f),
        KILOMETER(1f),
        METER(0.001f),
        ;

        private final float baseFactor;
        Length(float baseFactor) {
            this.baseFactor = baseFactor;
        }

        public float getBaseFactor() {return baseFactor;}
    }

    public enum Time implements Convertible {

        HOUR(1f),
        MINUTE(1f/60f),
        SECOND(1f/3600f),
        ;

        private final float baseFactor;
        Time(float baseFactor) {
            this.baseFactor = baseFactor;
        }

        public float getBaseFactor() {return baseFactor;}
    }


    public enum Mass implements Convertible {

        SOLAR_MASS(1.989e27f),
        EARTH_MASS(5.972e21f),
        KILOTON(1000f),
        TON(1f),
        KG(0.001f),
        ;

        private final float baseFactor;
        Mass(float baseFactor) {
            this.baseFactor = baseFactor;
        }

        public float getBaseFactor() {return baseFactor;}
    }


    public static float toSU(float value, Convertible unit) {
        return value * unit.getBaseFactor();
    }

    public static double toSU(double value, Convertible unit) {
        return value*unit.getBaseFactor();
    }


    public static boolean assertEqualWithinError(float a, float b, float e) {
        return Math.abs(a - b) < e;
    }

    public static boolean assertEqualWithinError(double a, double b, double e) {
        return Math.abs(a - b) < e;
    }

}
