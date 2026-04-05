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
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.core.gamestates.LoadingPlan;
import karnickeldev.solar.render.StarField;
import karnickeldev.solar.ui.core.SkinManager;
import karnickeldev.solar.ui.core.UI;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Displays a lightweight, animated loading screen
 * Loads assets and dispatches tasks while trying to stay at 50fps
 * @author KarnickelDev
 * @since 05.07.2025
 **/
public class LoadingScreen implements Screen {

    public static final StarField.Container background = StarField.generateRandom((short) (1024 * 4), 0.3f, 0.3f);

    private static final int TARGET_FPS = 40;
    private static final int FRAME_MS = 1000 / TARGET_FPS;
    private static final float DEFAULT_FADEOUT_SECONDS = 0.5f;


    // state keeping
    private final Runnable onComplete;
    private final List<LoadingPlan> plans;
    private int currentPlan = 0;

    private float progress_anim_speed = 3f; // fraction per second
    private float displayedProgress = 0f;
    private final float fadeout_seconds;

    private float completionTimer = 0f;
    private boolean allDone = false;

    private Consumer<Throwable> onFailure = null;

    // render components
    private final GlyphLayout versionGlyphLayout = new GlyphLayout();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    private static final Viewport viewport = new FitViewport(1920, 1080);
    private static final Viewport backgroundViewport = new ExtendViewport(1920, 1080);

    private float angle0 = 1.57f, angle1 = 1.57f;
    private final static float planet0Radius = 4, planet1Radius = 6, sunRadius = 11;
    private final static float orbit0Radius = 40, orbit1Radius = 70;
    private final static int centerX = Math.round(1920 - orbit1Radius - 20);
    private final static int centerY = Math.round(orbit1Radius + 20);

    private static final Color DARK_ORANGE = new Color(0x9D5E2AFF);
    private static final Color BEIGE = new Color(0xD1A46BFF);

    public LoadingScreen(float fadeout_seconds, Runnable onComplete, List<LoadingPlan> loadingPlans) {
        this.onComplete = Objects.requireNonNull(onComplete);
        this.fadeout_seconds = fadeout_seconds;

        this.plans = new ArrayList<>(loadingPlans);
    }

    public LoadingScreen(Runnable onComplete, List<LoadingPlan> loadingPlans) {
        this(DEFAULT_FADEOUT_SECONDS, onComplete, loadingPlans);
    }

    public void setOnFailure(Consumer<Throwable> onFailure) {
        this.onFailure = onFailure;
    }

    @Override
    public void show() {
        SolarMain.getInstance().getSettingsManager().setFpsOverride(TARGET_FPS);

        StarField.loadAssets();

        if(!plans.isEmpty()) {
            plans.getFirst().begin();
        } else {
            allDone = true;
            displayedProgress = 1f;
        }
    }

    @Override
    public void render(float delta) {
        drawLoadingScreen(delta);
        updateLoadingLogic(delta);
    }

    @Override
    public void resize(int width, int height) {
        StarField.regenerateStarTexture(background);
        viewport.update(width, height, true);
        backgroundViewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }

    private void updateLoadingLogic(float delta) {

        // execute loading plans
        if(currentPlan < plans.size()) {
            // IMPORTANT: check for any failure
            LoadingPlan plan = plans.get(currentPlan);
            boolean stepDone = plan.step(FRAME_MS);

            if(plan.failed()) {
                triggerFailure(plan.getFailure());
                return;
            }
            if(plan.isCancelled()) {
                triggerFailure(new CancellationException("loading was cancelled"));
                return;
            }

            // start next step if current step done
            if(stepDone) {
                currentPlan++;
                if(currentPlan < plans.size()) {
                    plans.get(currentPlan).begin();
                }
            }
        }

        // loading done, start transition
        if(!allDone && currentPlan >= plans.size()) {
            allDone = true;
            completionTimer = 0;
            progress_anim_speed = (1 - displayedProgress) / (0.85f*fadeout_seconds);
        }

        // animate transition
        if(allDone) {
            completionTimer += delta;
            if(completionTimer >= fadeout_seconds) {
                SolarMain.getInstance().getSettingsManager().clearFpsOverride();
                onComplete.run();
            }
        }
    }

    private void drawLoadingScreen(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        drawBackground();
        drawLoadingSpinner(delta);
        drawProgressBar(delta);
    }

    private void drawBackground() {
        backgroundViewport.apply();
        SolarMain.getInstance().getBatch().setProjectionMatrix(backgroundViewport.getCamera().combined);
        SolarMain.getInstance().getBatch().setColor(1f,1f,1f,1f);
        SolarMain.getInstance().getBatch().begin();
        SolarMain.getInstance().getBatch().draw(StarField.starFieldBuffer.getColorBufferTexture(),0,0);

        if(SkinManager.isInit()) {
            BitmapFont font = UI.getFontManager().getFont(28, false);
            versionGlyphLayout.setText(font, Metadata.APP_NAME + " v" + Metadata.VERSION);
            // TODO: find why this is necessary, appeared after adding (and typing) in player chat
            font.setColor(1,1,1,1);
            font.draw(SolarMain.getInstance().getBatch(), versionGlyphLayout,5,versionGlyphLayout.height + 5);
        }
        SolarMain.getInstance().getBatch().end();
    }

    private void drawLoadingSpinner(float delta) {
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
    }

    private void drawProgressBar(float delta) {
        //Draw progress bar
        float edgePad = 250;
        float height = 24;
        float width = 1920 - 2*edgePad;

        float progress = getOverallProgress();

        if(!allDone) {
            displayedProgress += (progress - displayedProgress) * progress_anim_speed * delta;
        } else {
            displayedProgress += progress_anim_speed * delta;
        }
        displayedProgress = Math.min(1f, displayedProgress);

        shapeRenderer.setColor(DARK_ORANGE);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.rect(edgePad - 5, orbit1Radius - 5, width + 10, height + 10);
        shapeRenderer.end();

        shapeRenderer.setColor(BEIGE);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(edgePad, orbit1Radius, displayedProgress*width, height);
        shapeRenderer.end();
    }

    private float getOverallProgress() {
        if(plans.isEmpty()) return 1f;

        float perPlan = 1f / plans.size();
        float sum = currentPlan * perPlan;  // plans that are done

        if(currentPlan < plans.size()) {
            sum += plans.get(currentPlan).getProgress() * perPlan;  // add progress of current plan
        }

        return sum;
    }

    private void triggerFailure(Throwable cause) {
        Throwable finalCause = cause != null ? cause : new RuntimeException("Unknown loading failure");

        for(int i = currentPlan; i < plans.size(); i++) plans.get(i).cancel();

        if(onFailure != null) {
            try {
                onFailure.accept(finalCause);
            } catch (Exception ignored) {}
        }

        allDone = true;
        completionTimer = Float.MAX_VALUE;
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

}
