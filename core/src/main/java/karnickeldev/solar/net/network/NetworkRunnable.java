package karnickeldev.solar.net.network;

import karnickeldev.solar.util.Logger;

public class NetworkRunnable implements Runnable {

    private static final int IDLE_THRESHOLD = 8;

    private final Network network;
    private final Network localNetwork2;

    private volatile boolean running = true;

    public NetworkRunnable(Network network) {
        this.network = network;
        localNetwork2 = null;
    }

    public NetworkRunnable(Network network1, Network network2) {
        this.network = network1;
        this.localNetwork2 = network2;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {

        int idleCount = 0;

        while (running) {

            if (network.updateNetwork() || (localNetwork2 != null && localNetwork2.updateNetwork())) {
                idleCount = 0;
            } else {
                idleCount++;

                if (idleCount >= IDLE_THRESHOLD) {
                    try {
                        //noinspection BusyWait
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        break;
                    }
                }

            }

        }
        Logger.log(Logger.NETWORK, "NetworkThread stopped");
    }
}
