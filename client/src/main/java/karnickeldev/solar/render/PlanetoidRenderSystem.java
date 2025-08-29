package karnickeldev.solar.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import karnickeldev.solar.ecs.ClientECS;
import karnickeldev.solar.ecs.Tag;
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
    private static double frustumCullingRadiusSquared = 0;
    private final SpriteBatch batch;
    public static Texture testTex;

    private final WorldManager<ClientWorld> worldManager;

    public PlanetoidRenderSystem(WorldManager<ClientWorld> worldManager, SpriteBatch batch) {
        this.worldManager = worldManager;
        this.batch = batch;

        int size = 256;
        int r = (size / 2) - 1;
        Pixmap tmp = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        tmp.setColor(1f, 1f, 1f, 0f);
        tmp.fill();
        tmp.setColor(1f, 1f, 1f, 1f);
        tmp.fillCircle(r, r, r);
        tmp.fillCircle(r, r+1, r);
        tmp.fillCircle(r+1, r, r);
        tmp.fillCircle(r+1, r+1, r);

        testTex = new Texture(tmp);
        testTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        tmp.dispose();
    }

    private boolean isNearFrustum(double x, double y, double size) {
        double distSq = x * x + y * y;
        return distSq <= frustumCullingRadiusSquared + (size * size);
    }

    private void updateFrustumCullingCircle() {
        FloatingOriginCamera cam = worldManager.getActiveWorld().getCamera();
        double halfW = (cam.getViewportWidth() / 2.0) * cam.getRenderZoom();
        double halfH = (cam.getViewportHeight() / 2.0) * cam.getRenderZoom();

        frustumCullingRadiusSquared = (halfW * halfW) + (halfH * halfH);
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
        double renderZoom = camera.getRenderZoom();

        double alpha = hcs.getAlpha();

        Vector2D reuseVec0 = new Vector2D();
        Vector2D reuseVec1 = new Vector2D();

        batch.setProjectionMatrix(camera.getCombinedMatrix());
        //batch.begin();

        Vector2D camOrigin = camera.getRenderOrigin();

        Vector2D trackPos = ecs.toWorldSpace(track, alpha).add(camOrigin);

        Color drawColor = Color.WHITE;
        Color lastColor = batch.getColor();

        for (int entity = 0; entity < ecs.getEntityManager().getAll(); entity++) {
            if (!ecs.getEntityManager().isValid(entity) || !hcs.getCurrent().has(entity)) continue;

            reuseVec0.zero();

            Vector2D ePos = reuseVec0;

            ecs.toWorldSpace(ePos, entity, alpha);

            ePos.subtract(trackPos);

            double localX = ePos.getX();
            double localY = ePos.getY();

            double size = Math.max(16 * renderZoom, radius.getRadius(entity));

            // fast reject culling with sphere check
            if (!isNearFrustum(localX, localY, size)) {
                continue;
            }

            reuseVec1.set(ePos).add(camOrigin);
            Vector2D screenPos = camera.projectReuse(reuseVec1);
            double screenX = screenPos.getX();
            double screenY = screenPos.getY();

            double sizePixels = size / renderZoom;
            if (screenX < -sizePixels || screenX > width + sizePixels || screenY < -sizePixels || screenY > height + sizePixels) {
                continue;
            }

            // avoid color changes to not flush GPU unnecessarily
            if (tags.has(entity, Tag.PLANET)) {
                drawColor = Color.WHITE;
            } else if(tags.has(entity, Tag.STAR)) {
                drawColor = Color.ORANGE;
            } else {
                drawColor = Color.GRAY;
            }
            if (entity == track) {
                drawColor = Color.CYAN;
            }

            if(lastColor != drawColor) {
                batch.setColor(drawColor);
                lastColor = drawColor;
            }

            batch.draw(testTex, (float) (localX - 0.5 * size), (float) (localY - 0.5 * size), (float)size, (float)size);
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
