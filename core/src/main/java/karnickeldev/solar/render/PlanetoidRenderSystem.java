package karnickeldev.solar.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.ecs.Tags;
import karnickeldev.solar.physics.Vector2D;
import karnickeldev.solar.render.camera.FloatingOriginCamera;
import karnickeldev.solar.server.servers.DefaultServer;
import karnickeldev.solar.util.MathUtil;

public class PlanetoidRenderSystem {

    private final EntityManager em;
    private final SpriteBatch batch;
    private final FloatingOriginCamera camera;

    private final Texture testTex;

    public static long lastFixedUpdateTime = System.currentTimeMillis();

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
        double halfW = (camera.viewportWidth / 2.0) * camera.getZoom();
        double halfH = (camera.viewportHeight / 2.0) * camera.getZoom();

        double radius = Math.sqrt(halfW * halfW + halfH * halfH);
        frustumCullingRadiusSquared = (float) (radius * radius);
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

        float alpha = (System.currentTimeMillis() - lastFixedUpdateTime) / (1000f / DefaultServer.TICK_RATE);
        alpha = MathUtils.clamp(alpha, 0f, 1f);

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

            double localX = MathUtil.lerp(em.hcs.getOldX(entity), em.hcs.getLocalX(entity), alpha);
            double localY = MathUtil.lerp(em.hcs.getOldY(entity), em.hcs.getLocalY(entity), alpha);

            // Cull if completely offscreen
//            if (!isNearFrustum(localX, localY)) {
//                if(localX < bottom_left_x
//                    || localX > top_right_x
//                    || localY < bottom_left_y
//                    || localY > top_right_y
//                ) continue;
//            }

            float size = (float) (16 * camera.getZoom());

            batch.draw(testTex, (float) (localX - 0.5f*size), (float) (localY - 0.5f*size), size, size);

        }
        batch.end();
    }

}
