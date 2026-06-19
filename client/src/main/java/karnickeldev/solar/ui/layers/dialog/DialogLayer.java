package karnickeldev.solar.ui.layers.dialog;

import karnickeldev.solar.ui.core.UIExitReason;
import karnickeldev.solar.ui.core.UILayer;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author KarnickelDev
 * @since 05.04.2026
 **/
public class DialogLayer extends UILayer {

    private static final AtomicInteger refCounter = new AtomicInteger(0);

    public DialogLayer() {
        super("dialog-" + refCounter.get());
    }

    @Override
    public void onEnter() {
        refCounter.incrementAndGet();
    }

    @Override
    public void onExit(UIExitReason reason) {
        refCounter.decrementAndGet();
    }

    @Override
    public void onFocus() {

    }

    @Override
    public void onBlur() {

    }

    @Override
    public void act(float delta) {

    }
}
