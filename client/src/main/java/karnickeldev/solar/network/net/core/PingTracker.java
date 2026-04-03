package karnickeldev.solar.network.net.core;

import karnickeldev.solar.logging.LogTag;
import karnickeldev.solar.logging.Logger;

import java.util.Arrays;

/**
 * @author KarnickelDev
 * @since 26.06.2025
 **/
public class PingTracker {

    private static final byte HISTORY_SIZE = 16;
    private static final int REFRESH_MS = 500;

    private static final int[] rttHistoryMicros = new int[HISTORY_SIZE];
    static {
        Arrays.fill(rttHistoryMicros, 0);
    }

    private static int pos = 0;

    private static float avg_rtt = 0;
    private static float std_deviation = 0;

    private static long lastUpdate = 0;

    private static boolean init = false;

    public static void add(long rttMicros) {
        if(rttMicros > Integer.MAX_VALUE) {
            Logger.get(LogTag.GENERAL).error("PingTracker: Insanely high Ping, something is very wrong!");
            return;
        }

        if(!init) {
            init = true;
            Arrays.fill(rttHistoryMicros, (int) rttMicros);
        }

        rttHistoryMicros[pos] = (int) rttMicros;
        pos = (pos + 1) % rttHistoryMicros.length;

        long now = System.nanoTime();
        if((now - lastUpdate) / 1_000_000 > REFRESH_MS) {
            lastUpdate = now;
            averageAndStandardDeviation();
        }
    }

    public static float getAvgRTT() {
        return avg_rtt;
    }

    public static int getPing() {
        return Math.round(0.5e-3f * avg_rtt);
    }

    public static float getStdDev() {
        return 0.5f * std_deviation;
    }

    private static void averageAndStandardDeviation() {
        float sum = 0f;
        for (int rtt : rttHistoryMicros) {
            sum += rtt;
        }
        avg_rtt =  (sum / rttHistoryMicros.length);

        // standard deviation
        double std_dev = 0f;
        for(int rtt : rttHistoryMicros) {
            std_dev += Math.pow((rtt - avg_rtt), 2);
        }
        std_dev /= rttHistoryMicros.length;
        std_deviation = (float) Math.sqrt(std_dev);
    }

}
