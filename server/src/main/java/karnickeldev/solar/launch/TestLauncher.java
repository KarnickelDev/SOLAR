package karnickeldev.solar.launch;

import karnickeldev.solar.Metadata;
import karnickeldev.solar.network.net.core.DedicatedServerNetwork;
import karnickeldev.solar.network.net.dispatcher.DefaultMainThreadDispatcher;
import karnickeldev.solar.network.net.dispatcher.MainThreadDispatcher;
import karnickeldev.solar.network.net.core.NetworkThread;
import karnickeldev.solar.network.net.core.ServerNetwork;
import karnickeldev.solar.network.net.listener.DefaultServerNetworkListener;
import karnickeldev.solar.network.server.DedicatedServer;
import karnickeldev.solar.network.server.Server;

public class TestLauncher {

    public static void main(String[] args) {

        Metadata.loadVersionData();

        System.out.println(Metadata.APP_NAME + " v" + Metadata.VERSION);

        MainThreadDispatcher dispatcher = new DefaultMainThreadDispatcher();

        ServerNetwork serverNetwork = new DedicatedServerNetwork(25907, new DefaultServerNetworkListener(), dispatcher);

        NetworkThread networkThread = new NetworkThread(serverNetwork, "ServerNetwork");

        Server server = DedicatedServer.create(serverNetwork, networkThread, dispatcher);

        networkThread.start();
        server.start();
        serverNetwork.start();
    }

}
