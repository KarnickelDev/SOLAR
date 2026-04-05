package karnickeldev.solar.logging;

import com.badlogic.gdx.graphics.Color;
import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.logging.appender.LogFormatter;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.layers.hud.chat.ChatMessageBuilder;
import karnickeldev.solar.ui.layers.hud.chat.ChatWindow;

/**
 * @author KarnickelDev
 * @since 07.04.2026
 **/
public class ChatLogAppender implements LogAppender{

    private final ChatWindow chatWindow;

    public ChatLogAppender(ChatWindow chatWindow) {
        this.chatWindow = chatWindow;
    }

    @Override
    public void append(LogEvent event) {
        ChatMessageBuilder b = new ChatMessageBuilder();

        // Log Level
        b.text(getColor(event.level), "[" + event.level.toString().trim() + "] ");

        // Log Tag
        b.text(getColor(event.level),"[" + event.tag + "] ");

        // message
        String msg = LogFormatter.format(event.template, event.args);
        if(event.level == LogLevel.INFO) {
            b.color(UI.WHITE);
        } else {
            b.color(getColor(event.level));
        }
        b.text(msg);

        GameContext.get().getScheduler().schedule(() -> chatWindow.addChatMessage(b.build()));
    }

    @Override
    public void close() {

    }

    private Color getColor(LogLevel level) {
        return switch (level) {
            case INFO -> Color.GREEN;
            case DEBUG -> Color.GRAY;
            case WARN -> Color.YELLOW;
            case ERROR -> Color.RED;
        };
    }

}
