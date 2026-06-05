package karnickeldev.solar.render.core;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import karnickeldev.solar.ui.fontutil.kernel.MSDFBatch;

/**
 * @author KarnickelDev
 * @since 12.12.2025
 **/
public record RendererContext(
    SpriteBatch batch,
    UIRenderer uiRenderer,
    ShapeRenderer debug
) {}
