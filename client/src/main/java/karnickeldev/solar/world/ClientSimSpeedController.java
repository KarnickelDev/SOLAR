package karnickeldev.solar.world;

import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.network.packets.SimTimeUpdateRequestPacket;
import karnickeldev.solar.simulation.execution.SimSpeedController;
import karnickeldev.solar.util.MathUtil;

/**
 * @author : KarnickelDev
 * @since : 05.08.2025
**/
public class ClientSimSpeedController {

    private final ClientClock clock;

    /** Should be Thread-Safe, as we only change from main thread */
    protected ClientSimSpeedController(ClientClock clock) {
        this.clock = clock;
    }

    public void changeSpeed(int change) {
        int target = MathUtil.clamp(clock.getTargetSimSpeedIndex() + change, 0, SimSpeedController.SPEED_PRESETS.length);

        if(target != clock.getTargetSimSpeedIndex()) request((byte)target, clock.isPaused());
    }

    public void requestPause(boolean pause) {
        request((byte)-1, pause);
    }

    public void togglePause() {
        request((byte)-1, !clock.isPaused());
    }

    public void request(byte index, boolean pause) {
        GameContext.get().getClientNetwork().send(new SimTimeUpdateRequestPacket(index, pause));
    }


}
