package karnickeldev.solar.physics;

public class Units {

    public static double G_SI = 6.6743015e-11;
    public static double G_KM_TON = G_SI * 1e-6;

    public static double toSU(double value, Convertible unit) {
        return value * unit.getBaseFactor();
    }

    public static float toSU(float value, Convertible unit) {
        return (float) (value * unit.getBaseFactor());
    }

    public static double convert(double value, Convertible from, Convertible to) {
        return value * (from.getBaseFactor() / to.getBaseFactor());
    }

    public static double convert(float value, Convertible from, Convertible to) {
        return value * (from.getBaseFactor() / to.getBaseFactor());
    }

    public static boolean assertEqualWithinError(float a, float b, float e) {
        return Math.abs(a - b) < e;
    }

    public static boolean assertEqualWithinError(double a, double b, double e) {
        return Math.abs(a - b) < e;
    }

    public enum Length implements Convertible {
        AU(1.495978707e8),
        KILOMETER(1),
        METER(1e-3),
        ;

        private static final String name = "km";
        private final double baseFactor;

        Length(double baseFactor) {
            this.baseFactor = baseFactor;
        }

        public double getBaseFactor() {
            return baseFactor;
        }

        public String getBaseFactorName() {
            return name;
        }
    }

    public enum Time implements Convertible {

        YEAR(365.25f),
        DAY(1),
        HOUR((1 / 24d)),
        MINUTE(1d / (24 * 60)),
        SECOND(1d / (24 * 3600)),
        ;

        private static final String name = "day";
        private final double baseFactor;

        Time(double baseFactor) {
            this.baseFactor = baseFactor;
        }

        public double getBaseFactor() {
            return baseFactor;
        }

        public String getBaseFactorName() {
            return name;
        }
    }

    public enum Mass implements Convertible {

        SOLAR_MASS(1.989e27),
        EARTH_MASS(5.972e21),
        KILOTON(1e3),
        TON(1),
        KG(1e-3),
        ;

        private static final String name = "Ton";
        private final double baseFactor;

        Mass(double baseFactor) {
            this.baseFactor = baseFactor;
        }

        public double getBaseFactor() {
            return baseFactor;
        }

        public String getBaseFactorName() {
            return name;
        }
    }

    public interface Convertible {
        double getBaseFactor();

        String getBaseFactorName();
    }

}
