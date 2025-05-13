package karnickeldev.solar.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import karnickeldev.solar.assetmanager.Asset;
import karnickeldev.solar.assetmanager.AssetWrapper;
import karnickeldev.solar.render.BackgroundStarRenderer;
import karnickeldev.solar.util.Logger;
import karnickeldev.solar.util.MathUtil;

import java.util.Random;

/**
 * Project: SOLAR
 *
 * @author KarnickelDev
 * @since 14.10.2024
 */
public class MainMenuScreen implements Screen {

    private final SolarMain game;
    private final Texture dark_blue;
    float X = 0;
    private Viewport viewport;
    private OrthographicCamera camera;

    public MainMenuScreen(SolarMain solarMain) {
        this.game = solarMain;

        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport = new ScreenViewport(camera);

        int STAR_COUNT = 1024;
        float[] ra = new float[STAR_COUNT];
        float[] dec = new float[STAR_COUNT];
        float[] brightness = new float[STAR_COUNT];
        char[] size = new char[STAR_COUNT];
        char[] color = new char[STAR_COUNT];

        Random rnd = new Random();
        for (int i = 0; i < STAR_COUNT; i++) {
            ra[i] = MathUtil.random(0, 360);
            dec[i] = (float) Math.toDegrees(Math.asin(MathUtil.random(-1, 1)));
            int s = rnd.nextInt(100);
            if (s < 86) {
                size[i] = 0;
                brightness[i] = MathUtil.random(0.3f, 1);
            } else if (s < 97) {
                size[i] = 1;
                brightness[i] = MathUtil.random(0.5f, 1);
            } else {
                size[i] = 2;
                brightness[i] = MathUtil.random(0.8f, 1);
            }

            int c = rnd.nextInt(100);
            if (c < 70) {
                color[i] = 'w';
            } else if (c < 80) {
                color[i] = 'y';
            } else if (c < 90) {
                color[i] = 'r';
            } else {
                color[i] = 'b';
            }
        }

        BackgroundStarRenderer.setStarParameters(STAR_COUNT, size, color, brightness, ra, dec);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        int c = 255;    // alpha 255
        c |= (15 << 8); // blue
        c |= (9 << 16); // green
        c |= (9 << 24); // red
        pixmap.setColor(c);
        pixmap.drawPixel(0, 0);
        dark_blue = new Texture(pixmap);
        pixmap.dispose();

        game.getUIManager().getMainMenu().show();

        game.getInputManager().addInput(game.pausedStage);
    }

    @Override
    public void show() {
        BackgroundStarRenderer.loadAssets();
        BackgroundStarRenderer.setPosition(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.5f, 50f, 11f);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();

        game.batch.setProjectionMatrix(camera.combined);

        X += delta;

        game.batch.begin();
        game.batch.setColor(1f, 1f, 1f, 1f);

        game.batch.draw(dark_blue, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        if (!BackgroundStarRenderer.drawStarScape(game.batch, delta, true)) {
            Logger.error("Erroneous input for background starscape");
        }

        // Draw the background
        Texture background = AssetWrapper.getInstance().getAsset(Asset.MAIN_MENU_BACKGROUND_SCENERY);
        game.batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        game.batch.end();

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
        dark_blue.dispose();
    }
}
