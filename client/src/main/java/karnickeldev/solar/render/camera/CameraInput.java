package karnickeldev.solar.render.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.ecs.ClientECS;
import karnickeldev.solar.ecs.systems.HCSClientSystem;
import karnickeldev.solar.physics.Vector2D;
import karnickeldev.solar.render.PlanetoidRenderSystem;
import karnickeldev.solar.render.background.BackgroundGridRenderer;
import karnickeldev.solar.render.background.RingRenderer;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.core.UIManager;
import karnickeldev.solar.world.ClientWorld;
import karnickeldev.solar.world.WorldManager;

public class CameraInput extends InputAdapter {

    private static final float ROTATION_SPEED = (float) Math.toRadians(90);
    private static final float ZOOM_KEY_SPEED = 14f;
    private static final float ZOOM_SCROLL_SPEED = 0.6f;

    private int lastMouseX = 0, lastMouseY = 0;
    private boolean dragging = false;

    private final WorldManager<ClientWorld> worldManager;
    private FloatingOriginCamera camera;
    private ClientECS ecs;

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
        CameraPlayerInput signal = camera.getPlayerInput();

        signal.zoomImpulse += amountY * ZOOM_SCROLL_SPEED;
        signal.zoomCursor.set(Gdx.input.getX(), Gdx.input.getY());

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
}
