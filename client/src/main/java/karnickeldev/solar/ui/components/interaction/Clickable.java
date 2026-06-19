package karnickeldev.solar.ui.components.interaction;

/**
 * @author KarnickelDev
 * @since 19.06.2026
 **/
public interface Clickable {

    boolean onMouseDown(int x, int y, int button);

    boolean onMouseUp(int x, int y, int button);

    boolean onPressed(int x, int y, int button);
}
