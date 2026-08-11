package karnickeldev.solar.ui.components.widgets;

import karnickeldev.solar.render.core.RendererContext;
import karnickeldev.solar.ui.components.UIElement;
import karnickeldev.solar.ui.components.UILayoutEngine;
import karnickeldev.solar.ui.components.interaction.Draggable;
import karnickeldev.solar.ui.components.interaction.Hoverable;
import karnickeldev.solar.util.MathUtil;

/**
 * @author KarnickelDev
 * @since 23.06.2026
 **/
public class SliderTrack extends UIElement implements Draggable, Hoverable {

    private final float min;
    private final float max;
    private final float step;

    private float value;

    public SliderTrack(float min, float max, float step) {
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = 0.5f;
    }

    @Override
    public void act(float dt) {

    }

    @Override
    public void render(RendererContext ctx) {
        ctx.uiRenderer().setQuadColor(0xFFFFFFFF);
        float t = 0.15f * getContentHeight();
        ctx.uiRenderer().drawQuad(Panel.WHITE,
            getContentX(),
            getContentY() + (getContentHeight() - t) * 0.5f,
            getContentWidth(),
            t
        );

        float thumbX = value * getContentWidth();
        float s = 0.5f * getContentHeight();

        ctx.uiRenderer().drawQuad(Panel.WHITE,
            getContentX() + thumbX - s*0.5f,
            getContentY() + (getContentHeight() - s) * 0.5f,
            s, s
        );
    }

    @Override
    public void measure(UILayoutEngine.UILayoutContext ctx) {
        prefWidth = 100f;
        prefHeight = 20f;
    }

    @Override
    public boolean onDragStart(float x, float y) {
        return false;
    }

    @Override
    public boolean onDrag(float x, float y, float dx, float dy) {

        float tx = value * getContentWidth();
        tx += dx;

        value = MathUtil.clamp(tx / getContentWidth(), 0, 1);

        return true;
    }

    @Override
    public boolean onDragEnd(float x, float y) {
        return false;
    }

    @Override
    public void onHoverEnter() {

    }

    @Override
    public void onHoverExit() {

    }
}
