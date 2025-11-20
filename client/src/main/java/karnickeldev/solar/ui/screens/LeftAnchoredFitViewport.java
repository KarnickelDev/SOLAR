package karnickeldev.solar.ui.screens;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * @author KarnickelDev
 * @since 05.07.2025
 **/
public class LeftAnchoredFitViewport extends FitViewport {

    public LeftAnchoredFitViewport(float worldWidth, float worldHeight, Camera camera) {
        super(worldWidth, worldHeight, camera);
    }

    @Override
    public void update(int screenWidth, int screenHeight, boolean centerCamera) {
        super.update(screenWidth, screenHeight, false); // disable centering camera

        float scale = Math.min((float)screenWidth / getWorldWidth(),
            (float)screenHeight / getWorldHeight());

        int viewportWidth = Math.round(getWorldWidth() * scale);
        int viewportHeight = Math.round(getWorldHeight() * scale);

        // Instead of centering the viewport, offset it to the left
        int x = 0; // Always align to left
        int y = (screenHeight - viewportHeight) / 2; // Center vertically

        setScreenBounds(x, y, viewportWidth, viewportHeight);
        apply(false);

        // Optional: position camera accordingly
        if (getCamera() instanceof OrthographicCamera) {
            OrthographicCamera cam = (OrthographicCamera) getCamera();
            cam.position.set(getWorldWidth() / 2f, getWorldHeight() / 2f, 0);
            cam.update();
        }
    }
}

