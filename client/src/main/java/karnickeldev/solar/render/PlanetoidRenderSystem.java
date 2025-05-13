package karnickeldev.solar.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import karnickeldev.solar.ecs.ClientECS;
import karnickeldev.solar.ecs.Tags;
import karnickeldev.solar.ecs.components.RadiusComponent;
import karnickeldev.solar.ecs.components.TagComponent;
import karnickeldev.solar.ecs.components.client.HCSClientSystem;
import karnickeldev.solar.net.server.LocalServer;
import karnickeldev.solar.physics.Vector2D;
import karnickeldev.solar.render.camera.FloatingOriginCamera;

public class PlanetoidRenderSystem {

    public static int track = 3;
    private static float frustumCullingRadiusSquared = 0f;
    private final ClientECS ecs;
    private final SpriteBatch batch;
    private final FloatingOriginCamera camera;
    private final Texture testTex;
    double prevX, prevY;

    public PlanetoidRenderSystem(ClientECS ecs, SpriteBatch batch, FloatingOriginCamera camera) {
        this.ecs = ecs;
        this.batch = batch;
        this.camera = camera;

        Pixmap tmp = new Pixmap(128, 128, Pixmap.Format.RGBA8888);
        tmp.setColor(1f, 1f, 1f, 0f);
        tmp.fill();
        tmp.setColor(1f, 1f, 1f, 1f);
        tmp.fillCircle(63, 63, 63);

        testTex = new Texture(tmp);
        testTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        tmp.dispose();
    }

    private boolean isNearFrustum(double x, double y) {
        double dx = x - camera.getRenderOrigin().getX();
        double dy = y - camera.getRenderOrigin().getY();
        double distSq = dx * dx + dy * dy;
        return distSq <= frustumCullingRadiusSquared;
    }

    private void updateFrustumCullingCircle() {
        double halfW = (camera.getViewportWidth() / 2.0) / camera.getZoom();
        double halfH = (camera.getViewportWidth() / 2.0) / camera.getZoom();

        frustumCullingRadiusSquared = (float) (halfW * halfW + halfH * halfH);
    }

    public void renderPlanetoids() {
        Vector2D tmp = new Vector2D();

        // Precompute frustum bounds
        tmp.set(0, 0);
        tmp.set(camera.unproject(tmp));
        double bottom_left_x = tmp.getX();
        double bottom_left_y = tmp.getY();

        tmp.set(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        tmp.set(camera.unproject(tmp));
        double top_right_x = tmp.getX();
        double top_right_y = tmp.getY();

        updateFrustumCullingCircle();

        HCSClientSystem hcs = ecs.hcs;
        TagComponent tags = ecs.getComponentRegistry().get(TagComponent.class);
        RadiusComponent radius = ecs.getComponentRegistry().get(RadiusComponent.class);

        double alpha = hcs.getAlpha(LocalServer.TICK_RATE);

        batch.setProjectionMatrix(camera.getCombinedMatrix());

        batch.begin();
        for (int entity = 0; entity < ecs.getEntityManager().getAll(); entity++) {
            if (!ecs.getEntityManager().isValid(entity) && hcs.getCurrent().has(entity)) continue;

            if (tags.has(entity, Tags.STAR)) {
                batch.setColor(Color.ORANGE);
            } else {
                batch.setColor(Color.WHITE);
            }
            if (entity == track) {
                batch.setColor(Color.CYAN);
            }

            double localX = hcs.getInterpolatedX(entity, alpha);
            double localY = hcs.getInterpolatedY(entity, alpha);

            localX -= hcs.getInterpolatedX(track, alpha);
            localY -= hcs.getInterpolatedY(track, alpha);

            int parent = hcs.getCurrent().getParent(entity);
            localX += hcs.getInterpolatedX(parent, alpha);
            localY += hcs.getInterpolatedY(parent, alpha);

            localX -= camera.getRenderOrigin().getX();
            localY -= camera.getRenderOrigin().getY();

            // Cull if completely offscreen
            if (!isNearFrustum(localX, localY)) {
                //continue;
            } else if (localX < bottom_left_x
                || localX > top_right_x
                || localY < bottom_left_y
                || localY > top_right_y
            ) {
                //continue;
            }
            float size = (float) Math.max(16 * camera.getZoom(), radius.getRadius(entity));
            //float size = (float) (16 * camera.getZoom());

            batch.draw(testTex, (float) (localX - 0.5 * size), (float) (localY - 0.5 * size), size, size);

            prevX = localX;
            prevY = localY;
        }
        batch.end();
    }

}
