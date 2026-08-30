package karnickeldev.solar.ui.components;

/**
 * @author KarnickelDev
 * @since 21.06.2026
 **/
public enum UIState {
    NORMAL,
    HOVERED,
    PRESSED,
    DISABLED,
    ;

    public static UIState[] all() {
        return values();
    }
}
