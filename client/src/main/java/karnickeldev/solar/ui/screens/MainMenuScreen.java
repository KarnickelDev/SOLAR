package karnickeldev.solar.ui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.core.gamestates.GameState;
import karnickeldev.solar.core.gamestates.GameStateID;
import karnickeldev.solar.core.gamestates.GameStateScreen;
import karnickeldev.solar.render.GasGiantTest;
import karnickeldev.solar.render.StarField;
import karnickeldev.solar.ui.components.DebugToolTip;
import karnickeldev.solar.ui.components.mainmenu.MainMenu;
import karnickeldev.solar.ui.components.mainmenu.MultiplayerMenu;
import karnickeldev.solar.ui.components.optionsmenu.OptionsMenu;
import karnickeldev.solar.ui.core.UI;

/**
 * @author KarnickelDev
 * @since 04.07.2025
 **/
public class MainMenuScreen implements GameStateScreen {

    private final SolarMain game;
    private final Viewport backgroundViewport;

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    private final MainMenu mainMenu = new MainMenu();
    private final OptionsMenu optionsMenu = new OptionsMenu();

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
    public void enter() {
        SolarMain.getInstance().setScreen(this);

        UI.getUIManager().addForceComponent("debug", new DebugToolTip(true));
        UI.getUIManager().showComponent("debug");

        UI.getUIManager().addComponent("main_menu", mainMenu);
        UI.getUIManager().showComponent("main_menu");

        UI.getUIManager().addComponent("options_menu", optionsMenu);
        UI.getUIManager().hideComponent("options_menu");

        UI.getUIManager().addComponent("multiplayer_menu", new MultiplayerMenu());
        UI.getUIManager().hideComponent("multiplayer_menu");

        Gdx.input.setInputProcessor(UI.getUIManager().getStage());

        SolarMain.getInstance().getSettingsManager().setFpsOverride(30);
    }

    @Override
    public void exit() {
        SolarMain.getInstance().getSettingsManager().clearFpsOverride();
        UI.getUIManager().removeComponent("main_menu");
        UI.getUIManager().removeComponent("multiplayer_menu");

        UI.getUIManager().hideAll();
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

    @Override
    public GameStateID getGameStateID() {
        return GameStateID.MAIN_MENU;
    }
}
