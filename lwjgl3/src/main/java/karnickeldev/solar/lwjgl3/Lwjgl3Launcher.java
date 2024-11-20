package karnickeldev.solar.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import karnickeldev.solar.core.Logger;
import karnickeldev.solar.settings.Resolution;
import karnickeldev.solar.settings.Settings;
import karnickeldev.solar.core.Metadata;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.ui.ErrorTextbox;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {

    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.

        StartupCommands.handleStartupCommands(args);

        Logger.log(Logger.STARTUP,"loading");

        if(!Metadata.loadVersionData()) {
            Logger.error(Logger.STARTUP,"Unable to load version data");
            new ErrorTextbox("Unable to load version data");
            return;
        }

        if(!Metadata.ROOT_DIR.exists()) {
            System.out.println("Creating Folder " + Metadata.ROOT_DIR);
            if(Metadata.ROOT_DIR.mkdir()) {
                System.out.println("Folder created");
            } else {
                Logger.error(Logger.STARTUP,"Unable to create folder " + Metadata.ROOT_DIR);
                return;
            }
        }


        File cfg = new File(Metadata.ROOT_DIR.getAbsolutePath(), "settings.properties");
        Settings settings = new Settings(cfg);

        if(!cfg.exists()) {
            Logger.log(Logger.STARTUP, "Creating File settings.properties");
            try {
                if(!cfg.createNewFile()) System.exit(1);
            } catch (IOException e) {
                System.exit(1);
            }


            settings.setBorderless(false);
            settings.setFullscreen(false);
            settings.setVsync(true);
            settings.setFpsLimit(300);

            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            int width = gd.getDisplayMode().getWidth();
            int height = gd.getDisplayMode().getHeight();

            Resolution bestResolution = Resolution.matchResolution(width, height);
            Logger.log(Logger.STARTUP, "Detected Resolution: " + Resolution.resolutionToString(width, height) + ", best match is: " + bestResolution);

            if(bestResolution == Resolution.FALLBACK_RESOLUTION) {
                if(bestResolution.getWidth() > width || bestResolution.getHeight() > height) {
                    Logger.error(Logger.STARTUP,"Unable to detect resolution");
                    System.exit(1);
                }
            }

            settings.setScreenWidth(bestResolution.getWidth());
            settings.setScreenHeight(bestResolution.getHeight());

            try {
                settings.save();
            } catch (IOException e) {
                System.exit(1);
            }

            Logger.log(Logger.STARTUP, "File settings.properties created");
        }


        try {
            settings.load();
        } catch (IOException e) {
            Logger.error(Logger.STARTUP,"Unable to load settings.properties");
            System.exit(1);
        }
        Logger.log(Logger.STARTUP,"Settings loaded");

        createApplication(settings);
    }

    private static Lwjgl3Application createApplication(Settings settings) {
        return new Lwjgl3Application(new SolarMain(settings), getDefaultConfiguration(settings));
    }


    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration(Settings settings) {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();

        configuration.setTitle(Metadata.APP_NAME + " v" + Metadata.VERSION);
        configuration.setForegroundFPS(90);
        configuration.useVsync(true);

        configuration.setDecorated(!settings.isFullscreen() && !settings.isBorderless());
        configuration.setWindowedMode(settings.getScreenWidth(), settings.getScreenHeight());
        if(settings.isFullscreen()) configuration.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());

        configuration.setWindowIcon("solar256.png","solar128.png", "solar64.png", "solar48.png", "solar32.png", "solar16.png");

        configuration.setInitialVisible(true);
        configuration.setResizable(true);

        return configuration;
    }
}
