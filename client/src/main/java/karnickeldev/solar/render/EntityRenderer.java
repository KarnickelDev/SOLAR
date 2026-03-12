package karnickeldev.solar.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import karnickeldev.solar.assetmanager.Asset;
import karnickeldev.solar.assetmanager.AssetWrapper;
import karnickeldev.solar.core.SimTestScreen;
import karnickeldev.solar.ecs.ClientECS;
import karnickeldev.solar.ecs.Tag;
import karnickeldev.solar.ecs.components.*;
import karnickeldev.solar.render.camera.FloatingOriginCamera;
import karnickeldev.solar.render.core.RenderPass;
import karnickeldev.solar.render.core.RendererContext;
import karnickeldev.solar.util.SplitCoordMath;
import karnickeldev.solar.util.WorldDelta;
import karnickeldev.solar.util.WorldPos;
import karnickeldev.solar.world.ClientWorld;
import karnickeldev.solar.world.WorldManager;
import karnickeldev.solar.worldview.orbitgraph.OrbitGraphData;
import karnickeldev.solar.worldview.transform.WorldTransformData;

/**
 * @author KarnickelDev
 * @since 04.03.2026
 **/
public class EntityRenderer implements RenderPass {

    public static int TRACK = 1;

    private final WorldManager<ClientWorld> worldManager;

    private double frustumCullingRadiusSquared = 0;

    public EntityRenderer(WorldManager<ClientWorld> worldManager) {
        this.worldManager = worldManager;
    }

    private boolean isNearFrustum(double x, double y, double size) {
        double distSq = x * x + y * y;
        return distSq <= frustumCullingRadiusSquared + (size * size);
    }

    private void updateFrustumCullingCircle(FloatingOriginCamera cam) {
        double halfW = (cam.getViewportWidth() * 0.5) * cam.getZoom();
        double halfH = (cam.getViewportHeight() * 0.5) * cam.getZoom();

        frustumCullingRadiusSquared = (halfW * halfW) + (halfH * halfH);
    }

    @Override
    public void render(RendererContext ctx, float delta) {
        ClientECS ecs = worldManager.getActiveWorld().getECS();
        TagComponent tags = ecs.getComponentRegistry().get(TagComponent.class);
        RadiusComponent radius = ecs.getComponentRegistry().get(RadiusComponent.class);
        RenderComponent renderComponent = ecs.getComponentRegistry().get(RenderComponent.class);

        OrbitGraphData orbitGraph = worldManager.getActiveWorld().getOrbitGraphSystem().getOrbitGraph();
        WorldTransformData worldTransform = worldManager.getActiveWorld().getWorldTransform();

        FloatingOriginCamera camera = worldManager.getActiveWorld().getCamera();
        double renderZoom = camera.getZoom();

        // UPDATE frustum for render culling
        updateFrustumCullingCircle(camera);

        ctx.batch().setProjectionMatrix(camera.getCombinedMatrix());
        ctx.batch().begin();

        Color drawColor = Color.WHITE;
        Color lastColor = ctx.batch().getColor();

        Texture texture = null;
        short textureID = 0;

        WorldPos camCoord = camera.getOrigin();

        WorldPos worldPosReuse = new WorldPos();
        WorldDelta deltaReuse = new WorldDelta();

        int trackAnchor = orbitGraph.getEntityToAnchor()[TRACK];
        WorldPos trackPos = new WorldPos();
        trackPos.setFromArray(worldTransform.getAnchorSX(), worldTransform.getAnchorLX(), worldTransform.getAnchorSY(), worldTransform.getAnchorLY(), trackAnchor);
        SplitCoordMath.addInPlace(trackPos, camCoord.sx, camCoord.lx, camCoord.sy, camCoord.ly);

        for (int i = 0; i < orbitGraph.getAnchorCount(); i++) {
            int anchor = orbitGraph.getDfsOrder()[i];
            int entity = orbitGraph.getAnchorToEntity()[anchor];

            if(!renderComponent.has(entity)) continue;

            // transform to render position (worldPos-camera delta)
            worldPosReuse.setFromArray(worldTransform.getAnchorSX(), worldTransform.getAnchorLX(), worldTransform.getAnchorSY(), worldTransform.getAnchorLY(), anchor);
            WorldDelta.delta(deltaReuse, worldPosReuse, trackPos);

            double renderX = deltaReuse.toDoubleX();
            double renderY = deltaReuse.toDoubleY();

            float size = (float) Math.max(16 * renderZoom, radius.getRadius(entity));

            // RENDER CULLING
            if(!isNearFrustum(renderX, renderY, size)) {
                continue;
            }

            // avoid color changes to not flush GPU unnecessarily
            if (tags.has(entity, Tag.PLANET)) {
                drawColor = Color.WHITE;
            } else if(tags.has(entity, Tag.STAR)) {
                //drawColor = Color.ORANGE;
                ctx.batch().end();
                SimTestScreen.starRenderer.renderStar(renderX, renderY, size*0.5f);
                ctx.batch().begin();
                continue;
            } else {
                drawColor = Color.GRAY;
            }
            if (entity == TRACK) {
                drawColor = Color.CYAN;
            }

            if(lastColor != drawColor) {
                ctx.batch().setColor(drawColor);
                lastColor = drawColor;
            }

            short tmp = renderComponent.getEntityType(entity);
            if(tmp != textureID) {
                textureID = tmp;
                texture = AssetWrapper.getInstance().getAsset(Asset.values()[renderComponent.getEntityType(entity)]);
            }

            ctx.batch().draw(texture, (float) (renderX - 0.5 * size), (float) (renderY - 0.5 * size), size, size);
        }

        ctx.batch().setColor(1,1,1,1);
        ctx.batch().end();
    }


}
