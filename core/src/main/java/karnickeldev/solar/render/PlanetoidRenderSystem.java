package karnickeldev.solar.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.ecs.Tags;
import karnickeldev.solar.ecs.components.HCSServerComponent;
import karnickeldev.solar.physics.Vector2D;
import karnickeldev.solar.render.camera.FloatingOriginCamera;
import karnickeldev.solar.server.servers.DefaultServer;
import karnickeldev.solar.util.MathUtil;

import java.util.concurrent.atomic.AtomicLong;

public class PlanetoidRenderSystem {

    private final EntityManager em;
    private final SpriteBatch batch;
    private final FloatingOriginCamera camera;

    private final Texture testTex;

    public static AtomicLong lastFixedUpdateTime = new AtomicLong(System.nanoTime());

    public static int track = 0;

    private static float frustumCullingRadiusSquared = 0f;

    public PlanetoidRenderSystem(EntityManager entityManager, SpriteBatch batch, FloatingOriginCamera camera) {
        this.em = entityManager;
        this.batch = batch;
        this.camera = camera;


        Pixmap tmp = new Pixmap(128,128, Pixmap.Format.RGBA8888);
        tmp.setColor(1f,1f,1f,0f);
        tmp.fill();
        tmp.setColor(1f,1f,1f,1f);
        tmp.fillCircle(63,63,63);

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
        tmp.set(0,0);
        tmp.set(camera.unproject(tmp));
        double bottom_left_x = tmp.getX();
        double bottom_left_y = tmp.getY();

        tmp.set(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        tmp.set(camera.unproject(tmp));
        double top_right_x = tmp.getX();
        double top_right_y = tmp.getY();

        updateFrustumCullingCircle();

        double alpha = (System.nanoTime() - lastFixedUpdateTime.get()) / (1_000_000_000d / DefaultServer.TICK_RATE);

        HCSServerComponent.RenderBuffer buffer = em.hcs.getLocals();

        batch.setProjectionMatrix(camera.getCombinedMatrix());


        batch.begin();
        for(int entity = 0; entity < em.getAll(); entity++) {
            if(!em.isValid(entity)) continue;

            if(em.tags.has(entity, Tags.STAR)) {
                batch.setColor(Color.ORANGE);
            } else {
                batch.setColor(Color.WHITE);
            }
            if(entity == track) {
                batch.setColor(Color.CYAN);
            }
            if(em.names.getName(entity).equalsIgnoreCase("moon")) {
                batch.setColor(Color.FOREST);
            }


            double localX = MathUtil.lerp(buffer.prev[2*entity], buffer.curr[2*entity], alpha);
            double localY = MathUtil.lerp(buffer.prev[2*entity + 1], buffer.curr[2*entity + 1], alpha);

            localX -= MathUtil.lerp(buffer.prev[2*track], buffer.curr[2*track], alpha);
            localY -= MathUtil.lerp(buffer.prev[2*track + 1], buffer.curr[2*track + 1], alpha);

            int parent = em.hcs.getParent(entity);
            localX += MathUtil.lerp(buffer.prev[2*parent], buffer.curr[2*parent], alpha);
            localY += MathUtil.lerp(buffer.prev[2*parent + 1], buffer.curr[2*parent + 1], alpha);

            // Cull if completely offscreen
            if (!isNearFrustum(localX, localY)) {
                if(localX < bottom_left_x
                    || localX > top_right_x
                    || localY < bottom_left_y
                    || localY > top_right_y
                ) {
                    //continue;
                }
            }
            float size = (float) Math.max(16 * camera.getZoom(), em.radius.getRadius(entity));
            //float size = (float) (16 * camera.getZoom());

            batch.draw(testTex, (float) (localX - 0.5*size), (float) (localY - 0.5*size), size, size);

        }
        batch.end();
    }

}
