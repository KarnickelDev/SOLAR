package karnickeldev.solar.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import karnickeldev.solar.physics.*;

import java.util.ArrayList;
import java.util.List;

public class SimTestScreen implements Screen {



    ShapeRenderer shapeRenderer;

    List<PhysicsObject> planets = new ArrayList<>();

    public SimTestScreen() {
        shapeRenderer = new ShapeRenderer();

        PhysicsObject sun = new Star("sun", Units.toSU(1, Units.Mass.SOLAR_MASS),
            new Vector2D(Units.toSU(1.1, Units.Length.AU), Units.toSU(1.1, Units.Length.AU)));

        Planet mercury = new Planet("mercury", 1, 1, 0, sun,
            Units.toSU(0.387, Units.Length.AU),0.206,0,0);

        Planet venus = new Planet("venus", 1, 1, 0, sun,
            Units.toSU(0.723, Units.Length.AU),0.007,0,0);

        Planet earth = new Planet("earth", 1, 1, 0,
            new OrbitData(sun, Units.toSU(1, Units.Length.AU),0.017,0,0));

        Planet moon = new Planet("moon", 1, 1, 0, earth,
            Units.toSU(384_399, Units.Length.KILOMETER),0.055,0,0);

        planets.add(sun);
        planets.add(mercury);
        planets.add(venus);
        planets.add(earth);
        planets.add(moon);
    }



    @Override
    public void show() {

    }

    double time = 0;

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        time += (3600*24*30) * delta;

        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.begin();
        for(PhysicsObject object: planets) {
            object.update(time);
            float x = (float)(object.getPosition().getX() / Units.toSU(2.2, Units.Length.AU)) * Gdx.graphics.getHeight();
            float y = (float)(object.getPosition().getY() / Units.toSU(2.2, Units.Length.AU)) * Gdx.graphics.getHeight();
            shapeRenderer.circle(
                x,
                y,
                2);
        }
        shapeRenderer.end();


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
