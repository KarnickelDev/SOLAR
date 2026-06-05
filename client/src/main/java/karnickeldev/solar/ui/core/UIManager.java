package karnickeldev.solar.ui.core;

import karnickeldev.solar.input.InputManager;
import karnickeldev.solar.render.core.RendererContext;
import karnickeldev.solar.ui.components.UILayoutEngine;

import java.util.*;

/**
 * @author KarnickelDev
 * @since 03.07.2025
 **/
public class UIManager {

    private static final UIManager instance = new UIManager();

    public static UIManager get() {
        return instance;
    }

    private final Deque<UILayer> uiLayers = new ArrayDeque<>();

    private final InputManager inputManager = new InputManager();

    private UILayoutEngine.UILayoutContext  uiLayoutContext;

    public InputManager getInputManager() {
        return inputManager;
    }

    private UIManager() {}

    public UILayoutEngine.UILayoutContext getLayoutContext() {
        return uiLayoutContext;
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
    }

    public void pop(UIExitReason reason) {
        if(uiLayers.isEmpty()) return;

        UILayer top = uiLayers.removeFirst();
        top.onExit(reason != null ? reason : UIExitReason.SYSTEM);

        UILayer newTop = top();
        if(newTop != null) {
            newTop.onFocus();
        }
    }

    public void clear() {
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
        layer.onExit(reason != null ? reason : UIExitReason.SYSTEM);

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
            layer.update(ctx, delta);
        }
    }

    /** Renders all visible UI groups */
    public void draw() {
        for (UILayer layer : getLayersBottomToTop()) {
            if(layer.isVisible()) layer.draw();
        }
    }

    public void render(RendererContext ctx) {
        for(UILayer layer : getLayersBottomToTop()) {
            if(layer.isVisible()) layer.render(ctx);
        }
    }
}

