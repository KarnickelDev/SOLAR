package karnickeldev.solar.network.net.handlers;

import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.context.GameContextContainer;
import karnickeldev.solar.network.packets.TimestampPacket;
import karnickeldev.solar.ui.components.UIComponent;
import karnickeldev.solar.ui.components.game.TimeControl;
import karnickeldev.solar.ui.core.UI;

/**
 * @author KarnickelDev
 * @since 06.10.2025
 **/
public class TimestampHandler implements PacketHandler<TimestampPacket> {

    @Override
    public void handle(int clientId, TimestampPacket packet) {
        GameContextContainer gc = GameContext.get();
        gc.getClock().updateClockData(
            packet.getSimTimeMicros(),
            System.nanoTime() / 1000L,
            packet.getCurrentSimSpeed(),
            packet.getTargetSimSpeedIndex()
        );

        gc.getDispatcher().dispatch(() -> {
            UIComponent cmp = UI.getUIManager().getComponent("time_control");
            if(cmp != null) ((TimeControl) cmp).setTargetSpeedIndex(packet.getTargetSimSpeedIndex());
        });
    }

    @Override
    public Class<TimestampPacket> getPacketClass() {
        return TimestampPacket.class;
    }
}
