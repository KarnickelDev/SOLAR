package karnickeldev.solar.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import karnickeldev.solar.ecs.ClientECS;
import karnickeldev.solar.ecs.SystemGroup;
import karnickeldev.solar.net.network.*;
import karnickeldev.solar.net.packets.Packet;
import karnickeldev.solar.net.server.LocalServer;
import karnickeldev.solar.render.BackgroundStarRenderer;
import karnickeldev.solar.render.PlanetoidRenderSystem;
import karnickeldev.solar.render.camera.CameraInput;
import karnickeldev.solar.render.camera.FloatingOriginCamera;
import karnickeldev.solar.simulation.execution.SimulationManager;
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

    public SimTestScreen() {
        clientWorldManager = new WorldManager<>(new ClientWorld(0));

        clientListener = new DefaultClientNetworkListener(clientWorldManager);
        ServerNetworkListener serverListener = new DefaultServerNetworkListener();

        clientDispatcher = new DefaultMainThreadDispatcher();
        MainThreadDispatcher serverDispatcher = new DefaultMainThreadDispatcher();

        BlockingQueue<Packet> toServer = new LinkedBlockingQueue<>(128);
        BlockingQueue<Packet> fromServer = new LinkedBlockingQueue<>(128);

        clientNetwork = new LocalClientNetwork(toServer, fromServer, clientListener, clientDispatcher);
        ServerNetwork serverNetwork = new LocalServerNetwork(toServer, fromServer, serverListener, serverDispatcher);

        networkThread = new NetworkThread("SharedNetworkThread", clientNetwork, serverNetwork);

        server = LocalServer.create(serverNetwork, networkThread, serverDispatcher);

        rs = new PlanetoidRenderSystem(clientWorldManager, SolarMain.getInstance().batch);

        cameraInput = new CameraInput(clientWorldManager);
    }


    @Override
    public void show() {

        server.start();
        networkThread.start();

        SolarMain.getInstance().getInputManager().addInput(cameraInput);
        SolarMain.getInstance().getInputManager().addInput(new MenuInput());
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        clientDispatcher.update();

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
