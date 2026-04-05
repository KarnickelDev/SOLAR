package karnickeldev.solar.ui.layers.mainmenu;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import karnickeldev.solar.ui.components.DebugToolTip;
import karnickeldev.solar.ui.layers.settings.SettingsMenuLayer;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.core.UIExitReason;
import karnickeldev.solar.ui.core.UILayer;

/**
 * @author KarnickelDev
 * @since 05.04.2026
 **/
public class MainMenuLayer extends UILayer {

    private final MainMenu mainMenu = new MainMenu(this);

    public MainMenuLayer() {
        super("main_menu", new Stage(new FitViewport(UI.VIRTUAL_WIDTH, UI.VIRTUAL_HEIGHT)));
    }

    @Override
    public boolean isModal() {
        return true;
    }

    @Override
    public void onEnter() {
        addForceComponent("debug", new DebugToolTip(true));
        addComponent("main_menu", mainMenu);
        addComponent("multiplayer_menu", new MultiplayerMenu());
    }

    @Override
    public void onExit(UIExitReason reason) {
        removeComponent("debug");
        removeComponent("main_menu");
        removeComponent("multiplayer_menu");
    }

    @Override
    public void onFocus() {
        showComponent("debug");
        showComponent("main_menu");
        hideComponent("multiplayer_menu");

        getStage().setKeyboardFocus(mainMenu.getGroup());
    }

    @Override
    public void onBlur() {
        getStage().unfocusAll();
    }

    @Override
    public boolean keyDown(int keycode) {
        if(keycode == Input.Keys.ESCAPE) {
            UI.getUIManager().push(new SettingsMenuLayer());
            return true;
        }

        return false;
    }

}
