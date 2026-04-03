package karnickeldev.solar.logging;

/**
 * INTERNAL ONLY class to hold data for Log Entries
 * @author KarnickelDev
 * @since 03.04.2026
 **/
public final class LogEvent {

    public final long time;
    public final LogLevel level;
    public final String tag;
    public final String template;
    public final Object[] args;
    public final Throwable throwable;

    LogEvent(LogLevel level, String tag, String template, Object[] args, Throwable throwable) {
        this.time = System.currentTimeMillis();
        this.level = level;
        this.tag = tag;
        this.template = template;
        this.args = args;
        this.throwable = throwable;
    }
}
