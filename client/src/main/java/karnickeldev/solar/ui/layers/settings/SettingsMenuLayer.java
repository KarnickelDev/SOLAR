package karnickeldev.solar.ui.layers.settings;

import karnickeldev.solar.input.Keys;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.core.UIExitReason;
import karnickeldev.solar.ui.core.UILayer;

/**
 * @author KarnickelDev
 * @since 05.04.2026
 **/
public class SettingsMenuLayer extends UILayer {

    SettingsMenu settingsMenu = new SettingsMenu(() -> UI.getUIManager().requestPop(this, UIExitReason.USER_CLOSE));

    public SettingsMenuLayer() {
        super("settings_menu");
    }

    @Override
    public boolean isModal() {
        return true;
    }

    @Override
    public void onEnter() {
        addComponent("settings_menu", settingsMenu);
    }

    @Override
    public void onExit(UIExitReason reason) {
        removeComponent("settings_menu");
    }

    @Override
    public void onFocus() {
        showComponent("settings_menu");
    }

    @Override
    public void onBlur() {
        hideComponent("settings_menu");
    }

    @Override
    public void act(float delta) {

    }

    @Override
    public boolean keyDown(int keycode) {
        if(keycode == Keys.ESCAPE) {
            UI.getUIManager().pop(UIExitReason.USER_CLOSE);
            return true;
        }

        return false;
    }
}
