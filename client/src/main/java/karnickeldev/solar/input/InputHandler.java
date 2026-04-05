package karnickeldev.solar.input;

/**
 * @author KarnickelDev
 * @since 05.04.2026
 **/
public interface InputHandler {

    /** runs every frame on main thread (after all other input events) */
    default void handleInput() {
        // nop
    }

    /**
     * Works like in LibGDX's InputProcessor
     * @return true to prevent event to bubble
     */
    default boolean keyDown(int keycode) {
        return false;
    }

    /**
     * Works like in LibGDX's InputProcessor
     * @return true to prevent event to bubble
     */
    default boolean keyUp(int keycode) {
        return false;
    }

    /**
     * Works like in LibGDX's InputProcessor
     * @return true to prevent event to bubble
     */
    default boolean keyTyped(char character) {
        return false;
    }

    /**
     * Works like in LibGDX's InputProcessor
     * @return true to prevent event to bubble
     */
    default boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    /**
     * Works like in LibGDX's InputProcessor
     * @return true to prevent event to bubble
     */
    default boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    /**
     * Works like in LibGDX's InputProcessor
     * @return true to prevent event to bubble
     */
    default boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    /**
     * Works like in LibGDX's InputProcessor
     * @return true to prevent event to bubble
     */
    default boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    /**
     * Works like in LibGDX's InputProcessor
     * @return true to prevent event to bubble
     */
    default boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    /**
     * Works like in LibGDX's InputProcessor
     * @return true to prevent event to bubble
     */
    default boolean scrolled(float amountX, float amountY) {
        return false;
    }

}
