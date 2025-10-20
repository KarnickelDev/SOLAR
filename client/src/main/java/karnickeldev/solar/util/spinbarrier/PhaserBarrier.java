package karnickeldev.solar.util.spinbarrier;

import java.util.concurrent.Phaser;

/**
 * @author : KarnickelDev
 * @since : 21.10.2025
 **/
public class PhaserBarrier implements SyncBarrier {
    private final int parties;
    private final Phaser phaser;
    public PhaserBarrier(int parties) {
        this.parties = parties;
        this.phaser = new Phaser(parties);
    }


    @Override
    public void await() {
        phaser.arriveAndAwaitAdvance();
    }

    @Override
    public int getParties() {
        return parties;
    }
}
