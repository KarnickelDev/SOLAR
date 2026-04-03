package karnickeldev.solar.launch;

import karnickeldev.solar.Metadata;
import karnickeldev.solar.context.EngineContext;
import karnickeldev.solar.context.ServerContext;
import karnickeldev.solar.context.ServerContextBuilder;
import karnickeldev.solar.logging.LogLevel;
import karnickeldev.solar.logging.LogManager;
import karnickeldev.solar.logging.LogTag;
import karnickeldev.solar.logging.Logger;
import karnickeldev.solar.network.net.core.ServerNetwork;
import karnickeldev.solar.scheduler.DefaultScheduler;
import karnickeldev.solar.network.net.listener.DefaultServerNetworkListener;
import karnickeldev.solar.network.net.transport.netty.NettyServerNetwork;
import karnickeldev.solar.network.server.DedicatedServer;
import karnickeldev.solar.network.server.Server;
import karnickeldev.solar.scheduler.Scheduler;
import karnickeldev.solar.util.threadlayout.ThreadContext;

import java.util.List;

public class TestLauncher {

    public static void main(String[] args) {
        Logger logger = Logger.get(LogTag.SERVER);

        for(String cmd: args) {
            if (cmd.equals("--verbose")) {
                // TODO: add
            } else if(cmd.equals("--debug")) {
                LogManager.setLevel(LogLevel.DEBUG);
                logger.info("Set Logging Level to DEBUG");
            }
        }

        Metadata.loadVersionData();

        logger.info(Metadata.APP_NAME + " v" + Metadata.VERSION);

        ThreadContext sim = new ThreadContext("sim", true, List.of(2, 4));

        EngineContext.init(2);

        Scheduler scheduler = new DefaultScheduler();

        ServerNetwork serverNetwork = new NettyServerNetwork(25566, new DefaultServerNetworkListener(), scheduler.main());

        Server server = DedicatedServer.create(serverNetwork, scheduler, sim);

        ServerContext.setContext(ServerContextBuilder.buildServerContext(server));

        server.start();
    }

}
