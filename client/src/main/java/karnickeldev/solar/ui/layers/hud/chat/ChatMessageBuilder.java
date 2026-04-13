package karnickeldev.solar.ui.layers.hud.chat;

import com.badlogic.gdx.graphics.Color;
import karnickeldev.solar.ui.core.UI;

import java.util.ArrayList;

/**
 * @author KarnickelDev
 * @since 08.04.2026
 **/
public class ChatMessageBuilder {

    public static final Color DEFAULT_SENDER_COLOR = new Color(Color.CYAN);

    private ChatMessage message = new ChatMessage(1,1);

    private int prevColor = 0xFFFFFFFF;

    public ChatMessageBuilder() {}

    public ChatMessageBuilder(String sender, String text) {
        text(DEFAULT_SENDER_COLOR, sender + ": ");
        text(UI.WHITE, text);
    }

    public ChatMessageBuilder text(String text) {
        message.addSegment(text, prevColor);
        return this;
    }

    public ChatMessageBuilder text(Color color, String text) {
        prevColor = Color.rgba8888(color);
        message.addSegment(text, prevColor);
        return this;
    }

    public ChatMessageBuilder color(Color color) {
        prevColor = Color.rgba8888(color);
        return this;
    }

    public ChatMessage build() {
        return message;
    }
}
