package karnickeldev.solar.logging;

/**
 * @author KarnickelDev
 * @since 03.04.2026
 **/
public enum LogTag {

    GENERAL,
    DEBUG,
    STARTUP,
    SHUTDOWN,
    ASSETS,
    ECS,
    ENTITY,
    SERVER,
    SYNC,
    NETWORK,
    UI,
    SHADER,
    SCHEDULER,
    ORBT_SLVR,
    ;

    private static short maxWidth = -1;

    public static short getMaxWidth() {
        if(maxWidth > 0) return maxWidth;
        int max = 0;
        for(LogTag tag : LogTag.values()) {
            max = Math.max(max, tag.name().length());
        }
        maxWidth = (short) max;
        return maxWidth;
    }

}
