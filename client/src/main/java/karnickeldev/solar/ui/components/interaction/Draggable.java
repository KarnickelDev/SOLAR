package karnickeldev.solar.ui.components.interaction;

/**
 * @author KarnickelDev
 * @since 22.06.2026
 **/
public interface Draggable {

    void onDragStart(float x, float y);

    void onDrag(float x, float y, float dx, float dy);

    void onDragEnd(float x, float y);

}
