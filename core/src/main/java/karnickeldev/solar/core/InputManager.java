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

    public static final char MAIN_MENU_INPUT = 0;

    private InputMultiplexer inputMultiplexer;
    private final InputProcessor[] inputProcessors = new InputProcessor[1];

    public InputManager() {
        inputMultiplexer = new InputMultiplexer();
    }

    public InputMultiplexer getInputMultiplexer() {return inputMultiplexer;}

    public void addInput(char inputType, InputProcessor processor) {
        inputMultiplexer.addProcessor(processor);
        inputProcessors[inputType] = processor;
    }

    public void removeInput(char inputType) {
        inputMultiplexer.removeProcessor(inputProcessors[inputType]);
    }

}
