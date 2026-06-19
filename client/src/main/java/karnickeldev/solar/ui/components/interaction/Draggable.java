package karnickeldev.solar.ui.components.interaction;

/**
 * @author KarnickelDev
 * @since 22.06.2026
 **/
public interface Draggable {

    boolean onDragStart(float x, float y);

    boolean onDrag(float x, float y, float dx, float dy);

    boolean onDragEnd(float x, float y);

}
