package karnickeldev.solar.net.network;

public interface MainThreadDispatcher {

    void dispatch(Runnable run);

    void update();
}
