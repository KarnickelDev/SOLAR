package karnickeldev.solar.ui.layers.hud.chat;

import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;

/**
 * @author KarnickelDev
 * @since 08.04.2026
 **/
public class ChatMessageBuilder {

    public static final Color DEFAULT_SENDER_COLOR = new Color(Color.CYAN);

    private final ArrayList<ChatMessage.ChatSegment> segments = new ArrayList<>();

    private boolean senderSet = false;

    private final Color prevColor = new Color(1,1,1,1);

    public ChatMessageBuilder() {}

    public ChatMessageBuilder(String sender, String text) {
        sender(sender);
        text(text);
    }

    public ChatMessageBuilder text(String text) {
        segments.add(new ChatMessage.ChatSegment(text, prevColor));
        return this;
    }

    public ChatMessageBuilder text(Color color, String text) {
        prevColor.set(color);
        segments.add(new ChatMessage.ChatSegment(text, Color.rgba8888(color)));
        return this;
    }

    public ChatMessageBuilder color(Color color) {
        prevColor.set(color);
        return this;
    }

    public ChatMessageBuilder sender(Color color, String sender) {
        prevColor.set(color);
        if(!senderSet) {
            segments.addFirst(new ChatMessage.ChatSegment(sender + ": ", prevColor));
            senderSet = true;
        } else {
            segments.set(0, new ChatMessage.ChatSegment(sender + ": ", prevColor));
        }
        return this;
    }
    public ChatMessageBuilder sender(String sender) {
        return sender(DEFAULT_SENDER_COLOR, sender);
    }

    public ChatMessage build() {
        return new ChatMessage(segments);
    }
}
