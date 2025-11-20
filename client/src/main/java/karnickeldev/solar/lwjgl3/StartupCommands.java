package karnickeldev.solar.lwjgl3;

import karnickeldev.solar.util.Logger;

/**
 * Project: SOLAR
 *
 * @author KarnickelDev
 * @since 06.11.2024
 */
public class StartupCommands {

    public static void handleStartupCommands(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String cmd = args[i];

            if (cmd.equals("--verbose")) {
                Logger.setVerbose(true);
                Logger.log("Set Logging to VERBOSE");
            } else if(cmd.equals("--debug")) {
                Logger.setLogLevel(Logger.LOG_DEBUG);
                Logger.log("Set Logging Level to DEBUG");
            }

        }
    }


}
