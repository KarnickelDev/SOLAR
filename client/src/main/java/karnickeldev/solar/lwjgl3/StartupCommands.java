package karnickeldev.solar.lwjgl3;

import karnickeldev.solar.logging.LogLevel;
import karnickeldev.solar.logging.LogManager;

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
                // TODO: add verbose mode
            } else if(cmd.equals("--debug")) {
                LogManager.setLevel(LogLevel.DEBUG);
            }

        }
    }


}
