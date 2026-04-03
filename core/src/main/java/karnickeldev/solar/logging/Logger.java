package karnickeldev.solar.logging;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author KarnickelDev
 * @since 03.04.2026
 **/
public class Logger {

    private static final ConcurrentHashMap<String, Logger> LOGGERS = new ConcurrentHashMap<>();

    private final String tag;

    private Logger(String tag) {
        this.tag = tag;
    }

    public static Logger get(String tag) {
        return LOGGERS.computeIfAbsent(tag, Logger::new);
    }

    public static Logger get(LogTag tag) {
        return get(tag.name());
    }

    // INFO
    public void info(String msg) {
        log(LogLevel.INFO, msg, null, null);
    }

    public void info(String template, Object... args) {
        if(!LogManager.isEnabled(LogLevel.INFO)) return;
        log(LogLevel.INFO, template, args, null);
    }

    // DEBUG
    public void debug(String msg) {
        log(LogLevel.DEBUG, msg, null, null);
    }

    public void debug(String template, Object... args) {
        if(!LogManager.isEnabled(LogLevel.DEBUG)) return;
        log(LogLevel.DEBUG, template, args, null);
    }

    // WARN
    public void warn(String msg) {
        log(LogLevel.WARN, msg, null, null);
    }

    public void warn(String format, Object... args) {
        if(!LogManager.isEnabled(LogLevel.WARN)) return;
        log(LogLevel.WARN, format, args, null);
    }

    // ERROR
    public void error(String msg) {
        log(LogLevel.ERROR, msg, null, null);
    }

    public void error(String msg, Throwable throwable) {
        log(LogLevel.ERROR, msg, null, throwable);
    }

    public void error(Throwable throwable, String template, Object... args) {
        log(LogLevel.ERROR, template, args, throwable);
    }

    // CORE
    private void log(LogLevel level, String template, Object[] args, Throwable throwable) {
        LogManager.enqueue(new LogEvent(level, tag, template, args, throwable));
    }

}
