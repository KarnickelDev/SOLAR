package karnickeldev.solar.core.gamestates;

/**
 * @author KarnickelDev
 * @since 04.07.2025
 **/
public interface GameState {

    /**
     * Runs before enter(), during LoadingScreen
     */
    LoadingPlan preEnterLoadingPlan();

    /**
     * Runs when entering a GameState, after LoadingScreen
     */
    void enter();

    /**
     * Runs when exiting a GameState, before LoadingScreen
     */
    void exit();

    /**
     * Runs after exit(), during LoadingScreen
     */
    LoadingPlan postExitLoadingPlan();

    GameStateID getID();
}
