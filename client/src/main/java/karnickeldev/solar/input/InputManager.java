package karnickeldev.solar.input;

import com.badlogic.gdx.utils.Disposable;

import java.util.*;

/**
 * @author KarnickelDev
 * @since 19.06.2026
 **/
public final class InputManager implements Disposable {

    private static final short MAX_KEYS = 512;
    private static final byte MAX_BUTTONS = 8;

    private static final int MAX_POINTERS = 4;
    private final boolean[] pointerDown = new boolean[MAX_POINTERS];

    private final boolean[] keysDown = new boolean[MAX_KEYS];
    private final boolean[] keysPressed = new boolean[MAX_KEYS];
    private final boolean[] keysReleased = new boolean[MAX_KEYS];

    private final boolean[] buttonsDown = new boolean[MAX_BUTTONS];
    private final boolean[] buttonsPressed = new boolean[MAX_BUTTONS];
    private final boolean[] buttonsReleased = new boolean[MAX_BUTTONS];

    private final StringBuilder typedChars = new StringBuilder();

    private float mouseX, mouseY;
    private boolean mouseMoved = false;

    private float scrollX, scrollY;
    private float scrollDX, scrollDY;

    private final List<InputHandler> inputHandlers = new ArrayList<>(4);

    private final InputBackend backend;
    private boolean disposed;

    public InputManager(InputBackend backend) {
        this.backend = Objects.requireNonNull(backend, "backend");
        this.backend.init(this);
    }

    public void beginFrame() {
        Arrays.fill(keysPressed, false);
        Arrays.fill(keysReleased, false);
        Arrays.fill(buttonsPressed, false);
        Arrays.fill(buttonsReleased, false);
        scrollDX = 0;
        scrollDY = 0;
        mouseMoved = false;
    }

    public void poll() {
        backend.poll();
    }

    @Override
    public void dispose() {
        if(disposed) return;
        disposed = true;

        clearListeners();
        backend.dispose();
    }

    public void addListener(InputHandler handler) {
        inputHandlers.add(handler);
    }

    public void removeListener(InputHandler handler) {
        inputHandlers.remove(handler);
    }

    public void clearListeners() {
        inputHandlers.clear();
    }

    public void dispatchEvents() {
        char[] chars = typedChars.toString().toCharArray();
        typedChars.setLength(0);

        if(mouseMoved) {
            for(InputHandler handler: inputHandlers) {
                if(handler.mouseMoved((int)Math.round(mouseX), (int)Math.round(mouseY))) break;
            }
        }

        if(scrollDX != 0 || scrollDY != 0) {
            for(InputHandler handler: inputHandlers) {
                if(handler.scrolled(Math.round(scrollDX), Math.round(scrollDY))) break;
            }
        }

        for(char c: chars) {
            for(InputHandler handler: inputHandlers) {
                if(handler.keyTyped(c)) break;
            }
        }

        // process buttons
        for(int button = 0; button < MAX_BUTTONS; button++) {
            for(InputHandler handler: inputHandlers) {
                if (wasButtonPressed(button)) {
                    if(handler.touchDown((int) mouseX, (int) mouseY, 0, button)) break;
                }
                if (wasButtonReleased(button)) {
                    if(handler.touchUp((int) mouseX, (int) mouseY, 0, button)) break;
                }
            }
        }

        // process keys
        for(int key = 0; key < MAX_KEYS; key++) {
            for(InputHandler handler: inputHandlers) {
                if (wasKeyPressed(key)) {
                    if(handler.keyDown(key)) break;
                }

                if (wasKeyReleased(key)) {
                    if(handler.keyUp(key)) break;
                }
            }
        }

        for(InputHandler handler: inputHandlers) {
            handler.handleInput();
        }

    }

    // ----------------------------------------------------
    // Keyboard
    // ----------------------------------------------------

    public String getChars() {
        String result = typedChars.toString();
        typedChars.setLength(0);
        return result;
    }

    public boolean isKeyDown(int key) {
        return key >= 0 && key < MAX_KEYS && keysDown[key];
    }

    public boolean wasKeyPressed(int key) {
        return key >= 0 && key < MAX_KEYS && keysPressed[key];
    }

    public boolean wasKeyReleased(int key) {
        return key >= 0 && key < MAX_KEYS && keysReleased[key];
    }

    // ----------------------------------------------------
    // Mouse buttons
    // ----------------------------------------------------

    public boolean isButtonDown(int button) {
        return button >= 0 && button < MAX_BUTTONS && buttonsDown[button];
    }

    public boolean wasButtonPressed(int button) {
        return button >= 0 && button < MAX_BUTTONS && buttonsPressed[button];
    }

    public boolean wasButtonReleased(int button) {
        return button >= 0 && button < MAX_BUTTONS && buttonsReleased[button];
    }

    // ----------------------------------------------------
    // Mouse
    // ----------------------------------------------------

    public float mouseX() {
        return mouseX;
    }

    public float mouseY() {
        return mouseY;
    }

    public double scrollX() {
        return scrollX;
    }

    public double scrollY() {
        return scrollY;
    }

    public double scrollDeltaX() {
        return scrollDX;
    }

    public double scrollDeltaY() {
        return scrollDY;
    }

    // ----------------------------------------------------
    // Internal
    // ----------------------------------------------------

    private void updatePointerMove(int pointer, float x, float y) {

        if (pointer >= MAX_POINTERS) return;

        if (pointerDown[pointer]) {

            // ALWAYS emit dragged if down
            for (InputHandler handler : inputHandlers) {
                if (handler.touchDragged((int)x, (int)y, pointer)) {
                    break;
                }
            }
        }

    }

    void setKey(int key, boolean down) {
        if(key < 0 || key >= MAX_KEYS) return;

        if(down) {
            if(!keysDown[key]) {
                keysPressed[key] = true;
            }
            keysDown[key] = true;
        } else {
            if(keysDown[key]) {
                keysReleased[key] = true;
            }
            keysDown[key] = false;
        }
    }

    void setButton(int button, boolean down) {
        if(button < 0 || button >= MAX_BUTTONS) return;

        if (down) {

            if (!buttonsDown[button]) {
                buttonsPressed[button] = true;
            }

            buttonsDown[button] = true;

            int pointer = 0; // mouse pointer

            pointerDown[pointer] = true;
        } else {

            if (buttonsDown[button]) {
                buttonsReleased[button] = true;
            }

            buttonsDown[button] = false;

            int pointer = 0;

            pointerDown[pointer] = false;
        }
    }

    void setMousePosition(float x, float y) {
        if (mouseX == x && mouseY == y) return;

        mouseX = x;
        mouseY = y;
        mouseMoved = true;

        // treat mouse as pointer 0 (typical desktop case)
        updatePointerMove(0, x, y);
    }

    void addScroll(float xOffset, float yOffset) {
        scrollX -= xOffset;
        scrollY -= yOffset;

        scrollDX -= xOffset;
        scrollDY -= yOffset;
    }

    void addChar(char c) {
        typedChars.append(c);
    }
}
