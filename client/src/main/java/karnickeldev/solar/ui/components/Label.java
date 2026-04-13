package karnickeldev.solar.ui.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Align;
import karnickeldev.solar.render.core.RendererContext;
import karnickeldev.solar.ui.core.UILayoutEngine;
import karnickeldev.solar.ui.fontutil.MSDFBatch;
import karnickeldev.solar.ui.fontutil.MSDFFont;
import karnickeldev.solar.ui.fontutil.TextCache;
import org.lwjgl.opengl.GL20;

/**
 * @author KarnickelDev
 * @since 13.04.2026
 **/
public class Label extends UIElement {

    public static MSDFFont font;
    public static MSDFBatch batch;

    private final TextCache layout = new TextCache("");

    private int fontSize = 14;

    protected final Color fontColor = new Color(1,1,1,1);
    protected final Color backgroundColor = new Color(0,0,0,0.5f);
    protected final Texture backgroundTex;

    protected float padding = 10f;

    protected int align = Align.center;

    private boolean wrap = false;

    public Label(String text, Texture backgroundTex) {
        this.backgroundTex = backgroundTex;
        layout.setText(text);
    }

    public Label(String text) {
        this(text, Panel.WHITE);
    }

    public void pad(float pad) {
        this.padding = pad;
    }

    public void setText(String text) {
        layout.setText(text);
    }

    public String getText() {
        return "";
    }

    public void setWrap(boolean wrap) {
        this.wrap = wrap;
    }

    public void setAlignment(int align) {
        this.align = align;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
        invalidateLayout();
    }

    public void setFontColor(Color fontColor) {
        this.fontColor.set(fontColor);
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor.set(backgroundColor);
    }

    @Override
    public void layout(float width, float height, float ui_scale) {
        super.layout(width, height, ui_scale);
        layout.setFontSize(fontSize * UILayoutEngine.getUIScaleY());
    }

    @Override
    public void act(float dt) {
        //layout.setText(font, text, fontColor,getWidth() - 2 * padding * UILayoutEngine.getUIScaleY(), align, wrap);
        layout.rebuildIfNeeded(font);
    }

    @Override
    public void render(RendererContext ctx) {
        Matrix4 proj = new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        if(backgroundColor.a > 0) {
            ctx.batch().end();
            ctx.batch().begin();
            ctx.batch().setColor(backgroundColor);
            ctx.batch().draw(backgroundTex, getX(), getY(), getWidth(), getHeight());
        }

        ctx.batch().end();

        float pad = padding * UILayoutEngine.getUIScaleY();

        //font.draw(ctx.batch(), layout, getX() + pad, getY() + getHeight() - layout.height - pad);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        batch.begin(proj);

        float dx;
        float dy;

        if(Align.isLeft(align)) {
            dx = pad;
        } else if(Align.isRight(align)) {
            dx = getWidth() - pad - layout.layout().width;
        } else {
            dx = (getWidth() - layout.layout().width) * 0.5f;
        }

        if(Align.isTop(align)) {
            dy = getHeight() - pad - layout.layout().height;
        } else if(Align.isBottom(align)) {
            dy = pad;
        } else {
            dy = (getHeight() - layout.layout().height) * 0.5f;
        }

        batch.draw(font, layout.layout(), getX() + dx, getY() + dy);

        batch.end();

        ctx.batch().begin();
    }

    @Override
    public boolean handleInput(InputEvent e) {
        return false;
    }
}
