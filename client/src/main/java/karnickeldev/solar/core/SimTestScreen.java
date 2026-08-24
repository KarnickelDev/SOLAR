package karnickeldev.solar.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.context.GameContextContainer;
import karnickeldev.solar.ecs.components.MassComponent;
import karnickeldev.solar.ecs.components.OrbitDataComponent;
import karnickeldev.solar.input.*;
import karnickeldev.solar.logging.ChatLogAppender;
import karnickeldev.solar.logging.LogManager;
import karnickeldev.solar.network.packets.PacketFactory;
import karnickeldev.solar.network.packets.TestCamPacket;
import karnickeldev.solar.render.EntityRenderer;
import karnickeldev.solar.render.StarField;
import karnickeldev.solar.render.background.BackgroundGridRenderer;
import karnickeldev.solar.render.background.RingRenderer;
import karnickeldev.solar.render.core.RenderPipeline;
import karnickeldev.solar.render.core.RendererContext;
import karnickeldev.solar.render.core.UIRenderer;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.components.UILayoutEngine;
import karnickeldev.solar.ui.fontutil.*;
import karnickeldev.solar.ui.fontutil.kernel.*;
import karnickeldev.solar.ui.layers.hud.HudLayer;
import karnickeldev.solar.world.ClientWorld;
import karnickeldev.solar.world.WorldManager;
import karnickeldev.solar.worldview.orbitgraph.OrbitGraphData;
import karnickeldev.solar.worldview.orbitgraph.OrbitGraphSystem;
import karnickeldev.solar.worldview.orbitsolver.OrbitSolveInput;
import karnickeldev.solar.worldview.orbitsolver.OrbitSolver;
import karnickeldev.solar.worldview.transform.WorldTransformData;
import karnickeldev.solar.worldview.transform.WorldTransformSystem;
import org.lwjgl.opengl.GL20;

public class SimTestScreen implements Screen {

    private final Viewport backgroundViewport;
    private final Viewport screenViewport;

    private final RenderPipeline renderPipeline;
    private final RendererContext renderCtx;

    ShaderProgram shaderProgram;
    public static MSDFBatch msdfBatch;
    TextBlock textCache;
    public static MSDFFont font;

    private final InputRouter inputRouter;

    public SimTestScreen() {
        backgroundViewport = new ExtendViewport(UI.VIRTUAL_WIDTH, UI.VIRTUAL_HEIGHT);
        screenViewport = new ScreenViewport();

        inputRouter = new InputRouter();

        inputRouter.setUiInputManager(UI.getUIManager());
        inputRouter.setGameplayInputManager(new GameplayInputManager());

        shaderProgram = Engine.shaderManager().get("msdf");
        msdfBatch = new MSDFBatch(1024, shaderProgram);

        renderCtx = new RendererContext(SolarMain.getInstance().getBatch(), new UIRenderer(new SpriteBatch(), msdfBatch), new ShapeRenderer());
        renderPipeline = new RenderPipeline();

        renderPipeline.add(new BackgroundGridRenderer());
        renderPipeline.add(new RingRenderer());

        renderPipeline.add(new EntityRenderer(GameContext.get().getWorldManager()));
    }

    public static SimpleStarRenderer starRenderer;

    @Override
    public void show() {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        HudLayer.INSTANCE = new HudLayer();
        UI.getUIManager().push(HudLayer.INSTANCE);

        LogManager.addAppender(new ChatLogAppender());

        Gdx.input.setInputProcessor(null);

        starRenderer = new SimpleStarRenderer(SolarMain.getInstance().getBatch());

        textCache = new TextBlock(new RichTextBuilder().scale(18).text("Test: 123456\nHallo Welt! \udb80\udc18").build());

        Engine.input().clearListeners();
        Engine.input().addListener(inputRouter);
    }

    private void test(float uiScale) {
        //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        msdfBatch.getProjectionMatrix().setToOrtho2D(0,0, Gdx.graphics.getWidth(),Gdx.graphics.getHeight());

        textCache.setUiScale(uiScale);

        msdfBatch.begin();
        msdfBatch.draw(font, textCache.layout(font),  0, 0);
        msdfBatch.draw(font, textCache.layout(font),  600, 200);
        msdfBatch.end();
    }

    double tmp = 0;
    TestCamPacket camPacket = new TestCamPacket();
    @Override
    public void render(float delta) {
        ScreenUtils.clear(0,0,0,1,true);

        GameContextContainer gameContext = GameContext.get();

        WorldManager<ClientWorld> clientWorldManager = gameContext.getWorldManager();

        tmp += delta;
        camPacket.x = clientWorldManager.getActiveWorld().getCamera().getRenderOrigin().getX();
        camPacket.y = clientWorldManager.getActiveWorld().getCamera().getRenderOrigin().getY();
        if(tmp > 0.3) {
            tmp = 0;
            if(gameContext.isMultiplayer()) {
                gameContext.getClientNetwork().send(PacketFactory.createPingPacket(System.nanoTime()));
            }
            gameContext.getClientNetwork().send(camPacket);
        }

        Engine.input().update();

        // camera
        clientWorldManager.getActiveWorld().getCamera().update(delta);

        gameContext.getClock().updateFrameClockTime();

        gameContext.getSyncLayer().update(gameContext.getClock().getFrameClockTime());

        // probably better to do after processing input
        gameContext.getScheduler().main().update();
        gameContext.getScheduler().timer().update(System.currentTimeMillis());

        ClientWorld activeWorld = clientWorldManager.getActiveWorld();

        // update orbitgraph and solve orbits
        OrbitGraphSystem orbitNodes = activeWorld.getOrbitGraphSystem();
        orbitNodes.rebuildIfNecessary(activeWorld.getECS());

        OrbitGraphData orbitGraph = orbitNodes.getOrbitGraph();
        OrbitSolver orbitSolver = GameContext.get().getOrbitSolver();

        OrbitSolveInput orbitSolveInput = new OrbitSolveInput(
            GameContext.get().getClock().getFrameClockTime(),
            orbitGraph,
            activeWorld.getECS().getComponentRegistry().get(OrbitDataComponent.class),
            activeWorld.getECS().getComponentRegistry().get(MassComponent.class)
        );

        // THIS IS VERY IMPORTANT
        // without this we have stale references to anchors in OrbitSolver!!!
        if(orbitNodes.wasRebuildThisFrame()) {
            GameContext.get().getOrbitSolver().onOrbitGraphRebuild(orbitSolveInput);
        }
        orbitSolver.beginNextFrame(orbitSolveInput);

        // resolve absolute world pos
        WorldTransformData worldTransform = activeWorld.getWorldTransform();
        WorldTransformSystem.transformToGlobalPos(orbitGraph, orbitSolver.getCurrentFrame(), worldTransform);

        // rendering
        backgroundViewport.apply();
        SolarMain.getInstance().getBatch().setColor(1,1,1,1);
        SolarMain.getInstance().getBatch().setProjectionMatrix(backgroundViewport.getCamera().combined);
        SolarMain.getInstance().getBatch().begin();
        SolarMain.getInstance().getBatch().draw(StarField.starFieldBuffer.getColorBufferTexture(),0,0);
        SolarMain.getInstance().getBatch().end();

        screenViewport.apply();

        renderPipeline.render(renderCtx, delta);

        renderCtx.batch().setColor(1,1,1,1);
        renderCtx.debug().begin(ShapeRenderer.ShapeType.Line);

        UILayoutEngine.UILayoutContext uiContext = UILayoutEngine.computeLayoutContext(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        renderCtx.uiRenderer().updateViewport(0,0, uiContext.screenWidth(), uiContext.screenHeight());
        renderCtx.uiRenderer().begin();

        UI.getUIManager().update(uiContext, delta);
        UI.getUIManager().render(renderCtx);

        renderCtx.uiRenderer().end();
        renderCtx.debug().end();

        // finish next orbitsolve frame
        GameContext.get().getOrbitSolver().finishFrame();
        test(uiContext.uiScaleY());
    }

    @Override
    public void resize(int width, int height) {
        backgroundViewport.update(width, height, true);
        screenViewport.update(width, height);

        UI.getUIManager().resize(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        UI.getUIManager().clear();

        Engine.input().clearListeners();
    }

    @Override
    public void dispose() {

    }
}
