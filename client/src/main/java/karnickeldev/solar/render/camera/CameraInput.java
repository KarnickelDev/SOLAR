package karnickeldev.solar.render.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.TimeUtils;
import karnickeldev.solar.ecs.ClientECS;
import karnickeldev.solar.ecs.components.client.HCSClientSystem;
import karnickeldev.solar.net.server.LocalServer;
import karnickeldev.solar.physics.Vector2D;
import karnickeldev.solar.render.PlanetoidRenderSystem;
import karnickeldev.solar.util.MathUtil;

public class CameraInput extends InputAdapter {

    private static final float ZOOM_SPEED = 0.16f;
    private static final float ZOOM_ACCELERATION = 1.17f;
    private static final float MIN_ZOOM = 1e-30f;
    private static final float MAX_ZOOM = 1e30f;
    private static final float ROTATION_SPEED = 50f;
    private final FloatingOriginCamera camera;
    private final ClientECS ecs;
    private long lastScrollTime = 0;
    private int scrollCount = 0;
    private int lastMouseX = 0, lastMouseY = 0;
    private boolean dragging = false;

    public CameraInput(FloatingOriginCamera camera, ClientECS ecs) {
        this.camera = camera;
        this.ecs = ecs;
    }

    public void processInputs() {
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            camera.move(FloatingOriginCamera.UP);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.move(FloatingOriginCamera.LEFT);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            camera.move(FloatingOriginCamera.DOWN);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            camera.move(FloatingOriginCamera.RIGHT);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            camera.rotate(-ROTATION_SPEED);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            camera.rotate(+ROTATION_SPEED);
        }
    }

    @Override
    public boolean keyDown(int keycode) {

        if (keycode == Input.Keys.R) {
            camera.setPosition(0, 0);
            return true;
        }

        return false;
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
        double zoom = MathUtil.clamp(currentZoom + (scrollDir * factor), MIN_ZOOM, MAX_ZOOM);

        camera.zoomToward(zoom, new Vector2D(Gdx.input.getX(), Gdx.input.getY()));
        return true;
    }

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
        if (button == Input.Buttons.LEFT) {
            HCSClientSystem hcs = ecs.hcs;
            for (int entity = 0; entity < ecs.getEntityManager().getAll(); entity++) {
                if (ecs.getEntityManager().isValid(entity)) {
                    if (!hcs.getCurrent().has(entity)) continue;
                    double x = getPosX(entity);
                    double y = getPosY(entity);
                    Vector2D screen = camera.project(new Vector2D(x, y));

                    double dx = screenX - (screen.getX());
                    double dy = (screenY) - screen.getY();
                    if (dx * dx + dy * dy < 16 * 16) {
                        PlanetoidRenderSystem.track = entity;
                        camera.setPosition(0, 0);
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

    private double getPosX(int entity) {
        double x = ecs.hcs.getInterpolatedX(entity, ecs.hcs.getAlpha(LocalServer.TICK_RATE));
        int parent = ecs.hcs.getCurrent().getParent(entity);

        x += ecs.hcs.getInterpolatedX(parent, ecs.hcs.getAlpha(LocalServer.TICK_RATE));
        parent = ecs.hcs.getCurrent().getParent(parent);
        x += ecs.hcs.getInterpolatedX(parent, ecs.hcs.getAlpha(LocalServer.TICK_RATE));

        x -= ecs.hcs.getInterpolatedX(PlanetoidRenderSystem.track, ecs.hcs.getAlpha(LocalServer.TICK_RATE));

        return x;
    }

    private double getPosY(int entity) {
        double y = ecs.hcs.getInterpolatedY(entity, ecs.hcs.getAlpha(LocalServer.TICK_RATE));
        int parent = ecs.hcs.getCurrent().getParent(entity);

        y += ecs.hcs.getInterpolatedY(parent, ecs.hcs.getAlpha(LocalServer.TICK_RATE));
        parent = ecs.hcs.getCurrent().getParent(parent);
        y += ecs.hcs.getInterpolatedY(parent, ecs.hcs.getAlpha(LocalServer.TICK_RATE));

        y -= ecs.hcs.getInterpolatedY(PlanetoidRenderSystem.track, ecs.hcs.getAlpha(LocalServer.TICK_RATE));

        return y;
    }

}
