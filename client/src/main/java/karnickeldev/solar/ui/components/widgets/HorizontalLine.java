package karnickeldev.solar.ui.components.widgets;

import karnickeldev.solar.render.core.RendererContext;
import karnickeldev.solar.ui.components.UIElement;
import karnickeldev.solar.ui.components.UILayoutEngine;

/**
 * @author KarnickelDev
 * @since 22.06.2026
 **/
public class HorizontalLine extends UIElement {

    private final float thickness;
    private float cThickness;
    private int color;

    public HorizontalLine(int color, float thickness) {
        this.thickness = thickness;
        this.color = color;
    }

    public HorizontalLine(int color) {
        this(color, 2f);
    }

    @Override
    public void onAct(float dt) {}

    @Override
    public void onLayout(UILayoutEngine.UILayoutContext ctx) {
        cThickness = thickness * ctx.uiScaleY();
    }

    @Override
    public void render(RendererContext ctx) {
        ctx.uiRenderer().setQuadColor(color);
        ctx.uiRenderer().drawQuad(
            Panel.WHITE,
            getContentX(),
            getContentY() + (getContentHeight() - cThickness) * 0.5f,
            getContentWidth(),
            cThickness
        );
    }

    @Override
    public void measure(UILayoutEngine.UILayoutContext ctx) {
        prefWidth = 10f;
        prefHeight = (padBottom + padTop + thickness) * ctx.uiScaleY();
    }
}
