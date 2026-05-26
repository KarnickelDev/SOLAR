package karnickeldev.solar.util;

import com.badlogic.gdx.graphics.Color;

import java.util.Random;

public class MathUtil {

    private static final Random random = new Random(System.currentTimeMillis());

    private static final double PI_2 = Math.PI * 2;

    public static float clamp(float value, float min, float max) {
        return Math.min(Math.max(min, value), max);
    }

    public static double clamp(double value, double min, double max) {
        return Math.min(Math.max(min, value), max);
    }

    public static int clamp(int value, int min, int max) {
        return Math.min(Math.max(min, value), max);
    }

    public static float getStandardNormalDistribution(float x) {
        return (float) ((1f / 2 * Math.sqrt(PI_2)) * Math.pow(Math.E, -0.5f * (x * x)));
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

    public static float normalizeRotationDeg(float degrees) {
        float angle = degrees % 360f;
        while(angle < 0f) angle += 360f;
        return angle;
    }

    public static double normalizeRotationRad(double radians) {
        double angle = radians % PI_2;
        while(angle < 0f) angle += PI_2;
        return angle;
    }

    public static float mapLinear(float value, float inX, float inY, float outX, float outY) {
        float v = clamp(value, inX, inY);
        return outX + (v / (inY - inX)) * (outY - outX);
    }

    public static boolean AABB(int x, int y, int x1, int y1, int x2, int y2) {
        return x >= x1 && x <= x2 && y >= y1 && y <= y2;
    }

    public static boolean AABB(float x, float y, float x1, float y1, float x2, float y2) {
        return x >= x1 && x <= x2 && y >= y1 && y <= y2;
    }

    public static float packColor(int rgba8888) {
        return Float.intBitsToFloat(Integer.reverseBytes(rgba8888) & 0xfeffffff);
    }

}
