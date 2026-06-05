package karnickeldev.solar.ui.components;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import karnickeldev.solar.render.core.RendererContext;

/**
 * @author KarnickelDev
 * @since 31.05.2026
 **/
public class Spacer extends UIElement {

    private final float prefWidth;
    private final float prefHeight;

    public Spacer(float width, float height) {
        this.prefWidth = width;
        this.prefHeight = height;
    }

    @Override
    public float getPreferredWidth(float scale) {
        return prefWidth * scale;
    }

    @Override
    public float getPreferredHeight(float scale) {
        return prefHeight * scale;
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
