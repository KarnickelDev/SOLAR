package karnickeldev.solar.launch;

import karnickeldev.solar.Metadata;
import karnickeldev.solar.context.ServerContext;
import karnickeldev.solar.context.ServerContextBuilder;
import karnickeldev.solar.network.net.dispatcher.DefaultDispatcher;
import karnickeldev.solar.network.net.dispatcher.Dispatcher;
import karnickeldev.solar.network.net.core.ServerNetwork;
import karnickeldev.solar.network.net.listener.DefaultServerNetworkListener;
import karnickeldev.solar.network.net.transport.netty.NettyServerNetwork;
import karnickeldev.solar.network.server.DedicatedServer;
import karnickeldev.solar.network.server.Server;
import karnickeldev.solar.util.Logger;

public class TestLauncher {

    public static void main(String[] args) {

        for(String cmd: args) {
            if (cmd.equals("--verbose")) {
                Logger.setVerbose(true);
                Logger.log("Set Logging to VERBOSE");
            } else if(cmd.equals("--debug")) {
                Logger.setLogLevel(Logger.LOG_DEBUG);
                Logger.log("Set Logging Level to DEBUG");
            }
        }

        Metadata.loadVersionData();

        Logger.log(Logger.SERVER, Metadata.APP_NAME + " v" + Metadata.VERSION);

        Dispatcher dispatcher = new DefaultDispatcher();

        ServerNetwork serverNetwork = new NettyServerNetwork(25566, new DefaultServerNetworkListener(), dispatcher);

        Server server = DedicatedServer.create(serverNetwork, dispatcher);

        ServerContext.setContext(ServerContextBuilder.buildServerContext(server));

        server.start();
    }

}
