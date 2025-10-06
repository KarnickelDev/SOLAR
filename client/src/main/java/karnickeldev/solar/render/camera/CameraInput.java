package karnickeldev.solar.render.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.ecs.ClientECS;
import karnickeldev.solar.ecs.systems.HCSClientSystem;
import karnickeldev.solar.physics.Vector2D;
import karnickeldev.solar.render.PlanetoidRenderSystem;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.core.UIManager;
import karnickeldev.solar.util.MathUtil;
import karnickeldev.solar.world.ClientWorld;
import karnickeldev.solar.world.WorldManager;

public class CameraInput extends InputAdapter {

    private static final float ZOOM_SPEED = 0.04f;
    private static final float ZOOM_ACCELERATION = 1.12f;
    private static final float MIN_ZOOM = 1e-5f;
    private static final float MAX_ZOOM = 1e12f;
    private static final float ROTATION_SPEED = 50f;
    private long lastScrollTime = 0;
    private int scrollCount = 0;
    private int lastMouseX = 0, lastMouseY = 0;
    private boolean dragging = false;

    private final WorldManager<ClientWorld> worldManager;
    private FloatingOriginCamera camera;
    private ClientECS ecs;

    long lastKeyboardZoom = 0;

    public CameraInput(WorldManager<ClientWorld> worldManager) {
        this.worldManager = worldManager;
        setWorld(worldManager.getActiveWorld());
    }

    private void setWorld(ClientWorld world) {
        if(world == null || worldManager.getActiveWorld() == null) return;

        this.camera = worldManager.getActiveWorld().getCamera();
        this.ecs = worldManager.getActiveWorld().getECS();
    }

    public void processInputs() {
//        // there has to be a better way xD
//        if(UI.getUIManager().getComponent("escape_menu").getGroup().isVisible()) return;
//        if(UI.getUIManager().getComponent("options_menu").getGroup().isVisible()) return;

        setWorld(worldManager.getActiveWorld());

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

        Vector2D screenCenter = new Vector2D(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()).scale(0.5);
        long now = System.nanoTime();

        if(now - lastKeyboardZoom > 60_000_000) {
            if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
                double currentZoom = camera.getTargetZoom();

                float scrollDir = -1;
                double factor = ZOOM_SPEED * currentZoom * Math.pow(ZOOM_ACCELERATION, 4);
                double zoom = MathUtil.clamp(currentZoom + (scrollDir * factor), MIN_ZOOM, MAX_ZOOM);

                camera.zoomToward(zoom, screenCenter);
                lastKeyboardZoom = now;
            }

            if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                double currentZoom = camera.getTargetZoom();

                float scrollDir = +1;
                double factor = ZOOM_SPEED * currentZoom * Math.pow(ZOOM_ACCELERATION, 4);
                double zoom = MathUtil.clamp(currentZoom + (scrollDir * factor), MIN_ZOOM, MAX_ZOOM);

                camera.zoomToward(zoom, screenCenter);
                lastKeyboardZoom = now;
            }
        }

    }

    @Override
    public boolean keyDown(int keycode) {

        if (keycode == Input.Keys.R) {
            if(Gdx.input.isKeyPressed(Input.Keys.F3)) {
                if(GameContext.isSet()) {
                    GameContext.get().getShaderManager().reload();
                    return true;
                }
            } else {
                camera.setPosition(0, 0);
                return true;
            }
        }

        if(keycode == Input.Keys.NUMPAD_ADD) {
            GameContext.get().getClock().getSimSpeedController().changeSpeed(+1);
            return true;
        }

        if(keycode == Input.Keys.NUMPAD_SUBTRACT) {
            GameContext.get().getClock().getSimSpeedController().changeSpeed(-1);
            return true;
        }

        if(keycode == Input.Keys.F3) {
            if(UIManager.get().getComponent("debug").getGroup().isVisible()) {
                UIManager.get().hideComponent("debug");
            } else {
                UIManager.get().showComponent("debug");
            }
            return true;
        }

        if(keycode == Input.Keys.SPACE) {
            GameContext.get().getClock().getSimSpeedController().togglePause();
            return true;
        }

        if(keycode == Input.Keys.ESCAPE) {
            if(UI.getUIManager().getComponent("escape_menu") != null) {
                boolean escVis = UI.getUIManager().getComponent("escape_menu").getGroup().isVisible();
                boolean optVis = UI.getUIManager().getComponent("options_menu").getGroup().isVisible();

                if(!escVis && !optVis) {
                    // open escape menu
                    UI.getUIManager().showComponent("escape_menu");
                } else if(escVis && !optVis) {
                    UI.getUIManager().hideComponent("escape_menu");
                } else {
                    // either both open or only options open
                    // close options, then escape
                    UI.getUIManager().hideComponent("options_menu");
                    UI.getUIManager().hideComponent("escape_menu");
                }

            }
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

        double currentZoom = camera.getTargetZoom();

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

            // Convert mouse delta to world-space direction
            double cos = Math.cos(Math.toRadians(camera.getRotation()));
            double sin = Math.sin(Math.toRadians(camera.getRotation()));

            double worldDX = -dx * cos + dy * sin; // x axis mirrored, so formula is -(dx * cos - dy * sin)
            double worldDY = dx * sin + dy * cos;

            // Apply zoom scaling
            Vector2D moveVec = new Vector2D(worldDX * camera.getZoom(), worldDY * camera.getZoom());

            camera.move(moveVec);

            lastMouseX = screenX;
            lastMouseY = screenY;
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
                    //if (!hcs.getCurrent().has(entity)) continue;
                    Vector2D screen = camera.project(ecs.toRelativeSpace(entity, PlanetoidRenderSystem.track, hcs.getAlpha()));

                    double dx = screenX - screen.getX();
                    double dy = screenY - screen.getY();
                    if (dx * dx + dy * dy < 16 * 16) {
                        PlanetoidRenderSystem.track = entity;
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
