package karnickeldev.solar.ui.components.interaction;

/**
 * @author KarnickelDev
 * @since 19.06.2026
 **/
public interface Clickable {

    /** @return true to accept and capture this pointer/button gesture */
    boolean onMouseDown(int x, int y, int button);

    void onMouseUp(int x, int y, int button);

    void onPressed(int x, int y, int button);
}
