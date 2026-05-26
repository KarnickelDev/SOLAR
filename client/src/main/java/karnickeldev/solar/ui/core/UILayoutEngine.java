package karnickeldev.solar.ui.core;

import com.badlogic.gdx.Gdx;

/**
 * @author KarnickelDev
 * @since 10.04.2026
 **/
public class UILayoutEngine {

    private static final float TARGET_ASPECT = 16f / 9f;

    /** UI Viewport*/
    public static class UIViewport {
        private float x, y, width, height;

        private UIViewport() {}

        public float x() {
            return x;
        }
        public float y() {
            return y;
        }
        public float width() {
            return width;
        }
        public float height() {
            return height;
        }
    }

    private static final UIViewport viewport = new UIViewport();

    /** UI Viewport given as int[x, y, width, height] */
    public static UIViewport getUiViewportRect() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float aspectRatio = screenWidth / screenHeight;

        if(aspectRatio < TARGET_ASPECT) {
            // vertical letterbox
            viewport.width = screenWidth;
            viewport.height = screenWidth / TARGET_ASPECT;
            viewport.x = 0;
            viewport.y = (screenHeight - viewport.height) * 0.5f;
        } else {
            // no letterboxing
            viewport.width = screenWidth;
            viewport.height = screenHeight;
            viewport.x = 0;
            viewport.y = 0;
        }

        return viewport;
    }

    public static float getUIScaleY() {
        return getUiViewportRect().height() / UI.VIRTUAL_HEIGHT;
    }

    public static void apply(UIElement e, float screenWidth, float screenHeight, float uiScale) {
        UILayout l = e.getLayout();

        // IMPORTANT: FORCE UPDATE!!!
        getUiViewportRect();

        float baseX, baseY, baseW, baseH;

        if (e.getParent() != null) {
            UIElement p = e.getParent();
            baseX = p.getX();
            baseY = p.getY();
            baseW = p.getWidth();
            baseH = p.getHeight();
        } else {
            baseX = viewport.x();
            baseY = viewport.y();
            baseW = viewport.width();
            baseH = viewport.height();
        }

        float scale = viewport.height() / UI.VIRTUAL_HEIGHT;

        // compute size
        float width = resolveWidth(l, baseW, scale);
        float height = resolveHeight(l, baseH, scale);

        // compute anchor position
        float x = computeAnchorX(l, baseW, width);
        float y = computeAnchorY(l, baseH, height);

//        if(e.getParent() != null) {
//            x = 0;
//            y = 0;
//        }

        // apply parent offset
        x += baseX;
        y += baseY;

        // apply scaled offset
        x += l.offsetX * scale;
        y += l.offsetY * scale;

        // persist changes
        e.setBounds(x, y, width, height);
    }

    private static float resolveWidth(UILayout l, float baseW, float scale) {
        if (l.widthPercent > 0) {
            return l.widthPercent * baseW;
        }
        return l.fixedWidth * scale;
    }

    private static float resolveHeight(UILayout l, float baseH, float scale) {
        if (l.heightPercent > 0) {
            return l.heightPercent * baseH;
        }
        return l.fixedHeight * scale;
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
