package karnickeldev.solar.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import karnickeldev.solar.assetmanager.Asset;
import karnickeldev.solar.assetmanager.AssetWrapper;
import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.ecs.ClientECS;
import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.ecs.Tag;
import karnickeldev.solar.ecs.components.*;
import karnickeldev.solar.network.net.DefaultClientNetworkListener;
import karnickeldev.solar.physics.Vector2D;
import karnickeldev.solar.render.camera.FloatingOriginCamera;
import karnickeldev.solar.worldview.orbitgraph.OrbitGraphSystem;
import karnickeldev.solar.worldview.orbitgraph.OrbitGraphData;
import karnickeldev.solar.worldview.orbitsolver.ClientOrbitSolveSystem;
import karnickeldev.solar.worldview.orbitsolver.OrbitSolveInput;
import karnickeldev.solar.util.WorldDelta;
import karnickeldev.solar.util.WorldPos;
import karnickeldev.solar.util.SplitCoordMath;
import karnickeldev.solar.util.threadlayout.ClientThreadLayout;
import karnickeldev.solar.world.ClientWorld;
import karnickeldev.solar.world.WorldManager;
import karnickeldev.solar.worldview.transform.WorldTransformData;
import karnickeldev.solar.worldview.transform.WorldTransformSystem;

public class PlanetoidRenderSystem {

    public static int track = 1;
    private static double frustumCullingRadiusSquared = 0;
    private final SpriteBatch batch;
    public static Texture testTex;

    private final WorldManager<ClientWorld> worldManager;

    public final ClientOrbitSolveSystem orbitUpdater;

    short[] anchorSx = new short[EntityManager.MAX_ENTITIES];
    double[] anchorLx = new double[EntityManager.MAX_ENTITIES];
    short[] anchorSy = new short[EntityManager.MAX_ENTITIES];
    double[] anchorLy = new double[EntityManager.MAX_ENTITIES];

    public PlanetoidRenderSystem(WorldManager<ClientWorld> worldManager, SpriteBatch batch, ClientThreadLayout threadLayout) {
        this.worldManager = worldManager;
        this.batch = batch;
        this.orbitUpdater = new ClientOrbitSolveSystem(EntityManager.MAX_ENTITIES, threadLayout);

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
        double halfW = (cam.getViewportWidth() * 0.5) * cam.getZoom();
        double halfH = (cam.getViewportHeight() * 0.5) * cam.getZoom();

        frustumCullingRadiusSquared = (halfW * halfW) + (halfH * halfH);
    }

    private final double[] worldX = new double[EntityManager.MAX_ENTITIES];
    private final double[] worldY = new double[EntityManager.MAX_ENTITIES];

    private final OrbitGraphSystem orbitNodes = new OrbitGraphSystem();

    public void renderPlanetoids() {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        updateFrustumCullingCircle();

        ClientECS ecs = worldManager.getActiveWorld().getECS();
        TagComponent tags = ecs.getComponentRegistry().get(TagComponent.class);
        RadiusComponent radius = ecs.getComponentRegistry().get(RadiusComponent.class);
        RenderComponent renderComponent = ecs.getComponentRegistry().get(RenderComponent.class);
        OrbitDataComponent orbitDataComponent = ecs.getComponentRegistry().get(OrbitDataComponent.class);

        FloatingOriginCamera camera = worldManager.getActiveWorld().getCamera();
        double renderZoom = camera.getZoom();

        if(orbitNodes.getOrbitGraph().getAnchorCount() == 0) {
            orbitNodes.rebuild(ecs);

            int anchorCount = orbitNodes.getOrbitGraph().getAnchorCount();
            OrbitGraphData frame = orbitNodes.getOrbitGraph();
            for(int i = 0; i < anchorCount; i++) {
                int a = frame.getDfsOrder()[i];
                if(frame.getParent()[a] == -1) {
                    System.out.println("anchor: " + a + ", e: " + frame.getAnchorToEntity()[a] + ", p: -1");
                } else {
                    System.out.println("anchor: " + a + ", e: " + frame.getAnchorToEntity()[a] + ", p: " + frame.getAnchorToEntity()[frame.getParent()[a]]);
                }
            }
        }

        OrbitGraphData orbitGraph = orbitNodes.getOrbitGraph();

        Vector2D ePos = new Vector2D();
        Vector2D screenPos = new Vector2D();

        batch.setProjectionMatrix(camera.getCombinedMatrix());
        //batch.begin();

        //update(GameContext.get().getClock().getFrameClockTime(), ecs);
        //hcs.swapBuffers();

        orbitUpdater.beginNextFrame(new OrbitSolveInput(
            orbitGraph.getAnchorCount(),
            GameContext.get().getClock().getFrameClockTime(),
            orbitDataComponent,
            ecs.getComponentRegistry().get(MassComponent.class),
            orbitGraph
        ));

        Vector2D camOrigin = camera.getRenderOrigin();
        WorldPos camCoord = camera.getOrigin();

        Color drawColor = Color.WHITE;
        Color lastColor = batch.getColor();

        Texture texture = null;
        short textureID = 0;

        float maxZoom = (float) renderZoom * 16;

        WorldPos worldPosReuse = new WorldPos();
        WorldDelta deltaReuse = new WorldDelta();

        // resolve absolute world pos
        WorldTransformData worldTransform = worldManager.getActiveWorld().getWorldTransform();
        WorldTransformSystem.transformToGlobalPos(orbitGraph, orbitUpdater.getCurrentFrame(), worldTransform);

        int trackAnchor = orbitGraph.getEntityToAnchor()[track];
        WorldPos trackPos = new WorldPos();
        trackPos.set(anchorSx[trackAnchor], anchorLx[trackAnchor], anchorSy[trackAnchor], anchorLy[trackAnchor]);
        SplitCoordMath.addInPlace(trackPos, camCoord.sx, camCoord.lx, camCoord.sy, camCoord.ly);

        for (int i = 0; i < orbitGraph.getAnchorCount(); i++) {
            int anchor = orbitGraph.getDfsOrder()[i];
            int entity = orbitGraph.getAnchorToEntity()[anchor];
            if (!ecs.getEntityManager().isValid(entity) || !orbitDataComponent.has(entity) || !renderComponent.has(entity)) {
                worldX[entity] = Double.MAX_VALUE;
                worldY[entity] = Double.MAX_VALUE;
                continue;
            }

            ePos.zero();

            //ecs.toWorldSpace(ePos, entity, alpha);
            //ePos.add(orbitUpdater.getFrameData().posX[entity], orbitUpdater.getFrameData().posY[entity]);

            //ePos.subtract(trackPos);

            worldPosReuse.setFromArray(worldTransform.getAnchorSX(), worldTransform.getAnchorLX(), worldTransform.getAnchorSY(), worldTransform.getAnchorLY(), anchor);

            WorldDelta.delta(deltaReuse, worldPosReuse, trackPos);

            ePos.set(deltaReuse.toDoubleX(), deltaReuse.toDoubleY());

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

        for (int i = 0; i < orbitGraph.getAnchorCount(); i++) {
            int anchor = orbitGraph.getDfsOrder()[i];
            int entity = orbitGraph.getAnchorToEntity()[anchor];

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

        float tsize = (float) (16 * camera.getZoom());
        batch.setColor(Color.GREEN);
        for(Double[] camPos: DefaultClientNetworkListener.clientCamPos.values()) {
            Vector2D remotePos = new Vector2D(camPos[0], camPos[1]);

            Vector2D pos = remotePos.scale(1).subtract(camera.getRenderOrigin());

            batch.draw(testTex, (float)pos.getX() - 0.5f*tsize, (float)pos.getY() - 0.5f*tsize, tsize, tsize);
        }

        orbitUpdater.finishFrame();

        batch.setColor(1,1,1,1);
        //batch.end();
    }

    public void shutdown() {
        orbitUpdater.shutdown();
    }

}
