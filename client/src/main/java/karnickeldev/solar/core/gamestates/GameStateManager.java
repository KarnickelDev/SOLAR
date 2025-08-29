package karnickeldev.solar.core.gamestates;

import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.ui.screens.BootGameState;

/**
 * @author : KarnickelDev
 * @since : 04.07.2025
 **/
public class GameStateManager {

    private static final GameStateManager instance = new GameStateManager();
    private GameState current = BootGameState.getInstance();

    public static GameStateManager get() {
        return instance;
    }

    private GameStateManager() {}

    public GameState getState() {
        return current;
    }

    public void changeState(GameState to) {
        if(to == null) throw new NullPointerException("GameState can't be null");
        if(to.getID() == GameStateID.BOOT || to.equals(BootGameState.getInstance())) {
            throw new IllegalStateException("Can't change back to Boot GameState");
        }

        if(current != null) current.exit();

        current = to;
        //SolarMain.getInstance().setScreen(new GameStateScreen(current));
        current.enter();
    }



}
