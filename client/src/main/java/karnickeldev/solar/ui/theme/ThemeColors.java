package karnickeldev.solar.ui.theme;

/**
 * Stores the central Colors of a UI Theme as integers in rgba8888 format
 * @author KarnickelDev
 * @since 20.06.2026
 **/
public record ThemeColors (
    int background,
    int surface,
    int border,
    int accent,
    int textPrimary,
    int textSecondary,
    int success,
    int warning,
    int danger
) {

    public static final ThemeColors DEFAULT = new ThemeColors(
        0x0,
        0x0,
        0xFFFFFFFF,
        0xBBBBBBFF,
        0xFFFFFFFF,
        0xFFFFFFFF,
        0x00FF00FF,
        0xFFFF00FF,
        0xFF0000FF
    );

}
