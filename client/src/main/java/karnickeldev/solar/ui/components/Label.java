package karnickeldev.solar.ui.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Align;
import karnickeldev.solar.render.core.RendererContext;
import karnickeldev.solar.ui.core.UILayoutEngine;
import karnickeldev.solar.ui.fontutil.TextRun;
import karnickeldev.solar.ui.fontutil.TextStyle;
import karnickeldev.solar.ui.fontutil.kernel.MSDFBatch;
import karnickeldev.solar.ui.fontutil.kernel.MSDFFont;
import karnickeldev.solar.ui.fontutil.kernel.TextLayout;
import org.lwjgl.opengl.GL20;

/**
 * @author KarnickelDev
 * @since 13.04.2026
 **/
@SuppressWarnings("deprecation")
public class Label extends UIElement {

    public static MSDFFont font;
    public static MSDFBatch batch;

    private final TextRun[] textRun;
    private final TextLayout layout;

    private float fontSize = TextStyle.DEFAULT_FONT_SIZE;

    protected final Color backgroundColor = new Color(0,0,0,0.5f);
    protected final Texture backgroundTex;

    protected float padLeft, padRight, padTop, padBottom;

    protected int contentAlign = Align.center;
    protected int textAlign = Align.center;

    private boolean dirty = true;

    public Label(String text, Texture backgroundTex) {
        this.backgroundTex = backgroundTex;
        this.textRun = new TextRun[]{new TextRun(text, 0xFFFFFFFF, fontSize, 0)};
        layout = new TextLayout(textRun[0].text().length(), 1);
    }

    public Label(String text) {
        this(text, Panel.WHITE);
    }

    public void pad(float pad) {
        pad(pad, pad, pad, pad);
    }

    public void pad(float padLeft, float padRight, float padTop, float padBottom) {
        this.padLeft = padLeft;
        this.padRight = padRight;
        this.padTop = padTop;
        this.padBottom = padBottom;
    }

    public void setText(String text) {
        if(text.equals(textRun[0].text())) return;
        textRun[0].setText(text);
        dirty = true;
    }

    public String getText() {
        return textRun[0].text();
    }

    public void setContentAlignment(int align) {
        this.contentAlign = align;
    }

    public void setTextAlign(int align) {
        if(this.textAlign == align) return;
        this.textAlign = align;
        dirty = true;
    }

    public void setFontSize(int fontSize) {
        if(this.fontSize == fontSize) return;
        this.fontSize = fontSize;
        this.textRun[0].setScale(fontSize);
        dirty = true;
    }

    public void setFontColor(int fontColor) {
        if(this.textRun[0].color() == fontColor) return;
        this.textRun[0].setColor(fontColor);
        dirty = true;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor.set(backgroundColor);
    }

    @Override
    public void layout(float width, float height, float ui_scale) {
        super.layout(width, height, ui_scale);
        if(dirty) {
            layout.layout(font, textRun, textAlign,
                getWidth() - (padLeft + padRight) * UILayoutEngine.getUIScaleY(), UILayoutEngine.getUIScaleY());
        }
    }

    @Override
    public void act(float dt) {

    }

    @Override
    public void render(RendererContext ctx) {
        if(backgroundColor.a > 0) {
            ctx.batch().end();
            ctx.batch().begin();
            ctx.batch().setColor(backgroundColor);
            ctx.batch().draw(backgroundTex, getX(), getY(), getWidth(), getHeight());
        }

        ctx.batch().end();

        float scale = UILayoutEngine.getUIScaleY();
        float pLeft = padLeft * scale;
        float pRight = padRight * scale;
        float pTop = padTop * scale;
        float pBottom = padBottom * scale;

        //font.draw(ctx.batch(), layout, getX() + pad, getY() + getHeight() - layout.height - pad);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        batch.begin();

        float dx;
        float dy;

        if(Align.isLeft(contentAlign)) {
            dx = pLeft;
        } else if(Align.isRight(contentAlign)) {
            dx = getWidth() - pRight - layout.getBoundsWidth();
        } else {
            dx = (getWidth() - layout.getBoundsWidth()) * 0.5f;
        }

        if(Align.isTop(contentAlign)) {
            dy = getHeight() - pTop - layout.getBoundsHeight();
        } else if(Align.isBottom(contentAlign)) {
            dy = pBottom;
        } else {
            dy = (getHeight() - layout.getBoundsHeight()) * 0.5f;
        }

        batch.draw(font, layout, getX() + dx, getY() + dy);

        batch.end();

        ctx.batch().begin();
    }

    @Override
    public boolean handleInput(InputEvent e) {
        return false;
    }
}
