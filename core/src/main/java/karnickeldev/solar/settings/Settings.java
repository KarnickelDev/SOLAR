package karnickeldev.solar.settings;

import java.io.*;
import java.util.Properties;

/**
 * Project: SOLAR
 *
 * @author KarnickelDev
 * @since 12.10.2024
 */
public class Settings {

    private static final String SCREEN_WIDTH_KEY = "Screen-Width";
    private static final String SCREEN_HEIGHT_KEY = "Screen-Height";
    private static final String FULLSCREEN_KEY = "Fullscreen";
    private static final String BORDERLESS_KEY = "Borderless";
    private static final String VSYNC_KEY = "Vsync";
    private static final String FPS_LIMIT_KEY = "FpsLimit";

    private final Properties properties;
    private final String file_path;


    private int screen_width, screen_height;
    private boolean fullscreen, borderless;
    private boolean vsync;
    private int fps_limit;

    public Settings(Settings old_settings) {
        properties = new Properties();
        file_path = old_settings.file_path;
        screen_width = old_settings.screen_width;
        screen_height = old_settings.screen_height;
        fullscreen = old_settings.fullscreen;
        borderless = old_settings.borderless;
        vsync = old_settings.vsync;
        fps_limit = old_settings.fps_limit;
    }

    public Settings(String file_path) {
        this.properties = new Properties();
        this.file_path = file_path;
    }

    public Settings(File file) {
        this(file.getAbsolutePath());
    }

    /**
     * Loads this Settings Object from its physical File
     * @throws IllegalArgumentException if a malformed Unicode escape appears in read File
     * @throws IOException if an error occurs while reading the .properties File
     * @throws FileNotFoundException if there is no physical File present
     * @throws NumberFormatException if a String couldn't be parsed as a Number
     */
    public void load() throws IllegalArgumentException, IOException {
        properties.load(new FileReader(file_path));

        screen_width = Integer.parseInt(properties.getProperty(SCREEN_WIDTH_KEY));
        screen_height = Integer.parseInt(properties.getProperty(SCREEN_HEIGHT_KEY));

        fullscreen = Boolean.parseBoolean(properties.getProperty(FULLSCREEN_KEY));
        borderless = Boolean.parseBoolean(properties.getProperty(BORDERLESS_KEY));

        vsync = Boolean.parseBoolean(properties.getProperty(VSYNC_KEY));
        fps_limit = Integer.parseInt(properties.getProperty(FPS_LIMIT_KEY));
    }

    public void save() throws IllegalArgumentException, IOException {
        properties.setProperty(SCREEN_WIDTH_KEY, Integer.toString(screen_width));
        properties.setProperty(SCREEN_HEIGHT_KEY, Integer.toString(screen_height));
        properties.setProperty(FULLSCREEN_KEY, Boolean.toString(fullscreen));
        properties.setProperty(BORDERLESS_KEY, Boolean.toString(borderless));
        properties.setProperty(VSYNC_KEY, Boolean.toString(vsync));
        properties.setProperty(FPS_LIMIT_KEY, Integer.toString(fps_limit));

        properties.store(new FileWriter(file_path),
            "test comment");
    }

    public boolean equals(Settings other) {
        return other.screen_width == screen_width
            && other.screen_height == screen_height
            && other.fullscreen == fullscreen
            && other.borderless == borderless
            && other.vsync == vsync
            && other.fps_limit == fps_limit;
    }

    public void setScreenWidth(int screen_width) {
        this.screen_width = screen_width;
    }

    public int getScreenWidth() {
        return screen_width;
    }

    public void setScreenHeight(int screen_height) {
        this.screen_height = screen_height;
    }

    public int getScreenHeight() {
        return screen_height;
    }

    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public void setBorderless(boolean borderless) {
        this.borderless = borderless;
    }

    public boolean isBorderless() {
        return borderless;
    }

    public void setVsync(boolean vsync) {
        this.vsync = vsync;
    }

    public boolean isVsync() {
        return vsync;
    }

    public void setFpsLimit(int fps_limit) {
        this.fps_limit = fps_limit;
    }

    public int getFpsLimit() {
        return fps_limit;
    }

}
