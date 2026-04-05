package karnickeldev.solar.ui.layers.escapemenu;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.core.UIExitReason;
import karnickeldev.solar.ui.core.UILayer;

/**
 * @author KarnickelDev
 * @since 05.04.2026
 **/
public class EscapeMenuLayer extends UILayer {

    public EscapeMenuLayer() {
        super("escape_menu", new Stage(new FitViewport(UI.VIRTUAL_WIDTH, UI.VIRTUAL_HEIGHT)));
    }

    @Override
    public boolean isModal() {
        return true;
    }

    @Override
    public void onEnter() {
        addComponent("escape_menu", new EscapeMenu(() -> UI.getUIManager().requestPop(this, UIExitReason.USER_CLOSE)));
    }

    @Override
    public void onExit(UIExitReason reason) {
        removeComponent("escape_menu");
    }

    @Override
    public void onFocus() {
        showComponent("escape_menu");
    }

    @Override
    public void onBlur() {
        hideComponent("escape_menu");
    }

    @Override
    public boolean keyDown(int keycode) {
        if(keycode == Input.Keys.ESCAPE) {
            UI.getUIManager().requestPop(this, UIExitReason.USER_CLOSE);
            return true;
        }

        return false;
    }
}
