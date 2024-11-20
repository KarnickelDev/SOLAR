package karnickeldev.solar.settings;

import com.badlogic.gdx.Gdx;
import karnickeldev.solar.core.Logger;

import java.io.IOException;

/**
 * Project: SOLAR
 *
 * @author KarnickelDev
 * @since 06.11.2024
 */
public class SettingsManager {

    private Settings settings;

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

    public void applySettings(Settings newSettings) {

        Gdx.graphics.setUndecorated(newSettings.isFullscreen() || newSettings.isBorderless());

        Gdx.graphics.setWindowedMode(newSettings.getScreenWidth(), newSettings.getScreenHeight());

        if(newSettings.isFullscreen()) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());

        Gdx.graphics.setForegroundFPS(newSettings.getFpsLimit());
        Gdx.graphics.setVSync(newSettings.isVsync());
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

}
