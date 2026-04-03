package karnickeldev.solar.logging;

/**
 * @author KarnickelDev
 * @since 03.04.2026
 **/
public interface LogAppender {
    void append(LogEvent event);
    void close();
}
