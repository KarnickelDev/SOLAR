package karnickeldev.solar.ui.core;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import karnickeldev.solar.input.InputHandler;

import java.util.function.Function;

/**
 * @author KarnickelDev
 * @since 04.04.2026
 **/
public class UIInputManager implements InputHandler {

    /** Routing logic, i.e. which layers gets input */
    private boolean route(Function<UILayer, Boolean> dispatchLayer, Function<Stage, Boolean> dispatchStage) {
        for(UILayer layer: UIManager.get().getLayersTopToBottom()) {
            if(!layer.receivesInput()) continue;

            if(dispatchLayer != null) {
                boolean handled = dispatchLayer.apply(layer);
                if(handled) return true;
            }

            if(dispatchStage != null) {
                boolean handled = dispatchStage.apply(layer.getStage());
                if(handled) return true;
            }

            if(layer.isModal() || layer.blocksInput()) return true;
        }
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        return route(
            layer -> layer.keyDown(keycode),
            stage -> stage.keyDown(keycode)
        );
    }

    @Override
    public boolean keyUp(int keycode) {
        return route(
            layer -> layer.keyUp(keycode),
            stage -> stage.keyUp(keycode)
        );
    }

    @Override
    public boolean keyTyped(char character) {
        return route(
            layer -> layer.keyTyped(character),
            stage -> stage.keyTyped(character)
        );
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return route(
            layer -> layer.touchDown(screenX, screenY, pointer, button),
            stage -> stage.touchDown(screenX, screenY, pointer, button)
        );
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return route(
            layer -> layer.touchUp(screenX, screenY, pointer, button),
            stage ->  stage.touchUp(screenX, screenY, pointer, button)
        );
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return route(
            layer -> layer.touchCancelled(screenX, screenY, pointer, button),
            stage ->  stage.touchCancelled(screenX, screenY, pointer, button)
        );
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return route(
            layer -> layer.touchDragged(screenX, screenY, pointer),
            stage ->  stage.touchDragged(screenX, screenY, pointer)
        );
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return route(
            layer -> layer.mouseMoved(screenX, screenY),
            stage -> stage.mouseMoved(screenX, screenY)
        );
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return route(
            layer -> layer.scrolled(amountX, amountY),
            stage -> stage.scrolled(amountX, amountY)
        );
    }
}
