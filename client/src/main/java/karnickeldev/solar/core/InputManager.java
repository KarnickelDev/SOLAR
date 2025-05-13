package karnickeldev.solar.core;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;

/**
 * Project: SOLAR
 *
 * @author KarnickelDev
 * @since 29.10.2024
 */
public class InputManager {


    private final InputMultiplexer inputMultiplexer;

    public InputManager() {
        inputMultiplexer = new InputMultiplexer();
    }

    public InputMultiplexer getInputMultiplexer() {
        return inputMultiplexer;
    }

    public void addInput(InputProcessor processor) {
        inputMultiplexer.addProcessor(processor);
    }

    public void removeInput(int processor) {
        inputMultiplexer.removeProcessor(processor);
    }

}
