package karnickeldev.solar.context;

import karnickeldev.solar.network.server.Server;

/**
 * @author KarnickelDev
 * @since 01.07.2025
 **/
public class ServerContextContainer {

    private final Server server;

    protected ServerContextContainer(Server server) {
        this.server = server;
    }

    public Server getServer() {
        return server;
    }
}
