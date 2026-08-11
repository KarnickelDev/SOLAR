package karnickeldev.solar.ui.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import karnickeldev.solar.core.Engine;
import karnickeldev.solar.render.core.UIRenderer;
import karnickeldev.solar.util.MathUtil;

/**
 * @author KarnickelDev
 * @since 01.06.2026
 **/
public final class UIHelper {

    public static boolean hit(UIElement e, float x, float y) {
        float my = Engine.getHeight() - y;
        return MathUtil.AABB(x, my, e.getX(), e.getY(), e.getRight(), e.getTop());
    }

    public static void drawBackground(UIRenderer renderer, UIElement element, int backgroundColor, Texture tex) {
        if((backgroundColor & 0x000000FF) > 0) {
            renderer.setQuadColor(backgroundColor);
            renderer.drawQuad(tex, element.getX(), element.getY(), element.getWidth(), element.getHeight());
        }
    }

    public static void drawBackground(UIRenderer renderer, UIElement element, Color backgroundColor, Texture tex) {
        drawBackground(renderer, element, Color.rgba8888(backgroundColor), tex);
    }

    public static void drawBorder(UIRenderer renderer, UIElement element, int borderColor, float borderSize, Texture tex) {
        if(borderSize != 0 && (borderColor & 0x000000FF) > 0) {
            renderer.setQuadColor(borderColor);

            renderer.drawQuad(tex, element.getX(), element.getY(), borderSize, element.getHeight());
            renderer.drawQuad(tex, element.getRight() - borderSize, element.getY(), borderSize, element.getHeight());
            renderer.drawQuad(tex, element.getX() + borderSize, element.getY(), element.getWidth() - 2*borderSize, borderSize);
            renderer.drawQuad(tex, element.getX() + borderSize, element.getTop() - borderSize, element.getWidth() - 2*borderSize, borderSize);
        }
    }

}
