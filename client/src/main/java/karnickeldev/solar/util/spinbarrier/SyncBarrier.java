package karnickeldev.solar.util.spinbarrier;

/**
 * @author : KarnickelDev
 * @since : 21.10.2025
 **/
public interface SyncBarrier {

    void await();

    int getParties();
}
