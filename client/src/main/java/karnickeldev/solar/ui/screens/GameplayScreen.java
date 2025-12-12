package karnickeldev.solar.ui.screens;

import karnickeldev.solar.core.gamestates.GameState;
import karnickeldev.solar.core.gamestates.GameStateID;
import karnickeldev.solar.core.gamestates.LoadingPlan;
import karnickeldev.solar.core.gamestates.LoadingPlanBuilder;

/**
 * @author KarnickelDev
 * @since 04.07.2025
 **/
public class GameplayScreen implements GameState {

    @Override
    public LoadingPlan preEnterLoadingPlan() {
        return LoadingPlanBuilder.empty();
    }

    @Override
    public void enter() {

    }

    @Override
    public void exit() {

    }

    @Override
    public LoadingPlan postExitLoadingPlan() {
        return LoadingPlanBuilder.empty();
    }

    @Override
    public GameStateID getID() {
        return null;
    }
}
