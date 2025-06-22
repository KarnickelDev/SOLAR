package karnickeldev.solar.launch;

import karnickeldev.solar.Metadata;
import karnickeldev.solar.net.network.core.DedicatedServerNetwork;
import karnickeldev.solar.net.network.dispatcher.DefaultMainThreadDispatcher;
import karnickeldev.solar.net.network.dispatcher.MainThreadDispatcher;
import karnickeldev.solar.net.network.core.NetworkThread;
import karnickeldev.solar.net.network.core.ServerNetwork;
import karnickeldev.solar.net.network.listener.DefaultServerNetworkListener;
import karnickeldev.solar.net.server.DedicatedServer;
import karnickeldev.solar.net.server.Server;

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
