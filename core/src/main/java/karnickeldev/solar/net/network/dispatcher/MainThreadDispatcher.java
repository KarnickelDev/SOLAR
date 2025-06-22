package karnickeldev.solar.net.network.dispatcher;

public interface MainThreadDispatcher {

    void dispatch(Runnable run);

    void update();
}
