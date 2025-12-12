package karnickeldev.solar.scheduler;

/**
 * @author KarnickelDev
 * @since 24.11.2025
 **/
public interface Cancellable {

    void cancel();

    boolean isCancelled();

}
