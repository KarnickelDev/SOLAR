package karnickeldev.solar.render.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.TimeUtils;
import karnickeldev.solar.util.MathUtil;

public class CameraInput extends InputAdapter {

    private final FloatingOriginCamera camera;

    private static final float zoomSpeed = 0.0001f;
    private static final float zoomAcceleration = 1.16f;
    private static final float minZoom = 1e-10f;
    private static final float maxZoom = 30f;

    private long lastScrollTime = 0;
    private int scrollCount = 0;


    public CameraInput(FloatingOriginCamera camera) {
        this.camera = camera;
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

        float scale = camera.getScale();

        float scrollDir = Math.signum(amountY);
        float factor = zoomSpeed * scale * (float) Math.pow(zoomAcceleration, scrollCount);

        camera.setZoom(MathUtil.clamp(camera.getZoom() + (scrollDir * factor), minZoom * scale, maxZoom * scale));

        camera.update();

        return true;
    }
}
