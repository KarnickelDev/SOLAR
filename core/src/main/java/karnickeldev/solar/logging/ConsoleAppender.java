package karnickeldev.solar.logging;

/**
 * @author KarnickelDev
 * @since 03.04.2026
 **/
final class ConsoleAppender implements LogAppender {

    @Override
    public void append(LogEvent e) {
        StringBuilder sb = new StringBuilder();

        // LOG LEVEL
        sb.append(e.level.getANSI()).append('[').append(e.level).append("] ");

        // LOG TAG
        sb.append('[');
        appendPadded(sb, e.tag, LogTag.getMaxWidth());
        sb.append("] ");

        String msg = e.args == null ? e.template : Formatter.format(e.template, e.args);
        if(e.level == LogLevel.INFO) sb.append(Ansi.RESET);
        sb.append(msg).append(Ansi.RESET);

        String formattedMsg = sb.toString();
        if(!Ansi.isEnabled()) formattedMsg = Ansi.stripAnsi(formattedMsg);

        System.out.println(formattedMsg);

        if(e.throwable != null) {
            e.throwable.printStackTrace(System.out);
        }
    }

    private static void appendPadded(StringBuilder sb, String text, int width) {
        int padding = width - text.length();
        int paddingLeft = padding / 2;
        int paddingRight = padding - paddingLeft;
        sb.repeat(" ", Math.max(0, paddingLeft));
        sb.append(text);
        sb.repeat(" ", Math.max(0, paddingRight));
    }

}
