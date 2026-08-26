package karnickeldev.solar.ui.core;

import karnickeldev.solar.context.GameContext;
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

    /** element receiving keyboard input */
    private UIElement focused;

    private final PointerGesture pointerGesture = new PointerGesture();

    private UIManager() {}

    public UILayoutEngine.UILayoutContext getLayoutContext() {
        return uiLayoutContext;
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
        if(current == hovered) return;

        UIElement old = hovered;
        hovered = current;

        if(old instanceof Hoverable h) {
            h.onHoverExit();
        }

        if(current instanceof Hoverable h) {
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
        if(pointerGesture.targets(element, includeDescendants)) {
            pointerGesture.cancel();
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
        pointerGesture.cancel();
    }

    @Override
    public void inputCancelled() {
        clearInteraction();
    }

    public void setFocus(UIElement element) {
        if (focused == element) return;

        UIElement old = focused;
        focused = element;

        if(old instanceof Focusable f) {
            f.onFocusLost();
        }

        if (element instanceof Focusable f) {
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
        UIElement current = hit(mx, my);
        updateHover(current);

        return false;
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        return pointerGesture.begin(hit(x, y), x, y, pointer, button);
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        return pointerGesture.release(hit(x, y), x, y, pointer, button);
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer, int button) {
        return pointerGesture.drag(x, y, pointer, button);
    }

    @Override
    public boolean scrolled(float dx, float dy) {
        if (hit(Engine.input().mouseX(), Engine.input().mouseY()) instanceof Scrollable s) {
            return s.onScrolled(dx, dy);
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
            if(!layer.isVisible()) continue;
            // if(!layer.isActive()) continue; // TODO: check isActive semantics
            layer.update(ctx, delta);
        }
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
