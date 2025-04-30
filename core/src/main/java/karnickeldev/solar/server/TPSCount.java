package karnickeldev.solar.server;

import karnickeldev.solar.core.Logger;

public class TPSCount {

    private static final float RAW_TPS_UPDATE_THRESHOLD = 0.2f; // in seconds
    private static final float TPS_UPDATE_THRESHOLD = 0.8f; // in seconds
    private static final float DELAY_UPDATE_THRESHOLD = 5f;

    private final int[] ticks;
    private long prevTick = 0;
    private int pos = 0;

    private float rawTPS = 0;
    private float smoothTPS = 0;
    private float duration_raw = 0;
    private float duration_smooth = 0;
    private float duration_delay = 0;

    private long maxTickDuration = 0;
    private long minTickDuration = Long.MAX_VALUE;

    private int tickCount = 0;
    private int delayedCount = 0;
    private float delayedness = 0f;

    public TPSCount(float timeAverageSeconds) {
        int size = (Math.round(timeAverageSeconds / RAW_TPS_UPDATE_THRESHOLD));
        ticks = new int[Math.max(4, size)];
    }

    public void tick(boolean isDelayed) {
        long now = System.nanoTime();
        if (prevTick == 0) {
            prevTick = now;
            return; // Skip this tick to avoid huge delta
        }
        long delta = now - prevTick;
        if(delta >= Integer.MAX_VALUE - 16) {
            Logger.debug("Tick delta too large for Integer!");
            delta = Integer.MAX_VALUE;
        }
        prevTick = now;

        // measure delayed ticks + overflow protection (happens at ~ 20days runtime with 1200 fps)
        tickCount++;
        if(isDelayed) delayedCount++;
        if(tickCount >= Integer.MAX_VALUE - 10) {
            tickCount = 1000;
            delayedCount = (int) (delayedness * tickCount);
            throw new RuntimeException("overflow");
        }



        maxTickDuration = Math.max(maxTickDuration, delta);
        minTickDuration = Math.min(minTickDuration, delta);

        duration_raw += delta / 1e9f;
        if(duration_raw >= RAW_TPS_UPDATE_THRESHOLD) {
            ticks[pos] = (int) delta;
            pos = (pos + 1) % ticks.length;

            duration_smooth += duration_raw;
            duration_raw = 0;

            float sum = 0;
            for (long tick : ticks) {
                sum += (tick / 1e9f);
            }

            rawTPS = ticks.length / sum;
        }

        if(duration_smooth >= TPS_UPDATE_THRESHOLD) {
            duration_delay += duration_smooth;

            smoothTPS = rawTPS;
            delayedness = ((float) delayedCount) / tickCount;
            duration_smooth = 0;
        }

        if(duration_delay >= DELAY_UPDATE_THRESHOLD) {
            duration_delay = 0;
            tickCount = 10;
            delayedCount = (int) (delayedness * tickCount);
        }
    }

    public float getRawTPS() {
        return rawTPS;
    }

    public float getTPS() {
        return smoothTPS;
    }

    public float getMaxTickDuration() {
        return maxTickDuration / 1e6f;
    }

    public float getMinTickDuration() {
        return minTickDuration / 1e6f;
    }

    public float getDelayedness() {
        return delayedness;
    }
}
