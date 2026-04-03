package karnickeldev.solar.logging;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

import java.util.regex.Pattern;

/**
 * @author KarnickelDev
 * @since 03.04.2026
 **/
public final class Ansi {

    // COLOR PLACEHOLDERS
    public static final String RESET = "\u001B[0m";

    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    public static final String GRAY = "\u001B[90m";

    private static final Pattern ANSI_PATTERN = Pattern.compile("\\u001B\\[[;\\d]*m");

    public static String stripAnsi(String input) {
        return ANSI_PATTERN.matcher(input).replaceAll("");
    }


    // ANSI SUPPORT
    private static final boolean ENABLED;
    static {
        ENABLED = detect();
    }

    public static boolean isEnabled() {
        return ENABLED;
    }

    private static boolean detect() {

        // Respect standard override
        if (System.getenv("NO_COLOR") != null) {
            System.out.println("NO_COLOR is set");
            return false;
        }

        if (System.getenv("FORCE_COLOR") != null) {
            System.out.println("FORCE_COLOR is set");
            return true;
        }

        String os = System.getProperty("os.name").toLowerCase();

        // Non-Windows -> enable
        if (!os.contains("win")) return true;

        // Modern Windows terminals
        if (System.getenv("WT_SESSION") != null) return true;

        // Try enabling ANSI via WinAPI
        if (enableWinANSI()) return true;

        // Explicitly dumb terminal
        String term = System.getenv("TERM");
        if ("dumb".equals(term)) {
            System.out.println("dumb terminal, no color");
            return false;
        }

        // Default: assume it works
        return true;
    }


    // enable ANSI on Windows via JNA
    private static final int STD_OUTPUT_HANDLE = -11;
    private static final int ENABLE_VIRTUAL_TERMINAL_PROCESSING = 0x0004;

    private interface Kernel32 extends Library {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);

        Pointer GetStdHandle(int nStdHandle);

        boolean GetConsoleMode(Pointer hConsoleHandle, IntByReference lpMode);

        boolean SetConsoleMode(Pointer hConsoleHandle, int dwMode);
    }

    private static boolean enableWinANSI() {
        try {
            Pointer handle = Kernel32.INSTANCE.GetStdHandle(STD_OUTPUT_HANDLE);

            if (handle == null) return false;

            IntByReference modeRef = new IntByReference();

            if (!Kernel32.INSTANCE.GetConsoleMode(handle, modeRef)) {
                return false;
            }

            int mode = modeRef.getValue();

            // Already enabled?
            if ((mode & ENABLE_VIRTUAL_TERMINAL_PROCESSING) != 0) {
                return true;
            }

            int newMode = mode | ENABLE_VIRTUAL_TERMINAL_PROCESSING;

            return Kernel32.INSTANCE.SetConsoleMode(handle, newMode);

        } catch (Throwable t) {
            return false;
        }
    }

}
