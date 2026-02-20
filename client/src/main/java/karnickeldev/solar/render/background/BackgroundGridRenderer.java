package karnickeldev.solar.render.background;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.render.camera.FloatingOriginCamera;
import karnickeldev.solar.render.core.RenderPass;
import karnickeldev.solar.render.core.RendererContext;
import karnickeldev.solar.util.MathUtil;

public class BackgroundGridRenderer implements RenderPass {

    private static final Color LINE_COLOR = new Color(28 / 255f, 28 / 255f, 32 / 255f, 1f);

    private static final float GRID_THICKNESS_PX = 3f;
    private static final float TARGET_GRID_SPACING_PX = 200f;

    private static boolean renderOn = false;

    public static void toggleRender() {
        renderOn = !renderOn;
    }

    @Override
    public void render(RendererContext ctx, float delta) {
        if (!renderOn) return;

        ShapeRenderer shapes = ctx.shapes();
        FloatingOriginCamera cam = GameContext.get().getWorldManager().getActiveWorld().getCamera();

        int screenW = Gdx.graphics.getWidth();
        int screenH = Gdx.graphics.getHeight();
        float halfW = screenW * 0.5f;
        float halfH = screenH * 0.5f;

        double zoom = cam.getZoom();
        //long camX_mm = cam.getOriginXmm();  // camera in world coordinates
        //long camY_mm = cam.getOriginYmm();

        double cos = Math.cos(-cam.getRotation());
        double sin = Math.sin(-cam.getRotation());

        long camX_mm = (long) (cam.getOriginXmm() * cos + cam.getOriginYmm() * sin);
        long camY_mm = (long) (-cam.getOriginXmm() * sin + cam.getOriginYmm() * cos);

        // =====================================================
        // Determine grid spacing in world units based on zoom
        // =====================================================
        double desiredWorldSpacing = TARGET_GRID_SPACING_PX * zoom; // pixels → world

        double log = Math.log10(desiredWorldSpacing);
        double baseExp = Math.floor(log);
        double frac = log - baseExp;

        double minorWorld = Math.pow(10, baseExp);
        double majorWorld = minorWorld * 10.0;

        long minorStep_mm = (long) (minorWorld * 1e6);
        long majorStep_mm = (long) (majorWorld * 1e6);

        float minorAlpha = smoothstep(0.2f, 0.8f, 1f - (float) frac);
        float majorAlpha = smoothstep(0.2f, 0.8f, (float) frac);

        // =====================================================
        // Render setup
        // =====================================================
        shapes.setProjectionMatrix(shapes.getProjectionMatrix().idt().setToOrtho2D(0, 0, screenW, screenH));
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // minor lines
        drawAnchoredGrid(shapes, camX_mm, camY_mm, zoom, screenW, screenH, halfW, halfH, minorStep_mm, minorAlpha);
        // major lines
        drawAnchoredGrid(shapes, camX_mm, camY_mm, zoom, screenW, screenH, halfW, halfH, majorStep_mm, majorAlpha);

        shapes.end();
    }

    /**
     * Draws a grid that is anchored in world-space.
     * The grid lines stay fixed relative to world (0,0).
     */
    private void drawAnchoredGrid(ShapeRenderer shapes, long camX_mm, long camY_mm, double zoom,
                                  int screenW, int screenH, float halfW, float halfH,
                                  long step_mm, float alpha) {

        if (alpha <= 0.01f || step_mm <= 0) return;

        shapes.setColor(LINE_COLOR.r, LINE_COLOR.g, LINE_COLOR.b, alpha);

        long halfW_mm = (long) (halfW * zoom * 1e6);
        long halfH_mm = (long) (halfH * zoom * 1e6);

        long minX_mm = camX_mm - halfW_mm;
        long maxX_mm = camX_mm + halfW_mm;
        long minY_mm = camY_mm - halfH_mm;
        long maxY_mm = camY_mm + halfH_mm;

        long startX = floorDiv(minX_mm, step_mm) * step_mm;
        long endX = floorDiv(maxX_mm, step_mm) * step_mm;
        long startY = floorDiv(minY_mm, step_mm) * step_mm;
        long endY = floorDiv(maxY_mm, step_mm) * step_mm;

        // vertical lines
        for (long x_mm = startX; x_mm <= endX; x_mm += step_mm) {
            float px = (float) (halfW + (x_mm - camX_mm) / zoom / 1e6);
            shapes.rect(px, 0, GRID_THICKNESS_PX, screenH);
        }

        // horizontal lines
        for (long y_mm = startY; y_mm <= endY; y_mm += step_mm) {
            float py = (float) (halfH + (y_mm - camY_mm) / zoom / 1e6);
            shapes.rect(0, py, screenW, GRID_THICKNESS_PX);
        }
    }

    private static long floorDiv(long a, long b) {
        long r = a / b;
        if ((a ^ b) < 0 && a % b != 0) r--;
        return r;
    }

    private static float smoothstep(float edge0, float edge1, float x) {
        x = MathUtil.clamp((x - edge0) / (edge1 - edge0), 0f, 1f);
        return x * x * (3f - 2f * x);
    }
}
