package karnickeldev.solar.launch;

import karnickeldev.solar.Metadata;
import karnickeldev.solar.context.EngineContext;
import karnickeldev.solar.context.ServerContext;
import karnickeldev.solar.context.ServerContextBuilder;
import karnickeldev.solar.network.net.core.ServerNetwork;
import karnickeldev.solar.scheduler.DefaultDispatcher;
import karnickeldev.solar.scheduler.DefaultScheduler;
import karnickeldev.solar.scheduler.Dispatcher;
import karnickeldev.solar.network.net.listener.DefaultServerNetworkListener;
import karnickeldev.solar.network.net.transport.netty.NettyServerNetwork;
import karnickeldev.solar.network.server.DedicatedServer;
import karnickeldev.solar.network.server.Server;
import karnickeldev.solar.scheduler.Scheduler;
import karnickeldev.solar.simulation.execution.SimulationManagerThread;
import karnickeldev.solar.util.Logger;
import karnickeldev.solar.util.threadlayout.ClientThreadLayout;
import karnickeldev.solar.util.threadlayout.ThreadContext;

import java.util.List;

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

        ThreadContext sim = new ThreadContext("sim", true, List.of(2, 4));

        EngineContext.init(2);

        Scheduler scheduler = new DefaultScheduler();

        ServerNetwork serverNetwork = new NettyServerNetwork(25566, new DefaultServerNetworkListener(), scheduler.main());

        Server server = DedicatedServer.create(serverNetwork, scheduler, sim);

        ServerContext.setContext(ServerContextBuilder.buildServerContext(server));

        server.start();
    }

}
