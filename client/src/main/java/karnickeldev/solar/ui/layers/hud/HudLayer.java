package karnickeldev.solar.ui.layers.hud;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.ui.components.DebugToolTip;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.core.UIExitReason;
import karnickeldev.solar.ui.core.UILayer;
import karnickeldev.solar.ui.layers.hud.chat.ChatWindow;
import karnickeldev.solar.ui.layers.hud.game.DateDisplay;

/**
 * @author KarnickelDev
 * @since 04.04.2026
 **/
public class HudLayer extends UILayer {

    public static HudLayer INSTANCE = new HudLayer();

    private boolean wasGamePaused = false;

    public ChatWindow chatActor;

    public HudLayer() {
        super("hud", new Stage(new FitViewport(UI.VIRTUAL_WIDTH, UI.VIRTUAL_HEIGHT)));

        chatActor = new ChatWindow();
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void onEnter() {
        addForceComponent("debug", new DebugToolTip(false));
        addComponent("date_display", new DateDisplay());

        addComponent("chat_window", chatActor);
    }

    @Override
    public void onExit(UIExitReason reason) {
        removeComponent("debug");
        removeComponent("date_display");

        removeComponent("chat_window");
    }

    @Override
    public void onFocus() {
        showComponent("debug");
        showComponent("date_display");

        if(GameContext.get().isSingleplayer()) GameContext.get().getClock().getSimSpeedController().requestPause(wasGamePaused);
    }

    @Override
    public void onBlur() {
        wasGamePaused = GameContext.get().getClock().isPaused();
        if(GameContext.get().isSingleplayer()) GameContext.get().getClock().getSimSpeedController().requestPause(true);
    }

    @Override
    public boolean blocksInput() {
        return chatActor.isActive();
    }

    @Override
    public boolean keyDown(int keycode) {
        if(keycode == Input.Keys.ESCAPE) {
            if(chatActor.isActive()) {
                chatActor.deactivate();
                return true;
            }
        }

        if(keycode == Input.Keys.ENTER) {
            if(blocksInput()) {
                chatActor.handleInput();
            } else {
                chatActor.activate();
            }
            return true;
        }

        return false;
    }

}
