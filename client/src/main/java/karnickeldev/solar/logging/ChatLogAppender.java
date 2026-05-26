package karnickeldev.solar.logging;

import com.badlogic.gdx.graphics.Color;
import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.logging.appender.LogFormatter;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.fontutil.RichTextBuilder;
import karnickeldev.solar.ui.fontutil.TextBlock;
import karnickeldev.solar.ui.layers.hud.HudLayer;
import karnickeldev.solar.ui.layers.hud.chat.ChatWindow;
import karnickeldev.solar.ui.layers.hud.chat.MessageRenderer;

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
        if(!GameContext.isSet()) return;

        RichTextBuilder b = new RichTextBuilder(MessageRenderer.DEFAULT_CHAT_FONT_SIZE);

        // Log Level
        b.color(getColor(event.level));
        b.text("[" + event.level.toString().trim() + "] ");

        // Log Tag
        b.color(getColor(event.level));
        b.text("[" + event.tag + "] ");

        // message
        String msg = LogFormatter.format(event.template, event.args);
        if(event.level == LogLevel.INFO) {
            b.color(Color.rgba8888(UI.WHITE));
        } else {
            b.color(getColor(event.level));
        }
        b.text(msg);

        GameContext.get().getScheduler().schedule(() -> HudLayer.INSTANCE.renderer.addMessage(new TextBlock(b.build())));
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
