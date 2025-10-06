package karnickeldev.solar.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import karnickeldev.solar.assetmanager.Asset;
import karnickeldev.solar.assetmanager.AssetWrapper;
import karnickeldev.solar.ecs.ClientECS;
import karnickeldev.solar.ecs.Tag;
import karnickeldev.solar.ecs.components.RadiusComponent;
import karnickeldev.solar.ecs.components.RenderComponent;
import karnickeldev.solar.ecs.components.TagComponent;
import karnickeldev.solar.ecs.systems.HCSClientSystem;
import karnickeldev.solar.network.net.DefaultClientNetworkListener;
import karnickeldev.solar.physics.Vector2D;
import karnickeldev.solar.render.camera.FloatingOriginCamera;
import karnickeldev.solar.world.ClientWorld;
import karnickeldev.solar.world.WorldManager;

public class PlanetoidRenderSystem {

    public static int track = 0;
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

    double[] worldX = new double[100010];
    double[] worldY = new double[100010];

    public void renderPlanetoids() {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        updateFrustumCullingCircle();

        ClientECS ecs = worldManager.getActiveWorld().getECS();
        HCSClientSystem hcs = ecs.hcs;
        TagComponent tags = ecs.getComponentRegistry().get(TagComponent.class);
        RadiusComponent radius = ecs.getComponentRegistry().get(RadiusComponent.class);
        RenderComponent renderComponent = ecs.getComponentRegistry().get(RenderComponent.class);

        FloatingOriginCamera camera = worldManager.getActiveWorld().getCamera();
        double renderZoom = camera.getRenderZoom();

        double alpha = hcs.getAlpha();

        Vector2D ePos = new Vector2D();
        Vector2D screenPos = new Vector2D();

        batch.setProjectionMatrix(camera.getCombinedMatrix());
        //batch.begin();

        Vector2D camOrigin = camera.getRenderOrigin();

        Vector2D trackPos = ecs.toWorldSpace(track, alpha).add(camOrigin);

        Color drawColor = Color.WHITE;
        Color lastColor = batch.getColor();

        Texture texture = null;
        short textureID = 0;

        float maxZoom = (float) renderZoom * 16;

        double x0, x1, y0, y1;
        x0 = camera.unprojectReuse(new Vector2D(0,0)).getX();
        y0 = camera.unprojectReuse(new Vector2D(0,0)).getY();
        x1 = camera.unprojectReuse(new Vector2D(Gdx.graphics.getWidth(),Gdx.graphics.getHeight())).getX();
        y1 = camera.unprojectReuse(new Vector2D(Gdx.graphics.getWidth(),Gdx.graphics.getHeight())).getY();

        for (int entity = 0; entity < ecs.getEntityManager().getAll(); entity++) {
            if (!ecs.getEntityManager().isValid(entity) || !hcs.getCurrent().has(entity) || !renderComponent.has(entity)) {
                worldX[entity] = Double.MAX_VALUE;
                worldY[entity] = Double.MAX_VALUE;
                continue;
            }

            ePos.zero();

            ecs.toWorldSpace(ePos, entity, alpha);

            ePos.subtract(trackPos);

            double localX = ePos.getX();
            double localY = ePos.getY();

            double size = Math.max(maxZoom, radius.getRadius(entity));

            // fast reject culling with sphere check
            if (!isNearFrustum(localX, localY, size)) {
                worldX[entity] = Double.MAX_VALUE;
                worldY[entity] = Double.MAX_VALUE;
                continue;
            }

            screenPos.set(ePos).add(camOrigin);
            screenPos = camera.projectReuse(screenPos);
            double screenX = screenPos.getX();
            double screenY = screenPos.getY();

            double sizePixels = size / renderZoom;
            if (screenX < -sizePixels || screenX > width + sizePixels || screenY < -sizePixels || screenY > height + sizePixels) {
                worldX[entity] = Double.MAX_VALUE;
                worldY[entity] = Double.MAX_VALUE;
                continue;
            }

            worldX[entity] = localX;
            worldY[entity] = localY;
        }

        for (int entity = 0; entity < ecs.getEntityManager().getAll(); entity++) {

            double localX = worldX[entity];
            double localY = worldY[entity];

            if(localX == Double.MAX_VALUE || localY == Double.MAX_VALUE) continue;

            double size = Math.max(maxZoom, radius.getRadius(entity));

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

            short tmp = renderComponent.getEntityType(entity);
            if(tmp != textureID) {
                textureID = tmp;
                texture = AssetWrapper.getInstance().getAsset(Asset.values()[renderComponent.getEntityType(entity)]);
            }

            batch.draw(texture, (float) (localX - 0.5 * size), (float) (localY - 0.5 * size), (float)size, (float)size);
        }

        float tsize = (float) (16 * camera.getRenderZoom());
        batch.setColor(Color.GREEN);
        for(Double[] camPos: DefaultClientNetworkListener.clientCamPos.values()) {
            Vector2D remotePos = new Vector2D(camPos[0], camPos[1]);

            Vector2D pos = remotePos.scale(1).subtract(camera.getRenderOrigin());

            batch.draw(testTex, (float)pos.getX() - 0.5f*tsize, (float)pos.getY() - 0.5f*tsize, tsize, tsize);
        }
        batch.setColor(1,1,1,1);
        //batch.end();
    }

}
