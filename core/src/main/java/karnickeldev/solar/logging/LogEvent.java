package karnickeldev.solar.logging;

/**
 * INTERNAL ONLY class to hold data for Log Entries
 * @author KarnickelDev
 * @since 03.04.2026
 **/
public final class LogEvent {

    final long time;
    final LogLevel level;
    final String tag;
    final String template;
    final Object[] args;
    final Throwable throwable;

    LogEvent(LogLevel level, String tag, String template, Object[] args, Throwable throwable) {
        this.time = System.currentTimeMillis();
        this.level = level;
        this.tag = tag;
        this.template = template;
        this.args = args;
        this.throwable = throwable;
    }
}
