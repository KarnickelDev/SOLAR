package karnickeldev.solar.ui.components.widgets;

import karnickeldev.solar.render.core.RendererContext;
import karnickeldev.solar.ui.components.UIElement;
import karnickeldev.solar.ui.components.UIHelper;
import karnickeldev.solar.ui.components.UILayoutEngine;
import karnickeldev.solar.ui.components.UIState;
import karnickeldev.solar.ui.components.styles.TextWidgetStyle;
import karnickeldev.solar.ui.core.Align;
import karnickeldev.solar.ui.fontutil.TextRun;
import karnickeldev.solar.ui.fontutil.kernel.TextLayout;

/**
 * @author KarnickelDev
 * @since 13.04.2026
 **/
@SuppressWarnings("deprecation")
public class TextWidget extends UIElement {

    protected final TextRun[] textRun;
    protected final TextLayout layout;

    protected UIState state = UIState.NORMAL;
    protected TextWidgetStyle textStyle;

    protected final TextLayout prefLayoutCache = new TextLayout();

    public TextWidget(String text, TextWidgetStyle textStyle) {
        this.textStyle = textStyle;
        this.textRun = new TextRun[]{new TextRun(text, textStyle.fontColor(state), 0)};
        layout = new TextLayout(textRun[0].text().length(), 1);
    }

    public TextWidget(String text) {
        this(text, new TextWidgetStyle());
    }

    public void setTextStyle(TextWidgetStyle textStyle) {
        this.textStyle = textStyle;
        this.textRun[0].setColor(textStyle.fontColor(state));
        invalidateLayout();
    }

    public void setText(String text) {
        if(text.equals(textRun[0].text())) return;
        textRun[0].setText(text);
        invalidateLayout();
    }

    public String getText() {
        return textRun[0].text();
    }

    public void setFontColor(int fontColor) {
        if(this.textRun[0].color() == fontColor) return;
        this.textRun[0].setColor(fontColor);
        invalidateLayout();
    }

    @Override
    public void measure(UILayoutEngine.UILayoutContext ctx) {
        prefLayoutCache.layout(textStyle.font, textRun, textStyle.textAlign, 1e4f, textStyle.fontSize);
        float cachedPrefWidth = prefLayoutCache.getLogicalWidth();
        float cachedPrefHeight = prefLayoutCache.getLogicalHeight();

        prefWidth = 1 + ctx.uiScaleY()*(cachedPrefWidth + padLeft + padRight + 2*borderThickness);
        prefHeight = 1 + ctx.uiScaleY()*(cachedPrefHeight + padTop + padBottom + 2*borderThickness);
    }

    @Override
    public void onLayout(UILayoutEngine.UILayoutContext ctx) {
        // layout can be invalidated from color change
        textRun[0].setColor(textStyle.fontColor(state));

        layout.layout(textStyle.font, textRun, textStyle.textAlign, getContentWidth(), ctx.uiScaleY() * textStyle.fontSize);
    }

    @Override
    public void act(float dt) {

    }

    @Override
    public void render(RendererContext ctx) {
        UIHelper.drawBackground(ctx.uiRenderer(), this, textStyle.backgroundColor(state), Panel.WHITE);
        UIHelper.drawBorder(ctx.uiRenderer(), this, textStyle.borderColor(state), getBorderThickness(), Panel.WHITE);

        float dx = contentAlignX(getContentWidth(), layout.getLogicalWidth(), textStyle.contentAlign);
        float dy = contentAlignY(getContentHeight(), layout.getLogicalHeight(), textStyle.contentAlign);

        ctx.uiRenderer().drawText(textStyle.font, layout, getContentX() + dx, getContentY() + dy);
    }



    // Alignment Helpers

    public static float contentAlignX(float maxWidth, float width, int align) {
        if(Align.isRight(align)) {
            return maxWidth - width;
        } else if(Align.isCenter(align)) {
            return (maxWidth - width) * 0.5f;
        }

        return 0f;
    }

    public static float contentAlignY(float maxHeight, float height, int align) {
        if(Align.isTop(align)) {
            return maxHeight - height;
        } else if(Align.isMiddle(align)) {
            return (maxHeight - height) * 0.5f;
        }

        return 0f;
    }
}
