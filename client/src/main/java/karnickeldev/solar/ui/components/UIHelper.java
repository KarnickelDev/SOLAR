package karnickeldev.solar.ui.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import karnickeldev.solar.render.core.RendererContext;
import karnickeldev.solar.render.core.UIRenderer;

/**
 * @author KarnickelDev
 * @since 01.06.2026
 **/
public final class UIHelper {

    public static void drawBackground(UIRenderer renderer, UIElement element, Color backgroundColor, Texture tex) {
        if(backgroundColor.a > 0) {
            renderer.setQuadColor(backgroundColor);
            renderer.drawQuad(tex, element.getX(), element.getY(), element.getWidth(), element.getHeight());
        }
    }

    public static void drawBorder(UIRenderer renderer, UIElement element, Color borderColor, Texture tex) {
        float border = element.getBorderThickness();
        if(border != 0 && borderColor.a > 0) {
            renderer.setQuadColor(borderColor);

            renderer.drawQuad(tex, element.getX(), element.getY(), border, element.getHeight());
            renderer.drawQuad(tex, element.getRight() - border, element.getY(), border, element.getHeight());
            renderer.drawQuad(tex, element.getX() + border, element.getY(), element.getWidth() - 2*border, border);
            renderer.drawQuad(tex, element.getX() + border, element.getTop() - border, element.getWidth() - 2*border, border);
        }
    }

}
