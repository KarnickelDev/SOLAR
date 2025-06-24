package karnickeldev.solar.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import karnickeldev.solar.ecs.components.ComponentType;
import karnickeldev.solar.network.net.*;
import karnickeldev.solar.network.net.core.*;
import karnickeldev.solar.network.net.dispatcher.DefaultMainThreadDispatcher;
import karnickeldev.solar.network.net.dispatcher.MainThreadDispatcher;
import karnickeldev.solar.network.net.listener.DefaultServerNetworkListener;
import karnickeldev.solar.network.net.listener.ServerNetworkListener;
import karnickeldev.solar.network.packets.Packet;
import karnickeldev.solar.network.packets.PacketFactory;
import karnickeldev.solar.network.packets.PacketTypes;
import karnickeldev.solar.network.server.LocalServer;
import karnickeldev.solar.render.BackgroundStarRenderer;
import karnickeldev.solar.render.PlanetoidRenderSystem;
import karnickeldev.solar.render.camera.CameraInput;
import karnickeldev.solar.ui.menus.MenuInput;
import karnickeldev.solar.util.Logger;
import karnickeldev.solar.world.ClientWorld;
import karnickeldev.solar.world.WorldManager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SimTestScreen implements Screen {

    public static LocalServer server;

    private final WorldManager<ClientWorld> clientWorldManager;

    public static DefaultClientNetworkListener clientListener;
    private final PlanetoidRenderSystem rs;
    private final NetworkThread networkThread;

    ClientNetwork clientNetwork;
    MainThreadDispatcher clientDispatcher;

    private final CameraInput cameraInput;

    private final boolean multiplayer;

    public SimTestScreen(boolean multiplayer) {
        this.multiplayer = multiplayer;

        clientWorldManager = new WorldManager<>(new ClientWorld(0));

        clientListener = new DefaultClientNetworkListener(clientWorldManager);
        clientDispatcher = new DefaultMainThreadDispatcher();

        if(multiplayer) {
            clientNetwork = new DedicatedClientNetwork(25907, clientListener, clientDispatcher);
            networkThread = new NetworkThread(clientNetwork, "ClientNetworkThread");
        } else {
            ServerNetworkListener serverListener = new DefaultServerNetworkListener();

            MainThreadDispatcher serverDispatcher = new DefaultMainThreadDispatcher();

            BlockingQueue<Packet> toServer = new LinkedBlockingQueue<>(128);
            BlockingQueue<Packet> fromServer = new LinkedBlockingQueue<>(128);

            clientNetwork = new LocalClientNetwork(toServer, fromServer, clientListener, clientDispatcher);
            ServerNetwork serverNetwork = new LocalServerNetwork(toServer, fromServer, serverListener, serverDispatcher);

            networkThread = new NetworkThread("SharedNetworkThread", clientNetwork, serverNetwork);

            server = LocalServer.create(serverNetwork, networkThread, serverDispatcher);
        }

        rs = new PlanetoidRenderSystem(clientWorldManager, SolarMain.getInstance().batch);

        cameraInput = new CameraInput(clientWorldManager);
    }


    @Override
    public void show() {

        if(multiplayer) PacketTypes.registerAll();
        ComponentType.registerSnapshotDeserializers();

        if(!multiplayer) server.start();
        networkThread.start();
        clientNetwork.connect();

        SolarMain.getInstance().getInputManager().addInput(cameraInput);
        SolarMain.getInstance().getInputManager().addInput(new MenuInput());
    }

    double tmp = 0;

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        clientDispatcher.update();

        tmp += delta;
        if(tmp > 1) {
            tmp = 0;
            clientNetwork.send(PacketFactory.createPingPacket(System.nanoTime()));
        }

        // camera
        cameraInput.processInputs();
        clientWorldManager.getActiveWorld().getCamera().update();

        SolarMain.getInstance().batch.begin();
        if (!BackgroundStarRenderer.drawStarScape(SolarMain.getInstance().batch, delta, false))
            Logger.error("Erroneous input for background starscape");
        SolarMain.getInstance().batch.end();

        rs.renderPlanetoids();

        SolarMain.getInstance().pausedStage.act(delta);
        SolarMain.getInstance().pausedStage.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
