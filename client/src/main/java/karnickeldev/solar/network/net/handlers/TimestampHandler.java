package karnickeldev.solar.network.net.handlers;

import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.context.GameContextContainer;
import karnickeldev.solar.network.packets.TimestampPacket;
import karnickeldev.solar.ui.core.UIComponent;
import karnickeldev.solar.ui.layers.hud.HudLayer;
import karnickeldev.solar.ui.layers.hud.game.TimeControl;

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

        gc.getScheduler().schedule(() -> {
            UIComponent cmp = HudLayer.INSTANCE.getComponent("time_control");
            if(cmp != null) ((TimeControl) cmp).setTargetSpeedIndex(packet.getTargetSimSpeedIndex());
        });
    }

    @Override
    public Class<TimestampPacket> getPacketClass() {
        return TimestampPacket.class;
    }
}
