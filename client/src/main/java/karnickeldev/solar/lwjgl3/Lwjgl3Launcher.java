package karnickeldev.solar.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import karnickeldev.solar.Metadata;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.logging.LogAppender;
import karnickeldev.solar.logging.LogLevel;
import karnickeldev.solar.logging.LogManager;
import karnickeldev.solar.logging.Logger;
import karnickeldev.solar.logging.appender.FileAppender;
import karnickeldev.solar.settings.Resolution;
import karnickeldev.solar.settings.Settings;
import karnickeldev.solar.ui.ErrorTextbox;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Launches the desktop (LWJGL3) application.
 */
public class Lwjgl3Launcher {

    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.

        // PREPARE GAME DIRECTORY
        if (!Metadata.loadVersionData()) {
            System.err.println("Unable to load version data");
            new ErrorTextbox("Unable to load version data");
            return;
        }

        if (!Metadata.ROOT_DIR.exists()) {
            System.out.println("Creating Folder" + Metadata.ROOT_DIR);
            if (Metadata.ROOT_DIR.mkdir()) {
                System.out.println("Folder created");
            } else {
                System.err.println("Unable to create folder " + Metadata.ROOT_DIR);
                new ErrorTextbox("Unable to create folder " + Metadata.ROOT_DIR);
                return;
            }
        }

        // STARTUP
        Logger logger = Logger.get("Startup");
        logger.info("loading");

        try {
            LogManager.addAppender(new FileAppender(new File(Metadata.ROOT_DIR, "log.txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        LogManager.setLevel(LogLevel.DEBUG);
        LogManager.init();

        StartupCommands.handleStartupCommands(args);

        File cfg = new File(Metadata.ROOT_DIR.getAbsolutePath(), "settings.properties");
        Settings settings = new Settings(cfg);

        if (!cfg.exists()) {
            logger.info("Creating File {}", cfg.getName());
            try {
                if (!cfg.createNewFile()) System.exit(1);
            } catch (IOException e) {
                System.exit(1);
            }


            settings.setBorderless(false);
            settings.setFullscreen(false);
            settings.setVsync(false);
            settings.setFpsLimit(150);

            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            int width = gd.getDisplayMode().getWidth();
            int height = gd.getDisplayMode().getHeight();

            Resolution bestResolution = Resolution.matchResolution(width, height);
            logger.info("Detected Resolution: {}, best match is: {}",Resolution.resolutionToString(width, height), bestResolution);

            if (bestResolution == Resolution.FALLBACK_RESOLUTION) {
                if (Resolution.FALLBACK_RESOLUTION.getWidth() > width || Resolution.FALLBACK_RESOLUTION.getHeight() > height) {
                    logger.error("Unable to detect resolution");
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

            logger.info("File {} created", cfg.getName());
        }


        try {
            settings.load();
        } catch (IOException e) {
            logger.error(null, "Unable to load {}", cfg.getName());
            System.exit(1);
        }
        logger.info("Settings loaded");

        createApplication(settings);
    }

    private static Lwjgl3Application createApplication(Settings settings) {
        return new Lwjgl3Application(new SolarMain(settings), getDefaultConfiguration(settings));
    }


    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration(Settings settings) {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();

        configuration.setTitle(Metadata.APP_NAME + " v" + Metadata.VERSION);
        configuration.setForegroundFPS(90);
        configuration.useVsync(false);

        configuration.setDecorated(!settings.isFullscreen() && !settings.isBorderless());
        configuration.setWindowedMode(settings.getScreenWidth(), settings.getScreenHeight());
        if (settings.isFullscreen()) configuration.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());

        configuration.setWindowIcon("color_256.png", "color_128.png", "color_64.png", "color_48.png", "color_32.png", "color_16.png");

        DisplayMode displayMode = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getDefaultScreenDevice().getDisplayMode();
        if (settings.isFullscreen()
            || (settings.getScreenWidth() == displayMode.getWidth()
            && settings.getScreenHeight() == displayMode.getHeight())) {
            configuration.setWindowPosition(0, 0);
        } else {
            configuration.setWindowPosition(
                (displayMode.getWidth() - settings.getScreenWidth()) / 2,
                (displayMode.getHeight() - settings.getScreenHeight()) / 2
            );
        }

        configuration.setInitialVisible(true);
        configuration.setResizable(true);

        return configuration;
    }
}
