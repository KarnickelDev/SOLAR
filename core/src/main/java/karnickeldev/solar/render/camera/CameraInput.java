package karnickeldev.solar.render.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.TimeUtils;
import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.physics.Vector2D;
import karnickeldev.solar.render.PlanetoidRenderSystem;
import karnickeldev.solar.util.MathUtil;

public class CameraInput extends InputAdapter {

    private final FloatingOriginCamera camera;

    private static final float ZOOM_SPEED = 0.16f;
    private static final float ZOOM_ACCELERATION = 1.17f;
    private static final float MIN_ZOOM = 1e-30f;
    private static final float MAX_ZOOM = 1e30f;
    private static final float ROTATION_SPEED = 50f;

    private long lastScrollTime = 0;
    private int scrollCount = 0;

    private final EntityManager em;

    public CameraInput(FloatingOriginCamera camera, EntityManager em) {
        this.camera = camera;
        this.em = em;
    }

    public void processInputs() {
        if(Gdx.input.isKeyPressed(Input.Keys.W)) {
            camera.move(FloatingOriginCamera.UP);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.move(FloatingOriginCamera.LEFT);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S)) {
            camera.move(FloatingOriginCamera.DOWN);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.D)) {
            camera.move(FloatingOriginCamera.RIGHT);
        }

        if(Gdx.input.isKeyPressed(Input.Keys.E)) {
            camera.rotate(-ROTATION_SPEED);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.Q)) {
            camera.rotate(+ROTATION_SPEED);
        }

    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        long now = TimeUtils.millis();

        if (now - lastScrollTime < 150) {
            scrollCount++; // Fast consecutive scrolls
        } else {
            scrollCount = 1; // Reset if too slow
        }

        lastScrollTime = now;

        double currentZoom = camera.getZoom();

        float scrollDir = Math.signum(amountY);
        double factor = ZOOM_SPEED * currentZoom * Math.pow(ZOOM_ACCELERATION, scrollCount);
        float zoom = MathUtil.clamp((float) (currentZoom + (scrollDir * factor)), MIN_ZOOM, MAX_ZOOM);

        camera.zoomToward(zoom, new Vector2D(Gdx.input.getX(), Gdx.input.getY()));
        return true;
    }

    private int lastMouseX = 0, lastMouseY = 0;
    private boolean dragging = false;

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.MIDDLE) {
            dragging = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (dragging) {
            int dx = screenX - lastMouseX;
            int dy = screenY - lastMouseY;

            // Apply drag to origin
            Vector2D origin = new Vector2D(camera.getOrigin());
            origin.add(-dx * camera.getZoom(), dy * camera.getZoom());  // y is flipped in screen coords

            lastMouseX = screenX;
            lastMouseY = screenY;

            camera.setPosition(origin);

            return true;
        }
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        boolean processed = false;
        if(button == Input.Buttons.LEFT) {
            for(int entity = 0; entity < em.getAll(); entity++) {
                if(em.isValid(entity)) {
                    double x = em.hcs.getLocalX(entity);
                    double y = em.hcs.getLocalY(entity);
                    Vector2D screen = camera.project(new Vector2D(x, y));

                    double dx = screenX - (screen.getX());
                    double dy = (screenY) - screen.getY();
                    if(dx*dx + dy*dy < 0.7f*16*16) {
                        PlanetoidRenderSystem.track = entity;
                        //camera.track(EntityReference.create(em, entity));
                        break;
                    }

                }
            }

            processed = true;
        }

        if (button == Input.Buttons.MIDDLE) {
            dragging = true;
            lastMouseX = screenX;
            lastMouseY = screenY;
            processed = true;
        }

        return processed;
    }

}
