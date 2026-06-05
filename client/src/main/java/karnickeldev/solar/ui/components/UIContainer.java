package karnickeldev.solar.ui.components;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import karnickeldev.solar.render.core.RendererContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author KarnickelDev
 * @since 10.04.2026
 **/
public class UIContainer extends UIElement {

    protected final List<UIElement> children = new ArrayList<>(4);

    public UIContainer() {}

    public List<UIElement> getChildren() {
        return children;
    }

    public void add(UIElement child) {
        children.add(child);
        child.setParent(this);
        child.invalidateLayout();
    }

    @Override
    public void act(float dt) {
        for(UIElement child : children) {
            child.act(dt);
        }
    }

    @Override
    public void render(RendererContext ctx) {
        if(!isVisible()) return;

        // last, render children
        for(UIElement child : children) {
            child.render(ctx);
        }
    }

    @Override
    public boolean handleInput(InputEvent e) {
        for(UIElement child : children) {
            if(child.handleInput(e)) return true;
        }
        return false;
    }

    @Override
    public void invalidateLayout() {
        super.invalidateLayout();

        for(UIElement child : children) {
            child.invalidateLayout();
        }
    }

    @Override
    public void updateLayout(UILayoutEngine.UILayoutContext ctx) {
        super.updateLayout(ctx);

        for(UIElement child : children) {
            child.updateLayout(ctx);
        }
    }

    @Override
    public void setDebug(boolean debug) {
        super.setDebug(debug);
        for(UIElement child : children) {
            child.setDebug(debug);
        }
    }

    @Override
    public void renderDebug(RendererContext ctx) {
        // IMPORTANT: render in reverse, so child debug outline not obscured
        super.renderDebug(ctx);

        for(UIElement child : children) {
            child.renderDebug(ctx);
        }
    }

}
