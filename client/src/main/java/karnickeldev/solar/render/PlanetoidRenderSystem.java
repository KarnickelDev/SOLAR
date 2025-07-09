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
import karnickeldev.solar.ecs.systems.HCSClientSystem;
import karnickeldev.solar.network.net.DefaultClientNetworkListener;
import karnickeldev.solar.physics.Vector2D;
import karnickeldev.solar.render.camera.FloatingOriginCamera;
import karnickeldev.solar.world.ClientWorld;
import karnickeldev.solar.world.WorldManager;

public class PlanetoidRenderSystem {

    public static int track = 3;
    private static float frustumCullingRadiusSquared = 0f;
    private final SpriteBatch batch;
    public static Texture testTex;

    private final WorldManager<ClientWorld> worldManager;

    public PlanetoidRenderSystem(WorldManager<ClientWorld> worldManager, SpriteBatch batch) {
        this.worldManager = worldManager;
        this.batch = batch;

        int size = 256;
        Pixmap tmp = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        tmp.setColor(1f, 1f, 1f, 0f);
        tmp.fill();
        tmp.setColor(1f, 1f, 1f, 1f);
        tmp.fillCircle(size/2, size/2, size/2);

        testTex = new Texture(tmp);
        testTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        tmp.dispose();
    }

    private boolean isNearFrustum(double x, double y, double size) {
        double distSq = x * x + y * y;
        return distSq <= frustumCullingRadiusSquared + (size * size);
    }

    private void updateFrustumCullingCircle() {
        double halfW = (worldManager.getActiveWorld().getCamera().getViewportWidth() / 2.0) * worldManager.getActiveWorld().getCamera().getRenderZoom();
        double halfH = (worldManager.getActiveWorld().getCamera().getViewportWidth() / 2.0) * worldManager.getActiveWorld().getCamera().getRenderZoom();

        frustumCullingRadiusSquared = (float) (halfW * halfW + halfH * halfH);
    }

    public void renderPlanetoids() {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        updateFrustumCullingCircle();

        ClientECS ecs = worldManager.getActiveWorld().getECS();
        HCSClientSystem hcs = ecs.hcs;
        TagComponent tags = ecs.getComponentRegistry().get(TagComponent.class);
        RadiusComponent radius = ecs.getComponentRegistry().get(RadiusComponent.class);

        FloatingOriginCamera camera = worldManager.getActiveWorld().getCamera();

        double alpha = hcs.getAlpha();

        Vector2D reuseVec0 = new Vector2D();
        Vector2D reuseVec1 = new Vector2D();

        batch.setProjectionMatrix(camera.getCombinedMatrix());
        //batch.begin();

        for (int entity = 0; entity < ecs.getEntityManager().getAll(); entity++) {
            if (!ecs.getEntityManager().isValid(entity) || !hcs.getCurrent().has(entity)) continue;

            if (tags.has(entity, Tags.STAR)) {
                batch.setColor(Color.ORANGE);
            } else {
                batch.setColor(Color.WHITE);
            }
            if (entity == track) {
                batch.setColor(Color.CYAN);
            }

            reuseVec0.zero();
            reuseVec1.zero();

            Vector2D ePos = ecs.toWorldSpace(reuseVec0, entity, alpha);
            ePos.subtract(ecs.toWorldSpace(reuseVec1, track, alpha));

            Vector2D screenPos = camera.project(ePos);
            double screenX = screenPos.getX();
            double screenY = screenPos.getY();

            ePos.subtract(camera.getRenderOrigin());

            double localX = ePos.getX();
            double localY = ePos.getY();

            float size = (float) Math.max(16 * camera.getRenderZoom(), radius.getRadius(entity));
            float sizePixels = (float) (size / camera.getRenderZoom());

            // Cull if completely offscreen
            if (!isNearFrustum(localX, localY, size)) {
                continue;
            } else if (screenX < -sizePixels || screenX > width + sizePixels || screenY < -sizePixels || screenY > height + sizePixels) {
                continue;
            }


            batch.draw(testTex, (float) (localX - 0.5 * size), (float) (localY - 0.5 * size), size, size);
        }
        float tsize = (float) (16 * camera.getRenderZoom());
        batch.setColor(Color.GREEN);
        for(Double[] camPos: DefaultClientNetworkListener.clientCamPos.values()) {
            Vector2D remotePos = new Vector2D(camPos[0], camPos[1]);

            Vector2D pos = remotePos.scale(1).subtract(camera.getRenderOrigin());

            batch.draw(testTex, (float)pos.getX() - 0.5f*tsize, (float)pos.getY() - 0.5f*tsize, tsize, tsize);
        }
        //batch.end();
    }

}
