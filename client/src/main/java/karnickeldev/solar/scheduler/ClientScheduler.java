package karnickeldev.solar.scheduler;

/**
 * @author KarnickelDev
 * @since 24.11.2025
 **/
public final class ClientScheduler extends DefaultScheduler {

    private final Dispatcher stagingDispatcher;

    public ClientScheduler() {
        super();

        stagingDispatcher = new DefaultDispatcher();
    }

    public Dispatcher getStagingDispatcher() {
        return stagingDispatcher;
    }

}
