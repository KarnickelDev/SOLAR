package karnickeldev.solar.ui.core;

import karnickeldev.solar.core.Engine;
import karnickeldev.solar.input.InputHandler;
import karnickeldev.solar.input.PointerCapture;
import karnickeldev.solar.render.core.RendererContext;
import karnickeldev.solar.ui.components.UIElement;
import karnickeldev.solar.ui.components.UILayoutEngine;
import karnickeldev.solar.ui.components.interaction.*;

import java.util.*;

/**
 * @author KarnickelDev
 * @since 03.07.2025
 **/
public class UIManager implements InputHandler {

    private static final UIManager instance = new UIManager();
    private static final float DRAG_THRESHOLD_SQUARED = (float) Math.pow(4, 2);

    public static UIManager get() {
        return instance;
    }

    private final Deque<UILayer> uiLayers = new ArrayDeque<>();

    private UILayoutEngine.UILayoutContext  uiLayoutContext;

    /** element currently under mouse */
    private UIElement hovered;

    /** element receiving keyboard input */
    private UIElement focused;

    private final PointerCapture<UIElement> pointerCapture = new PointerCapture<>();

    private UIManager() {}

    public UILayoutEngine.UILayoutContext getLayoutContext() {
        return uiLayoutContext;
    }

    /** Returns the deepest pointer-occupying element from the first layer hit. */
    private UIElement hit(float x, float y) {
        for(UILayer layer : getLayersTopToBottom()) {
            if(!layer.isVisible() || !layer.receivesInput()) continue;

            UIElement hit = layer.getCanvas().hit(x, y);
            if(hit != null) return hit;

            if(layer.isModal()) return null;
        }

        return null;
    }

    /** Finds the nearest ancestor (including the raw hit) with the requested input capability. */
    private <T> T bubble(UIElement rawHit, Class<T> capability) {
        for(UIElement current = rawHit; current != null; current = current.getParent()) {
            if(capability.isInstance(current)) return capability.cast(current);
        }
        return null;
    }

    /** Hover is resolved per layer so an occupied region never falls through to a lower layer. */
    private UIElement hoverTarget(float x, float y) {
        for(UILayer layer : getLayersTopToBottom()) {
            if(!layer.isVisible() || !layer.receivesInput()) continue;

            UIElement rawHit = layer.getCanvas().hit(x, y);
            if(rawHit != null) {
                // bubbling, check if any ancestor is Hoverable
                // TODO: verify works as intended
                for(UIElement current = rawHit; current != null; current = current.getParent()) {
                    if(current instanceof Hoverable) {
                        return current;
                    }
                }
            }

            if(layer.isModal()) return null;
        }

        return null;
    }

    private void updateHover(UIElement current) {
        if(current == hovered) return;

        UIElement old = hovered;
        hovered = current;

        if(old instanceof Hoverable h) {
            h.onHoverExit();
        }

        // An exit callback may have changed the hover target.
        if(hovered == current && current instanceof Hoverable h) {
            h.onHoverEnter();
        }

    }

    public void clearInteraction(UIElement element, boolean includeDescendants) {
        if(element == null) return;

        if(matches(hovered, element, includeDescendants)) {
            updateHover(null);
        }
        if(matches(focused, element, includeDescendants)) {
            setFocus(null);
        }
        if(matches(pointerCapture.target(), element, includeDescendants)) {
            cancelPointerCapture();
        }

    }

    private boolean matches(UIElement current, UIElement element, boolean includeDescendants) {
        if(current == null) return false;
        if(current == element) return true;
        if(!includeDescendants) return false;

        for(UIElement parent = current.getParent(); parent != null; parent = parent.getParent()) {
            if(parent == element) return true;
        }

        return false;
    }

    private void clearInteraction() {
        updateHover(null);
        setFocus(null);
        cancelPointerCapture();
    }

    @Override
    public void inputCancelled() {
        clearInteraction();
    }

    public boolean hasPointerCapture(int pointer, int button) {
        return pointerCapture.matches(pointer, button);
    }

    public void cancelPointerCapture() {
        PointerCapture.PointerState<UIElement> cancelled = pointerCapture.cancel();
        if(cancelled == null) return;

        if(cancelled.dragging() && cancelled.target() instanceof Draggable draggable) {
            draggable.onDragCancel(cancelled.x(), cancelled.y());
        }
        if(cancelled.target() instanceof Clickable clickable) {
            clickable.onClickCancel(Math.round(cancelled.x()), Math.round(cancelled.y()), cancelled.button());
        }
    }

    public void setFocus(UIElement element) {
        if (focused == element) return;

        UIElement old = focused;
        focused = element;

        if(old instanceof Focusable f) {
            f.onFocusLost();
        }

        // A callback may have selected a different focus target.
        if (focused == element && element instanceof Focusable f) {
            f.onFocusGained();
        }
    }

    private boolean belongsToLayer(UIElement element, UILayer layer) {
        if (element == null) return false;
        for (UIElement current = element;
             current != null;
             current = current.getParent()) {
            if (current == layer.getCanvas()) return true;
        }
        return false;
    }

    public UIElement getFocused() {
        return focused;
    }

    /** Whether the current UI layer prevents input from reaching gameplay. */
    public boolean blocksGameplayInput() {
        for (UILayer layer : getLayersTopToBottom()) {
            if (!layer.isVisible() || !layer.receivesInput()) continue;

            if (layer.isModal() || layer.blocksInput()) return true;
        }

        return false;
    }

    @Override
    public boolean mouseMoved(int mx, int my) {
        updateHover(hoverTarget(mx, my));

        return false;
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        for(UILayer layer : getLayersTopToBottom()) {
            if(!layer.isVisible() || !layer.receivesInput()) continue;

            UIElement rawHit = layer.getCanvas().hit(x, y);
            if(rawHit != null) {
                for(UIElement current = rawHit; current != null; current = current.getParent()) {
                    if(current instanceof Clickable clickable) {
                        if(!pointerCapture.begin(current, x, y, pointer, button)) return true;
                        if(clickable.onMouseDown(x, y, button)) return true;

                        // Declined presses may continue bubbling to a clickable parent.
                        pointerCapture.cancel();
                    }
                }

                // An occupied UI region blocks lower UI layers and gameplay even without a handler.
                return true;
            }

            if(layer.isModal()) return true;
        }

        return false;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        PointerCapture.PointerState<UIElement> released = pointerCapture.release(x, y, pointer, button);
        if(released == null) return false;

        if(released.dragging() && released.target() instanceof Draggable draggable) {
            draggable.onDragEnd(x, y);
        }
        if(released.target() instanceof Clickable clickable) {
            clickable.onMouseUp(x, y, button);
            if(!released.dragging() && matches(hit(x, y), released.target(), true)) {
                clickable.onClicked(x, y, button);
            }
        }
        return true;
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer, int button) {
        PointerCapture.PointerState<UIElement> moved = pointerCapture.move(x, y, pointer, button);
        if(moved == null) return false;

        if(moved.target() instanceof Draggable draggable) {
            if(pointerCapture.tryStartDrag(x, y, DRAG_THRESHOLD_SQUARED)) {
                draggable.onDragStart(moved.pressX(), moved.pressY());
            }
            if(pointerCapture.isDragging()) {
                draggable.onDrag(x, y, moved.dx(), moved.dy());
            }
        }
        return true;
    }

    @Override
    public boolean scrolled(float dx, float dy) {
        float x = Engine.input().mouseX();
        float y = Engine.input().mouseY();

        for(UILayer layer : getLayersTopToBottom()) {
            if(!layer.isVisible() || !layer.receivesInput()) continue;

            UIElement rawHit = layer.getCanvas().hit(x, y);
            if(rawHit != null) {
                for(UIElement current = rawHit; current != null; current = current.getParent()) {
                    if(current instanceof Scrollable scrollable && scrollable.onScrolled(dx, dy)) {
                        return true;
                    }
                }

                return true;
            }

            if(layer.isModal()) return true;
        }

        return false;
    }

    @Override
    public boolean keyDown(int key) {
        for(UILayer layer : getLayersTopToBottom()) {
            if(!layer.receivesInput() || !layer.isVisible()) continue;

            if(belongsToLayer(focused, layer) && focused instanceof KeyInputTarget t) {
                if(t.keyDown(key)) return true;
            }

            if(layer.keyDown(key)) return true;

            if(layer.isModal()) return true;
        }

        return false;
    }

    @Override
    public boolean keyUp(int key) {
        for(UILayer layer : getLayersTopToBottom()) {
            if(!layer.receivesInput() || !layer.isVisible()) continue;

            if(belongsToLayer(focused, layer) && focused instanceof KeyInputTarget t) {
                if(t.keyUp(key)) return true;
            }

            if(layer.keyUp(key)) return true;

            if(layer.isModal()) return true;
        }

        return false;
    }

    @Override
    public boolean keyTyped(char c) {
        for(UILayer layer : getLayersTopToBottom()) {
            if(!layer.receivesInput() || !layer.isVisible()) continue;

            if(belongsToLayer(focused, layer) && focused instanceof KeyInputTarget t) {
                if(t.keyTyped(c)) return true;
            }

            if(layer.keyTyped(c)) return true;

            if(layer.isModal()) return true;
        }

        return false;
    }

    public void push(UILayer uiLayer) {
        if(uiLayer == null) return;

        UILayer previous = top();

        if (previous != null) {
            previous.onBlur();
        }

        clearInteraction();

        uiLayers.addFirst(uiLayer);

        uiLayer.onEnter();
        uiLayer.onFocus();
    }

    public void pop(UIExitReason reason) {
        if(uiLayers.isEmpty()) return;

        UILayer top = uiLayers.removeFirst();

        removeLayer(top, reason);

        UILayer newTop = top();
        if(newTop != null) {
            newTop.onFocus();
        }
    }

    public void clear() {
        clearInteraction();

        while(!uiLayers.isEmpty()) {
            UILayer uiLayer = uiLayers.removeFirst();
            uiLayer.onExit(UIExitReason.TRANSITION);
        }
    }

    public void requestPop(UILayer layer, UIExitReason reason) {
        if (layer == null || uiLayers.isEmpty()) return;

        boolean wasTop = (layer == top());

        if (!uiLayers.remove(layer)) return;

        // Always call exit
        removeLayer(layer, reason);

        // If it was top, restore focus to new top
        if (wasTop) {
            UILayer newTop = top();
            if (newTop != null) {
                newTop.onFocus();
            }
        }
    }

    public UILayer top() {
        return uiLayers.peekFirst();
    }

    public List<UILayer> getLayersTopToBottom() {
        return new ArrayList<>(uiLayers);
    }

    public List<UILayer> getLayersBottomToTop() {
        List<UILayer> list = new ArrayList<>(uiLayers);
        Collections.reverse(list);
        return list;
    }

    /** Handles resizing all UI component (called from resize in screens) */
    public void resize(int width, int height) {
        for (UILayer layer : getLayersBottomToTop()) {
            layer.resize(width, height);
        }
    }

    /** Updates all UI (called from render loop) */
    public void update(UILayoutEngine.UILayoutContext ctx, float delta) {
        uiLayoutContext = ctx;
        for (UILayer layer : getLayersTopToBottom()) {
            layer.update(ctx, delta); // the layer checks isActive itself in update()
        }

        // Layout may move, reveal, or insert elements without a mouse-move event.
        updateHover(hoverTarget(Engine.input().mouseX(), Engine.input().mouseY()));
    }

    public void render(RendererContext ctx) {
        for(UILayer layer : getLayersBottomToTop()) {
            if(layer.isVisible()) layer.render(ctx);
        }
    }

    private void removeLayer(UILayer layer, UIExitReason reason) {
        clearInteraction(layer.getCanvas(), true);
        layer.onExit(reason != null ? reason : UIExitReason.SYSTEM);
    }

}
