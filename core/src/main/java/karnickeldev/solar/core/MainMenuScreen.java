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
import karnickeldev.solar.util.TileLoader;

/**
 * Project: SOLAR
 *
 * @author KarnickelDev
 * @since 14.10.2024
 */
public class MainMenuScreen implements Screen {

    private final SolarMain game;

    private Viewport viewport;
    private OrthographicCamera camera;

    private final Texture background_atlas;
    private final TextureRegion[] background_tiles;

    public MainMenuScreen(SolarMain solarMain) {
        this.game = solarMain;

        background_atlas = AssetWrapper.getInstance().getAsset(Asset.STARRY_SKY_BACKGROUND_TILES);
        background_tiles = TileLoader.getTiles(background_atlas, 4, 256);

        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport = new ScreenViewport(camera);

        game.getUIManager().getMainMenu().show();

        game.getInputManager().addInput(InputManager.MAIN_MENU_INPUT, game.pausedStage);
    }

    @Override
    public void show() {

    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();

        game.batch.setProjectionMatrix(camera.combined);

        // Draw the background
        TileLoader.renderBackground(game.batch, background_tiles, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        SolarMain.getInstance().pausedStage.act(delta);
        SolarMain.getInstance().pausedStage.draw();
    }

    @Override
    public void resize(int width, int height) {



        game.getUIManager().resize(width, height);
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
}
