package karnickeldev.solar.network.util;

import karnickeldev.solar.util.Logger;

import java.util.Arrays;

/**
 * @author : KarnickelDev
 * @since : 26.06.2025
 **/
public class PingTracker {

    private static final byte HISTORY_SIZE = 16;
    private static final int REFRESH_MS = 1200;

    private static final int[] rttHistoryMS = new int[HISTORY_SIZE];
    private static int pos = 0;

    private static float avg_rtt = 0;
    private static int median = 0;
    private static float std_deviation = 0;

    private static long lastUpdate = 0;


    public static void add(long rttMicros) {
        long rttMS = rttMicros / 1_000;
        if(rttMS > Integer.MAX_VALUE) {
            Logger.error("Insanely high Ping, something is very wrong!");
            return;
        }

        rttHistoryMS[pos] = (int) rttMS;
        pos = (pos + 1) % rttHistoryMS.length;

        long now = System.nanoTime();
        if((now - lastUpdate) / 1_000_000 > REFRESH_MS) {
            lastUpdate = now;
            averageAndStandardDeviation();
            //medianRTT();
        }
    }

    public static int getPing() {
        return Math.round(0.5f * avg_rtt);
    }

    public static float getMedian() {
        return 0.5f * median;
    }

    public static float getStdDev() {
        return 0.5f * std_deviation;
    }

    private static void medianRTT() {
        int[] copy = Arrays.copyOf(rttHistoryMS, rttHistoryMS.length);
        Arrays.sort(copy);
        if(copy.length % 2 == 0) {
            median = ( (copy[copy.length/2]) + (copy[-1 + copy.length/2]) ) / 2;
        } else {
            median = copy[copy.length / 2];
        }
    }

    private static void averageAndStandardDeviation() {
        float sum = 0f;
        for (int rtt : rttHistoryMS) {
            sum += rtt;
        }
        avg_rtt =  (sum / rttHistoryMS.length);

        // standard deviation
        double std_dev = 0f;
        for(int rtt : rttHistoryMS) {
            std_dev += Math.pow((rtt - avg_rtt), 2);
        }
        std_dev /= rttHistoryMS.length;
        std_deviation = (float) Math.sqrt(std_dev);
    }

}
