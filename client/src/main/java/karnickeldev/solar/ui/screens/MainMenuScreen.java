package karnickeldev.solar.ui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.core.gamestates.GameStateID;
import karnickeldev.solar.core.gamestates.LoadingPlan;
import karnickeldev.solar.core.gamestates.GameStateScreen;
import karnickeldev.solar.core.gamestates.LoadingPlanBuilder;
import karnickeldev.solar.input.GameplayInputManager;
import karnickeldev.solar.render.StarField;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.layers.mainmenu.MainMenuLayer;

/**
 * @author KarnickelDev
 * @since 04.07.2025
 **/
public class MainMenuScreen implements GameStateScreen {

    private final SolarMain game;
    private final Viewport backgroundViewport;

    private final MainMenuLayer mainMenuLayer = new MainMenuLayer();

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
        return LoadingPlanBuilder.empty();
    }

    @Override
    public void enter() {
        SolarMain.getInstance().setScreen(this);

        UI.getUIManager().clear();
        UI.getUIManager().push(mainMenuLayer);

        Gdx.input.setInputProcessor(UI.getUIManager().getInputManager());

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

        UI.getUIManager().act(delta);
        UI.getUIManager().draw();

        if(onInitRunnable != null) {
            onInitRunnable.run();
            onInitRunnable = null;
        }
    }

    @Override
    public void resize(int width, int height) {
        backgroundViewport.update(width, height, true);
        StarField.regenerateStarTexture(LoadingScreen.background);
        UI.getSkinManager().reload(height);
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
