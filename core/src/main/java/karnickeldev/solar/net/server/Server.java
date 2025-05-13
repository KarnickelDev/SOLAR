package karnickeldev.solar.net.server;

import karnickeldev.solar.net.network.ServerNetwork;

public abstract class Server {

    protected final ServerNetwork serverNetwork;

    private boolean running = false;


    public Server(ServerNetwork serverNetwork) {
        this.serverNetwork = serverNetwork;
    }

    public final void start() {
        running = true;
    }

    public final void stop() {
        running = false;
        serverNetwork.shutdown();
    }

    public final boolean isRunning() {
        return running;
    }

    protected void tick() {

    }

    protected abstract void preTick();

    protected abstract void postTick();
}
