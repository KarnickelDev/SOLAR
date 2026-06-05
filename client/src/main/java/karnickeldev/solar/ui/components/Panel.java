package karnickeldev.solar.ui.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import karnickeldev.solar.core.SolarMain;
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
    private final Color borderColor = new Color(1,1,1,0);

    private final Texture texture;

    public Panel(Texture texture, Color color, Color borderColor) {
        this.texture = texture;
        if(color != null) this.color.set(color);
        if(borderColor != null) this.borderColor.set(borderColor);
    }

    public Panel(Color background, Color borderColor) {
        this(WHITE, background, borderColor);
    }

    @Override
    public void act(float dt) {

    }

    @Override
    public void render(RendererContext ctx) {
        if(!isVisible() || color.a == 0) return;

        UIHelper.drawBackground(ctx.uiRenderer(), this, color, texture);
        UIHelper.drawBorder(ctx.uiRenderer(), this, borderColor, texture);
    }

    @Override
    public boolean handleInput(InputEvent e) {
        return false;
    }
}
