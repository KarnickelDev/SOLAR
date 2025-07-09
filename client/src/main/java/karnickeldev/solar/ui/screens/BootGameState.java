package karnickeldev.solar.ui.screens;

import karnickeldev.solar.core.gamestates.GameState;
import karnickeldev.solar.core.gamestates.GameStateID;

/**
 * @author : KarnickelDev
 * @since : 09.07.2025
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
    public void render(float delta) {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public GameStateID getID() {
        return GameStateID.BOOT;
    }
}
