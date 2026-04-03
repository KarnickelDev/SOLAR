package karnickeldev.solar.core;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import karnickeldev.solar.assetmanager.Asset;
import karnickeldev.solar.assetmanager.AssetWrapper;
import karnickeldev.solar.context.EngineContext;
import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.context.GameContextContainer;
import karnickeldev.solar.context.ServerContext;
import karnickeldev.solar.core.gamestates.GameStateManager;
import karnickeldev.solar.core.gamestates.LoadingPlanBuilder;
import karnickeldev.solar.logging.LogTag;
import karnickeldev.solar.logging.Logger;
import karnickeldev.solar.settings.Settings;
import karnickeldev.solar.settings.SettingsManager;
import karnickeldev.solar.ui.core.SkinManager;
import karnickeldev.solar.ui.screens.LoadingScreen;
import karnickeldev.solar.ui.screens.MainMenuScreen;

import java.util.List;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class SolarMain extends Game {

    private static volatile boolean shuttingDown = false;

    private static SolarMain instance;
    private final SettingsManager settingsManager;
    private final InputManager inputManager;

    public static float tps;

    SpriteBatch batch;

    public SolarMain(Settings settings) {
        instance = this;

        this.settingsManager = new SettingsManager(settings);

        this.inputManager = new InputManager();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown hook triggered");
            try {
                shutdown();
            } catch (Exception e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }));
    }

    public static SolarMain getInstance() {
        return instance;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public Settings getSettings() {
        return settingsManager.getSettings();
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public InputManager getInputManager() {
        return inputManager;
    }

    @Override
    public void create() {
        Thread.currentThread().setName("Solar-Main-Thread");
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        getSettingsManager().applySettings();

        EngineContext.init(2);

        batch = new SpriteBatch();

        Gdx.input.setInputProcessor(getInputManager().getInputMultiplexer());

        // load minimal assets
        AssetWrapper.getInstance().loadGlobal(Asset.GAME_ICON);
        AssetWrapper.getInstance().loadGlobal(Asset.STARS_ATLAS);
        AssetWrapper.getInstance().finishLoading();

        LoadingPlanBuilder b = new LoadingPlanBuilder()
            .syncTask(SkinManager::init)
            .loadAsset(
                Asset.STARRY_SKY_BACKGROUND_TILES,
                Asset.MAIN_MENU_BACKGROUND_SCENERY,
                Asset.DEBUG_CIRCLE
            );

        // TODO: load more of the above during loading screen
        setScreen(new LoadingScreen(0f,
            () -> GameStateManager.get().requestStateLoading(new MainMenuScreen(this)),
            List.of(b.build())));

        Logger.get(LogTag.STARTUP).info("Startup complete");
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

    @Override
    public void dispose() {
        shutdown();

        // disposing MUST be AFTER orderly shutdown
        // (shutdown might use stuff disposed of here, causing error)
        batch.dispose();
        AssetWrapper.getInstance().dispose();
    }

    public static synchronized void shutdown() {
        if (shuttingDown) return;
        shuttingDown = true;

        Logger.get(LogTag.SHUTDOWN).info("Shutting down game...");

        try {
            if(GameContext.isSet()) {
                GameContextContainer ctx = GameContext.get();

                if (ctx.getClientNetwork() != null) {
                    if(ctx.getClientNetwork().isConnected()) {
                        ctx.getClientNetwork().disconnect();
                    }
                }

                ctx.getOrbitSolver().shutdown();

                ctx.getScheduler().shutdown();

                long nowMS = System.currentTimeMillis();
                ctx.getScheduler().timer().update(nowMS);
                long delta = System.currentTimeMillis() - nowMS;

                int remainingMS = (int) Math.max(500, 30_000 - delta);
                ctx.getScheduler().main().update(remainingMS);

                ctx.getScheduler().main().shutdownNow();
                ctx.getScheduler().async().shutdownNow();
            }

            if (ServerContext.isSet()) {
                ServerContext.get().getServer().stop();
            }

            // Dispose LibGDX resources
            if (Gdx.app != null) {
                Gdx.app.exit();
            }

            Logger.get(LogTag.SHUTDOWN).info("Shutdown complete, bye!");

        } catch (Exception e) {
            Logger.get(LogTag.SHUTDOWN).error("Error during shutdown: " + e.getMessage(), e);
        }
    }

}
