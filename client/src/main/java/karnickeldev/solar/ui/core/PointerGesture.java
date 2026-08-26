package karnickeldev.solar.ui.core;

import karnickeldev.solar.ui.components.UIElement;
import karnickeldev.solar.ui.components.interaction.Clickable;
import karnickeldev.solar.ui.components.interaction.Draggable;

/** Owns one pointer/button gesture from an accepted press through release or cancellation. */
final class PointerGesture {

    private static final float DRAG_THRESHOLD = 4f;
    private static final float DRAG_THRESHOLD_SQUARED = DRAG_THRESHOLD * DRAG_THRESHOLD;

    private UIElement target;
    private int pointer = -1;
    private int button = -1;

    private float pressX, pressY;
    private float lastX, lastY;
    private boolean dragging;

    public boolean begin(UIElement candidate, int x, int y, int pointer, int button) {
        if(isActive() || candidate == null) return false;

        if(!(candidate instanceof Draggable) && !(candidate instanceof Clickable)) return false;

        target = candidate;
        this.pointer = pointer;
        this.button = button;
        pressX = lastX = x;
        pressY = lastY = y;
        dragging = false;

        if(target instanceof Clickable clickable && !clickable.onMouseDown(x, y, button)) {
            reset();
            return false;
        }

        return true;
    }

    public boolean drag(int x, int y, int pointer, int button) {
        if(!matches(pointer, button)) return false;

        if(!dragging && target instanceof Draggable draggable) {
            float dxTotal = x - pressX;
            float dyTotal = y - pressY;
            if(dxTotal * dxTotal + dyTotal * dyTotal > DRAG_THRESHOLD_SQUARED) {
                dragging = true;
                draggable.onDragStart(pressX, pressY);
            }
        }

        if(dragging && target instanceof Draggable draggable) {
            draggable.onDrag(x, y, x - lastX, y - lastY);
        }

        lastX = x;
        lastY = y;
        return true;
    }

    public boolean release(UIElement releasedOver, int x, int y, int pointer, int button) {
        if(!matches(pointer, button)) return false;
        finish(releasedOver, x, y, false);
        return true;
    }

    public void cancel() {
        if(!isActive()) return;
        finish(null, lastX, lastY, true);
    }

    public boolean targets(UIElement element, boolean includeDescendants) {
        if(target == null || element == null) return false;
        if(target == element) return true;
        if(!includeDescendants) return false;

        for(UIElement parent = target.getParent(); parent != null; parent = parent.getParent()) {
            if(parent == element) return true;
        }
        return false;
    }

    private void finish(UIElement releasedOver, float x, float y, boolean cancelled) {
        UIElement finishedTarget = target;
        int finishedButton = button;
        boolean wasDragging = dragging;

        // Clear before callbacks so removing layers from a callback cannot corrupt this gesture.
        reset();

        if(wasDragging && finishedTarget instanceof Draggable draggable) {
            draggable.onDragEnd(x, y);
        }

        if(finishedTarget instanceof Clickable clickable) {
            int screenX = Math.round(x);
            int screenY = Math.round(y);
            clickable.onMouseUp(screenX, screenY, finishedButton);
            if(!cancelled && !wasDragging && isTargetOrDescendant(releasedOver, finishedTarget)) {
                clickable.onPressed(screenX, screenY, finishedButton);
            }
        }
    }

    private boolean isTargetOrDescendant(UIElement element, UIElement possibleAncestor) {
        for(UIElement current = element; current != null; current = current.getParent()) {
            if(current == possibleAncestor) return true;
        }
        return false;
    }

    private boolean matches(int pointer, int button) {
        return isActive() && this.pointer == pointer && this.button == button;
    }

    private boolean isActive() {
        return target != null;
    }

    private void reset() {
        target = null;
        pointer = -1;
        button = -1;
        dragging = false;
    }
}
