package karnickeldev.solar.util.spinbarrier;

import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : KarnickelDev
 * @since : 19.10.2025
 **/
public class SpinBarrier implements SyncBarrier {

    private final int parties;
    private final AtomicInteger arrived = new AtomicInteger(0);
    private volatile int phase = 0;

    public SpinBarrier(int parties) {
        this.parties = parties;
    }

    public void await() {
        VarHandle.fullFence();
        int currentPhase = phase;
        if (arrived.incrementAndGet() == parties) {
            arrived.set(0);
            phase = currentPhase + 1;   // release all
        } else {
            while (phase == currentPhase) {
                Thread.onSpinWait();
            }
        }
        VarHandle.fullFence();
    }

    @Override
    public int getParties() {
        return parties;
    }
}
