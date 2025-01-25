package karnickeldev.solar.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import karnickeldev.solar.gamestate.GameState;
import karnickeldev.solar.physics.*;
import karnickeldev.solar.gamestate.StarSystemTree;
import karnickeldev.solar.server.servers.DefaultServer;
import karnickeldev.solar.server.servers.SolarServer;

public class SimTestScreen implements Screen {



    ShapeRenderer shapeRenderer;
    public static SolarServer server;

    public SimTestScreen() {
        shapeRenderer = new ShapeRenderer();

        StarSystemTree sol = new StarSystemTree();

        PhysicsObject ghostNode = new GhostObject(1e31f, new Vector2D(Units.toSU(5, Units.Length.AU), Units.toSU(5, Units.Length.AU)));

        OrbitalObject sun = new Star("sun", Units.toSU(1, Units.Mass.SOLAR_MASS), 1, false,
            new OrbitData(ghostNode, Units.toSU(0.1, Units.Length.AU),0,0,0));

        Planetoid mercury = new Planetoid("mercury", Units.toSU(0.055f, Units.Mass.EARTH_MASS), 1, 0, sun,
            Units.toSU(0.387, Units.Length.AU),0.206,0,0);

        Planetoid venus = new Planetoid("venus", Units.toSU(0.815f, Units.Mass.EARTH_MASS), 1, 0, sun,
            Units.toSU(0.723, Units.Length.AU),0.007,0,0);

        Planetoid earth = new Planetoid("earth", Units.toSU(1, Units.Mass.EARTH_MASS), 1, 0,
            new OrbitData(sun, Units.toSU(1, Units.Length.AU),0.017,0,0));

        Planetoid moon = new Planetoid("moon", Units.toSU(7.348e22f, Units.Mass.KG), 1, 0, earth,
            Units.toSU(384_399, Units.Length.KILOMETER),0.055,0,0);

        Planetoid jupiter = new Planetoid("jupiter", Units.toSU(317.8f, Units.Mass.EARTH_MASS), 1, 0, sun,
            Units.toSU(5.2038, Units.Length.AU),0.0489,0,0);

        sol.insert(sun);
        sol.insert(mercury);
        sol.insert(venus);
        sol.insert(earth);
        sol.insert(jupiter);
        sol.insert(moon);

        server = new DefaultServer(new GameState(sol));
    }



    @Override
    public void show() {
        server.start();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        shapeRenderer.setAutoShapeType(true);
        for(OrbitalObject object: server.getCurrentGameState().getStarSystem().getAllObjects()) {
            float x = (float)(object.getPosition().getX() / Units.toSU(10, Units.Length.AU)) * Gdx.graphics.getHeight();
            float y = (float)(object.getPosition().getY() / Units.toSU(10, Units.Length.AU)) * Gdx.graphics.getHeight();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(object instanceof Star ? Color.ORANGE : Color.WHITE);
            shapeRenderer.circle(
                x,
                y,
                4);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.circle(
                x,
                y,
                (int) (object.getSphereOfInfluence() / Units.toSU(10, Units.Length.AU) * Gdx.graphics.getHeight()));
            shapeRenderer.end();
        }

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
