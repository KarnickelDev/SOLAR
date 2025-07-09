package karnickeldev.solar.settings;

import com.badlogic.gdx.Gdx;
import karnickeldev.solar.core.gamestates.GameStateID;
import karnickeldev.solar.core.gamestates.GameStateManager;
import karnickeldev.solar.util.Logger;

import java.awt.*;
import java.io.IOException;

/**
 * Project: SOLAR
 *
 * @author KarnickelDev
 * @since 06.11.2024
 */
public class SettingsManager {

    private Settings settings;

    private short fpsOverride = -1;

    public SettingsManager(Settings initialSettings) {
        this.settings = initialSettings;
    }

    public Settings getSettings() {
        return this.settings;
    }

    public void updateSettings(Settings newSettings) {
        this.settings = newSettings;
        applySettings();
    }

    public void applySettings() {
        applySettings(this.settings);
    }

    /** Applies new settings, WITHOUT changing the current setting configuration */
    public void applySettings(Settings newSettings) {
        DisplayMode displayMode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        assert (newSettings.getScreenWidth() <= displayMode.getWidth());
        assert (newSettings.getScreenHeight() <= displayMode.getHeight());

        Gdx.graphics.setUndecorated(newSettings.isFullscreen() || newSettings.isBorderless());

        Gdx.graphics.setWindowedMode(newSettings.getScreenWidth(), newSettings.getScreenHeight());

        if (newSettings.isFullscreen()) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());

        updateFPS(newSettings);
    }

    /**
     * Applies new settings and changes current setting configuration
     * @return True if the new settings differed from old ones
     */
    public boolean applySettings(String resolution, boolean vsync, boolean fullscreen, boolean borderless, String fpsLimit) {
        Settings newSettings = new Settings(settings);

        Resolution res = Resolution.extractResolution(resolution);
        newSettings.setScreenWidth(res.getWidth());
        newSettings.setScreenHeight(res.getHeight());

        newSettings.setVsync(vsync);
        newSettings.setFullscreen(fullscreen);
        newSettings.setBorderless(borderless);

        int newFPSLimit = settings.getFpsLimit();
        try {
            newFPSLimit = Integer.parseInt(fpsLimit);
        } catch (NumberFormatException ignored) {}

        newSettings.setFpsLimit(newFPSLimit);
        boolean diff = !newSettings.equals(settings);
        applySettings(newSettings);
        this.settings = newSettings;
        return diff;
    }

    public void saveToFile() {
        try {
            settings.save();
        } catch (IOException e) {
            Logger.error(Logger.GENERAL, "Error saving settings to disk", e);
            throw new RuntimeException(e);
        }
        Logger.log(Logger.GENERAL, "Settings saved!");
    }

    private void updateFPS(Settings currSettings) {
        if(currSettings.isVsync() && fpsOverride <= 0) {
            Gdx.graphics.setForegroundFPS(currSettings.getFpsLimit());
            Gdx.graphics.setVSync(true);
        } else {
            Gdx.graphics.setVSync(false);
            Gdx.graphics.setForegroundFPS(
                fpsOverride > 0 ? fpsOverride : currSettings.getFpsLimit()
            );
        }
    }

    public void setFpsOverride(int fps) {
        fpsOverride = (short) fps;
        updateFPS(settings);
    }

    public void clearFpsOverride() {
        fpsOverride = -1;
        updateFPS(settings);
    }

}
