package karnickeldev.solar.ui.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import karnickeldev.solar.input.InputHandler;
import karnickeldev.solar.logging.LogTag;
import karnickeldev.solar.logging.Logger;
import karnickeldev.solar.render.core.RendererContext;
import karnickeldev.solar.ui.components.UIElement;
import karnickeldev.solar.ui.components.UILayoutEngine;

import java.util.*;

/**
 * @author KarnickelDev
 * @since 04.04.2026
 **/
public abstract class UILayer implements InputHandler {

    private final Logger logger = Logger.get(LogTag.UI);

    protected final Stage stage;
    private final String name;

    protected boolean visible = true;

    private final Map<String, UIComponent> uiComponents = new HashMap<>();

    private final List<UIElement> uiElements = new ArrayList<>(16);

    public UILayer(String name, Stage stage) {
        this.name = name;
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
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

    public void addElement(UIElement element) {
        if(uiElements.contains(element)) return;

        uiElements.add(element);
    }

    public void removeElement(UIElement element) {
        uiElements.remove(element);
    }

    public void update(UILayoutEngine.UILayoutContext ctx, float delta) {
        for(UIElement uiElement : uiElements) {
            uiElement.updateLayout(ctx);
        }

        for(UIElement uiElement : uiElements) {
            if(isActive()) uiElement.act(delta);
        }

        act(delta);
    }

    public void render(RendererContext ctx) {
        for(UIElement uiElement : uiElements) {
            uiElement.render(ctx);
            uiElement.renderDebug(ctx);
        }
    }

    /** Handles resizing all UI component (called from resize in screens) */
    public void resize(int width, int height) {
        logger.debug("resizing {}", getName());
        for(UIComponent component: uiComponents.values()) {
            component.resize(width, height);
        }
        stage.getViewport().update(width, height, true);
    }

    /** Updates all UI logic (called from render loop) */
    public void act(float delta) {
        stage.getViewport().apply();
        for(UIComponent component: uiComponents.values()) {
            component.update(delta);
        }
        stage.act(delta);
    }

    /** Renders all visible UI groups */
    public void draw() {
        stage.draw();
    }

    /** Adds a Component by name if it's not present */
    public void addComponent(String name, UIComponent component) {
        if(uiComponents.containsKey(name)) {
            logger.warn("Component {} already exists", name);
            getStage().addActor(uiComponents.get(name).getGroup());
            return;
        }
        uiComponents.put(name, component);
        getStage().addActor(uiComponents.get(name).getGroup());
        uiComponents.get(name).resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    /** Adds a Component by name, overwriting existing Components of that name*/
    public void addForceComponent(String name, UIComponent component) {
        uiComponents.put(name, component);
        getStage().addActor(uiComponents.get(name).getGroup());
        uiComponents.get(name).resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    /** Gets an existing component or null if not found */
    public UIComponent getComponent(String name) {
        return uiComponents.get(name);
    }

    /** Shows a component by name */
    public void showComponent(String... names) {
        for(String name: names) {
            UIComponent component = uiComponents.get(name);
            if (component != null) {
                component.show();
            } else {
                logger.warn("showComponent: {} not found", name);
            }
        }
    }

    /** Hides a component by name */
    public void hideComponent(String... names) {
        for(String name: names) {
            UIComponent component = uiComponents.get(name);
            if (component != null) {
                component.hide();
            } else {
                logger.warn("hideComponent: {} not found", name);
            }
        }
    }

    /** Removes a component from stage and internal map */
    public void removeComponent(String... names) {
        for(String name: names) {
            UIComponent component = uiComponents.remove(name);
            if (component != null) {
                component.getGroup().remove();
            } else {
                logger.warn("removeComponent: {} not found", name);
            }
        }
    }

    public Collection<UIComponent> getComponents() {
        return uiComponents.values();
    }

    public void hideAll() {
        for(UIComponent component: uiComponents.values()) {
            component.hide();
        }
    }

}
