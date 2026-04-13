package karnickeldev.solar.ui.core;

import com.badlogic.gdx.Gdx;
import karnickeldev.solar.ui.components.TextButton;
import karnickeldev.solar.ui.components.UIElement;

/**
 * @author KarnickelDev
 * @since 10.04.2026
 **/
public class UILayoutEngine {

    private static final float TARGET_ASPECT = 16f / 9f;

    /** UI Viewport*/
    public static class UIViewport {
        private float x, y, width, height;

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

    public static void apply(UIElement e, float screenWidth, float screenHeight, float ui_scale) {
        UILayout l = e.getLayout();

        // IMPORTANT: FORCE UPDATE!!!
        getUiViewportRect();

        float baseX = viewport.x();
        float baseY = viewport.y();
        float baseW = viewport.width();
        float baseH = viewport.height();
        if (e.getParent() != null) {
            UIElement p = e.getParent();
            baseX = p.getX();
            baseY = p.getY();
            baseW = p.getWidth();
            baseH = p.getHeight();
        }

        float scale = viewport.height() / UI.VIRTUAL_HEIGHT;
        float scaleX = viewport.width() / UI.VIRTUAL_WIDTH;

        boolean isRoot = (e.getParent() == null);
        float width = resolveWidth(l, baseW, scale, isRoot);
        float height = resolveHeight(l, baseH, scale, isRoot);

        if(e instanceof TextButton t) {
            if(t.getText().contains("Resume")) {
                System.out.println(baseW);
            }
        }

        float x = 0;
        float y = 0;

        switch (l.anchor) {
            case BOTTOM_LEFT -> {
                x = 0;
                y = 0;
            }
            case BOTTOM_RIGHT -> {
                x = baseW - width;
                y = 0;
            }
            case TOP_LEFT -> {
                x = 0;
                y = baseH - height;
            }
            case TOP_RIGHT -> {
                x = baseW - width;
                y = baseH - height;
            }
            case CENTER -> {
                x = (baseW - width) * 0.5f;
                y = (baseH - height) * 0.5f;
            }
            case LEFT ->  {
                x = 0;
                y = (baseH - height) * 0.5f;
            }
            case RIGHT -> {
                x = baseW - width;
                y = (baseH - height) * 0.5f;
            }
            case TOP -> {
                x = (baseW - width) * 0.5f;
                y = baseH - height;
            }
            case BOTTOM ->  {
                x = (baseW - width) * 0.5f;
                y = 0;
            }
        }

        if(e.getParent() != null) {
            x = 0;
            y = 0;
        }

        // apply parent pos
        x += baseX;
        y += baseY;

        // apply scaled offset
        float offsetScale = isRoot ? scale : scale;
        x += l.offsetX * offsetScale;
        y += l.offsetY * offsetScale;

        // persist changes
        e.setWidth(width);
        e.setHeight(height);
        e.setX(x);
        e.setY(y);
    }

    private static float resolveWidth(UILayout l, float baseW, float scale, boolean isRoot) {
        if (l.widthPercent > 0) {
            return l.widthPercent * baseW;
        }
        return isRoot ? l.fixedWidth * scale : l.fixedWidth * scale;
    }

    private static float resolveHeight(UILayout l, float baseH, float scale, boolean isRoot) {
        if (l.heightPercent > 0) {
            return l.heightPercent * baseH;
        }
        return isRoot ? l.fixedHeight * scale : l.fixedHeight * scale;
    }

}
