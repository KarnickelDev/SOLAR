package karnickeldev.solar.ui.layers.dialog;

import com.badlogic.gdx.Input;
import karnickeldev.solar.ui.components.widgets.Message;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.core.UIExitReason;
import karnickeldev.solar.ui.core.UILayer;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author KarnickelDev
 * @since 05.04.2026
 **/
public class MessageLayer extends UILayer {

    private static final AtomicInteger refCounter = new AtomicInteger(0);

    private final String message;

    public MessageLayer(String message) {
        super("message-" + refCounter.getAndAdd(1));
        this.message = message;
    }

    @Override
    public boolean isModal() {
        return true;
    }

    @Override
    public void onEnter() {
        refCounter.incrementAndGet();
        addComponent("message", new Message(message, () -> UI.getUIManager().requestPop(this, UIExitReason.USER_CLOSE)));
    }

    @Override
    public void onExit(UIExitReason reason) {
        refCounter.decrementAndGet();
        removeComponent("message");
    }

    @Override
    public void onFocus() {
        showComponent("message");
    }

    @Override
    public void onBlur() {
        hideComponent("message");
    }

    @Override
    public void act(float delta) {

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
