package karnickeldev.solar.ui.core;

import karnickeldev.solar.ui.components.styles.TextWidgetStyle;
import karnickeldev.solar.ui.theme.ThemeColors;
import karnickeldev.solar.ui.theme.UITheme;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author KarnickelDev
 * @since 21.06.2026
 **/
public final class ThemeManager {

    private static final ThemeManager instance = new ThemeManager();

    public static ThemeManager get() {
        return instance;
    }

    private ThemeManager() {}

    private final UITheme DEFAULT = new UITheme(ThemeColors.DEFAULT, new TextWidgetStyle());

    private final Map<String, UITheme> themes = new HashMap<>();

    public UITheme getTheme(String name) {
        return themes.getOrDefault(name, DEFAULT);
    }

    private void add(String name, UITheme theme) {
        themes.put(name.toLowerCase(Locale.ENGLISH), theme);
    }

    public void loadThemes() {
        ThemeColors colors = new ThemeColors(
            0x0F1215FF,
            0,
            0x34312FFF,
            0,
            0xE6C8B2FF,
            0xF5F0E8FF,
            0,
            0,
            0
        );
        TextWidgetStyle textStyle = new TextWidgetStyle();
        textStyle.textAlign = Align.LEFT;
        textStyle.contentAlign = Align.LEFT | Align.MIDDLE;
        textStyle.fontSize = 20;

        UITheme theme = new UITheme(colors, textStyle);
        add("default", theme);

        // TODO: load from files
    }

}
