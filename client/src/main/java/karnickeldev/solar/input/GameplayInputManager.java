package karnickeldev.solar.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.ecs.ClientECS;
import karnickeldev.solar.ecs.systems.HCSClientSystem;
import karnickeldev.solar.physics.Vector2D;
import karnickeldev.solar.render.PlanetoidRenderSystem;
import karnickeldev.solar.render.background.BackgroundGridRenderer;
import karnickeldev.solar.render.background.RingRenderer;
import karnickeldev.solar.render.camera.CameraPlayerInput;
import karnickeldev.solar.render.camera.FloatingOriginCamera;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.layers.escapemenu.EscapeMenuLayer;

/**
 * @author KarnickelDev
 * @since 05.04.2026
 **/
public class GameplayInputManager implements InputHandler {

    private static final float ROTATION_SPEED = (float) Math.toRadians(90);
    private static final float ZOOM_KEY_SPEED = 14f;
    private static final float ZOOM_SCROLL_SPEED = 0.6f;

    private int lastMouseX = 0, lastMouseY = 0;
    private boolean dragging = false;

    private FloatingOriginCamera camera;
    private ClientECS ecs;

    public GameplayInputManager() {

    }

    @Override
    public void handleInput() {
        this.camera = GameContext.get().getWorldManager().getActiveWorld().getCamera();
        this.ecs = GameContext.get().getWorldManager().getActiveWorld().getECS();

        CameraPlayerInput signal = camera.getPlayerInput();

        if (Gdx.input.isKeyPressed(Input.Keys.W)) signal.dirY = 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) signal.dirY = -1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) signal.dirX = -1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) signal.dirX = 1;

        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            signal.rotationRad = -ROTATION_SPEED * Gdx.graphics.getDeltaTime();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            signal.rotationRad = +ROTATION_SPEED * Gdx.graphics.getDeltaTime();
        }

        if(Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            float scrollDir = Gdx.input.isKeyPressed(Input.Keys.UP) ? -1 : +1;
            signal.zoomImpulse += scrollDir * ZOOM_KEY_SPEED * Gdx.graphics.getDeltaTime();
            signal.zoomCursor.set(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
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
            }
        }

        if(keycode == Input.Keys.G) {
            if(Gdx.input.isKeyPressed(Input.Keys.F3)) {
                BackgroundGridRenderer.toggleRender();
            }
        }

        if(keycode == Input.Keys.C) {
            if(Gdx.input.isKeyPressed(Input.Keys.F3)) {
                RingRenderer.toggleRender();
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

        // TODO: add back
//        if(keycode == Input.Keys.F3) {
//            if(UIManager.get().getComponent("debug").getGroup().isVisible()) {
//                UIManager.get().hideComponent("debug");
//            } else {
//                UIManager.get().showComponent("debug");
//            }
//            return true;
//        }

        if(keycode == Input.Keys.SPACE) {
            GameContext.get().getClock().getSimSpeedController().togglePause();
            return true;
        }

        if(keycode == Input.Keys.ESCAPE) {
            UI.getUIManager().push(new EscapeMenuLayer());
            return true;
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        boolean processed = false;
        if (button == Input.Buttons.LEFT) {
            HCSClientSystem hcs = ecs.hcs;
            for (int entity = 0; entity < ecs.getEntityManager().getCapacityUsed(); entity++) {
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

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.MIDDLE) {
            dragging = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!dragging) return false;

        CameraPlayerInput signal = camera.getPlayerInput();
        signal.clear();
        signal.dirX = -(screenX - lastMouseX);
        signal.dirY = screenY - lastMouseY;
        signal.panning = true;

        lastMouseX = screenX;
        lastMouseY = screenY;

        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        CameraPlayerInput signal = camera.getPlayerInput();

        signal.zoomImpulse += amountY * ZOOM_SCROLL_SPEED;
        signal.zoomCursor.set(Gdx.input.getX(), Gdx.input.getY());

        return true;
    }
}
