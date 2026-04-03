package karnickeldev.solar.logging.appender;

/**
 * @author KarnickelDev
 * @since 03.04.2026
 **/
public final class LogFormatter {

    public static String format(String template, Object[] args) {
        if(args == null) return template;

        StringBuilder sb = new StringBuilder();

        int argIndex = 0;

        for (int i = 0; i < template.length(); i++) {
            char c = template.charAt(i);

            if (c == '{' && i + 1 < template.length() && template.charAt(i + 1) == '}') {
                if(argIndex < args.length) sb.append(args[argIndex++]);
                i++; // skip '}'
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

}
