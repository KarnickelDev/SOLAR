package karnickeldev.solar.ui.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Align;
import karnickeldev.solar.render.core.RendererContext;
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

    private final TextRun[] textRun;
    private final TextLayout layout;

    private float fontSize = TextStyle.DEFAULT_FONT_SIZE;

    protected final Color backgroundColor = new Color(0,0,0,0.5f);
    protected final Color borderColor = new Color(0,0,0,0);
    protected final Texture backgroundTex;

    protected int contentAlign = Align.center;
    protected int textAlign = Align.center;

    private final TextLayout prefLayoutCache = new TextLayout();
    private float cachedPrefWidth = 100f;
    private float cachedPrefHeight = 100f;

    private boolean dirty = true;

    public Label(String text, Texture backgroundTex) {
        this.backgroundTex = backgroundTex;
        this.textRun = new TextRun[]{new TextRun(text, 0xFFFFFFFF, fontSize, 0)};
        layout = new TextLayout(textRun[0].text().length(), 1);
    }

    public Label(String text) {
        this(text, Panel.WHITE);
    }

    public void setBorderColor(int rgba) {
        borderColor.set(rgba);
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
    public float getPreferredWidth(float scale) {
        return 1.1f*scale*(cachedPrefWidth + padLeft + padRight + 2*borderThickness);
    }

    @Override
    public float getPreferredHeight(float scale) {
        return 1.1f*scale*(cachedPrefHeight + padTop + padBottom + 2*borderThickness);
    }

    @Override
    public void updateLayout(UILayoutEngine.UILayoutContext ctx) {
        if(dirty) {
            prefLayoutCache.layout(font, textRun, textAlign, ctx.screenWidth(), 1f);
            cachedPrefWidth = prefLayoutCache.getBoundsWidth();
            cachedPrefHeight = prefLayoutCache.getBoundsHeight();
        }
        super.updateLayout(ctx);
        if(dirty) {
            layout.layout(font, textRun, textAlign, getContentWidth(), ctx.uiScaleY());
        }
    }

    @Override
    public void act(float dt) {

    }

    @Override
    public void render(RendererContext ctx) {
        UIHelper.drawBackground(ctx.uiRenderer(), this, backgroundColor, backgroundTex);
        UIHelper.drawBorder(ctx.uiRenderer(), this, borderColor, Panel.WHITE);

        float dx;
        float dy;

        if(Align.isLeft(contentAlign)) {
            dx = 0;
        } else if(Align.isRight(contentAlign)) {
            dx = getContentWidth() - layout.getBoundsWidth();
        } else {
            dx = (getContentWidth() - layout.getBoundsWidth()) * 0.5f;
        }

        if(Align.isTop(contentAlign)) {
            dy = getContentHeight() - layout.getBoundsHeight();
        } else if(Align.isBottom(contentAlign)) {
            dy = 0;
        } else {
            dy = (getContentHeight() - layout.getBoundsHeight()) * 0.5f;
        }

        ctx.uiRenderer().drawText(font, layout, getContentX() + dx, getContentY() + dy);
    }

    @Override
    public boolean handleInput(InputEvent e) {
        return false;
    }
}
