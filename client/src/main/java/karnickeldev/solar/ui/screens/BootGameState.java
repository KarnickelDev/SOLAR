package karnickeldev.solar.ui.screens;

import karnickeldev.solar.core.gamestates.GameState;
import karnickeldev.solar.core.gamestates.GameStateID;

/**
 * @author KarnickelDev
 * @since 09.07.2025
**/
public class BootGameState implements GameState {

    public static BootGameState instance = new BootGameState();
    public static BootGameState getInstance() {
        return instance;
    }

    private BootGameState() {}

    @Override
    public void enter() {

    }

    @Override
    public void exit() {

    }

    @Override
    public GameStateID getID() {
        return GameStateID.BOOT;
    }
}
