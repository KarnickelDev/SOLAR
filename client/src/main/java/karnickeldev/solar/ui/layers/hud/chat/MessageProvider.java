package karnickeldev.solar.ui.layers.hud.chat;

/**
 * @author KarnickelDev
 * @since 07.04.2026
 **/
public interface MessageProvider {

    ChatMessage getMessage(int i);

    int size();

    ChatMessage addMessage(ChatMessage chatMessage);

}
