package karnickeldev.solar.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector3;
import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.ecs.EntityReference;
import karnickeldev.solar.physics.Units;
import karnickeldev.solar.render.camera.CameraInput;
import karnickeldev.solar.render.camera.FloatingOriginCamera;
import karnickeldev.solar.render.PlanetoidRenderSystem;
import karnickeldev.solar.server.physics.KeplerianOrbitSystem;
import karnickeldev.solar.server.servers.DefaultServer;
import karnickeldev.solar.util.MathUtil;

public class SimTestScreen implements Screen {


    public static DefaultServer server;

    public static FloatingOriginCamera camera;
    private final CameraInput cameraInput;

    private final EntityManager em;
    private final PlanetoidRenderSystem rs;
    private final KeplerianOrbitSystem os;

    public SimTestScreen() {
        camera = new FloatingOriginCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();

        server = new DefaultServer();

        em = server.getEntityManager();
        cameraInput = new CameraInput(camera, em);
        os = new KeplerianOrbitSystem(em);
        rs = new PlanetoidRenderSystem(em, SolarMain.getInstance().batch, camera);
    }



    @Override
    public void show() {
        server.start();
        SolarMain.getInstance().getInputManager().addInput(InputManager.GAME_CAMERA, cameraInput);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // camera
        cameraInput.processInputs();
        camera.update();

        SolarMain.getInstance().batch.begin();
        if(!BackgroundStarRenderer.drawStarScape(SolarMain.getInstance().batch, delta, false)) Logger.error("Erroneous input for background starscape");
        SolarMain.getInstance().batch.end();

        rs.renderPlanetoids();
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
