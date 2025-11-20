package karnickeldev.solar.util;

/**
 * @author KarnickelDev
 * @since 30.09.2025
 **/
public class BlackBodyUtil {

    public static float[] rgb(float temperature) {
        float t = MathUtil.clamp(temperature, 1000f, 40000f) / 100f;

        float r, g, b;

        // Red
        if (t <= 66.0) {
            r = 1.0f;
        } else {
            r = (float) Math.clamp(1.292936186062745 * Math.pow(t - 60.0, -0.1332047592), 0.0, 1.0);
        }

        // Green
        if (t <= 66.0) {
            g = (float) Math.clamp(0.3900815787690196 * Math.log(t) - 0.6318414437886275, 0.0, 1.0);
        } else {
            g = (float) Math.clamp(1.129890860895294 * Math.pow(t - 60.0, -0.0755148492), 0.0, 1.0);
        }

        // Blue
        if (t >= 66.0) {
            b = 1.0f;
        } else if (t <= 19.0) {
            b = 0.0f;
        } else {
            b = (float) Math.clamp(0.543206789110196 * Math.log(t - 10.0) - 1.19625408914, 0.0, 1.0);
        }

        return new float[]{r, g, b};
    }


}
