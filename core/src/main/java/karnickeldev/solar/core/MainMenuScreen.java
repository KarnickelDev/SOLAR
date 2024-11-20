package karnickeldev.solar.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import karnickeldev.solar.assetmanager.Asset;
import karnickeldev.solar.assetmanager.AssetWrapper;
import karnickeldev.solar.ui.*;
import karnickeldev.solar.util.TileLoader;

/**
 * Project: SOLAR
 *
 * @author KarnickelDev
 * @since 14.10.2024
 */
public class MainMenuScreen implements Screen, UIElement {

    private static float MAIN_MENU_WIDTH;

    private final SolarMain game;

    private final Texture background_atlas;
    private final TextureRegion[] background_tiles;

    UIManager ui_manager;

    private final Stage stage;
    private final OrthographicCamera camera;
    private final Viewport viewport;

    private final Skin skin;

    private final Table mainMenuUITable;

    public MainMenuScreen(SolarMain solarMain) {
        this.game = solarMain;

        camera = new OrthographicCamera(
            game.getSettingsManager().getSettings().getScreenWidth(),
            game.getSettingsManager().getSettings().getScreenHeight()
        );
        camera.update();
        viewport = new ScreenViewport(camera);

        background_atlas = AssetWrapper.getInstance().getAsset(Asset.STARRY_SKY_BACKGROUND_TILES);
        background_tiles = TileLoader.getTiles(background_atlas, 4, 256);

        stage = new Stage(viewport);
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        ui_manager = new UIManager(stage);

        mainMenuUITable = new Table();

        mainMenuUITable.add(new MenuButton("Singleplayer", skin)).row();
        mainMenuUITable.add(new MenuButton("Multiplayer", skin)).row();
        mainMenuUITable.add(new MenuButton("Options", skin, () -> ui_manager.getOptionsMenu().show())).row();
        mainMenuUITable.add(new MenuButton("Credits", skin)).row();
        mainMenuUITable.add(new MenuButton("Exit", skin, game::exit)).row();

        stage.addActor(mainMenuUITable);

        resizeUI(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        mainMenuUITable.setVisible(true);

        game.getInputManager().addInput(InputManager.MAIN_MENU_INPUT, stage);
    }

    public static float getMainMenuWidth() {return MAIN_MENU_WIDTH;}

    public void resizeUI(int appWidth, int appHeight) {
        BitmapFont font = Fonts.MEDIUM_BOLD;

        GlyphLayout glyph_layout = new GlyphLayout();
        glyph_layout.setText(font,"Singleplayer");

        for(Actor actor : mainMenuUITable.getChildren()) {
            if(actor instanceof MenuButton) {
                MenuButton button = (MenuButton) actor;
                TextButton.TextButtonStyle style = button.getStyle();
                style.font = font;
                button.setStyle(style);
            }
        }

        int groupHeight = appHeight / 3;
        float groupSpacing = 0.5f * groupHeight;
        float elementHeight = 0.5f * groupHeight;
        int groupWidth = Math.min(appWidth, (int)(1.5f* glyph_layout.width));

        mainMenuUITable.setSize(groupWidth, groupHeight);
        mainMenuUITable.setPosition(appWidth * 0.03f, (appHeight * 0.72f) - groupHeight);

        for(Actor actor: mainMenuUITable.getChildren()) {
            if(actor instanceof MenuButton) {
                MenuButton button = (MenuButton) actor;
                mainMenuUITable.getCell(button).width(groupWidth).height(elementHeight / 5f)
                    .padBottom(groupSpacing / 4f)
                    .fill()
                    .expandX();
            }
        }

        mainMenuUITable.invalidate();
        mainMenuUITable.layout();

        MAIN_MENU_WIDTH = mainMenuUITable.getWidth() + mainMenuUITable.getX();
    }

    @Override
    public void show() {
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        game.batch.setProjectionMatrix(camera.combined);

        // Draw the background
        TileLoader.renderBackground(game.batch, background_tiles, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        camera.setToOrtho(false, width, height);
        camera.update();

        Fonts.resizeFonts(height);

        resizeUI(width, height);
        ui_manager.resize(width, height);
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
        stage.dispose();
        skin.dispose();
    }
}
