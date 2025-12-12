package karnickeldev.solar.ui.screens;

import karnickeldev.solar.core.gamestates.GameState;
import karnickeldev.solar.core.gamestates.GameStateID;
import karnickeldev.solar.core.gamestates.LoadingPlan;
import karnickeldev.solar.core.gamestates.LoadingPlanBuilder;

/**
 * This class is just an empty Placeholder used by the GameStateManager internally
 * @author KarnickelDev
 * @since 09.07.2025
**/
public class BootGameState implements GameState {

    public static BootGameState INSTANCE = new BootGameState();
    public static BootGameState getInstance() {
        return INSTANCE;
    }

    private BootGameState() {}

    @Override
    public LoadingPlan preEnterLoadingPlan() {
        return LoadingPlanBuilder.empty();
    }

    @Override
    public void enter() {
        // nop
    }

    @Override
    public void exit() {
        // nop
    }

    @Override
    public LoadingPlan postExitLoadingPlan() {
        return LoadingPlanBuilder.empty();
    }

    @Override
    public GameStateID getID() {
        return GameStateID.BOOT;
    }
}
