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

    static float resolveWidth(UIElement e, UILayout l, float baseW, float scale) {
        return switch(l.getWidthMode()) {

            case FIXED -> l.getWidthValue() * scale;

            case PERCENT -> l.getWidthValue() * baseW;

            case CONTENT -> e.getMeasuredWidth();

            default -> 0f;
        };
    }

     static float resolveHeight(UIElement e, UILayout l, float baseH, float scale) {
        return switch(l.getHeightMode()) {

            case FIXED -> l.getHeightValue() * scale;

            case PERCENT -> l.getHeightValue() * baseH;

            case CONTENT -> e.getMeasuredHeight();

            default -> 0f;
        };
    }

}
