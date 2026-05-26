package karnickeldev.solar.ui.layers.hud.chat;

import karnickeldev.solar.ui.fontutil.TextBlock;

/**
 * @author KarnickelDev
 * @since 07.04.2026
 **/
public interface MessageProvider {

    TextBlock getMessage(int i);

    int size();

    TextBlock addMessage(TextBlock chatMessage);

}
