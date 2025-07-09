package karnickeldev.solar.ui.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import karnickeldev.solar.ui.components.UIComponent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : KarnickelDev
 * @since : 03.07.2025
 **/
public class UIManager {

    private static final UIManager instance = new UIManager();

    public static UIManager get() {
        return instance;
    }

    private final Stage stage;
    private final Map<String, UIComponent> uiComponents = new HashMap<>();

    private UIManager() {
        this.stage = new Stage(new FitViewport(UI.VIRTUAL_WIDTH, UI.VIRTUAL_HEIGHT, new OrthographicCamera()));
    }

    /** Adds a Component by name */
    public void addComponent(String name, UIComponent component) {
        uiComponents.put(name, component);
        getStage().addActor(uiComponents.get(name).getGroup());
        uiComponents.get(name).resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    /** Gets an existing component or null if not found */
    public UIComponent getComponent(String name) {
        return uiComponents.get(name);
    }

    /** Shows a component by name */
    public void showComponent(String name) {
        UIComponent component = uiComponents.get(name);
        if (component != null) component.getGroup().setVisible(true);
    }

    /** Hides a component by name */
    public void hideComponent(String name) {
        UIComponent component = uiComponents.get(name);
        if (component != null) component.getGroup().setVisible(false);
    }

    /** Removes a component from stage and internal map */
    public void removeComponent(String name) {
        UIComponent component = uiComponents.remove(name);
        if (component != null) {
            component.getGroup().remove();
        }
    }

    /** Handles resizing all UI component (called from resize in screens) */
    public void resize(int width, int height) {
        for(UIComponent component: uiComponents.values()) {
            component.resize(width, height);
        }
        stage.getViewport().update(width, height, true);
    }

    /** Updates all UI logic (called from render loop) */
    public void act(float delta) {
        for(UIComponent component: uiComponents.values()) {
            component.update(delta);
        }
        stage.act(delta);
        stage.getViewport().apply();
    }

    /** Renders all visible UI groups */
    public void draw() {
        stage.draw();
    }

    public Stage getStage() {
        return stage;
    }
}

