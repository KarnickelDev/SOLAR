package karnickeldev.solar.render.background;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.physics.Units;
import karnickeldev.solar.physics.Vector2D;
import karnickeldev.solar.render.camera.FloatingOriginCamera;
import karnickeldev.solar.render.core.RenderPass;
import karnickeldev.solar.render.core.RendererContext;

/**
 * @author KarnickelDev
 * @since 19.02.2026
 **/
public class RingRenderer implements RenderPass {

    private static final Color LINE_COLOR = new Color(28 / 255f, 28 / 255f, 32 / 255f, 1f);

    private static final float THICKNESS_PX = 5f;

    private static boolean renderOn = true;

    public static void toggleRender() {
        renderOn = !renderOn;
    }

    private final float[] rings = {0.1f, 0.2f, 0.5f, 1f, 2f, 5f, 10f, 20f, 50f, 100f, 200f, 500f, 1000f, 2000f, 5000f};

    @Override
    public void render(RendererContext ctx, float delta) {
        ShapeRenderer shapes = ctx.shapes();
        FloatingOriginCamera cam = GameContext.get().getWorldManager().getActiveWorld().getCamera();

        shapes.setColor(LINE_COLOR);
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setProjectionMatrix(new Matrix4().setToOrtho2D(0,0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));

        Vector2D center = cam.project(new Vector2D().zero());

        float maxUsefulRadius = (float)Math.hypot(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()) * 3f;

        for(float ring : rings) {

            float r = (float) cam.projectLength(Units.toSU(ring, Units.Length.AU));
            if (r < 2f || r > maxUsefulRadius) continue;

            shapes.circle((float)center.getX(),Gdx.graphics.getHeight() - (float)center.getY(), r, 128);
        }

        shapes.end();

    }
}
