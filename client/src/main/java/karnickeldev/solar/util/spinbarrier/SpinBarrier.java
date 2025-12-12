package karnickeldev.solar.util.spinbarrier;

import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author KarnickelDev
 * @since 19.10.2025
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

        if(isTerminated()) return;

        if (arrived.incrementAndGet() == parties) {
            arrived.set(0);
            phase = currentPhase + 1;   // release all
        } else {
            while (phase == currentPhase) {
                if(isTerminated()) break;
                Thread.onSpinWait();
            }
        }
        VarHandle.fullFence();
    }

    @Override
    public boolean isTerminated() {
        return phase == Integer.MAX_VALUE;
    }

    @Override
    public int getParties() {
        return parties;
    }

    @Override
    public void forceTermination() {
        phase = Integer.MAX_VALUE;
        VarHandle.fullFence();
    }
}
