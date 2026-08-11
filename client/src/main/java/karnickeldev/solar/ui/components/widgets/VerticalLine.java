package karnickeldev.solar.ui.components.widgets;

import karnickeldev.solar.render.core.RendererContext;
import karnickeldev.solar.ui.components.UIElement;
import karnickeldev.solar.ui.components.UILayoutEngine;

/**
 * @author KarnickelDev
 * @since 22.06.2026
 **/
public class VerticalLine extends UIElement {

    private final float thickness;
    private float cThickness;
    private int color;

    public VerticalLine(int color, float thickness) {
        this.thickness = thickness;
        this.color = color;
    }

    public VerticalLine(int color) {
        this(color, 2f);
    }

    @Override
    public void act(float dt) {}

    @Override
    public void onLayout(UILayoutEngine.UILayoutContext ctx) {
        cThickness = thickness * ctx.uiScaleY();
    }

    @Override
    public void render(RendererContext ctx) {
        ctx.uiRenderer().setQuadColor(color);
        ctx.uiRenderer().drawQuad(
            Panel.WHITE,
            getContentX() + (getContentWidth() - cThickness) * 0.5f,
            getContentY(),
            cThickness,
            getContentHeight()
        );
    }

    @Override
    public void measure(UILayoutEngine.UILayoutContext ctx) {
        prefHeight = 10f;
        prefWidth = (padLeft + padRight + thickness) * ctx.uiScaleY();
    }
}
