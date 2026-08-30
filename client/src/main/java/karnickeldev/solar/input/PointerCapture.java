package karnickeldev.solar.input;

import java.util.Objects;

/** Stores one accepted pointer/button interaction until release or cancellation. */
public final class PointerCapture<T> {

    public record PointerState<T>(T target, float x, float y, float dx, float dy,
        float pressX, float pressY, int pointer, int button, boolean dragging) {}

    private T target;
    private int pointer = -1;
    private int button = -1;
    private float pressX, pressY;
    private float lastX, lastY;
    private boolean dragging;

    public boolean begin(T target, float x, float y, int pointer, int button) {
        if(isActive()) return false;

        this.target = Objects.requireNonNull(target, "target");
        this.pointer = pointer;
        this.button = button;
        pressX = lastX = x;
        pressY = lastY = y;
        dragging = false;
        return true;
    }

    public PointerState<T> move(float x, float y, int pointer, int button) {
        if(!matches(pointer, button)) return null;

        PointerState<T> state = snapshotState(x, y, x - lastX, y - lastY);
        lastX = x;
        lastY = y;
        return state;
    }

    /** Marks the capture as dragging once the pointer has moved beyond the threshold. */
    public boolean tryStartDrag(float x, float y, float thresholdSquared) {
        if(!isActive() || dragging) return false;

        float dx = x - pressX;
        float dy = y - pressY;
        if(dx * dx + dy * dy <= thresholdSquared) return false;

        dragging = true;
        return true;
    }

    public PointerState<T> release(float x, float y, int pointer, int button) {
        if(!matches(pointer, button)) return null;

        PointerState<T> state = snapshotState(x, y, x - lastX, y - lastY);
        clear();
        return state;
    }

    public PointerState<T> cancel() {
        if(!isActive()) return null;

        PointerState<T> state = snapshotState(lastX, lastY, 0, 0);
        clear();
        return state;
    }

    public boolean matches(int pointer, int button) {
        return isActive() && this.pointer == pointer && this.button == button;
    }

    public boolean isActive() {
        return target != null;
    }

    public boolean isDragging() {
        return dragging;
    }

    public T target() {
        return target;
    }

    private PointerState<T> snapshotState(float x, float y, float dx, float dy) {
        return new PointerState<>(target, x, y, dx, dy, pressX, pressY, pointer, button, dragging);
    }

    private void clear() {
        target = null;
        pointer = -1;
        button = -1;
        dragging = false;
    }
}
