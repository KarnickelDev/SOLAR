package karnickeldev.solar.network.net.dispatcher;

public interface MainThreadDispatcher {

    void dispatch(Runnable run);

    void update();
}
