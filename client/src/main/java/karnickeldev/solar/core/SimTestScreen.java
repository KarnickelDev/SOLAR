package karnickeldev.solar.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import karnickeldev.solar.ecs.ClientECS;
import karnickeldev.solar.net.network.*;
import karnickeldev.solar.net.packets.Packet;
import karnickeldev.solar.net.server.LocalServer;
import karnickeldev.solar.render.BackgroundStarRenderer;
import karnickeldev.solar.render.PlanetoidRenderSystem;
import karnickeldev.solar.render.camera.CameraInput;
import karnickeldev.solar.render.camera.FloatingOriginCamera;
import karnickeldev.solar.ui.menus.MenuInput;
import karnickeldev.solar.util.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SimTestScreen implements Screen {

    public static LocalServer server;
    public static CameraInput cameraInput;
    public static DefaultClientNetworkListener clientListener;
    private final ClientECS ecs;
    private final PlanetoidRenderSystem rs;
    private final NetworkThread networkThread;
    public FloatingOriginCamera camera;
    ClientNetwork clientNetwork;
    MainThreadDispatcher clientDispatcher;

    public SimTestScreen() {

        ecs = new ClientECS();

        camera = new FloatingOriginCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), ecs.hcs);
        camera.update();

        clientListener = new DefaultClientNetworkListener(ecs);
        ServerNetworkListener serverListener = new DefaultServerNetworkListener();

        clientDispatcher = new DefaultMainThreadDispatcher();
        MainThreadDispatcher serverDispatcher = new DefaultMainThreadDispatcher();

        BlockingQueue<Packet> toServer = new LinkedBlockingQueue<>(128);
        BlockingQueue<Packet> fromServer = new LinkedBlockingQueue<>(128);

        clientNetwork = new LocalClientNetwork(toServer, fromServer, clientListener, clientDispatcher);
        ServerNetwork serverNetwork = new LocalServerNetwork(toServer, fromServer, serverListener, serverDispatcher);

        networkThread = new NetworkThread("SharedNetworkThread", clientNetwork, serverNetwork);

        server = new LocalServer(serverNetwork, networkThread, serverDispatcher);

        cameraInput = new CameraInput(camera, ecs);
        rs = new PlanetoidRenderSystem(ecs, SolarMain.getInstance().batch, camera);
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
        camera.update();

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
