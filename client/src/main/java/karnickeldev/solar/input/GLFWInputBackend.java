package karnickeldev.solar.input;

import org.lwjgl.glfw.*;

import static org.lwjgl.glfw.GLFW.*;

/**
 * @author KarnickelDev
 * @since 19.06.2026
 **/
public final class GLFWInputBackend implements InputBackend {

    private final long window;

    private GLFWKeyCallback keyCallback;
    private GLFWMouseButtonCallback buttonCallback;
    private GLFWCursorPosCallback cursorPosCallback;
    private GLFWScrollCallback scrollCallback;
    private GLFWCharCallback charCallback;

    public GLFWInputBackend(long window) {
        this.window = window;
    }

    public void init(InputManager input) {

        keyCallback = GLFWKeyCallback.create((window, key, scancode, action, mods) -> {
            switch (action) {
                case GLFW_PRESS -> input.setKey(key, true);
                case GLFW_RELEASE -> input.setKey(key, false);
            }
        });

        buttonCallback = GLFWMouseButtonCallback.create((window, button, action, mods) -> {
           switch (action) {
               case GLFW_PRESS -> input.setButton(button, true);
               case GLFW_RELEASE -> input.setButton(button, false);
           }
        });

        cursorPosCallback = GLFWCursorPosCallback.create((window, x, y) -> {
            input.setMousePosition((float)x, (float)y);
        });

        scrollCallback = GLFWScrollCallback.create((window, xOffset, yOffset) -> {
            input.addScroll((float)xOffset, (float)yOffset);
        });

        charCallback = GLFWCharCallback.create((window, character) -> {
           if(character >= 0 && character < Character.MAX_VALUE) input.addChar((char)character);
        });

        glfwSetKeyCallback(window, keyCallback);
        glfwSetMouseButtonCallback(window, buttonCallback);
        glfwSetCursorPosCallback(window, cursorPosCallback);
        glfwSetScrollCallback(window, scrollCallback);
        glfwSetCharCallback(window, charCallback);
    }

    public void poll() {
        glfwPollEvents();
    }

    @Override
    public void dispose() {
        if(keyCallback != null) keyCallback.free();

        if(buttonCallback != null) buttonCallback.free();

        if(cursorPosCallback != null) cursorPosCallback.free();

        if(scrollCallback != null) scrollCallback.free();
    }

}
