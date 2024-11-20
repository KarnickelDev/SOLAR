package karnickeldev.solar.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import karnickeldev.solar.assetmanager.Asset;
import karnickeldev.solar.assetmanager.AssetWrapper;
import karnickeldev.solar.util.TileLoader;

/**
 * Project: SOLAR
 *
 * @author KarnickelDev
 * @since 13.10.2024
 */
public class LoadingScreen implements Screen {

    private SolarMain game;
    private int appWidth, appHeight;

    private final Texture background_atlas;
    private final TextureRegion[] background_tiles;

    private OrthographicCamera camera;
    private Viewport viewport;

    ShapeRenderer shapeRenderer;
    private float angle0 = 0f, angle1 = 0f;
    private float planet0Radius, planet1Radius, sunRadius;
    private float orbit0Radius, orbit1Radius;
    private float centerX, centerY;

    public LoadingScreen(SolarMain solarMain, Runnable loadRunnable) {
        this.game = solarMain;

        appWidth = solarMain.getSettingsManager().getSettings().getScreenWidth();
        appHeight = solarMain.getSettingsManager().getSettings().getScreenHeight();

        if(!AssetWrapper.getInstance().isLoaded(Asset.STARRY_SKY_BACKGROUND_TILES)) {
            AssetWrapper.getInstance().loadGlobal(Asset.STARRY_SKY_BACKGROUND_TILES);
            AssetWrapper.getInstance().finishLoading();
        }

        camera = new OrthographicCamera(
            solarMain.getSettingsManager().getSettings().getScreenWidth(),
            solarMain.getSettingsManager().getSettings().getScreenHeight()
        );
        camera.update();

        viewport = new ScreenViewport(camera);

        background_atlas = AssetWrapper.getInstance().getAsset(Asset.STARRY_SKY_BACKGROUND_TILES);
        background_tiles = TileLoader.getTiles(background_atlas, 4, 256);

        shapeRenderer = new ShapeRenderer();

        resizeUI(appWidth, appHeight);

        if(loadRunnable != null) loadRunnable.run();
    }

    private void resizeUI(int width, int height) {
        appWidth = width;
        appHeight = height;

        planet0Radius = 0.002f * appWidth;
        planet1Radius = 0.004f * appWidth;
        sunRadius = 0.005f * appWidth;
        orbit1Radius = 0.02f * appWidth;
        orbit0Radius = 0.666667f * orbit1Radius;
        centerX = appWidth - orbit1Radius - 3*sunRadius;
        centerY = orbit1Radius + 3*sunRadius;
    }


    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        TileLoader.renderBackground(game.batch, background_tiles, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        angle0 = (angle0 + (230*delta)) % 360;
        angle1 = (angle1 + (160*delta)) % 360;


        // Calculate the planet's position in the orbit
        float planet0X = centerX + orbit0Radius * (float)Math.cos(Math.toRadians(angle0));
        float planet0Y = centerY + orbit0Radius * (float)Math.sin(Math.toRadians(angle0));

        float planet1X = centerX + orbit1Radius * (float)Math.cos(Math.toRadians(angle1));
        float planet1Y = centerY + orbit1Radius * (float)Math.sin(Math.toRadians(angle1));

        game.batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Draw the orbit path
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.8f, 0.8f, 0.8f, 0.8f);  // White for the orbit path
        shapeRenderer.circle(centerX, centerY, orbit0Radius);  // Draw the orbit circle
        shapeRenderer.circle(centerX, centerY, orbit1Radius);  // Draw the orbit circle
        shapeRenderer.end();

        // Draw the center point (e.g., the sun)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1, 1, 1, 1);
        shapeRenderer.circle(centerX, centerY, sunRadius);

        // Draw the planet orbiting the center
        shapeRenderer.setColor(1, 1, 1, 1);  // Blue for the planet
        shapeRenderer.circle(planet0X, planet0Y, planet0Radius);  // Draw the planet as a blue circle

        // Draw the planet orbiting the center
        shapeRenderer.setColor(1, 1, 1, 1);  // Blue for the planet
        shapeRenderer.circle(planet1X, planet1Y, planet1Radius);  // Draw the planet as a blue circle

        shapeRenderer.end();


        if(AssetWrapper.getInstance().update(16)) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
        }
    }

    @Override
    public void resize(int width, int height) {
        resizeUI(width, height);
        viewport.update(width, height, true);
        camera.setToOrtho(false, width, height);
        camera.update();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
