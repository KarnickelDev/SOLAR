package karnickeldev.solar.ui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import karnickeldev.solar.Metadata;
import karnickeldev.solar.assetmanager.Asset;
import karnickeldev.solar.assetmanager.AssetWrapper;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.network.net.dispatcher.DefaultDispatcher;
import karnickeldev.solar.network.net.dispatcher.Dispatcher;
import karnickeldev.solar.render.StarField;
import karnickeldev.solar.ui.core.SkinManager;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.util.MathUtil;

import java.util.List;

/**
 * Displays a lightweight, animated loading screen
 * Loads assets and dispatches tasks while trying to stay at 50fps
 * @author : KarnickelDev
 * @since : 05.07.2025
 **/
public class LoadingScreen implements Screen {

    private static final int FRAME_MS = 20;

    private final Runnable onComplete;
    private final List<Runnable> loadTasks;

    private static final ShapeRenderer shapeRenderer = new ShapeRenderer();

    private static final Viewport viewport = new FitViewport(1920, 1080);
    private static final Viewport backgroundViewport = new ExtendViewport(1920, 1080);

    private final GlyphLayout versionGlyphLayout;

    private float angle0 = 1.57f, angle1 = 1.57f;
    private final static float planet0Radius = 4, planet1Radius = 6, sunRadius = 11;
    private final static float orbit0Radius = 40, orbit1Radius = 70;
    private final static int centerX = Math.round(1920 - orbit1Radius - 20);
    private final static int centerY = Math.round(orbit1Radius + 20);

    private static final Color DARK_ORANGE = new Color(0x9D5E2AFF);
    private static final Color BEIGE = new Color(0xD1A46BFF);

    private final Asset[] loadAssets;

    private final Dispatcher dispatcher = new DefaultDispatcher();

    private float time_done = 0f;
    boolean done = false;

    public LoadingScreen(Runnable onComplete, List<Runnable> loadTasks, Asset... loadAssets) {
        this.onComplete = onComplete;
        this.loadTasks = loadTasks;
        this.loadAssets = loadAssets;

        versionGlyphLayout = new GlyphLayout();
    }

    @Override
    public void show() {
        time_done = 0;

        SolarMain.getInstance().getSettingsManager().setFpsOverride(50);

        StarField.loadAssets();

        if(loadTasks != null) {
            for(Runnable task: loadTasks) {
                dispatcher.dispatch(task);
            }
        }

        for(Asset asset: loadAssets) {
            if(AssetWrapper.getInstance().isLoaded(asset)) continue;
            AssetWrapper.getInstance().loadGlobal(asset);
        }
    }

    public static final StarField.Container background = StarField.generateRandom((short) 1024, 0.3f, 0.3f);

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        backgroundViewport.apply();
        SolarMain.getInstance().getBatch().setProjectionMatrix(backgroundViewport.getCamera().combined);
        SolarMain.getInstance().getBatch().setColor(1f,1f,1f,1f);
        SolarMain.getInstance().getBatch().begin();
        SolarMain.getInstance().getBatch().draw(StarField.starFieldBuffer.getColorBufferTexture(),0,0);

        if(SkinManager.isInit()) {
            BitmapFont font = UI.getFontManager().getFont(14, false);
            versionGlyphLayout.setText(font, Metadata.APP_NAME + " v" + Metadata.VERSION);
            font.draw(SolarMain.getInstance().getBatch(), versionGlyphLayout,5,versionGlyphLayout.height + 5);
        }
        SolarMain.getInstance().getBatch().end();

        angle0 = (angle0 + (10 * delta)) % 6.283185f;
        angle1 = (angle1 + (6 * delta)) % 6.283185f;

        // Calculate the planet's position in the orbit
        float planet0X = centerX + orbit0Radius * MathUtils.cos(angle0);
        float planet0Y = centerY + orbit0Radius * MathUtils.sin(angle0);

        float planet1X = centerX + orbit1Radius * MathUtils.cos(angle1);
        float planet1Y = centerY + orbit1Radius * MathUtils.sin(angle1);

        // Draw the orbit path
        viewport.apply();
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);

        Gdx.gl.glLineWidth(3);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(DARK_ORANGE);
        shapeRenderer.circle(centerX, centerY, orbit0Radius, 32);
        shapeRenderer.circle(centerX, centerY, orbit1Radius, 32);
        shapeRenderer.end();

        // Draw the center point (e.g., the sun)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(BEIGE);
        shapeRenderer.circle(centerX, centerY, sunRadius, 12);

        // Draw the planet orbiting the center
        shapeRenderer.setColor(BEIGE);
        shapeRenderer.circle(planet0X, planet0Y, planet0Radius, 8);

        // Draw the planet orbiting the center
        shapeRenderer.setColor(BEIGE);
        shapeRenderer.circle(planet1X, planet1Y, planet1Radius, 8);
        shapeRenderer.end();

        //Draw progress bar
        float edgePad = 250;
        float height = 24;
        float width = 1920 - 2*edgePad;

        float progress = MathUtil.clamp(
            (dispatcher.getProgress() + AssetWrapper.getInstance().getAssetManager().getProgress()) / 2f,
            0f, 1f);

        shapeRenderer.setColor(DARK_ORANGE);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.rect(edgePad - 5, orbit1Radius - 5, width + 10, height + 10);
        shapeRenderer.end();

        shapeRenderer.setColor(BEIGE);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(edgePad, orbit1Radius, progress*width, height);
        shapeRenderer.end();

        long start = System.nanoTime();
        boolean tasksLoaded = dispatcher.update(FRAME_MS);
        int assetTime = (int) (System.nanoTime() - start) / 1_000_000;

        if(AssetWrapper.getInstance().update(FRAME_MS - assetTime) && tasksLoaded && !done) {
            done = true;
            time_done = 0;
        }

        // delay onComplete a bit for smooth transition
        if(done) time_done += delta;
        if(done && time_done > 0.17f) {
            done = false;
            SolarMain.getInstance().getSettingsManager().clearFpsOverride();
            onComplete.run();
        }
    }

    @Override
    public void resize(int width, int height) {
        StarField.regenerateStarTexture(background);
        viewport.update(width, height, true);
        backgroundViewport.update(width, height, true);
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
        shapeRenderer.dispose();
    }
}
