package karnickeldev.solar.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import karnickeldev.solar.context.*;
import karnickeldev.solar.network.packets.*;
import karnickeldev.solar.network.sync.PacketSyncLayer;
import karnickeldev.solar.render.StarField;
import karnickeldev.solar.render.camera.CameraInput;
import karnickeldev.solar.ui.components.DebugToolTip;
import karnickeldev.solar.ui.components.escapemenu.EscapeMenu;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.util.Logger;
import karnickeldev.solar.world.ClientWorld;
import karnickeldev.solar.world.WorldManager;

public class SimTestScreen implements Screen {

    private final CameraInput cameraInput;

    private final Viewport backgroundViewport;
    private final Viewport screenViewport;

    public SimTestScreen() {
        backgroundViewport = new ExtendViewport(UI.VIRTUAL_WIDTH, UI.VIRTUAL_HEIGHT);
        screenViewport = new ScreenViewport();

        cameraInput = GameContext.get().getCameraInput();
    }


    @Override
    public void show() {

        UI.getUIManager().addComponent("debug", new DebugToolTip());
        UI.getUIManager().showComponent("debug");

        UI.getUIManager().addComponent("escape_menu", new EscapeMenu());
        UI.getUIManager().hideComponent("escape_menu");

        // ORDER HERE IMPORTANT! (Inputs processed in order of registration)
        SolarMain.getInstance().getInputManager().addInput(UI.stage());
        SolarMain.getInstance().getInputManager().addInput(cameraInput);
        Gdx.input.setInputProcessor(SolarMain.getInstance().getInputManager().getInputMultiplexer());
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
            if(gameContext.isMultiplayer()) {
                gameContext.getClientNetwork().send(PacketFactory.createPingPacket(System.nanoTime()));
            }
            gameContext.getClientNetwork().send(camPacket);
        }

        // camera
        gameContext.getCameraInput().processInputs();
        clientWorldManager.getActiveWorld().getCamera().update();

        // do not use current here, we manually subtract PacketSyncDelay
        gameContext.getSyncLayer().update(gameContext.getTimeSyncManager().getRenderSimTime(-PacketSyncLayer.syncDelayMicros));

        // probably better to do after processing input
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

        UI.getUIManager().resize(width, height);
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
        UI.getUIManager().removeComponent("escape_menu");
        SolarMain.getInstance().getInputManager().removeInput(UI.stage());
        SolarMain.getInstance().getInputManager().removeInput(cameraInput);
    }

    @Override
    public void dispose() {

    }
}
