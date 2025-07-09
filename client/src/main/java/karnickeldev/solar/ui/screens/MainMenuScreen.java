package karnickeldev.solar.ui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.core.gamestates.GameState;
import karnickeldev.solar.core.gamestates.GameStateID;
import karnickeldev.solar.render.StarField;
import karnickeldev.solar.ui.components.DebugToolTip;
import karnickeldev.solar.ui.components.MainMenu;
import karnickeldev.solar.ui.components.optionsmenu.OptionsMenu;
import karnickeldev.solar.ui.core.UI;

/**
 * @author : KarnickelDev
 * @since : 04.07.2025
 **/
public class MainMenuScreen implements GameState {

    private final SolarMain game;
    private final Viewport backgroundViewport;

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    private final DebugToolTip debug = new DebugToolTip();
    private final MainMenu mainMenu = new MainMenu();
    private final OptionsMenu optionsMenu = new OptionsMenu();

    public MainMenuScreen(SolarMain solarMain) {
        this.game = solarMain;
        backgroundViewport = new ExtendViewport(UI.VIRTUAL_WIDTH,UI.VIRTUAL_HEIGHT);
    }

    @Override
    public void enter() {
        UI.getUIManager().addComponent("debug", debug);
        UI.getUIManager().addComponent("main_menu", mainMenu);
        UI.getUIManager().addComponent("options_menu", optionsMenu);
        UI.getUIManager().hideComponent("options_menu");
        Gdx.input.setInputProcessor(UI.getUIManager().getStage());

        SolarMain.getInstance().getSettingsManager().setFpsOverride(30);
    }

    @Override
    public void exit() {
        SolarMain.getInstance().getSettingsManager().clearFpsOverride();
        UI.getUIManager().getStage().clear();
    }

    float time = 0;
    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1, true);

        backgroundViewport.apply();
        game.getBatch().begin();
        game.getBatch().setProjectionMatrix(backgroundViewport.getCamera().combined);
        game.getBatch().draw(StarField.starFieldBuffer.getColorBufferTexture(), 0, 0);
        game.getBatch().end();

        time += 10*delta;
        //GasGiantTest.genPlanet(shapeRenderer, backgroundViewport, time);

        UI.getUIManager().act(delta);
        UI.getUIManager().draw();
    }

    @Override
    public void resize(int width, int height) {
        backgroundViewport.update(width, height, true);
        StarField.regenerateStarTexture(LoadingScreen.background);
        UI.getSkinManager().reload(height);
        UI.getUIManager().resize(width, height);
    }

    @Override
    public GameStateID getID() {
        return GameStateID.MAIN_MENU;
    }
}
