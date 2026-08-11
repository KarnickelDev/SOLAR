package karnickeldev.solar.core;

import com.badlogic.gdx.Gdx;
import karnickeldev.solar.input.GLFWInputBackend;
import karnickeldev.solar.input.InputManager;
import karnickeldev.solar.render.shader.ShaderManager;

/**
 * @author KarnickelDev
 * @since 20.06.2026
 **/
public final class Engine {

    private static InputManager input;

    private static final ShaderManager shaderManager = new ShaderManager();

    private static boolean init = false;

    public static void init(long windowHandle) {
        if(init) throw new IllegalStateException("Engine already initialized");
        init = true;

        // setup input handling
        input = new InputManager(new GLFWInputBackend(windowHandle));
    }

    public static InputManager input() {
        if(!init) throw new IllegalStateException("Engine not initialized");
        return input;
    }

    public static void dispose() {
        if(!init) return;

        input.dispose();
        input = null;
        init = false;
    }

    public static ShaderManager shaderManager() {
        if(!init) throw new IllegalStateException("Engine not initialized");
        return shaderManager;
    }

    public static float getDeltaTime() {
        return Gdx.graphics.getDeltaTime();
    }

    public static int getWidth() {
        return Gdx.graphics.getWidth();
    }

    public static int getHeight() {
        return Gdx.graphics.getHeight();
    }

}
