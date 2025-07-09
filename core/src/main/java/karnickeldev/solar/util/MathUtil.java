package karnickeldev.solar.util;

import com.badlogic.gdx.graphics.Color;

import java.util.Random;

public class MathUtil {

    private static final Random random = new Random(System.currentTimeMillis());

    public static float clamp(float value, float min, float max) {
        return Math.min(Math.max(min, value), max);
    }

    public static double clamp(double value, double min, double max) {
        return Math.min(Math.max(min, value), max);
    }

    public static float getStandardNormalDistribution(float x) {
        return (float) ((1f / 2 * Math.sqrt(2f * Math.PI)) * Math.pow(Math.E, -0.5f * (x * x)));
    }

    public static float random(float min, float max) {
        assert (min <= max);
        return (min + random.nextFloat() * (max - min));
    }

    public static int ld(int bits) {
        if (bits == 0) return 0;
        return 31 - Integer.numberOfLeadingZeros(bits);
    }

    public static double lerp(double a, double b, double alpha) {
        return a + ((b - a) * alpha);
    }

    public static double distance2(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return dx * dx + dy * dy;
    }

    public static Color lerpColor(float r1, float g1, float b1, float r2, float g2, float b2, float alpha) {
        float a = clamp(alpha, 0, 1f);
        return new Color((float) lerp(r1, r2, a), (float) lerp(g1, g2, a), (float) lerp(b1, b2, a), 1f);
    }

    public static float remapClamped(float inMin, float inMax, float outMin, float outMax, float value) {
        if (inMin == inMax) return outMin;
        float t = clamp((value - inMin) / (inMax - inMin), 0f, 1f);
        return outMin + t * (outMax - outMin);
    }

    public static float normalizeRotationDeg(float degrees) {
        float angle = degrees % 360f;
        if (angle < 0f) angle += 360f;
        return angle;
    }

    public static float mapLinear(float value, float inX, float inY, float outX, float outY) {
        float v = clamp(value, inX, inY);
        return outX + (v / (inY - inX)) * (outY - outX);
    }

}
