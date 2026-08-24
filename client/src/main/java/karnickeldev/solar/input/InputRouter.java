package karnickeldev.solar.input;

import karnickeldev.solar.ui.core.UIManager;

import java.util.function.Function;

/**
 * @author KarnickelDev
 * @since 20.06.2026
 **/
public final class InputRouter implements InputHandler {

    private UIManager uiInputManager;
    private GameplayInputManager gameplayInputManager;

    public InputRouter() {}

    public void setUiInputManager(UIManager uiInputManager) {
        this.uiInputManager = uiInputManager;
    }

    public UIManager getUiInputManager() {
        return uiInputManager;
    }

    public void setGameplayInputManager(GameplayInputManager gameplayInputManager) {
        this.gameplayInputManager = gameplayInputManager;
    }

    public GameplayInputManager getGameplayInputManager() {
        return gameplayInputManager;
    }

    @Override
    public void handleInput() {
        if(uiInputManager != null) {
            uiInputManager.handleInput();
        }

        if(gameplayInputManager != null && !uiBlocksGameplayInput()) {
            gameplayInputManager.handleInput();
        }
    }

    @Override
    public void inputCancelled() {
        if(uiInputManager != null) uiInputManager.inputCancelled();
        if(gameplayInputManager != null) gameplayInputManager.inputCancelled();
    }

    private boolean uiBlocksGameplayInput() {
        return uiInputManager != null && uiInputManager.blocksGameplayInput();
    }

    private boolean route(Function<InputHandler, Boolean> function) {

        // UI first
        if(uiInputManager != null) {
            if(function.apply(uiInputManager)) return true;
            if(uiBlocksGameplayInput()) return true;
        }

        // gameplay
        if(gameplayInputManager != null) {
            if(function.apply(gameplayInputManager)) return true;
        }

        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        return route(p -> p.keyDown(keycode));
    }

    @Override
    public boolean keyUp(int keycode) {
        return route(p -> p.keyUp(keycode));
    }

    @Override
    public boolean keyTyped(char character) {
        return route(p -> p.keyTyped(character));
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return route(p -> p.touchDown(screenX, screenY, pointer, button));
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return route(p -> p.touchUp(screenX, screenY, pointer, button));
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer, int button) {
        return route(p -> p.touchDragged(screenX, screenY, pointer, button));
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return route(p -> p.mouseMoved(screenX, screenY));
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return route(p -> p.scrolled(amountX, amountY));
    }



}
