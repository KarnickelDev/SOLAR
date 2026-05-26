package karnickeldev.solar.ui.layers.hud.chat;

import karnickeldev.solar.ui.fontutil.TextBlock;

/**
 * @author KarnickelDev
 * @since 06.04.2026
 **/
public final class ChatMessageStore implements MessageProvider {

    public static final int MAX_MESSAGES = 64;

    private final TextBlock[] buffer = new TextBlock[MAX_MESSAGES];

    private int start;
    private int size = 0;

    @Override
    public int size() {
        return size;
    }

    @Override
    public TextBlock getMessage(int i) {
        return buffer[(start + i) % MAX_MESSAGES];
    }

    @Override
    public TextBlock addMessage(TextBlock message) {
        int idx = (start + size) % MAX_MESSAGES;

        if(size < MAX_MESSAGES) {
            buffer[idx] = message;
            size++;
            return null;
        } else {
            TextBlock old = buffer[start];
            buffer[start] = message;
            start = (start + 1) % MAX_MESSAGES;
            return old;
        }
    }
}
