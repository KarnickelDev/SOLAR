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
import karnickeldev.solar.util.MathUtil;

public class PlanetoidRenderSystem {

    private final EntityManager em;
    private final SpriteBatch batch;
    private final FloatingOriginCamera camera;

    private final Texture testTex;

    public static long lastFixedUpdateTime = System.currentTimeMillis();

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


    public void renderPlanetoids() {

        // Precompute view bounds in world units
        float halfW = camera.getCamera().viewportWidth * 0.5f * camera.getZoom();
        float halfH = camera.getCamera().viewportHeight * 0.5f * camera.getZoom();
        float camX = (float) camera.getOrigin().getX();
        float camY = (float) camera.getOrigin().getY();
        float left   = camX - halfW;
        float right  = camX + halfW;
        float bottom = camY - halfH;
        float top    = camY + halfH;

        Matrix4 pixelProjection = new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setProjectionMatrix(pixelProjection);

        batch.begin();
        for(int entity = 0; entity < em.getAll(); entity++) {
            if(!em.isValid(entity)) continue;

            float alpha = (System.currentTimeMillis() - lastFixedUpdateTime) / (1e-3f / 128);
            alpha = MathUtils.clamp(alpha, 0f, 1f);

            int size = 16;
            if(em.tags.has(entity, Tags.STAR)) {
                batch.setColor(Color.ORANGE);
            } else {
                batch.setColor(Color.WHITE);
            }

            double localX = MathUtil.lerp(em.hcs.getOldX(entity), em.hcs.getLocalX(entity), alpha);
            double localY = MathUtil.lerp(em.hcs.getOldY(entity), em.hcs.getLocalY(entity), alpha);

            // Cull if completely offscreen
            if (localX < left  - 1 ||
                localX > right + 1 ||
                localY < bottom- 1 ||
                localY > top   + 1) {
                continue;
            }

            Vector2D renderPos = camera.worldToRender(new Vector2D(localX, localY));
            renderPos.scale(1d / camera.getZoom());

            float screenX = (float)(0.5f * Gdx.graphics.getWidth() + renderPos.getX());
            float screenY = (float)(0.5f * Gdx.graphics.getHeight() + renderPos.getY());

            batch.draw(testTex, screenX - 0.5f*size, screenY - 0.5f*size, size, size);
        }
        batch.end();
    }

}
