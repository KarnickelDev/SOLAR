package karnickeldev.solar.core.gamestates;

import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.logging.Logger;
import karnickeldev.solar.ui.core.UIManager;
import karnickeldev.solar.ui.screens.BootGameState;
import karnickeldev.solar.ui.screens.LoadingScreen;
import karnickeldev.solar.ui.screens.MainMenuScreen;

import java.util.List;

/**
 * @author KarnickelDev
 * @since 04.07.2025
 **/
public class GameStateManager {

    private final Logger logger;

    private static final GameStateManager instance = new GameStateManager();
    private GameState current = BootGameState.getInstance();
    private GameState pending;

    private boolean transitioning = false;

    public static GameStateManager get() {
        return instance;
    }

    private GameStateManager() {
        logger = Logger.get("GSM");
    }

    public GameState getState() {
        return current;
    }

    public synchronized void requestStateLoading(GameState to) {
        requestStateLoading(to, false);
    }

    public synchronized void requestStateLoading(GameState to, boolean force) {
        if(to == null) throw new NullPointerException("GameState can't be null");
        if(to.getID() == GameStateID.BOOT || to.equals(BootGameState.getInstance())) {
            throw new IllegalStateException("Can't change back to Boot GameState");
        }

        // ignore duplicates
        if(!force) {
            if(current != null && to.getID() == current.getID()) return;
            if(pending != null && to.getID() == pending.getID()) return; // TODO: rethink
        }
        pending = to;

        logger.info("Starting transition: " + current.getID() + " to " + pending.getID());

        if(!transitioning) {
            transitioning = true;

            LoadingScreen loading = new LoadingScreen(this::finishTransition,
                List.of(current.postExitLoadingPlan(), to.preEnterLoadingPlan()));

            loading.setOnFailure((Throwable t) -> {
                synchronized (this) {
                    logger.error("loading failed: " + t.getMessage());
                    pending = null;
                    transitioning = false;
                    requestStateLoading(new MainMenuScreen(SolarMain.getInstance(), () -> UIManager.get().showMessage(t.getMessage())), true);
                }
            });

            SolarMain.getInstance().setScreen(loading);
        }

    }

    private synchronized void finishTransition() {
        try {
            try {
                current.exit();
            } catch (Exception e) {
                logger.error("exit failed: " + e.getMessage());
            }

            current = pending;
            pending = null;

            try {
                current.enter();
            } catch (Exception e) {
                logger.error("enter failed: " + e.getMessage());
            }
        } finally {
            transitioning = false;
            logger.info("transition done");
        }
    }

    public synchronized GameState getCurrent() {
        return current;
    }

    public synchronized boolean isTransitioning() {
        return transitioning;
    }

}
