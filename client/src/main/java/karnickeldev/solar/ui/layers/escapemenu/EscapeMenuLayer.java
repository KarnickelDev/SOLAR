package karnickeldev.solar.ui.layers.escapemenu;

import karnickeldev.solar.input.Keys;
import karnickeldev.solar.ui.components.Canvas;
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
        super("escape_menu");
        escapemenu = new EscapeMenu(() -> UI.getUIManager().requestPop(this, UIExitReason.USER_CLOSE));

        getCanvas().add(escapemenu, new Canvas.CanvasSlot().anchor(Canvas.Anchor.CENTER).fixedSize(360,500));
    }

    @Override
    public boolean isModal() {
        return true;
    }

    @Override
    public void onEnter() {

    }

    @Override
    public void onExit(UIExitReason reason) {

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
    public void act(float delta) {

    }

    @Override
    public boolean keyDown(int keycode) {
        if(keycode == Keys.ESCAPE) {
            UI.getUIManager().requestPop(this, UIExitReason.USER_CLOSE);
            return true;
        }

        return false;
    }
}
