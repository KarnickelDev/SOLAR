package karnickeldev.solar.net.network.core;

import karnickeldev.solar.util.Logger;

public class NetworkThread {

    private final NetworkRunnable networkRunnable;
    private final Thread thread;

    private final String name;

    public NetworkThread(Network network, String name) {
        this(name, network, null);
    }

    public NetworkThread(String name, Network network1, Network network2) {
        this.name = name;
        networkRunnable = new NetworkRunnable(network1, network2);
        thread = new Thread(networkRunnable, name);
    }

    public NetworkRunnable getNetworkRunnable() {
        return networkRunnable;
    }

    public void start() {
        if (thread.isAlive()) return;

        thread.start();
    }

    public void stop() {
        networkRunnable.stop();
        try {
            thread.join(10000);
        } catch (InterruptedException e) {
            Logger.log(Logger.NETWORK, "Thread " + name + " interrupted during stop");
            Thread.currentThread().interrupt();
        }
        if (thread.isAlive()) {
            Logger.error(Logger.NETWORK, "Failed to stop Thread " + name);
        }
    }

}
