package karnickeldev.solar.simulation.execution;

/**
 * @author : KarnickelDev
 * @since : 20.07.2025
 **/
public class SimSpeedController {

    private static final float TRANSITION_DURATION_SECONDS = 1;

    public static final float[] SPEED_PRESETS = {
        100f, 1000f, 5_000f, 10_000f,
    };

    private float currentSimSpeed = 1.0f;
    private float targetSimSpeed = 1.0f;

    private float transitionTimer = 0.0f;

    private float startSimSpeed = 1.0f;
    private boolean transitioning = false;

    // Call when the user selects a new preset
    public void setSpeedPreset(int index) {
        if (index < 0 || index >= SPEED_PRESETS.length) return;

        float newTarget = SPEED_PRESETS[index];
        if (newTarget != targetSimSpeed) {
            startSimSpeed = currentSimSpeed;
            targetSimSpeed = newTarget;
            transitionTimer = 0f;
            transitioning = true;
        }
    }

    // Call every frame with deltaTime in seconds
    public void update(float deltaTime) {
        if (transitioning) {
            transitionTimer += deltaTime;
            float t = transitionTimer / TRANSITION_DURATION_SECONDS;

            if (t >= 1f) {
                currentSimSpeed = targetSimSpeed;
                transitioning = false;
            } else {
                currentSimSpeed = interpolate(startSimSpeed, targetSimSpeed, smoothStep(t));
            }
        }
    }

    // Smoothstep for nice easing (feel free to tweak)
    private float smoothStep(float t) {
        return t * t * (3 - 2 * t);
    }

    private float interpolate(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public float getCurrentSimSpeed() {
        return currentSimSpeed;
    }

    public float getTargetSimSpeed() {
        return targetSimSpeed;
    }

    public boolean isTransitioning() {
        return transitioning;
    }

    public byte getPresetIndex() {
        for (int i = 0; i < SPEED_PRESETS.length; i++) {
            if (SPEED_PRESETS[i] == targetSimSpeed) {
                return (byte) i;
            }
        }
        return (byte) -1;
    }

    public byte getNearestIndex(float speed) {
        for (byte i = 0; i < SPEED_PRESETS.length; i++) {
            if (SPEED_PRESETS[i] >= speed) {
                return i;
            }
        }
        return -1;
    }

}

