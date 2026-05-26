package karnickeldev.solar.ui.fontutil;

/**
 * @author KarnickelDev
 * @since 23.05.2026
 **/
public final class TextStyle {

    private TextStyle() {}

    public static final byte BOLD          = 1;
    public static final byte ITALIC        = 1 << 1;
    public static final byte UNDERLINE     = 1 << 2;
    public static final byte STRIKETHROUGH = 1 << 3;


    public static final float DEFAULT_FONT_SIZE = 16f;

}
