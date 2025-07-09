package karnickeldev.solar.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import karnickeldev.solar.context.*;
import karnickeldev.solar.network.packets.PacketFactory;
import karnickeldev.solar.network.packets.TestCamPacket;
import karnickeldev.solar.render.StarField;
import karnickeldev.solar.render.camera.CameraInput;
import karnickeldev.solar.ui.components.DebugToolTip;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.world.ClientWorld;
import karnickeldev.solar.world.WorldManager;

public class SimTestScreen implements Screen {

    private final DebugToolTip debug;

    private final CameraInput cameraInput;

    private final Viewport backgroundViewport;
    private final Viewport screenViewport;

    public SimTestScreen() {
        backgroundViewport = new ExtendViewport(UI.VIRTUAL_WIDTH, UI.VIRTUAL_HEIGHT);
        screenViewport = new ScreenViewport();

        cameraInput = GameContext.get().getCameraInput();

        debug = new DebugToolTip();
        UI.getUIManager().addComponent("debug",debug);
    }


    @Override
    public void show() {
        GameContextContainer gameContext = GameContext.get();

        Gdx.input.setInputProcessor(SolarMain.getInstance().getInputManager().getInputMultiplexer());

        SolarMain.getInstance().getInputManager().addInput(cameraInput);
    }

    double tmp = 0;
    TestCamPacket camPacket = new TestCamPacket();

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0,0,0,1,true);

        GameContextContainer gameContext = GameContext.get();

        WorldManager<ClientWorld> clientWorldManager = gameContext.getWorldManager();

        tmp += delta;
        camPacket.x = clientWorldManager.getActiveWorld().getCamera().getRenderOrigin().getX();
        camPacket.y = clientWorldManager.getActiveWorld().getCamera().getRenderOrigin().getY();
        if(tmp > 0.3) {
            tmp = 0;
            gameContext.getClientNetwork().send(PacketFactory.createPingPacket(System.nanoTime()));
            gameContext.getClientNetwork().send(camPacket);
        }

        // camera
        gameContext.getCameraInput().processInputs();
        clientWorldManager.getActiveWorld().getCamera().update();

        long simTimeEstimate = GameContext.get().getTime().getSimTimeEstimate();
        gameContext.getSyncLayer().update(simTimeEstimate);

        // probably better to process before sending
        gameContext.getDispatcher().update();

        backgroundViewport.apply();
        SolarMain.getInstance().getBatch().setColor(1,1,1,1);
        SolarMain.getInstance().getBatch().setProjectionMatrix(backgroundViewport.getCamera().combined);
        SolarMain.getInstance().getBatch().begin();
        SolarMain.getInstance().getBatch().draw(StarField.starFieldBuffer.getColorBufferTexture(),0,0);
        //SolarMain.getInstance().getBatch().end();

        screenViewport.apply();
        gameContext.getPlanetoidRenderSystem().renderPlanetoids();
        SolarMain.getInstance().getBatch().end();

        UI.getUIManager().act(delta);
        UI.getUIManager().draw();
    }

    @Override
    public void resize(int width, int height) {
        backgroundViewport.update(width, height, true);
        screenViewport.update(width, height);
        debug.resize(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        UI.getUIManager().hideComponent("debug");
        SolarMain.getInstance().getInputManager().removeInput(cameraInput);
    }

    @Override
    public void dispose() {

    }
}
