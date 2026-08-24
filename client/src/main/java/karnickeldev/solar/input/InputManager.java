package karnickeldev.solar.input;

import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Collects native input events and dispatches them in backend order on the main thread. */
public final class InputManager implements Disposable {

    private static final int MAX_KEYS = 512;
    private static final int MAX_BUTTONS = 16;

    private enum EventType {
        KEY_DOWN,
        KEY_UP,
        BUTTON_DOWN,
        BUTTON_UP,
        POINTER_MOVE,
        SCROLL,
        CHARACTER,
        CANCEL
    }

    private record QueuedEvent(EventType type, int code, float x, float y) {}

    /** Logical state visible to handlers while events are dispatched. */
    private final boolean[] keysDown = new boolean[MAX_KEYS];
    private final boolean[] keysPressed = new boolean[MAX_KEYS];
    private final boolean[] keysReleased = new boolean[MAX_KEYS];

    private final boolean[] buttonsDown = new boolean[MAX_BUTTONS];
    private final boolean[] buttonsPressed = new boolean[MAX_BUTTONS];
    private final boolean[] buttonsReleased = new boolean[MAX_BUTTONS];

    /** Native state used to reject duplicate callbacks before dispatch. */
    private final boolean[] backendKeysDown = new boolean[MAX_KEYS];
    private final boolean[] backendButtonsDown = new boolean[MAX_BUTTONS];

    private final StringBuilder typedChars = new StringBuilder();

    private float mouseX, mouseY;
    private float backendMouseX, backendMouseY;

    private float scrollDX, scrollDY;

    private final List<QueuedEvent> eventQueue = new ArrayList<>();

    private final List<InputHandler> inputHandlers = new ArrayList<>(4);

    private final InputBackend backend;
    private boolean disposed;

    public InputManager(InputBackend backend) {
        this.backend = Objects.requireNonNull(backend, "backend");
        this.backend.init(this);
    }

    /** Clears per-frame observations. Call before polling and dispatching the next frame. */
    private void beginFrame() {
        Arrays.fill(keysPressed, false);
        Arrays.fill(keysReleased, false);
        Arrays.fill(buttonsPressed, false);
        Arrays.fill(buttonsReleased, false);
        scrollDX = 0;
        scrollDY = 0;
        typedChars.setLength(0);
    }

    private void poll() {
        backend.poll();
    }

    private void dispatchEvents() {
        // Listener mutations from a callback take effect on the next dispatch.
        InputHandler[] handlers = inputHandlers.toArray(InputHandler[]::new);
        List<QueuedEvent> events = List.copyOf(eventQueue);
        eventQueue.clear();

        for(QueuedEvent event : events) {
            switch(event.type()) {
                case KEY_DOWN -> dispatchKeyDown(handlers, event.code());
                case KEY_UP -> dispatchKeyUp(handlers, event.code());
                case BUTTON_DOWN -> dispatchButtonDown(handlers, event.code(), event.x(), event.y());
                case BUTTON_UP -> dispatchButtonUp(handlers, event.code(), event.x(), event.y());
                case POINTER_MOVE -> dispatchPointerMove(handlers, event.x(), event.y());
                case SCROLL -> dispatchScroll(handlers, event.x(), event.y());
                case CHARACTER -> dispatchCharacter(handlers, (char) event.code());
                case CANCEL -> dispatchCancellation(handlers);
            }
        }

        for(InputHandler handler : handlers) {
            handler.handleInput();
        }
    }

    public void update() {
        beginFrame();
        poll();
        dispatchEvents();
    }

    @Override
    public void dispose() {
        if(disposed) return;
        disposed = true;

        clearListeners();
        backend.dispose();
    }

    public void addListener(InputHandler handler) {
        Objects.requireNonNull(handler, "handler");
        if(!inputHandlers.contains(handler)) inputHandlers.add(handler);
    }

    public void removeListener(InputHandler handler) {
        inputHandlers.remove(handler);
    }

    public void clearListeners() {
        inputHandlers.clear();
    }

    private void dispatchKeyDown(InputHandler[] handlers, int key) {
        keysDown[key] = true;
        keysPressed[key] = true;

        for(InputHandler handler : handlers) {
            if(handler.keyDown(key)) break;
        }
    }

    private void dispatchKeyUp(InputHandler[] handlers, int key) {
        keysDown[key] = false;
        keysReleased[key] = true;

        for(InputHandler handler : handlers) {
            if(handler.keyUp(key)) break;
        }
    }

    private void dispatchButtonDown(InputHandler[] handlers, int button, float x, float y) {
        mouseX = x;
        mouseY = y;
        buttonsDown[button] = true;
        buttonsPressed[button] = true;

        for(InputHandler handler : handlers) {
            if(handler.touchDown(Math.round(x), Math.round(y), 0, button)) break;
        }
    }

    private void dispatchButtonUp(InputHandler[] handlers, int button, float x, float y) {
        mouseX = x;
        mouseY = y;
        buttonsDown[button] = false;
        buttonsReleased[button] = true;

        for(InputHandler handler : handlers) {
            if(handler.touchUp(Math.round(x), Math.round(y), 0, button)) break;
        }
    }

    private void dispatchPointerMove(InputHandler[] handlers, float x, float y) {
        mouseX = x;
        mouseY = y;

        for(InputHandler handler : handlers) {
            if(handler.mouseMoved(Math.round(x), Math.round(y))) break;
        }

        for(int button = 0; button < MAX_BUTTONS; button++) {
            if(!buttonsDown[button]) continue;

            for(InputHandler handler : handlers) {
                if(handler.touchDragged(Math.round(x), Math.round(y), 0, button)) break;
            }
        }
    }

    private void dispatchScroll(InputHandler[] handlers, float dx, float dy) {
        scrollDX += dx;
        scrollDY += dy;

        for(InputHandler handler : handlers) {
            if(handler.scrolled(dx, dy)) break;
        }
    }

    private void dispatchCharacter(InputHandler[] handlers, char character) {
        typedChars.append(character);

        for(InputHandler handler : handlers) {
            if(handler.keyTyped(character)) break;
        }
    }

    private void dispatchCancellation(InputHandler[] handlers) {

        // cancel first, so if needed, elements can determine that the following releases are synthetic
        for(InputHandler handler : handlers) {
            handler.inputCancelled();
        }

        for(int key = 0; key < MAX_KEYS; key++) {
            if(!keysDown[key]) continue;
            dispatchKeyUp(handlers, key);
        }

        for(int button = 0; button < MAX_BUTTONS; button++) {
            if(!buttonsDown[button]) continue;
            dispatchButtonUp(handlers, button, mouseX, mouseY);
        }
    }

    /** Clears held native state and queues cancellation at its position in the event stream. */
    public void cancelInput() {
        Arrays.fill(backendKeysDown, false);
        Arrays.fill(backendButtonsDown, false);

        if(eventQueue.isEmpty() || eventQueue.getLast().type() != EventType.CANCEL) {
            appendEvent(EventType.CANCEL, 0, 0, 0);
        }
    }

    public String getChars() {
        return typedChars.toString();
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

    public boolean isButtonDown(int button) {
        return button >= 0 && button < MAX_BUTTONS && buttonsDown[button];
    }

    public boolean wasButtonPressed(int button) {
        return button >= 0 && button < MAX_BUTTONS && buttonsPressed[button];
    }

    public boolean wasButtonReleased(int button) {
        return button >= 0 && button < MAX_BUTTONS && buttonsReleased[button];
    }

    public float mouseX() {
        return mouseX;
    }

    public float mouseY() {
        return mouseY;
    }

    public double scrollDeltaX() {
        return scrollDX;
    }

    public double scrollDeltaY() {
        return scrollDY;
    }

    // Backend API: append events, avoiding duplicates and merging if possible

    private void appendEvent(EventType type, int code, float x, float y) {
        eventQueue.add(new QueuedEvent(type, code, x, y));
    }

    void setKey(int key, boolean down) {
        if(key < 0 || key >= MAX_KEYS || backendKeysDown[key] == down) return;

        backendKeysDown[key] = down;
        appendEvent(down ? EventType.KEY_DOWN : EventType.KEY_UP, key, 0, 0);
    }

    void setButton(int button, boolean down) {
        if(button < 0 || button >= MAX_BUTTONS || backendButtonsDown[button] == down) return;

        backendButtonsDown[button] = down;
        appendEvent(down ? EventType.BUTTON_DOWN : EventType.BUTTON_UP, button, backendMouseX, backendMouseY);
    }

    void setMousePosition(float x, float y) {
        if(backendMouseX == x && backendMouseY == y) return;

        backendMouseX = x;
        backendMouseY = y;

        // Coalesce only adjacent moves, so the order relative to other event types is preserved.
        // i.e: click -> button -> click DOES NOT become button -> click,
        // but button -> click -> click DOES become button -> click
        int lastIndex = eventQueue.size() - 1;
        if(lastIndex >= 0 && eventQueue.get(lastIndex).type() == EventType.POINTER_MOVE) {
            eventQueue.set(lastIndex, new QueuedEvent(EventType.POINTER_MOVE, 0, x, y));
        } else {
            appendEvent(EventType.POINTER_MOVE, 0, x, y);
        }
    }

    void addScroll(float xOffset, float yOffset) {
        float dx = -xOffset;
        float dy = -yOffset;

        int lastIndex = eventQueue.size() - 1;
        if(lastIndex >= 0 && eventQueue.get(lastIndex).type() == EventType.SCROLL) {
            QueuedEvent previous = eventQueue.get(lastIndex);
            eventQueue.set(lastIndex, new QueuedEvent(EventType.SCROLL, 0, previous.x() + dx, previous.y() + dy));
        } else {
            appendEvent(EventType.SCROLL, 0, dx, dy);
        }
    }

    void addChar(char character) {
        appendEvent(EventType.CHARACTER, character, 0, 0);
    }

    void addCodePoint(int codePoint) {
        if(!Character.isValidCodePoint(codePoint)) return;

        for(char character : Character.toChars(codePoint)) {
            addChar(character);
        }
    }
}
