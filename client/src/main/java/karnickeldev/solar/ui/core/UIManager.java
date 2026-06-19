package karnickeldev.solar.ui.core;

import karnickeldev.solar.core.Engine;
import karnickeldev.solar.input.InputHandler;
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

    public static UIManager get() {
        return instance;
    }

    private final Deque<UILayer> uiLayers = new ArrayDeque<>();

    private UILayoutEngine.UILayoutContext  uiLayoutContext;

    /** element currently under mouse */
    private UIElement hovered;

    /** element last pressed */
    private UIElement pressed;

    /** element receiving keyboard input */
    private UIElement focused;

    /** element receiving mouse input */
    private UIElement captured;

    /** element being dragged */
    private UIElement dragged;
    private UIElement dragCandidate;

    private float pressX, pressY;
    private float lastX, lastY;
    private boolean dragging = false;

    private UIManager() {}

    public UILayoutEngine.UILayoutContext getLayoutContext() {
        return uiLayoutContext;
    }

    private void setCapture(UIElement e) {
        captured = e;
        dragged = e;
    }

    private UIElement hit(float x, float y) {
        for(UILayer layer : getLayersTopToBottom()) {
            if(!layer.isVisible() || !layer.receivesInput()) continue;

            UIElement hit = layer.getCanvas().hit(x, y);
            if(hit != null) return hit;

            if(layer.isModal()) return null;
        }

        return null;
    }

    private void updateHover(UIElement current) {
        if(current != hovered) {
            if(hovered instanceof Hoverable h) {
                h.onHoverExit();
            }

            hovered = current;

            if(hovered instanceof Hoverable h) {
                h.onHoverEnter();
            }
        }
    }

    @Override
    public boolean mouseMoved(int mx, int my) {
        UIElement current = hit(mx, my);
        updateHover(current);

        return false;
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        pressed = hit(x, y);

        dragCandidate = pressed;
        dragging = false;

        pressX = x;
        pressY = y;

        lastX = x;
        lastY = y;

        if(pressed instanceof Clickable c) {
            return c.onMouseDown(x, y, button);
        }

        return false;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        UIElement released = hit(x, y);

        boolean consumed = false;

        // finish drag FIRST
        if(captured instanceof Draggable d) {
            consumed |= d.onDragEnd(x, y);
        }

        // click only if not dragging
        if(!dragging && pressed instanceof Clickable c) {
            consumed |= c.onMouseUp(x, y, button);
        }

        // normal click release
        if(released instanceof Clickable c) {
            consumed |= c.onMouseUp(x, y, button);

            if(c == pressed) {
                consumed |= c.onPressed(x, y, button);
            }
        }

        pressed = null;
        dragCandidate = null;
        dragged = null;
        captured = null;
        dragging = false;

        return consumed;
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        float dxTotal = x - pressX;
        float dyTotal = y - pressY;

        boolean consumed = false;

        if (!dragging && dragCandidate != null) {
            if (dxTotal * dxTotal + dyTotal * dyTotal > 6*6) {
                dragging = true;
                dragged = dragCandidate;
                setCapture(dragged);

                if (dragged instanceof Draggable d) {
                    consumed |= d.onDragStart(pressX, pressY);
                }
            }
        }

        if (captured instanceof Draggable d) {
            float dx = x - lastX;
            float dy = y - lastY;

            consumed |= d.onDrag(x, y, dx, dy);
        }

        lastX = x;
        lastY = y;

        return consumed;
    }

    @Override
    public boolean scrolled(float dx, float dy) {
        if (hit(Engine.input().mouseX(), Engine.input().mouseY()) instanceof Scrollable s) {
            return s.onScrolled((int) dx, (int) dy);
        }
        return false;
    }

    @Override
    public boolean keyDown(int key) {
        if(focused instanceof KeyInputTarget t) {
            if(t.keyDown(key)) return true;
        }

        for(UILayer layer : getLayersTopToBottom()) {
            if(!layer.receivesInput() || !layer.isVisible()) continue;

            if(layer.keyDown(key)) return true;

            if(layer.isModal()) return true;
        }

        return false;
    }

    @Override
    public boolean keyUp(int key) {

        if(focused instanceof KeyInputTarget t) {
            if(t.keyUp(key)) return true;
        }

        for(UILayer layer : getLayersTopToBottom()) {
            if(!layer.receivesInput() || !layer.isVisible()) continue;

            if(layer.keyUp(key)) return true;

            if(layer.isModal()) return true;
        }

        return false;
    }

    @Override
    public boolean keyTyped(char c) {

        if(focused instanceof KeyInputTarget t) {
            if(t.keyTyped(c)) return true;
        }

        for(UILayer layer : getLayersTopToBottom()) {
            if(!layer.receivesInput() || !layer.isVisible()) continue;

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

        uiLayers.addFirst(uiLayer);

        uiLayer.onEnter();
        uiLayer.onFocus();

        updateHover(null);
    }

    public void pop(UIExitReason reason) {
        if(uiLayers.isEmpty()) return;

        UILayer top = uiLayers.removeFirst();
        top.onExit(reason != null ? reason : UIExitReason.SYSTEM);

        UILayer newTop = top();
        if(newTop != null) {
            newTop.onFocus();
        }

        updateHover(null);
        pressed = null;
        captured = null;
        focused = null;
    }

    public void clear() {
        while(!uiLayers.isEmpty()) {
            UILayer uiLayer = uiLayers.removeFirst();
            uiLayer.onExit(UIExitReason.TRANSITION);
        }

        updateHover(null);
        pressed = null;
        captured = null;
        focused = null;
    }

    public void requestPop(UILayer layer, UIExitReason reason) {
        if (layer == null || uiLayers.isEmpty()) return;

        boolean wasTop = (layer == top());

        if (!uiLayers.remove(layer)) return;

        // Always call exit
        layer.onExit(reason != null ? reason : UIExitReason.SYSTEM);

        // If it was top, restore focus to new top
        if (wasTop) {
            UILayer newTop = top();
            if (newTop != null) {
                newTop.onFocus();
            }
        }

        updateHover(null);
        pressed = null;
        captured = null;
        focused = null;
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
            layer.update(ctx, delta);
        }
    }

    public void render(RendererContext ctx) {
        for(UILayer layer : getLayersBottomToTop()) {
            if(layer.isVisible()) layer.render(ctx);
        }
    }
}

