package karnickeldev.solar.ui.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import karnickeldev.solar.render.core.RendererContext;

/**
 * @author KarnickelDev
 * @since 11.04.2026
 **/
public class Panel extends UIElement {

    public static final Texture WHITE;
    static {
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(1,1,1,1);
        pix.drawPixel(0,0);
        WHITE = new Texture(pix);
        pix.dispose();
    }

    private final Color color = new Color(1,1,1,0);
    private final Texture texture;

    public Panel(Texture texture, Color color) {
        this.texture = texture;
        if(color != null) this.color.set(color);
    }

    public Panel(Color color) {
        this(WHITE, color);
    }

    @Override
    public void act(float dt) {

    }

    @Override
    public void render(RendererContext ctx) {
        if(!isVisible() || color.a == 0) return;

        ctx.batch().setColor(color);
        ctx.batch().draw(texture, getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public boolean handleInput(InputEvent e) {
        return false;
    }
}
