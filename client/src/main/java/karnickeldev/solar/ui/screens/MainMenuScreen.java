package karnickeldev.solar.ui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import karnickeldev.solar.assetmanager.AssetWrapper;
import karnickeldev.solar.core.Engine;
import karnickeldev.solar.core.SimTestScreen;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.core.gamestates.*;
import karnickeldev.solar.render.StarField;
import karnickeldev.solar.render.core.RendererContext;
import karnickeldev.solar.render.core.UIRenderer;
import karnickeldev.solar.ui.components.UILayoutEngine;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.fontutil.kernel.JSONLoader;
import karnickeldev.solar.ui.fontutil.kernel.MSDFBatch;
import karnickeldev.solar.ui.layers.mainmenu.MainMenuLayer;

/**
 * @author KarnickelDev
 * @since 04.07.2025
 **/
public class MainMenuScreen implements GameStateScreen {

    private final SolarMain game;
    private final Viewport backgroundViewport;

    private RendererContext renderCtx;

    private Runnable onInitRunnable;

    public MainMenuScreen(SolarMain solarMain) {
        this(solarMain, null);
    }

    public MainMenuScreen(SolarMain solarMain, Runnable onInitRunnable) {
        this.game = solarMain;
        backgroundViewport = new ExtendViewport(UI.VIRTUAL_WIDTH,UI.VIRTUAL_HEIGHT);
        this.onInitRunnable = onInitRunnable;
    }

    @Override
    public LoadingPlan preEnterLoadingPlan() {
        return new LoadingPlanBuilder()
            .syncTask(() -> {
                Engine.shaderManager().registerFromInternalFile("msdf", "shaders/msdf/msdf.vert", "shaders/msdf/msdf.frag");

                AssetWrapper.getInstance().getAssetManager().load("fonts/atlas.png", Texture.class);
                AssetWrapper.getInstance().getAssetManager().finishLoading();
                Texture atlas = AssetWrapper.getInstance().getAssetManager().get("fonts/atlas.png", Texture.class);
                atlas.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

                SimTestScreen.font = JSONLoader.loadFont(Gdx.files.internal("fonts/atlas.json"), atlas);

                ShaderProgram shaderProgram = Engine.shaderManager().get("msdf");
                MSDFBatch msdfBatch = new MSDFBatch(1024, shaderProgram);
                renderCtx = new RendererContext(SolarMain.getInstance().getBatch(), new UIRenderer(new SpriteBatch(), msdfBatch), new ShapeRenderer());
            })
            .syncTask(() -> UI.getThemeManager().loadThemes())
            .build();
    }

    @Override
    public void enter() {
        SolarMain.getInstance().setScreen(this);

        UI.getUIManager().clear();
        UI.getUIManager().push(new MainMenuLayer());

        //Gdx.input.setInputProcessor(UI.getUIManager().getInputManager());
        Engine.input().addListener(UI.getUIManager());

        SolarMain.getInstance().getSettingsManager().setFpsOverride(30);
    }

    @Override
    public void exit() {
        SolarMain.getInstance().getSettingsManager().clearFpsOverride();

        UI.getUIManager().clear();
    }

    @Override
    public LoadingPlan postExitLoadingPlan() {
        return LoadingPlanBuilder.empty();
    }

    float time = 0;

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1, true);

        backgroundViewport.apply();
        game.getBatch().setColor(1f,1f,1f,1f);
        game.getBatch().begin();
        game.getBatch().setProjectionMatrix(backgroundViewport.getCamera().combined);
        game.getBatch().draw(StarField.starFieldBuffer.getColorBufferTexture(), 0, 0);
        game.getBatch().end();

        time += 10*delta;
        //GasGiantTest.genPlanet(shapeRenderer, backgroundViewport, time);

        Engine.input().poll();
        Engine.input().dispatchEvents();
        Engine.input().beginFrame();

        renderCtx.batch().setColor(1,1,1,1);
        renderCtx.debug().setProjectionMatrix(new Matrix4().setToOrtho2D(0,0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        renderCtx.debug().begin(ShapeRenderer.ShapeType.Line);

        UILayoutEngine.UILayoutContext uiContext = UILayoutEngine.computeLayoutContext(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        renderCtx.uiRenderer().updateViewport(0,0, uiContext.screenWidth(), uiContext.screenHeight());
        renderCtx.uiRenderer().begin();

        UI.getUIManager().update(uiContext, delta);
        UI.getUIManager().render(renderCtx);

        renderCtx.uiRenderer().end();
        renderCtx.debug().end();

        if(onInitRunnable != null) {
            onInitRunnable.run();
            onInitRunnable = null;
        }

        //GameStateManager.get().requestStateLoading(new GameplayLoadScreen(false, null));
    }

    @Override
    public void resize(int width, int height) {
        backgroundViewport.update(width, height, true);
        StarField.regenerateStarTexture(LoadingScreen.background);
        //UI.getSkinManager().reload(height);
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

    }

    @Override
    public void dispose() {

    }

    @Override
    public GameStateID getID() {
        return GameStateID.MAIN_MENU;
    }
}
