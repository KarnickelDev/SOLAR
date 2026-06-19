package karnickeldev.solar.ui.components.styles;

import karnickeldev.solar.core.SimTestScreen;
import karnickeldev.solar.ui.components.UIState;
import karnickeldev.solar.ui.core.Align;
import karnickeldev.solar.ui.fontutil.kernel.MSDFFont;
import karnickeldev.solar.ui.theme.ThemeColors;

/**
 * @author KarnickelDev
 * @since 20.06.2026
 **/
public class TextWidgetStyle {

    public MSDFFont font = SimTestScreen.font;
    public int fontSize = 20;

    public int textAlign = Align.CENTER;
    public int contentAlign = Align.CENTER | Align.MIDDLE;

    private final int[] backgroundColor = new int[UIState.values().length];
    private final int[] borderColor = new int[UIState.values().length];
    private final int[] fontColor = new int[UIState.values().length];

    public TextWidgetStyle() {
        this(ThemeColors.DEFAULT);
    }

    public TextWidgetStyle(ThemeColors colors) {
        for (int i = 0; i < backgroundColor.length; i++) {
            backgroundColor[i] = colors.background();
            borderColor[i] = colors.border();
            fontColor[i] = colors.textPrimary();
        }
    }

    public TextWidgetStyle(TextWidgetStyle copy) {
        this.font = copy.font;
        this.fontSize = copy.fontSize;

        this.textAlign = copy.textAlign;
        this.contentAlign = copy.contentAlign;

        System.arraycopy(copy.backgroundColor, 0, backgroundColor, 0, UIState.values().length);
        System.arraycopy(copy.borderColor, 0, borderColor, 0, UIState.values().length);
        System.arraycopy(copy.fontColor, 0, fontColor, 0, UIState.values().length);
    }

    public void setFontColor(int rgba8888, UIState... states) {
        for(UIState state : states) fontColor[state.ordinal()] = rgba8888;
    }

    public void setBorderColor(int rgba8888, UIState... states) {
        for(UIState state : states) borderColor[state.ordinal()] = rgba8888;
    }

    public void setBackgroundColor(int rgba8888, UIState... states) {
        for(UIState state : states) backgroundColor[state.ordinal()] = rgba8888;
    }

    public int fontColor(UIState state) {
        return fontColor[state.ordinal()];
    }

    public int backgroundColor(UIState state) {
        return backgroundColor[state.ordinal()];
    }

    public int borderColor(UIState state) {
        return borderColor[state.ordinal()];
    }

}
