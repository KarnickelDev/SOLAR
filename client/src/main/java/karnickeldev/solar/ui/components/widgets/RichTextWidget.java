package karnickeldev.solar.ui.components.widgets;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Align;
import karnickeldev.solar.core.SimTestScreen;
import karnickeldev.solar.render.core.RendererContext;
import karnickeldev.solar.ui.components.UIElement;
import karnickeldev.solar.ui.components.UILayoutEngine;
import karnickeldev.solar.ui.fontutil.RichText;
import karnickeldev.solar.ui.fontutil.TextBlock;
import karnickeldev.solar.ui.fontutil.kernel.TextLayout;

/**
 * @author KarnickelDev
 * @since 16.06.2026
 **/
public class RichTextWidget extends UIElement {

    private final TextBlock textBlock = new TextBlock();
    private final TextLayout prefLayout = new TextLayout();

    public RichTextWidget(RichText richText) {
        setText(richText);
        textBlock.setAlignment(Align.left);
    }

    public void setText(RichText richText) {
        textBlock.setText(richText);
        invalidateLayout();
    }

    @Override
    public void onLayout(UILayoutEngine.UILayoutContext ctx) {
        textBlock.setUiScale(ctx.uiScaleY());
        textBlock.setMaxWidth(getContentWidth());
        textBlock.layout(SimTestScreen.font);
    }

    @Override
    public void act(float dt) {

    }

    @Override
    public void render(RendererContext ctx) {
        ctx.uiRenderer().drawText(SimTestScreen.font, textBlock.getLayout(), getContentX(), getContentY());
    }

    @Override
    public void measure(UILayoutEngine.UILayoutContext ctx) {
        prefLayout.layout(SimTestScreen.font, textBlock.getText(), 1, 1e4f, 1f);
        float cachedPrefWidth = prefLayout.getLogicalWidth();
        float cachedPrefHeight = prefLayout.getLogicalHeight();

        prefWidth = 1.1f*ctx.uiScaleY()*(cachedPrefWidth + padLeft + padRight + 2*borderThickness);
        prefHeight = 1.1f*ctx.uiScaleY()*(cachedPrefHeight + padTop + padBottom + 2*borderThickness);
    }
}
