package karnickeldev.solar.ui.core;

import com.badlogic.gdx.Gdx;
import karnickeldev.solar.input.InputHandler;
import karnickeldev.solar.logging.LogTag;
import karnickeldev.solar.logging.Logger;
import karnickeldev.solar.render.core.RendererContext;
import karnickeldev.solar.ui.components.Canvas;
import karnickeldev.solar.ui.components.UIElement;
import karnickeldev.solar.ui.components.UILayoutEngine;

import java.util.*;

/**
 * @author KarnickelDev
 * @since 04.04.2026
 **/
public abstract class UILayer implements InputHandler {

    private final Logger logger = Logger.get(LogTag.UI);

    private final String name;

    protected boolean visible = true;

    protected final Canvas canvas = new Canvas();

    public UILayer(String name) {
        this.name = name;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return UIManager.get().top() == this;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isModal() {
        return false;
    }

    public boolean blocksInput() {
        return false;
    }

    public boolean receivesInput() {
        return true;
    }

    public abstract void onEnter();

    public abstract void onExit(UIExitReason reason);

    public abstract void onFocus();

    public abstract void onBlur();

    /** Updates all UI logic (called from render loop) */
    public abstract void act(float delta);

    public final void addToCanvas(UIElement element, Canvas.CanvasSlot slot) {
        getCanvas().add(element, slot);
    }

    public final void removeFromCanvas(UIElement element) {
        canvas.remove(element);
    }

    public final void update(UILayoutEngine.UILayoutContext ctx, float delta) {
        act(delta);
        canvas.act(delta);

        canvas.measure(ctx);
        canvas.arrange(ctx, ctx.viewportX(), ctx.viewportY(), ctx.viewportWidth(), ctx.viewportHeight());
    }

    public final void render(RendererContext ctx) {
        canvas.render(ctx);
        canvas.renderDebug(ctx);
    }

    /** Handles resizing all UI component (called from resize in screens) */
    public void resize(int width, int height) {
        logger.debug("resizing {}", getName());
        getCanvas().invalidateLayout();
    }

    // TODO: remove LEGACY (below)

    /** Adds a Component by name if it's not present */
    public void addComponent(String name, UIComponent component) {

    }

    /** Adds a Component by name, overwriting existing Components of that name*/
    public void addForceComponent(String name, UIComponent component) {

    }

    /** Gets an existing component or null if not found */
    public UIComponent getComponent(String name) {
        return null;
    }

    /** Shows a component by name */
    public void showComponent(String... names) {

    }

    /** Hides a component by name */
    public void hideComponent(String... names) {

    }

    /** Removes a component from stage and internal map */
    public void removeComponent(String... names) {

    }

}
