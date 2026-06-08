package karnickeldev.solar.ui.layers.escapemenu;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import karnickeldev.solar.ui.components.Canvas;
import karnickeldev.solar.ui.components.UIContainer;
import karnickeldev.solar.ui.components.UILayout;
import karnickeldev.solar.ui.components.UILayoutEngine;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.core.UIExitReason;
import karnickeldev.solar.ui.core.UILayer;

/**
 * @author KarnickelDev
 * @since 05.04.2026
 **/
public class EscapeMenuLayer extends UILayer {

    private final EscapeMenu escapemenu;

    public EscapeMenuLayer() {
        super("escape_menu", new Stage(new ScreenViewport()));
        escapemenu = new EscapeMenu(() -> UI.getUIManager().requestPop(this, UIExitReason.USER_CLOSE));

        getCanvas().add(escapemenu, new Canvas.CanvasSlot().anchor(Canvas.Anchor.CENTER).fixedSize(300,400));
    }

    @Override
    public boolean isModal() {
        return true;
    }

    @Override
    public void onEnter() {

    }

    @Override
    public void resize(int width, int height) {
        escapemenu.invalidateLayout();
    }

    @Override
    public void onExit(UIExitReason reason) {
        removeElement(escapemenu);
    }

    @Override
    public void onFocus() {
        escapemenu.setVisible(true);
    }

    @Override
    public void onBlur() {
        escapemenu.setVisible(false);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            if (!escapemenu.hit(screenX, screenY)) return false;
            InputEvent e = new InputEvent();
            e.setButton(button);
            e.setType(InputEvent.Type.touchDown);
            e.setStageX(screenX);
            e.setStageY(screenY);
            escapemenu.handleInput(e);
        }
        return false;
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
