package karnickeldev.solar.core;

/**
 * Project: SOLAR
 *
 * @author KarnickelDev
 * @since 06.11.2024
 */
public class Logger {

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";

    public static final String GENERAL = "[GENERAL] ";
    public static final String DEBUG = "[DEBUG] ";
    public static final String STARTUP = "[STARTUP] ";
    public static final String SHUTDOWN = "[SHUTDOWN] ";
    public static final String ASSETS = "[ASSETS] ";
    public static final String ENTITY = "[ENTITY] ";
    public static final String SERVER = "[SERVER] ";
    public static final String NETWORK = "[NETWORK] ";
    public static final String UI = "[UI] ";

    public static final char LOG_NONE = 0, LOG_INFO = 1, LOG_ERROR = 2, LOG_DEBUG = 3;
    private static char LOG_LEVEL = LOG_ERROR;

    private static boolean VERBOSE = false;
    public static void setVerbose(boolean verbose) {
        VERBOSE = verbose;
    }

    public static char getLogLevel() {
        return LOG_LEVEL;
    }
    public static void setLogLevel(char logLevel) {
       LOG_LEVEL = logLevel > LOG_DEBUG ? LOG_DEBUG : logLevel;
    }


    public static void log(String message) {
        log(GENERAL, message);
    }

    public static void log(String tag, String message) {
        if(LOG_LEVEL < LOG_INFO) return;
        System.out.println(tag + message);
    }

    public static void error(String message) {
        error(GENERAL, message);
    }

    public static void error(String tag, String message) {
        error(tag, message, null);
    }

    public static void error(String tag, String message, Throwable e) {
        if(LOG_LEVEL < LOG_ERROR) return;
        if(VERBOSE && e != null) {
            System.out.println(RED + tag + message + "\n" + e + RESET);
        } else {
            System.out.println(RED + tag + message + RESET);
        }
    }

    public static void debug(String message) {
        debug(DEBUG, message);
    }

    public static void debug(String tag, String message) {
        if(LOG_LEVEL < LOG_DEBUG) return;
        System.out.println(tag.replace("[", "[DEBUG/") + message);
    }

}
