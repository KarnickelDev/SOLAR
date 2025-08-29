package karnickeldev.solar.context;

import karnickeldev.solar.network.server.Server;

/**
 * @author : KarnickelDev
 * @since : 01.07.2025
 **/
public class ServerContextBuilder {

    public static ServerContextContainer buildServerContext(Server server) {
        return new ServerContextContainer(server);
    }

}
