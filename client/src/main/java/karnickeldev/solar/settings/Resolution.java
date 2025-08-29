package karnickeldev.solar.settings;

import karnickeldev.solar.util.Logger;

import java.util.Arrays;

/**
 * Project: SOLAR
 *
 * @author KarnickelDev
 * @since 06.11.2024
 */
public enum Resolution {

    R_2560_1440(2560, 1440),
    R_1920_1080(1920, 1080),
    R_1440_1080(1440, 1080),
    R_1728_1080(1728, 1080),
    R_2520_1080(2520, 1080),
    R_1280_960(1280, 960),
    ;

    public static final Resolution FALLBACK_RESOLUTION = R_1280_960;
    public static final String[] SUPPORTED_RESOLUTIONS = Arrays.stream(Resolution.values()).map(Enum::toString).toArray(String[]::new);
    private static final String DELIMITER = "x";
    private final int width, height;

    Resolution(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public static String resolutionToString(int width, int height) {
        return width + DELIMITER + height;
    }

    public static Resolution extractResolution(String resolution) throws IllegalArgumentException {
        if (resolution == null || !resolution.contains(DELIMITER)) {
            throw new IllegalArgumentException("Invalid Resolution format: \"" + resolution + "\"");
        }

        for (Resolution curr : Resolution.values()) {
            if (curr.toString().equals(resolution)) return curr;
        }
        throw new IllegalArgumentException("Can not parse String as Resolution: \"" + resolution + "\"");
    }

    public static Resolution matchResolution(String target) {
        if (target == null) return Resolution.FALLBACK_RESOLUTION;

        String[] parts = target.split(DELIMITER);
        if (parts.length != 2) {
            Logger.error(Logger.GENERAL, "", new IllegalArgumentException("Invalid resolution format: \"" + target + "\""));
            return FALLBACK_RESOLUTION;
        }

        try {
            int width = Integer.parseInt(parts[0]);
            int height = Integer.parseInt(parts[1]);
            return matchResolution(width, height);
        } catch (Exception e) {
            return FALLBACK_RESOLUTION;
        }
    }

    private static float compute_weight(int width, int height, int targetWidth, int targetHeight) {
        return 0.0001f * Math.abs((width * height) - (targetWidth * targetHeight)) + Math.abs((width / (float) height) - (targetWidth / (float) targetHeight));
    }

    public static Resolution matchResolution(int targetWidth, int targetHeight) {
        Resolution bestMatch = FALLBACK_RESOLUTION;

        float min_weight = compute_weight(bestMatch.width, bestMatch.height, targetWidth, targetHeight);
        for (Resolution supported : Resolution.values()) {
            float curr_weight = compute_weight(supported.width, supported.height, targetWidth, targetHeight);
            if (curr_weight < min_weight) {
                bestMatch = supported;
                min_weight = curr_weight;
            }
        }
        return bestMatch;
    }

    public static float getAdjustedWidth(int height) {
        return (4f / 3f) * height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return resolutionToString(width, height);
    }

}
