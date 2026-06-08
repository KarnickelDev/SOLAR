package karnickeldev.solar.ui.components;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import karnickeldev.solar.render.core.RendererContext;

/**
 * @author KarnickelDev
 * @since 31.05.2026
 **/
public class Spacer extends UIElement {

    private final float vPrefWidth;
    private final float vPrefHeight;

    public Spacer(float width, float height) {
        this.vPrefWidth = width;
        this.vPrefHeight = height;
    }

    @Override
    public void measure(UILayoutEngine.UILayoutContext ctx) {
        prefWidth = vPrefWidth * ctx.uiScaleY();
        prefHeight = vPrefHeight * ctx.uiScaleY();
    }

    @Override
    public void act(float dt) {
        // nop
    }

    @Override
    public void render(RendererContext ctx) {
        // nop
    }

    @Override
    public boolean handleInput(InputEvent e) {
        return false;
    }
}
