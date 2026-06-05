package karnickeldev.solar.ui.components;

import karnickeldev.solar.ui.core.UI;

/**
 * @author KarnickelDev
 * @since 10.04.2026
 **/
public class UILayoutEngine {

    private static final float TARGET_ASPECT = 16f / 9f;

    /** UI Viewport*/
    public record UILayoutContext(
        float screenWidth,
        float screenHeight,
        float aspectRatio,
        float viewportX,
        float viewportY,
        float viewportWidth,
        float viewportHeight,
        float uiScaleY
    ) {}

    /** UI Viewport given as int[x, y, width, height] */
    public static UILayoutContext computeLayoutContext(float screenWidth, float screenHeight) {
        float aspectRatio = screenWidth / screenHeight;

        float viewportX, viewportY, viewportWidth, viewportHeight;

        if(aspectRatio < TARGET_ASPECT) {
            // vertical letterbox
            viewportWidth = screenWidth;
            viewportHeight = screenWidth / TARGET_ASPECT;
            viewportX = 0;
            viewportY = (screenHeight - viewportHeight) * 0.5f;
        } else {
            // no letterboxing
            viewportWidth = screenWidth;
            viewportHeight = screenHeight;
            viewportX = 0;
            viewportY = 0;
        }

        float uiScaleY = viewportHeight / UI.VIRTUAL_HEIGHT;

        return new UILayoutContext(screenWidth, screenHeight, aspectRatio,  viewportX, viewportY, viewportWidth, viewportHeight, uiScaleY);
    }

    static void computeLayout(UIElement e, UILayoutContext ctx) {
        UILayout l = e.getLayout();

        float baseX, baseY, baseW, baseH;

        if (e.getParent() != null) {
            UIElement p = e.getParent();
            baseX = p.getContentX();
            baseY = p.getContentY();
            baseW = p.getContentWidth();
            baseH = p.getContentHeight();
        } else {
            baseX = ctx.viewportX();
            baseY = ctx.viewportY();
            baseW = ctx.viewportWidth();
            baseH = ctx.viewportHeight();
        }

        float scale = ctx.uiScaleY();

        // compute size
        float width = resolveWidth(e, l, baseW, scale);
        float height = resolveHeight(e, l, baseH, scale);

        // compute anchor position
        float x = computeAnchorX(l, baseW, width);
        float y = computeAnchorY(l, baseH, height);

        // this is important: without it, we can't use parent-relative positions for child elements
        if(e.getParent() != null) {
            x = 0;
            y = 0;
        }

        // apply parent offset
        x += baseX;
        y += baseY;

        // apply scaled offset
        x += l.offsetX * scale;
        y += l.offsetY * scale;

        // persist changes
        e.setBounds(x, y, width, height);
        e.updateMetrics(scale);
    }

    private static float resolveWidth(UIElement e, UILayout l, float baseW, float scale) {
        return switch(l.getWidthMode()) {

            case FIXED -> l.getWidthValue() * scale;

            case PERCENT -> l.getWidthValue() * baseW;

            case CONTENT -> e.getPreferredWidth(scale);

            default -> 0f;
        };
    }

    private static float resolveHeight(UIElement e, UILayout l, float baseH, float scale) {
        return switch(l.getHeightMode()) {

            case FIXED -> l.getHeightValue() * scale;

            case PERCENT -> l.getHeightValue() * baseH;

            case CONTENT -> e.getPreferredHeight(scale);

            default -> 0f;
        };
    }

    private static float computeAnchorX(UILayout l, float baseW, float w) {
        return switch (l.anchor) {
            case LEFT, TOP_LEFT, BOTTOM_LEFT -> 0;
            case RIGHT, TOP_RIGHT, BOTTOM_RIGHT -> baseW - w;
            case CENTER, TOP, BOTTOM -> (baseW - w) * 0.5f;
        };
    }

    private static float computeAnchorY(UILayout l, float baseH, float h) {
        return switch (l.anchor) {
            case BOTTOM, BOTTOM_LEFT, BOTTOM_RIGHT -> 0;
            case TOP, TOP_LEFT, TOP_RIGHT -> baseH - h;
            case CENTER, LEFT, RIGHT -> (baseH - h) * 0.5f;
        };
    }

}
