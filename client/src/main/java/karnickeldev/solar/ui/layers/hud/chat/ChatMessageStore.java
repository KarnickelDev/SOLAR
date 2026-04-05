package karnickeldev.solar.ui.layers.hud.chat;

/**
 * @author KarnickelDev
 * @since 06.04.2026
 **/
public final class ChatMessageStore implements MessageProvider {

    public static final int MAX_MESSAGES = 64;

    private final ChatMessage[] buffer = new ChatMessage[MAX_MESSAGES];

    private int start;
    private int size = 0;

    @Override
    public int size() {
        return size;
    }

    @Override
    public ChatMessage getMessage(int i) {
        return buffer[(start + i) % MAX_MESSAGES];
    }

    @Override
    public ChatMessage addMessage(ChatMessage message) {
        int idx = (start + size) % MAX_MESSAGES;

        if(size < MAX_MESSAGES) {
            buffer[idx] = message;
            size++;
            return null;
        } else {
            ChatMessage old = buffer[start];
            buffer[start] = message;
            start = (start + 1) % MAX_MESSAGES;
            return old;
        }
    }
}
