package karnickeldev.solar.input;

import com.badlogic.gdx.InputProcessor;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.core.UIInputManager;

import java.util.function.Function;

/**
 * @author KarnickelDev
 * @since 04.04.2026
 **/
public class InputManager implements InputProcessor {

    private final UIInputManager uiInputManager;
    private GameplayInputManager gameplayInputManager;

    public InputManager() {
        this.uiInputManager = new UIInputManager();
        gameplayInputManager = null;
    }

    /** Must be called every Frame*/
    public void pollInputs() {
        uiInputManager.handleInput();
        if(gameplayInputManager != null) {
            if(!UI.getUIManager().top().isModal() && ! UI.getUIManager().top().blocksInput()) gameplayInputManager.handleInput();
        }
    }

    private boolean route(Function<InputHandler, Boolean> function) {

        // UI first
        if(function.apply(uiInputManager)) return true;

        // gameplay
        if(gameplayInputManager != null) {
            if(function.apply(gameplayInputManager)) return true;
        }

        return false;
    }

    public void setGameplayInputManager(GameplayInputManager gameplayInputManager) {
        this.gameplayInputManager = gameplayInputManager;
    }

    public GameplayInputManager getGameplayInputManager() {
        return gameplayInputManager;
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
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return route(p -> p.touchCancelled(screenX, screenY, pointer, button));
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return route(p -> p.touchDragged(screenX, screenY, pointer));
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
