package karnickeldev.solar.network.net.dispatcher;

import java.util.List;

public interface Dispatcher {

    void dispatch(Runnable run);

    void update();

    boolean update(int ms);

    float getProgress();

    void shutdownGracefully();

    List<Runnable> shutdown();
}
