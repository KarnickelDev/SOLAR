package karnickeldev.solar.logging;

/**
 * @author KarnickelDev
 * @since 03.04.2026
 **/
public enum LogLevel {
    DEBUG   ("DEBUG", Ansi.GRAY),
    INFO    ("INFO ", Ansi.GREEN),
    WARN    ("WARN ", Ansi.YELLOW),
    ERROR   ("ERROR", Ansi.RED)
    ;

    private final String tag;
    private final String ansiColor;

    LogLevel(String tag, String ansiColor) {
        this.tag = tag;
        this.ansiColor = ansiColor;
    }

    public String getANSI() {
        return ansiColor;
    }

    @Override
    public String toString() {
        return tag;
    }
}
